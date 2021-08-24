package com.github.zimolab.jow.compiler.generator

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperFunction
import com.github.zimolab.jow.annotation.obj.JsObjectWrapperProperty
import com.github.zimolab.jow.annotation.obj.typecast.*
import com.github.zimolab.jow.array.JsObjectWrapper
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.generator.TypeCast.BuiltinTypeCastFunctions.JS_ARRAY_INTERFACE_SETTER_CAST_FUNC
import com.github.zimolab.jow.compiler.generator.TypeCast.BuiltinTypeCastFunctions.JS_OBJECT_WRAPPER_SETTER_CAST_FUNC
import com.github.zimolab.jow.compiler.resolver.ResolvedJsObjectWrapperFunction
import com.github.zimolab.jow.compiler.resolver.ResolvedJsObjectWrapperProperty
import com.github.zimolab.jsarray.base.JsArrayInterface
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import netscape.javascript.JSObject
import java.util.logging.Logger
import kotlin.math.abs

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

    object BuiltinTypeCastFunctions {
        // 内置的类型转换函数
        // func __castJsObjectWrapper__(arg: JsObjectWrapper?): JsObject? = arg?.source
        val JS_OBJECT_WRAPPER_SETTER_CAST_FUNC =
            createTypeCastFunction(
                functionName = "__cast${JsObjectWrapper::class.simpleName!!}__",
                parameterName = "arg",
                parameterType = JsObjectWrapper::class.asTypeName().copy(nullable = true),
                returnType = JSObject::class.asTypeName().copy(nullable = true),
                codeBlock = "return arg?.${JsObjectWrapper::source.name}".replace(" ", "·"),
            )

        // func __castJsArray__(arg: JsArrayInterface<Any?>?): JsObject? = arg?.reference
        val JS_ARRAY_INTERFACE_SETTER_CAST_FUNC =
            createTypeCastFunction(
                functionName = "__cast${JsArrayInterface::class.simpleName!!}__",
                parameterName = "arg",
                parameterType = JsArrayInterface::class.asTypeName()
                    .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName().copy(nullable = true)))
                    .copy(nullable = true),
                returnType = JSObject::class.asTypeName().copy(nullable = true),
                codeBlock = "return arg?.${JsArrayInterface<*>::reference.name}".replace(" ", "·"),
            )
    }

    companion object {
        private val logger: Logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

        /**
         * 生成规则：
         *  PROP_SETTER、FUNC_ARGUMENT:  ${prefix}${if(prefix=="") "cast" else "Cast"}${if(nullable) “Nullable” else “” }${TypeName}${suffix}
         *
         *  PROP_GETTER、FUNC_RETURN:   ${prefix}${if(prefix=="") "as" else "As"}${if(nullable) “Nullable” else “” }${TypeName}${suffix}
         *
         *  其中${TypeName}的生成规则如下：
         *  1、如类型含有泛型，则${TypeName} = ${simpleName_uid}
         *  1）simpleName
         *  2）uid == TypeName.hashCode()
         *
         *  2、如类型不含泛型，则${TypeName} = ${simpleName}
         *
         *  示例（假定：prefix=“”, suffix=""），
         *  @JsObjectWrapperProperty(setterTypeCast=AUTO_DETERMINE, getterTypeCast=AUTO_DETERMINE)
         *  var prop: List<String>
         *
         *  以上属性声明声明将生成如下函数声明
         *  abstract func castList_207086024(arg: List<String>): Ang?
         *  abstract func asList_207086024(arg: Any?): List<String>
         *  其中，uid部分（_207086024）可能会因为hashCode()方法的不同的实现而有所差异
         *
         * @param target TypeCastTarget
         * @param targetObj Any
         * @param prefix String
         * @param suffix String
         * @return String
         */
        fun generateTypeCastFunctionName(
            target: TypeCastTarget,
            targetObj: Any,
            prefix: String = "",
            suffix: String = "",
        ): String {
            return when (target) {
                TypeCastTarget.PROP_SETTER -> {
                    val prop = targetObj as ResolvedJsObjectWrapperProperty
                    val simpleName = prop.type.simpleName
                    val nullable = TypeUtils.isNullable(prop.type)
                    val hasTypeArguments = prop.type.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(prop.type.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "cast" else "Cast"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
                }
                TypeCastTarget.PROP_GETTER -> {
                    val prop = targetObj as ResolvedJsObjectWrapperProperty
                    val simpleName = prop.type.simpleName
                    val nullable = TypeUtils.isNullable(prop.type)
                    val hasTypeArguments = prop.type.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(prop.type.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "as" else "As"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
                }
                TypeCastTarget.FUNC_RETURN -> {
                    val func = targetObj as ResolvedJsObjectWrapperFunction
                    val simpleName = func.returnType.simpleName
                    val nullable = TypeUtils.isNullable(func.returnType)
                    val hasTypeArguments = func.returnType.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(func.returnType.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "as" else "As"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
                }
                TypeCastTarget.FUNC_ARGUMENT -> {
                    val func = targetObj as ResolvedJsObjectWrapperFunction
                    val simpleName = func.returnType.simpleName
                    val nullable = TypeUtils.isNullable(func.returnType)
                    val hasTypeArguments = func.returnType.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(func.returnType.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "cast" else "Cast"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
                }
            }
        }

        fun getBuiltinTypeCastFunction(type: KSType): FunSpec? {
            return if (TypeUtils.isJsArrayInterfaceType(type))
                JS_ARRAY_INTERFACE_SETTER_CAST_FUNC
            else if (TypeUtils.isJsObjectWrapperType(type))
                JS_OBJECT_WRAPPER_SETTER_CAST_FUNC
            else
                null
        }

        fun ofSetter(property: ResolvedJsObjectWrapperProperty): TypeCast {
            val category = property.resolver.resolveSetterTypeCastCategory()
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

            val nativeType =
                TypeUtils.isNativeType(property.type) || TypeUtils.isVoidType(property.type) || TypeUtils.isAnyType(
                    property.type
                )

            when (category) {
                // 自动决定如何使用类型转换函数
                AUTO_DETERMINE -> {
                    // 1、如果为原生类型则无需应用类型转换处理
                    if (nativeType)
                        return TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.PROP_SETTER, "")
                    // 2、如果有内置的类型转换函数，则使用内置的
                    val builtinCastFunction = getBuiltinTypeCastFunction(property.type)
                    return if (builtinCastFunction != null) {
                        TypeCast(
                            TypeCastMethod.CAST_FUNCTION,
                            TypeCastTarget.PROP_SETTER,
                            builtinCastFunction.name,
                            builtinCastFunction
                        )
                    } else {
                        //3、如果无内置的类型转换函数，则自动生成一个
                        TypeCast(
                            TypeCastMethod.CAST_FUNCTION,
                            TypeCastTarget.PROP_SETTER,
                            generateTypeCastFunctionName(TypeCastTarget.PROP_SETTER, property)
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
                        generateTypeCastFunctionName(TypeCastTarget.PROP_SETTER, targetObj = property)
                    )
                }
                // 采用用户指定的名称作为类型转换函数名称
                else -> {
                    return TypeCast(TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_SETTER, category)
                }
            }

        }

        fun ofGetter(property: ResolvedJsObjectWrapperProperty): TypeCast {
            val category = property.resolver.resolveGetterTypeCastCategory()
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
            val nativeType =
                TypeUtils.isNativeType(property.type) || TypeUtils.isVoidType(property.type) || TypeUtils.isAnyType(
                    property.type
                )
            when (category) {
                AUTO_DETERMINE -> {
                    // getter无内置的类型转换函数
                    return if (nativeType) {
                        TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.PROP_GETTER, "")
                    } else
                        TypeCast(
                            TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_GETTER,
                            generateTypeCastFunctionName(TypeCastTarget.PROP_GETTER, property)
                        )
                }
                // 对于getter而言，除原生类型之外的其他类型都必须提供一个类型转换函数，以避免类型转换错误
                // 换言之，getterTypeCast=NO_CAST仅适用于被注解属性为原生类型的情形
                NO_CAST -> {
                    if (nativeType) {
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
                        generateTypeCastFunctionName(TypeCastTarget.PROP_GETTER, property)
                    )
                }
                // 使用用户指定的名称
                else -> {
                    return TypeCast(TypeCastMethod.CAST_FUNCTION, TypeCastTarget.PROP_SETTER, category)
                }
            }

        }

        fun ofFunctionReturn(func: ResolvedJsObjectWrapperFunction): TypeCast {
            val category = func.resolver.resolveReturnTypeCastCategory()
            category.ifEmpty {
                AnnotationProcessingError("@${JsObjectWrapperFunction::class.simpleName}注解的${JsObjectWrapperFunction::returnTypeCast.name}参数不能为空").let {
                    logger.error(it)
                }
            }
            val nativeType =
                TypeUtils.isNativeType(func.returnType) || TypeUtils.isVoidType(func.returnType) || TypeUtils.isAnyType(
                    func.returnType
                )
            when (category) {
                AUTO_DETERMINE -> {
                    return if (nativeType) {
                        TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.FUNC_RETURN, "")
                    } else
                        TypeCast(
                            TypeCastMethod.CAST_FUNCTION, TypeCastTarget.FUNC_RETURN,
                            generateTypeCastFunctionName(TypeCastTarget.FUNC_RETURN, targetObj = func)
                        )
                }
                NO_CAST -> {
                    return if (nativeType) {
                        TypeCast(TypeCastMethod.NO_CAST, TypeCastTarget.FUNC_RETURN, "")
                    } else {
                        AnnotationProcessingError(
                            """NO_CAST(${NO_CAST})不适用于非原生类型属性，请将${JsObjectWrapperFunction::returnTypeCast.name}参数指定为以下值之一:
                                    |"$AUTO_DETERMINE" - 由系统自动决定（对于原生支持的类型，不使用转换；对于其他类型，自动生成一个抽象转换函数，由用户在子类中实现）
                                    |"$AUTO_GEN"       - 自动生成一个抽象转换函数，由用户在子类中实现
                                    |用户定义值          - 生成以该值为名称的抽象转换函数，由用户在子类中实现。
                                    |""".trimMargin()
                        ).let {
                            logger.error(it, throws = false)
                            throw it
                        }
                    }
                }
                AUTO_GEN -> {
                    return TypeCast(
                        TypeCastMethod.CAST_FUNCTION, TypeCastTarget.FUNC_RETURN,
                        generateTypeCastFunctionName(TypeCastTarget.FUNC_RETURN, targetObj = func)

                        //generateTypeCastFunctionName(TypeCastTarget.FUNC_RETURN, func)
                    )
                }
                else -> {
                    return TypeCast(
                        TypeCastMethod.CAST_FUNCTION, TypeCastTarget.FUNC_RETURN,
                        category
                    )
                }
            }
        }

        fun createTypeCastFunction(
            functionName: String,
            parameterName: String,
            parameterType: TypeName,
            returnType: TypeName,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return FunSpec.builder(functionName)
                .addParameter(parameterName, parameterType)
                .returns(returnType).let {
                    if (codeBlock != null) {
                        it.addCode(codeBlock, *args)
                    } else {
                        it.addModifiers(KModifier.ABSTRACT)
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

        // getter使用的类型转换函数符合以下定义:
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

        fun createReturnTypeCastFunction(
            funcName: String,
            parameterName: String,
            func: ResolvedJsObjectWrapperFunction,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                Any::class.asTypeName().copy(nullable = true),
                func.returnType.asTypeName(),
                codeBlock,
                *args
            )
        }
    }
}