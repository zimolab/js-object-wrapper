package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.github.zimolab.jow.annotation.obj.JsObjectParameter
import com.github.zimolab.jow.annotation.obj.typemapping.TypeMappingStrategy
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import java.util.logging.Logger

@ExperimentalUnsignedTypes
class FunctionResolver(
    private val declaration: KSFunctionDeclaration,
    private val annotation: KSAnnotation?
) {
    private val logger: Logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

    fun resolveName(): String {
        return declaration.simpleNameStr
    }

    fun resolveQualifiedName(): String {
        return declaration.qualifiedNameStr
    }

    fun resolveParameters(): MutableList<ResolvedFunctionParameter> {
        val parameters = mutableListOf<ResolvedFunctionParameter>()
        declaration.parameters.forEach { param->
            val annotation = param.findAnnotations(JsObjectParameter::class).firstOrNull()
            parameters.add(
                ResolvedFunctionParameter(param, annotation)
            )
        }
        return parameters
    }

    fun resolveReturnType(): KSType {
        val rt = declaration.returnType?.resolve()
            ?: AnnotationProcessingError("在解析函数(${declaration.simpleNameStr})返回值时出现一个错误").let {
                logger.error(it, throws = false)
                throw it
            }
        return rt
    }

    fun resolveReturnTypeMappingStrategy(): TypeMappingStrategy {
        val category = resolveAnnotationArgument(JsObjectFunction::returnTypeMappingStrategy.name, JsObjectFunction.DEFAULT_RETURN_TYPE_MAPPING_STRATEGY)
        category.ifEmpty {
            AnnotationProcessingError("@${JsObjectFunction::class.simpleName}注解的${JsObjectFunction::returnTypeMappingStrategy.name}参数不可为空").let {
                logger.error(it, throws = false)
                throw it
            }
        }
        return TypeMappingStrategy.of(category)
    }

    private inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }

    fun resolveUndefinedAsNull(): Boolean {
        return resolveAnnotationArgument(JsObjectFunction::undefinedAsNull.name, JsObjectFunction.UNDEFINED_AS_NULL)
    }

    fun resolveRaiseExceptionOnUndefined(): Boolean {
        return resolveAnnotationArgument(JsObjectFunction::raiseExceptionOnUndefined.name, JsObjectFunction.RAISE_EXCEPTION_ON_UNDEFINED)
    }

    fun resolveSkipped(): Boolean {
        return resolveAnnotationArgument(JsObjectFunction::skip.name, JsObjectFunction.SKIP)
    }

    fun resolveJsMemberName(): String {
        return  resolveAnnotationArgument(JsObjectFunction::jsMemberName.name, "")

    }
}