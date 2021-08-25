package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.github.zimolab.jow.annotation.obj.JsObjectParameter
import com.github.zimolab.jow.annotation.obj.typemapping.TypeCastStrategy
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import java.util.logging.Logger

@ExperimentalUnsignedTypes
class FunctionResolver(
    val declaration: KSFunctionDeclaration,
    val annotation: KSAnnotation?
) {
    private val logger: Logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

    fun resolveName(): String {
        return declaration.simpleNameStr
    }

    fun resolveQualifiedName(): String {
        return declaration.qualifiedNameStr
    }

    fun resolveParameters(): MutableList<ResolvedFunction.FunctionParameter> {
        val parameters = mutableListOf<ResolvedFunction.FunctionParameter>()
        declaration.parameters.forEach { param->
            val name = param.name?.asString()
            if (name == null) {
                AnnotationProcessingError("在解析函数(${declaration.simpleNameStr})参数时出现一个错误").let {
                    logger.error(it)
                }
            }
            parameters.add(
                ResolvedFunction.FunctionParameter(
                    name!!,
                    param.type.resolve(),
                    param.isVararg
                )
            )
        }
        return parameters
    }

    fun resolveParameters2(): MutableList<ResolvedFunctionParameter> {
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

    fun resolveReturnTypeCastCategory(): TypeCastStrategy {
        val category = resolveAnnotationArgument(JsObjectFunction::returnTypeCast.name, JsObjectFunction.DEFAULT_RETURN_TYPE_CAST_STRATEGY)
        category.ifEmpty {
            AnnotationProcessingError("@${JsObjectFunction::class.simpleName}注解的${JsObjectFunction::returnTypeCast.name}参数不可为空").let {
                logger.error(it, throws = false)
                throw it
            }
        }
        return TypeCastStrategy.of(category)
    }

    inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }
}