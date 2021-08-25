package com.github.zimolab.jow.compiler.generator

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.github.zimolab.jow.annotation.obj.JsObjectProperty
import com.github.zimolab.jow.annotation.obj.typecast.*
import com.github.zimolab.jow.array.JsObjectWrapper
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.generator.TypeCast.BuiltinTypeCastFunctions.JS_ARRAY_INTERFACE_SETTER_CAST_FUNC
import com.github.zimolab.jow.compiler.generator.TypeCast.BuiltinTypeCastFunctions.JS_OBJECT_WRAPPER_SETTER_CAST_FUNC
import com.github.zimolab.jow.compiler.resolver.ResolvedFunction
import com.github.zimolab.jow.compiler.resolver.ResolvedFunctionParameter
import com.github.zimolab.jow.compiler.resolver.ResolvedProperty
import com.github.zimolab.jow.compiler.utils.TypeUtils
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
    val typeCastMethod: TypeCastMethod,
    val target: TypeCastTarget,
    val functionName: String,
    functionSpec: FunSpec? = null,
    val isBuiltinFunction: Boolean = false
) {

    var functionSpec: FunSpec? = functionSpec
        private set

    /**
     * 内置类型转换函数
     */
    object BuiltinTypeCastFunctions {
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
                    val prop = targetObj as ResolvedProperty
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
                    val prop = targetObj as ResolvedProperty
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
                    val func = targetObj as ResolvedFunction
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
                    val func = targetObj as ResolvedFunction
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


        fun of(target: TypeCastTarget, targetObj: Any, classBuilder: TypeSpec.Builder): TypeCast {
            when (target) {
                TypeCastTarget.PROP_SETTER,
                TypeCastTarget.PROP_GETTER -> {
                    val prop = targetObj as ResolvedProperty

                    val typeCast = if (target == TypeCastTarget.PROP_SETTER) {
                        ofSetter(prop)
                    } else {
                        ofGetter(prop)
                    }

                    if (typeCast.typeCastMethod == TypeCastMethod.NO_CAST)
                        return typeCast

                    if (typeCast.isBuiltinFunction) {
                        if (classBuilder.funSpecs.firstOrNull { it.name == typeCast.functionName && it.parameters.size == 1 } == null)
                            classBuilder.addFunction(typeCast.functionSpec!!)
                        return typeCast
                    }

                    var castFunc =
                        classBuilder.funSpecs.firstOrNull { it.name == typeCast.functionName && it.parameters.size == 1 }

                    if (castFunc == null) {
                        castFunc = if (target == TypeCastTarget.PROP_SETTER) {
                            createSetterCastFunction(typeCast.functionName, "src", prop)
                        } else {
                            createGetterCastFunction(typeCast.functionName, "src", prop)
                        }
                        classBuilder.addFunction(castFunc)
                    }

                    typeCast.functionSpec = castFunc
                    return typeCast
                }
                TypeCastTarget.FUNC_RETURN -> {
                    val func = targetObj as ResolvedFunction
                    val typeCast = ofFunctionReturn(func)
                    if (typeCast.typeCastMethod == TypeCastMethod.NO_CAST || typeCast.functionSpec != null)
                        return typeCast
                    var castFunc =
                        classBuilder.funSpecs.firstOrNull { it.name == typeCast.functionName && it.parameters.size == 1 }
                    if (castFunc == null) {
                        castFunc = createReturnTypeCastFunction(typeCast.functionName, "arg", func)
                        classBuilder.addFunction(castFunc)
                    }
                    typeCast.functionSpec = castFunc
                    return typeCast
                }
                TypeCastTarget.FUNC_ARGUMENT -> {
                    TODO("")
                }
            }
        }

        fun ofSetter(property: ResolvedProperty): TypeCast {
            val category = property.meta.setterTypeCastCategory
            val nativeType = property.meta.isNativeType

            return when (category) {
                is TypeCastCategory.AutoDetermine -> {
                    if (nativeType) {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.PROP_SETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        val builtinCastFunc = getBuiltinTypeCastFunction(property.type)
                        if (builtinCastFunc != null) {
                            TypeCast(
                                typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                                target = TypeCastTarget.PROP_SETTER,
                                functionName = builtinCastFunc.name,
                                functionSpec = builtinCastFunc,
                                isBuiltinFunction = true
                            )
                        } else {
                            TypeCast(
                                typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                                target = TypeCastTarget.PROP_SETTER,
                                functionName = generateTypeCastFunctionName(TypeCastTarget.PROP_SETTER, property),
                                functionSpec = null,
                                isBuiltinFunction = false
                            )
                        }
                    }
                }

                is TypeCastCategory.NoCast -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.NO_CAST,
                        target = TypeCastTarget.PROP_SETTER,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeCastCategory.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.PROP_SETTER,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.PROP_SETTER, property),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeCastCategory.NoCastExceptBuiltin -> {
                    val builtinFunc = getBuiltinTypeCastFunction(property.type)
                    if (builtinFunc == null) {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.PROP_SETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                            target = TypeCastTarget.PROP_SETTER,
                            functionName = builtinFunc.name,
                            functionSpec = builtinFunc,
                            isBuiltinFunction = true
                        )
                    }
                }

                is TypeCastCategory.UserSpecify -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.PROP_SETTER,
                        functionName = category.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        fun ofGetter(property: ResolvedProperty): TypeCast {
            val category = property.meta.getterTypeCastCategory
            val nativeType = property.meta.isNativeType
            return when (category) {
                is TypeCastCategory.AutoDetermine -> {
                    // getter无内置的类型转换函数
                    if (nativeType) {
                         TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.PROP_GETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else
                        TypeCast(
                            typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                            target = TypeCastTarget.PROP_GETTER,
                            functionName = generateTypeCastFunctionName(TypeCastTarget.PROP_GETTER, property),
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                }

                is TypeCastCategory.NoCast -> {
                    if (nativeType) {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.PROP_GETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        AnnotationProcessingError(
                            "NO_CAST(${NO_CAST})不适用于非原生类型属性"
                        ).let {
                            logger.error(it, throws = false)
                            throw it
                        }
                    }
                }

                is TypeCastCategory.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.PROP_GETTER,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.PROP_GETTER, property),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeCastCategory.NoCastExceptBuiltin -> {
                    AnnotationProcessingError("${category.name}不适用于${JsObjectProperty::getterTypeCast.name}参数").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }

                is TypeCastCategory.UserSpecify -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.PROP_GETTER,
                        functionName = category.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        fun ofFunctionArgument(argument: ResolvedFunctionParameter): TypeCast {
            TODO()
        }

        fun ofFunctionReturn(func: ResolvedFunction): TypeCast {
            val category = func.meta.returnTypeCastCategory
            val nativeType =
                TypeUtils.isNativeType(func.returnType) || TypeUtils.isVoidType(func.returnType) || TypeUtils.isAnyType(
                    func.returnType
                )
            return when (category) {
                is TypeCastCategory.AutoDetermine -> {
                    if (nativeType) {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.FUNC_RETURN,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                            target = TypeCastTarget.FUNC_RETURN,
                            functionName = generateTypeCastFunctionName(TypeCastTarget.FUNC_RETURN, func),
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    }
                }
                is TypeCastCategory.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.FUNC_RETURN,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.FUNC_RETURN, func),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeCastCategory.NoCast -> {
                    if (!nativeType) {
                        AnnotationProcessingError("${category.name}不适用于${func.returnType.simpleName}类型的返回值").let {
                            logger.error(it)
                        }
                    }
                    TypeCast(
                        typeCastMethod = TypeCastMethod.NO_CAST,
                        target = TypeCastTarget.FUNC_RETURN,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeCastCategory.NoCastExceptBuiltin -> {
                    AnnotationProcessingError("${category.name}不适用于${JsObjectFunction::returnTypeCast.name}参数").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }
                is TypeCastCategory.UserSpecify -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.FUNC_RETURN,
                        functionName = category.name,
                        functionSpec = null,
                        isBuiltinFunction = false
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
            property: ResolvedProperty,
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
            property: ResolvedProperty,
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
            func: ResolvedFunction,
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

        fun createArgumentTypeCastFunction(
            funcName: String,
            parameterName: String,
            argumentType: TypeName,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                argumentType,
                Any::class.asTypeName().copy(nullable = true),
                codeBlock,
                *args
            )
        }
    }
}