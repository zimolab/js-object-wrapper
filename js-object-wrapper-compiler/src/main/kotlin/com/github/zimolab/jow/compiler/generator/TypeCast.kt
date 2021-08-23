package com.github.zimolab.jow.compiler.generator

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperProperty
import com.github.zimolab.jow.annotation.obj.typecast.*
import com.github.zimolab.jow.array.JsObjectWrapper
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.asTypeName
import com.github.zimolab.jow.compiler.resolver.ResolvedJsObjectWrapperProperty
import com.github.zimolab.jsarray.base.JsArrayInterface
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import netscape.javascript.JSObject
import java.util.logging.Logger

enum class TypeCastMethod {
    NO_CAST,
    CAST_FUNCTION,
}

enum class TypeCastTarget {
    PROP_SETTER,
    PROP_GETTER,
    FUNC_RETURN,
    FUNC_ARGUMENT,
}

class TypeCast private constructor(
    val category: TypeCastMethod,
    val target: TypeCastTarget,
    val typeCastFunctionName: String,
    val builtinCastFunction: FunSpec? = null
) {
    companion object {
        private val logger: Logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

        // 内置的类型转换函数
        private val JS_OBJECT_WRAPPER_SETTER_CAST = "__cast${JsObjectWrapper::class.simpleName!!}__" to
                FunSpec.builder("__cast${JsObjectWrapper::class.simpleName!!}__")
                    .addParameter("arg", JsObjectWrapper::class.asTypeName().copy(nullable = true))
                    .addStatement("return arg?.${JsObjectWrapper::source.name}".replace(" ", "·"))
                    .returns(JSObject::class.asTypeName().copy(nullable = true))
                    .build()

        private val JS_ARRAY_INTERFACE_SETTER_CAST = "__cast${JsArrayInterface::class.simpleName!!}__" to
                FunSpec
                    .builder("__cast${JsArrayInterface::class.simpleName!!}__")
                    .addParameter(
                        "arg",
                        JsArrayInterface::class.asTypeName()
                            .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName().copy(nullable = true)))
                            .copy(nullable = true)
                    )
                    .addStatement("return arg?.${JsArrayInterface<*>::reference.name}".replace(" ", "·"))
                    .returns(JSObject::class.asTypeName().copy(nullable = true))
                    .build()

        // 生成规则：
        // setter: {前缀}cast{类型名称}{Nullable|}{后缀}
        // getter: {前缀}as{类型名称}{Nullable|}{后缀}
        // 例如，对于Foo类型，前缀="__", 后缀="__"：
        // setter: __castFoo__
        // getter: __asFoo__
        // 对于Foo?类型，前缀="__", 后缀="__"：
        // setter: __castFooNullable__
        // getter: __asFooNullable__
        fun generateTypeCastFunctionName(
            isSetter: Boolean,
            property: ResolvedJsObjectWrapperProperty,
            prefix: String = "__",
            suffix: String = "__",
        ): String {
            return if (isSetter) {
                if (property.nullable) {
                    "${prefix}cast${property.type.simpleName}Nullable${suffix}"
                } else {
                    "${prefix}cast${property.type.simpleName}${suffix}"
                }
            } else {
                if (property.nullable) {
                    "${prefix}as${property.type.simpleName}Nullable${suffix}"

                } else {
                    "${prefix}as${property.type.simpleName}${suffix}"
                }
            }
        }

        fun ofProperty(isSetter: Boolean, property: ResolvedJsObjectWrapperProperty): TypeCast {
            val category = if (isSetter) {
                property.resolver.resolveSetterTypeCastCategory()
            } else {
                property.resolver.resolveGetterTypeCastorName()
            }

            // 空字符串检查
            category.ifEmpty {
                AnnotationProcessingError(
                    """@${JsObjectWrapperProperty::class.simpleName}注解的${JsObjectWrapperProperty::setterTypeCast.name}参数不可为空，请使用以下值：
                    |"$AUTO_DETERMINE" - 由系统自动决定（对于原生支持的类型，不使用转换。在setter中，对于特定类型（JsArrayInterface、JsObjectWrapper，及其子类），使用内置转换函数。对于其他类型，自动生成一个抽象转换函数，由用户在子类中实现）
                    |"$AUTO_GEN"       - 自动生成一个抽象转换函数，由用户在子类中实现
                    |"$NO_CAST"        - 不进行类型转换处理
                    |用户定义值          - 生成以该值为名称的抽象转换函数，由用户在子类中实现。
                """.trimMargin()
                ).let {
                    logger.error(it)
                }
            }

            val natives =
                TypeUtils.isNativeType(property.type) || TypeUtils.isVoidType(property.type) || TypeUtils.isAnyType(
                    property.type)

            if (isSetter) {
                when (category) {
                    // 自动决定最佳的类型转换函数生成规则
                    AUTO_DETERMINE -> {
                        return if (natives) { // 对于原生类型，不使用转换函数
                            TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.PROP_SETTER, "")
                        } else if (TypeUtils.isJsObjectWrapperType(property.type)) { // 对于特定类型，使用内置的转换函数
                            TypeCast(
                                TypeCastMethod.CAST_FUNCTION,
                                TypeCastTarget.PROP_SETTER,
                                JS_OBJECT_WRAPPER_SETTER_CAST.first,
                                builtinCastFunction = JS_OBJECT_WRAPPER_SETTER_CAST.second
                            )
                        } else if (TypeUtils.isJsArrayInterfaceType(property.type)) { // 对于特定类型，使用内置的转换函数
                            TypeCast(
                                TypeCastMethod.CAST_FUNCTION,
                                TypeCastTarget.PROP_SETTER,
                                JS_ARRAY_INTERFACE_SETTER_CAST.first,
                                builtinCastFunction = JS_ARRAY_INTERFACE_SETTER_CAST.second
                            )
                        } else { // 对于其他类型，自动生成转换函数
                            TypeCast(
                                TypeCastMethod.CAST_FUNCTION,
                                TypeCastTarget.PROP_SETTER,
                                generateTypeCastFunctionName(true, property)
                            )
                        }
                    }
                    // 对于setter而言，允许不使用类型转换
                    NO_CAST -> {
                        return TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.PROP_SETTER, "")
                    }
                    // 自动生成类型转换函数名
                    AUTO_GEN -> {
                        return TypeCast(
                            TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_SETTER,
                            generateTypeCastFunctionName(true, property)
                        )
                    }
                    // 采用用户指定的名称作为类型转换函数名称
                    else -> {
                        return TypeCast(TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_SETTER, category)
                    }
                }
            } else {
                when (category) {
                    AUTO_DETERMINE -> {
                        // getter无内置的类型转换函数
                        return if (natives) {
                            TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.PROP_GETTER, "")
                        } else
                            TypeCast(
                                TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_GETTER,
                                generateTypeCastFunctionName(false, property)
                            )
                    }
                    // 对于getter而言，除原生类型之外的其他类型都必须提供一个类型转换函数，以避免类型转换错误
                    // 换言之，getterTypeCast=NO_CAST仅适用于被注解属性为原生类型的情形
                    NO_CAST -> {
                        if (natives) {
                            return TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.PROP_GETTER, "")
                        } else {
                            AnnotationProcessingError(
                                """NO_CAST(${NO_CAST})不适用于非原生类型属性，请将${JsObjectWrapperProperty::getterTypeCast.name}参数指定为以下值之一:
                                    |"$AUTO_DETERMINE" - 由系统自动决定（对于原生支持的类型，不使用转换；对于其他类型，自动生成一个抽象转换函数，由用户在子类中实现）
                                    |"$AUTO_GEN"       - 自动生成一个抽象转换函数，由用户在子类中实现
                                    |用户定义值          - 生成以该值为名称的抽象转换函数，由用户在子类中实现。指定的值必须符合函数标识符命名规则
                                    |""".trimMargin()
                            ).let {
                                logger.error(it, throws = false)
                                throw it
                            }
                        }
                    }
                    // 自动生成转换函数名称
                    AUTO_GEN -> {
                        return TypeCast(
                            TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_GETTER,
                            generateTypeCastFunctionName(false, property)
                        )
                    }
                    // 使用用户指定的名称
                    else -> {
                        return TypeCast(TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_SETTER, category)
                    }
                }
            }
        }

        fun createTypeCastFunction(
            funcName: String,
            parameterName: String,
            parameterType: TypeName,
            returnType: TypeName,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return FunSpec.builder(funcName)
                .addParameter(parameterName, parameterType)
                .addModifiers(KModifier.ABSTRACT)
                .returns(returnType).let {
                    if (codeBlock != null) {
                        it.addCode(codeBlock, *args)
                    }
                    it.build()
                }
        }

        // setter使用的类型转换函数符合以下定义:
        // 包含1个入参，入参类型与属性类型一致，返回值类型为Any?
        fun createSetterCastFunction(
            funcName: String,
            parameterName: String,
            property: ResolvedJsObjectWrapperProperty,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                property.type.asTypeName(),
                Any::class.asTypeName().copy(nullable = true),
                codeBlock,
                *args
            )
        }

        // setter使用的类型转换函数符合以下定义:
        // 包含1个入参，入参类型Any?，返回值类型则与属性类型一致
        fun createGetterCastFunction(
            funcName: String,
            parameterName: String,
            property: ResolvedJsObjectWrapperProperty,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                Any::class.asTypeName().copy(nullable = true),
                property.type.asTypeName(),
                codeBlock,
                *args
            )
        }
    }
}