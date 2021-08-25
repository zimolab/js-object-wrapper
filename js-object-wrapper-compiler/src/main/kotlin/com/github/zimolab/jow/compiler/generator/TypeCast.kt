package com.github.zimolab.jow.compiler.generator

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.github.zimolab.jow.annotation.obj.JsObjectProperty
import com.github.zimolab.jow.annotation.obj.typemapping.*
import com.github.zimolab.jow.array.JsObjectWrapper
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.generator.TypeCast.BuiltinTypeCastFunctions.getBuiltinTypeCastFunction
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
    FUNC_PARAMETER,
}


@Suppress("unused")
@ExperimentalUnsignedTypes
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
     * 为某些类型定义的内置类型转换函数
     */
    object BuiltinTypeCastFunctions {
        // func __castJsObjectWrapper__(arg: JsObjectWrapper?): JsObject? = arg?.source
        private val JS_OBJECT_WRAPPER_SETTER_CAST_FUNC =
            createTypeCastFunction(
                functionName = "__cast${JsObjectWrapper::class.simpleName!!}__",
                parameterName = "arg",
                parameterType = JsObjectWrapper::class.asTypeName().copy(nullable = true),
                returnType = JSObject::class.asTypeName().copy(nullable = true),
                codeBlock = "return arg?.${JsObjectWrapper::source.name}".replace(" ", "·"),
            )

        // func __castJsArray__(arg: JsArrayInterface<Any?>?): JsObject? = arg?.reference
        private val JS_ARRAY_INTERFACE_SETTER_CAST_FUNC =
            createTypeCastFunction(
                functionName = "__cast${JsArrayInterface::class.simpleName!!}__",
                parameterName = "arg",
                parameterType = JsArrayInterface::class.asTypeName()
                    .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName().copy(nullable = true)))
                    .copy(nullable = true),
                returnType = JSObject::class.asTypeName().copy(nullable = true),
                codeBlock = "return arg?.${JsArrayInterface<*>::reference.name}".replace(" ", "·"),
            )

        fun getBuiltinTypeCastFunction(type: KSType): FunSpec? {
            return if (TypeUtils.isJsArrayInterfaceType(type))
                JS_ARRAY_INTERFACE_SETTER_CAST_FUNC
            else if (TypeUtils.isJsObjectWrapperType(type))
                JS_OBJECT_WRAPPER_SETTER_CAST_FUNC
            else
                null
        }
    }

    companion object {
        private val logger: Logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

        private val GETTER_CAST_FUNC_PARAM_TYPE = Any::class.asTypeName().copy(nullable = true)
        private val SETTER_CAST_FUNC_RETURN_TYPE = Any::class.asTypeName().copy(nullable = true)
        private val FUNC_RETURN_CAST_FUNC_PARAM_TYPE = Any::class.asTypeName().copy(nullable = true)
        private val FUNC_PARAM_CAST_FUNC_RETURN_TYPE = Any::class.asTypeName().copy(nullable = true)

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
                        if (classBuilder.findFunction(typeCast.functionName, typeCast.functionSpec!!.parameters[0].type) == null) {
                            classBuilder.addFunction(typeCast.functionSpec!!)
                            logger.debug("为属性\"${prop.simpleName}创建类型转换函数：${typeCast.functionSpec}\"")
                        }
                        return typeCast
                    }

                    var castFunc = if (target == TypeCastTarget.PROP_SETTER) {
                        classBuilder.findFunction(typeCast.functionName, prop.type.asTypeName())
                    } else {
                        classBuilder.findFunction(typeCast.functionName, GETTER_CAST_FUNC_PARAM_TYPE)
                    }

                    if (castFunc == null) {
                        castFunc = if (target == TypeCastTarget.PROP_SETTER) {
                            createSetterCastFunction(typeCast.functionName, "src", prop)
                        } else {
                            createGetterCastFunction(typeCast.functionName, "src", prop)
                        }
                        typeCast.functionSpec = castFunc
                        classBuilder.addFunction(castFunc)
                        logger.debug("为属性\"${prop.simpleName}创建类型转换函数：${typeCast.functionSpec}\"")
                    }
                    return typeCast
                }
                TypeCastTarget.FUNC_RETURN -> {
                    val func = targetObj as ResolvedFunction
                    val typeCast = ofFunctionReturn(func)
                    if (typeCast.typeCastMethod == TypeCastMethod.NO_CAST)
                        return typeCast

                    if (typeCast.isBuiltinFunction) {
                        if (classBuilder.findFunction(typeCast.functionName, typeCast.functionSpec!!.parameters[0].type) == null) {
                            classBuilder.addFunction(typeCast.functionSpec!!)
                            logger.debug("为函数\"${func.simpleName}的返回值创建类型转换函数：${typeCast.functionSpec}\"")
                        }
                        return typeCast
                    }


                    var castFunc = classBuilder.findFunction(typeCast.functionName, FUNC_RETURN_CAST_FUNC_PARAM_TYPE)
                    if (castFunc == null) {
                        castFunc = createReturnTypeCastFunction(typeCast.functionName, "src", func)
                        typeCast.functionSpec = castFunc
                        classBuilder.addFunction(castFunc)
                        logger.debug("为函数\"${func.simpleName}的返回值创建类型转换函数：${typeCast.functionSpec}\"")
                    }
                    return typeCast
                }
                TypeCastTarget.FUNC_PARAMETER -> {
                    val param = targetObj as ResolvedFunctionParameter
                    val typeCast = ofFunctionParameter(param)
                    if (typeCast.typeCastMethod == TypeCastMethod.NO_CAST)
                        return typeCast

                    if (typeCast.isBuiltinFunction) {
                        if (classBuilder.findFunction(typeCast.functionName, typeCast.functionSpec!!.parameters[0].type) == null) {
                            classBuilder.addFunction(typeCast.functionSpec!!)
                            logger.debug("为函数参数\"${param.name}创建类型转换函数：${typeCast.functionSpec}\"")
                        }
                        return typeCast
                    }

                    var castFunc = classBuilder.findFunction(typeCast.functionName, param.type.asTypeName())

                    if (castFunc == null) {
                        castFunc = createParameterTypeCastFunction(typeCast.functionName, "arg", param)
                        typeCast.functionSpec = castFunc
                        classBuilder.addFunction(castFunc)
                        logger.debug("为函数参数\"${param.name}创建类型转换函数：${typeCast.functionSpec}\"")
                    }
                    return typeCast
                }
            }
        }

        private fun ofSetter(property: ResolvedProperty): TypeCast {
            val category = property.meta.setterTypeCastCategory
            val nativeType = property.meta.isNativeType

            return when (category) {
                is TypeMappingStrategy.AutoDetermine -> {
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

                is TypeMappingStrategy.NoMapping -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.NO_CAST,
                        target = TypeCastTarget.PROP_SETTER,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.PROP_SETTER,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.PROP_SETTER, property),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
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

                is TypeMappingStrategy.UserSpecify -> {
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

        private fun ofGetter(property: ResolvedProperty): TypeCast {
            val category = property.meta.getterTypeCastCategory
            val nativeType = property.meta.isNativeType
            return when (category) {
                is TypeMappingStrategy.AutoDetermine -> {
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

                is TypeMappingStrategy.NoMapping -> {
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
                            "${category.name}不适用于非原生类型属性"
                        ).let {
                            logger.error(it, throws = false)
                            throw it
                        }
                    }
                }

                is TypeMappingStrategy.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.PROP_GETTER,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.PROP_GETTER, property),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    AnnotationProcessingError("${category.name}不适用于${JsObjectProperty::getterTypeMappingStrategy.name}参数").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }

                is TypeMappingStrategy.UserSpecify -> {
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

        private fun ofFunctionParameter(parameter: ResolvedFunctionParameter): TypeCast {
            val category = parameter.meta.typeCastCategory
            val nativeType = parameter.meta.isNativeType

            return when (category) {
                is TypeMappingStrategy.AutoDetermine -> {
                    if (nativeType) {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.FUNC_PARAMETER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        val builtinCastFunc = getBuiltinTypeCastFunction(parameter.type)
                        if (builtinCastFunc != null) {
                            TypeCast(
                                typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                                target = TypeCastTarget.FUNC_PARAMETER,
                                functionName = builtinCastFunc.name,
                                functionSpec = builtinCastFunc,
                                isBuiltinFunction = true
                            )
                        } else {
                            TypeCast(
                                typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                                target = TypeCastTarget.FUNC_PARAMETER,
                                functionName = generateTypeCastFunctionName(TypeCastTarget.FUNC_PARAMETER, parameter),
                                functionSpec = null,
                                isBuiltinFunction = false
                            )
                        }
                    }
                }

                is TypeMappingStrategy.NoMapping -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.NO_CAST,
                        target = TypeCastTarget.FUNC_PARAMETER,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.FUNC_PARAMETER,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.FUNC_PARAMETER, parameter),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    val builtinFunc = getBuiltinTypeCastFunction(parameter.type)
                    if (builtinFunc == null) {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.NO_CAST,
                            target = TypeCastTarget.FUNC_PARAMETER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        TypeCast(
                            typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                            target = TypeCastTarget.FUNC_PARAMETER,
                            functionName = builtinFunc.name,
                            functionSpec = builtinFunc,
                            isBuiltinFunction = true
                        )
                    }
                }

                is TypeMappingStrategy.UserSpecify -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.FUNC_PARAMETER,
                        functionName = category.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        private fun ofFunctionReturn(func: ResolvedFunction): TypeCast {
            val category = func.meta.returnTypeCastCategory
            val nativeType =
                TypeUtils.isNativeType(func.returnType) || TypeUtils.isVoidType(func.returnType) || TypeUtils.isAnyType(
                    func.returnType
                )
            return when (category) {
                is TypeMappingStrategy.AutoDetermine -> {
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
                is TypeMappingStrategy.AutoGenerate -> {
                    TypeCast(
                        typeCastMethod = TypeCastMethod.CAST_FUNCTION,
                        target = TypeCastTarget.FUNC_RETURN,
                        functionName = generateTypeCastFunctionName(TypeCastTarget.FUNC_RETURN, func),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeMappingStrategy.NoMapping -> {
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
                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    AnnotationProcessingError("${category.name}不适用于${JsObjectFunction::returnTypeMappingStrategy.name}参数").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }
                is TypeMappingStrategy.UserSpecify -> {
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
        private fun generateTypeCastFunctionName(
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
                TypeCastTarget.FUNC_PARAMETER -> {
                    val parameter = targetObj as ResolvedFunctionParameter
                    val simpleName = parameter.type.simpleName
                    val nullable = TypeUtils.isNullable(parameter.type)
                    val hasTypeArguments = parameter.type.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(parameter.type.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "cast" else "Cast"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
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
                    it.addKdoc("This is an auto-generated type cast function, which is used in the underlying web engine calls.")
                    it.addKdoc("\n")
                    if (codeBlock != null) {
                        it.addKdoc("This function has default implementation, you can override it in the subclass and provide your own type conversion logic.")
                        it.addKdoc("\n")
                        it.addModifiers(KModifier.OPEN)
                        it.addCode(codeBlock, *args)
                    } else {
                        it.addKdoc("This is an abstract function please implement it with your own type conversion logic.")
                        it.addKdoc("\n")
                        it.addModifiers(KModifier.ABSTRACT)
                    }
                    it.addKdoc("\n")
                    it.addKdoc("\t($parameterName:%T) -> %T", parameterType, returnType)
                    it.addKdoc("\n")
                    it.addKdoc("\n")
                    it.addKdoc("@param $parameterName %T\n", parameterType)
                    it.addKdoc("@return %T\n", returnType)
                    it.build()
                }
        }

        // setter使用的类型转换函数符合以下定义:
        // 包含1个入参，入参类型与属性类型一致，返回值类型为Any?
        private fun createSetterCastFunction(
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
                SETTER_CAST_FUNC_RETURN_TYPE,
                codeBlock,
                *args
            )
        }

        // getter使用的类型转换函数符合以下定义:
        // 包含1个入参，入参类型Any?，返回值类型则与属性类型一致
        private fun createGetterCastFunction(
            funcName: String,
            parameterName: String,
            property: ResolvedProperty,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                GETTER_CAST_FUNC_PARAM_TYPE,
                property.type.asTypeName(),
                codeBlock,
                *args
            )
        }

        private fun createReturnTypeCastFunction(
            funcName: String,
            parameterName: String,
            func: ResolvedFunction,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                FUNC_RETURN_CAST_FUNC_PARAM_TYPE,
                func.returnType.asTypeName(),
                codeBlock,
                *args
            )
        }

        private fun createParameterTypeCastFunction(
            funcName: String,
            parameterName: String,
            parameter: ResolvedFunctionParameter,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeCastFunction(
                funcName,
                parameterName,
                parameter.type.asTypeName(),
                FUNC_PARAM_CAST_FUNC_RETURN_TYPE,
                codeBlock,
                *args
            )
        }
    }
}