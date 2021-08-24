package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperFunction
import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import java.util.logging.Logger

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

    fun resolveParameters(): MutableList<ResolvedJsObjectWrapperFunction.FunctionParameter> {
        val parameters = mutableListOf<ResolvedJsObjectWrapperFunction.FunctionParameter>()
        declaration.parameters.forEach { param->
            val name = param.name?.asString()
            if (name == null) {
                AnnotationProcessingError("在解析函数(${declaration.simpleNameStr})返回值时出现一个错误").let {
                    logger.error(it)
                }
            }
            parameters.add(
                ResolvedJsObjectWrapperFunction.FunctionParameter(
                    name!!,
                    param.type.resolve(),
                    param.isVararg
                )
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

    fun resolveReturnTypeCastCategory(): String {
        return resolveAnnotationArgument(JsObjectWrapperFunction::returnTypeCast.name, JsObjectWrapperFunction.DEFAULT_RETURN_TYPE_CAST)
    }

    inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }
}