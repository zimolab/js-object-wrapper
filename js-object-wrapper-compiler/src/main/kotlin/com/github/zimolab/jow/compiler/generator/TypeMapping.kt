package com.github.zimolab.jow.compiler.generator

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.github.zimolab.jow.annotation.obj.JsObjectProperty
import com.github.zimolab.jow.annotation.obj.typemapping.*
import com.github.zimolab.jow.core.JsObjectWrapper
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.generator.TypeMapper.BuiltinTypeMappingFunctions.getBuiltinTypeMappingFunction
import com.github.zimolab.jow.compiler.resolve.ResolvedFunction
import com.github.zimolab.jow.compiler.resolve.ResolvedFunctionParameter
import com.github.zimolab.jow.compiler.resolve.ResolvedProperty
import com.github.zimolab.jow.compiler.utils.TypeUtils
import com.github.zimolab.jsarray.base.JsArrayInterface
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import netscape.javascript.JSObject
import java.util.logging.Logger
import kotlin.math.abs

enum class TypeMappingMethod {
    NO_MAPPING,
    USE_MAPPING_FUNCTION,
}

enum class TypeMappingTarget {
    PROP_SETTER,
    PROP_GETTER,
    FUNC_RETURN,
    FUNC_PARAMETER,
}


@Suppress("unused")
@ExperimentalUnsignedTypes
class TypeMapper private constructor(
    val method: TypeMappingMethod,
    val target: TypeMappingTarget,
    val functionName: String,
    functionSpec: FunSpec? = null,
    val isBuiltinFunction: Boolean = false
) {

    var functionSpec: FunSpec? = functionSpec
        private set

    /**
     * 为某些类型定义的内置类型映射函数
     */
    object BuiltinTypeMappingFunctions {
        // func __mappingJsObjectWrapper__(arg: JsObjectWrapper?): JsObject? = arg?.source
        private val JS_OBJECT_WRAPPER_SETTER_MAPPING_FUNC =
            createTypeMappingFunc(
                functionName = "__mapping${JsObjectWrapper::class.simpleName!!}__",
                parameterName = "arg",
                parameterType = JsObjectWrapper::class.asTypeName().copy(nullable = true),
                returnType = JSObject::class.asTypeName().copy(nullable = true),
                codeBlock = "return arg?.${JsObjectWrapper::source.name}".replace(" ", "·"),
            )

        // func __mappingJsArray__(arg: JsArrayInterface<Any?>?): JsObject? = arg?.reference
        private val JS_ARRAY_INTERFACE_SETTER_MAPPING_FUNC =
            createTypeMappingFunc(
                functionName = "__mapping${JsArrayInterface::class.simpleName!!}__",
                parameterName = "arg",
                parameterType = JsArrayInterface::class.asTypeName()
                    .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName().copy(nullable = true)))
                    .copy(nullable = true),
                returnType = JSObject::class.asTypeName().copy(nullable = true),
                codeBlock = "return arg?.${JsArrayInterface<*>::reference.name}".replace(" ", "·"),
            )

        fun getBuiltinTypeMappingFunction(type: KSType): FunSpec? {
            return if (TypeUtils.isJsArrayInterfaceType(type))
                JS_ARRAY_INTERFACE_SETTER_MAPPING_FUNC
            else if (TypeUtils.isJsObjectWrapperType(type))
                JS_OBJECT_WRAPPER_SETTER_MAPPING_FUNC
            else
                null
        }
    }

    companion object {
        private val logger: Logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

        private val GETTER_MAPPING_FUNC_PARAM_TYPE = Any::class.asTypeName().copy(nullable = true)
        private val SETTER_MAPPING_FUNC_RETURN_TYPE = Any::class.asTypeName().copy(nullable = true)
        private val FUNC_RETURN_TYPE_MAPPING_FUNC_PARAM_TYPE = Any::class.asTypeName().copy(nullable = true)
        private val FUNC_PARAM_MAPPING_FUNC_RETURN_TYPE = Any::class.asTypeName().copy(nullable = true)

        fun of(target: TypeMappingTarget, targetObj: Any, classBuilder: TypeSpec.Builder): TypeMapper {
            when (target) {
                TypeMappingTarget.PROP_SETTER,
                TypeMappingTarget.PROP_GETTER -> {
                    val prop = targetObj as ResolvedProperty

                    val typeMapper = if (target == TypeMappingTarget.PROP_SETTER) {
                        ofSetter(prop)
                    } else {
                        ofGetter(prop)
                    }

                    if (typeMapper.method == TypeMappingMethod.NO_MAPPING)
                        return typeMapper

                    if (typeMapper.isBuiltinFunction) {
                        if (classBuilder.findFunction(typeMapper.functionName, typeMapper.functionSpec!!.parameters[0].type) == null) {
                            classBuilder.addFunction(typeMapper.functionSpec!!)
                            logger.debug("为属性\"${prop.simpleName}创建类型映射函数：${typeMapper.functionSpec}\"")
                        }
                        return typeMapper
                    }

                    var mappingFunc = if (target == TypeMappingTarget.PROP_SETTER) {
                        classBuilder.findFunction(typeMapper.functionName, prop.type.asTypeName())
                    } else {
                        classBuilder.findFunction(typeMapper.functionName, GETTER_MAPPING_FUNC_PARAM_TYPE)
                    }

                    if (mappingFunc == null) {
                        mappingFunc = if (target == TypeMappingTarget.PROP_SETTER) {
                            createSetterMappingFunc(typeMapper.functionName, "src", prop)
                        } else {
                            createGetterMappingFunc(typeMapper.functionName, "src", prop)
                        }
                        typeMapper.functionSpec = mappingFunc
                        classBuilder.addFunction(mappingFunc)
                        logger.debug("为属性\"${prop.simpleName}创建类型映射函数：${typeMapper.functionSpec}\"")
                    }
                    return typeMapper
                }
                TypeMappingTarget.FUNC_RETURN -> {
                    val func = targetObj as ResolvedFunction
                    val typeMapper = ofFunctionReturn(func)
                    if (typeMapper.method == TypeMappingMethod.NO_MAPPING)
                        return typeMapper

                    if (typeMapper.isBuiltinFunction) {
                        if (classBuilder.findFunction(typeMapper.functionName, typeMapper.functionSpec!!.parameters[0].type) == null) {
                            classBuilder.addFunction(typeMapper.functionSpec!!)
                            logger.debug("为函数\"${func.simpleName}的返回值创建类型映射函数：${typeMapper.functionSpec}\"")
                        }
                        return typeMapper
                    }


                    var mappingFunc = classBuilder.findFunction(typeMapper.functionName, FUNC_RETURN_TYPE_MAPPING_FUNC_PARAM_TYPE)
                    if (mappingFunc == null) {
                        mappingFunc = createReturnTypeMappingFunc(typeMapper.functionName, "src", func)
                        typeMapper.functionSpec = mappingFunc
                        classBuilder.addFunction(mappingFunc)
                        logger.debug("为函数\"${func.simpleName}的返回值创建类型映射函数：${typeMapper.functionSpec}\"")
                    }
                    return typeMapper
                }
                TypeMappingTarget.FUNC_PARAMETER -> {
                    val param = targetObj as ResolvedFunctionParameter
                    val typeMapper = ofFunctionParameter(param)
                    if (typeMapper.method == TypeMappingMethod.NO_MAPPING)
                        return typeMapper

                    if (typeMapper.isBuiltinFunction) {
                        if (classBuilder.findFunction(typeMapper.functionName, typeMapper.functionSpec!!.parameters[0].type) == null) {
                            classBuilder.addFunction(typeMapper.functionSpec!!)
                            logger.debug("为函数参数\"${param.name}创建类型映射函数：${typeMapper.functionSpec}\"")
                        }
                        return typeMapper
                    }

                    var mappingFunc = classBuilder.findFunction(typeMapper.functionName, param.type.asTypeName())

                    if (mappingFunc == null) {
                        mappingFunc = createParameterMappingFunc(typeMapper.functionName, "arg", param)
                        typeMapper.functionSpec = mappingFunc
                        classBuilder.addFunction(mappingFunc)
                        logger.debug("为函数参数\"${param.name}创建类型映射函数：${typeMapper.functionSpec}\"")
                    }
                    return typeMapper
                }
            }
        }

        private fun ofSetter(property: ResolvedProperty): TypeMapper {
            val mappingStrategy = property.meta.setterTypeMappingStrategy
            val nativeType = property.meta.isNativeType

            return when (mappingStrategy) {
                is TypeMappingStrategy.AutoDetermine -> {
                    if (nativeType) {
                        TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.PROP_SETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        val builtinMappingFunc = getBuiltinTypeMappingFunction(property.type)
                        if (builtinMappingFunc != null) {
                            TypeMapper(
                                method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                                target = TypeMappingTarget.PROP_SETTER,
                                functionName = builtinMappingFunc.name,
                                functionSpec = builtinMappingFunc,
                                isBuiltinFunction = true
                            )
                        } else {
                            TypeMapper(
                                method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                                target = TypeMappingTarget.PROP_SETTER,
                                functionName = generateTypeMappingFuncName(TypeMappingTarget.PROP_SETTER, property),
                                functionSpec = null,
                                isBuiltinFunction = false
                            )
                        }
                    }
                }

                is TypeMappingStrategy.NoMapping -> {
                    TypeMapper(
                        method = TypeMappingMethod.NO_MAPPING,
                        target = TypeMappingTarget.PROP_SETTER,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.AutoGenerate -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.PROP_SETTER,
                        functionName = generateTypeMappingFuncName(TypeMappingTarget.PROP_SETTER, property),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    val builtinMappingFunc = getBuiltinTypeMappingFunction(property.type)
                    if (builtinMappingFunc == null) {
                        TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.PROP_SETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        TypeMapper(
                            method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                            target = TypeMappingTarget.PROP_SETTER,
                            functionName = builtinMappingFunc.name,
                            functionSpec = builtinMappingFunc,
                            isBuiltinFunction = true
                        )
                    }
                }

                is TypeMappingStrategy.UserSpecify -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.PROP_SETTER,
                        functionName = mappingStrategy.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        private fun ofGetter(property: ResolvedProperty): TypeMapper {
            val mappingStrategy = property.meta.getterTypeMappingStrategy
            val nativeType = property.meta.isNativeType
            return when (mappingStrategy) {
                is TypeMappingStrategy.AutoDetermine -> {
                    // getter无内置的类型映射函数
                    if (nativeType) {
                         TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.PROP_GETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else
                        TypeMapper(
                            method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                            target = TypeMappingTarget.PROP_GETTER,
                            functionName = generateTypeMappingFuncName(TypeMappingTarget.PROP_GETTER, property),
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                }

                is TypeMappingStrategy.NoMapping -> {
                    if (nativeType) {
                        TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.PROP_GETTER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        AnnotationProcessingError(
                            "${mappingStrategy.name}不适用于非原生类型属性"
                        ).let {
                            logger.error(it, throws = false)
                            throw it
                        }
                    }
                }

                is TypeMappingStrategy.AutoGenerate -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.PROP_GETTER,
                        functionName = generateTypeMappingFuncName(TypeMappingTarget.PROP_GETTER, property),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    AnnotationProcessingError("${mappingStrategy.name}不适用于${JsObjectProperty::getterTypeMappingStrategy.name}参数").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }

                is TypeMappingStrategy.UserSpecify -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.PROP_GETTER,
                        functionName = mappingStrategy.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        private fun ofFunctionParameter(parameter: ResolvedFunctionParameter): TypeMapper {
            val mappingStrategy = parameter.meta.typeMappingStrategy
            val nativeType = parameter.meta.isNativeType

            return when (mappingStrategy) {
                is TypeMappingStrategy.AutoDetermine -> {
                    if (nativeType) {
                        TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.FUNC_PARAMETER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        val builtinMappingFunc = getBuiltinTypeMappingFunction(parameter.type)
                        if (builtinMappingFunc != null) {
                            TypeMapper(
                                method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                                target = TypeMappingTarget.FUNC_PARAMETER,
                                functionName = builtinMappingFunc.name,
                                functionSpec = builtinMappingFunc,
                                isBuiltinFunction = true
                            )
                        } else {
                            TypeMapper(
                                method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                                target = TypeMappingTarget.FUNC_PARAMETER,
                                functionName = generateTypeMappingFuncName(TypeMappingTarget.FUNC_PARAMETER, parameter),
                                functionSpec = null,
                                isBuiltinFunction = false
                            )
                        }
                    }
                }

                is TypeMappingStrategy.NoMapping -> {
                    TypeMapper(
                        method = TypeMappingMethod.NO_MAPPING,
                        target = TypeMappingTarget.FUNC_PARAMETER,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.AutoGenerate -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.FUNC_PARAMETER,
                        functionName = generateTypeMappingFuncName(TypeMappingTarget.FUNC_PARAMETER, parameter),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }

                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    val builtinMappingFunc = getBuiltinTypeMappingFunction(parameter.type)
                    if (builtinMappingFunc == null) {
                        TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.FUNC_PARAMETER,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        TypeMapper(
                            method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                            target = TypeMappingTarget.FUNC_PARAMETER,
                            functionName = builtinMappingFunc.name,
                            functionSpec = builtinMappingFunc,
                            isBuiltinFunction = true
                        )
                    }
                }

                is TypeMappingStrategy.UserSpecify -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.FUNC_PARAMETER,
                        functionName = mappingStrategy.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        private fun ofFunctionReturn(func: ResolvedFunction): TypeMapper {
            val typeMappingStrategy = func.meta.returnTypeMappingStrategy
            val nativeType =
                TypeUtils.isNativeType(func.returnType) || TypeUtils.isVoidType(func.returnType) || TypeUtils.isAnyType(
                    func.returnType
                )
            return when (typeMappingStrategy) {
                is TypeMappingStrategy.AutoDetermine -> {
                    if (nativeType) {
                        TypeMapper(
                            method = TypeMappingMethod.NO_MAPPING,
                            target = TypeMappingTarget.FUNC_RETURN,
                            functionName = "",
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    } else {
                        TypeMapper(
                            method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                            target = TypeMappingTarget.FUNC_RETURN,
                            functionName = generateTypeMappingFuncName(TypeMappingTarget.FUNC_RETURN, func),
                            functionSpec = null,
                            isBuiltinFunction = false
                        )
                    }
                }
                is TypeMappingStrategy.AutoGenerate -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.FUNC_RETURN,
                        functionName = generateTypeMappingFuncName(TypeMappingTarget.FUNC_RETURN, func),
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeMappingStrategy.NoMapping -> {
                    if (!nativeType) {
                        AnnotationProcessingError("${typeMappingStrategy.name}不适用于${func.returnType.simpleName}类型的返回值").let {
                            logger.error(it)
                        }
                    }
                    TypeMapper(
                        method = TypeMappingMethod.NO_MAPPING,
                        target = TypeMappingTarget.FUNC_RETURN,
                        functionName = "",
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
                is TypeMappingStrategy.NoMappingExceptBuiltin -> {
                    AnnotationProcessingError("${typeMappingStrategy.name}不适用于${JsObjectFunction::returnTypeMappingStrategy.name}参数").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }
                is TypeMappingStrategy.UserSpecify -> {
                    TypeMapper(
                        method = TypeMappingMethod.USE_MAPPING_FUNCTION,
                        target = TypeMappingTarget.FUNC_RETURN,
                        functionName = typeMappingStrategy.name,
                        functionSpec = null,
                        isBuiltinFunction = false
                    )
                }
            }
        }

        /**
         * 生成规则：
         *  PROP_SETTER、FUNC_ARGUMENT:  ${prefix}${if(prefix=="") "mapping" else "Mapping"}${if(nullable) “Nullable” else “” }${TypeName}${suffix}
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
         *  @JsObjectWrapperProperty(setterTypeMappingStrategy=AUTO_DETERMINE, getterTypeMappingStrategy=AUTO_DETERMINE)
         *  var prop: List<String>
         *
         *  以上属性声明声明将生成如下函数声明
         *  abstract func mappingList_207086024(arg: List<String>): Ang?
         *  abstract func asList_207086024(arg: Any?): List<String>
         *  其中，uid部分（_207086024）可能会因为hashCode()方法的不同的实现而有所差异
         *
         * @param target TypeMappingTarget
         * @param targetObj Any
         * @param prefix String
         * @param suffix String
         * @return String
         */
        private fun generateTypeMappingFuncName(
            target: TypeMappingTarget,
            targetObj: Any,
            prefix: String = "",
            suffix: String = "",
        ): String {
            return when (target) {
                TypeMappingTarget.PROP_SETTER -> {
                    val prop = targetObj as ResolvedProperty
                    val simpleName = prop.type.simpleName
                    val nullable = TypeUtils.isNullable(prop.type)
                    val hasTypeArguments = prop.type.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(prop.type.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "mapping" else "Mapping"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
                }
                TypeMappingTarget.PROP_GETTER -> {
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
                TypeMappingTarget.FUNC_RETURN -> {
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
                TypeMappingTarget.FUNC_PARAMETER -> {
                    val parameter = targetObj as ResolvedFunctionParameter
                    val simpleName = parameter.type.simpleName
                    val nullable = TypeUtils.isNullable(parameter.type)
                    val hasTypeArguments = parameter.type.arguments.isNotEmpty()
                    val uid = if (hasTypeArguments) {
                        abs(parameter.type.asTypeName().hashCode()).toString()
                    } else {
                        ""
                    }
                    "${prefix}${if (prefix == "") "mapping" else "Mapping"}${if (nullable) "Nullable" else ""}${simpleName}${if (hasTypeArguments) "_${uid}" else ""}${suffix}"
                }
            }
        }

        fun createTypeMappingFunc(
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
                    it.addKdoc("This is an auto-generated type mapping function, which is used in the underlying web engine calls.")
                    it.addKdoc("\n")
                    if (codeBlock != null) {
                        it.addKdoc("This function has default implementation, you can override it in the subclass and provide your own type conversion logic.")
                        it.addKdoc("\n")
                        it.addModifiers(KModifier.OPEN)
                        it.addCode(codeBlock, *args)
                    } else {
                        it.addKdoc("This is an abstract function please implement it with your own type mapping logic.")
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

        // setter使用的类型映射函数符合以下定义:
        // 包含1个入参，入参类型与属性类型一致，返回值类型为Any?
        private fun createSetterMappingFunc(
            funcName: String,
            parameterName: String,
            property: ResolvedProperty,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeMappingFunc(
                funcName,
                parameterName,
                parameterType = property.type.asTypeName(),
                returnType = SETTER_MAPPING_FUNC_RETURN_TYPE,
                codeBlock,
                *args
            )
        }

        // getter使用的类型映射函数符合以下定义:
        // 包含1个入参，入参类型Any?，返回值类型则与属性类型一致
        private fun createGetterMappingFunc(
            funcName: String,
            parameterName: String,
            property: ResolvedProperty,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeMappingFunc(
                funcName,
                parameterName,
                parameterType = GETTER_MAPPING_FUNC_PARAM_TYPE,
                returnType = property.type.asTypeName(),
                codeBlock,
                *args
            )
        }

        private fun createReturnTypeMappingFunc(
            funcName: String,
            parameterName: String,
            func: ResolvedFunction,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeMappingFunc(
                funcName,
                parameterName,
                parameterType = FUNC_RETURN_TYPE_MAPPING_FUNC_PARAM_TYPE,
                returnType = func.returnType.asTypeName(),
                codeBlock,
                *args
            )
        }

        private fun createParameterMappingFunc(
            funcName: String,
            parameterName: String,
            parameter: ResolvedFunctionParameter,
            codeBlock: String? = null,
            vararg args: Any?
        ): FunSpec {
            return createTypeMappingFunc(
                funcName,
                parameterName,
                parameterType = parameter.type.asTypeName(),
                returnType = FUNC_PARAM_MAPPING_FUNC_RETURN_TYPE,
                codeBlock,
                *args
            )
        }
    }
}