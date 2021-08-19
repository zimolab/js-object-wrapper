package com.github.zimolab.jsobjectwrapper.compiler.resolver;

import com.github.zimolab.jsobjectwrapper.annotation.JsObjectFunction
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

class ResolvedJsObjectWrapperFunction(
    val originDeclaration: KSFunctionDeclaration,
    val originAnnotation: KSAnnotation?
) {
    private val resolver = JsObjectWrapperFunctionResolver(originDeclaration, originAnnotation)

    data class FunctionParameter(
        val name: String,
        val type: KSType,
        val varargs: Boolean = false
    )

    val simpleName by lazy {
        resolver.resolveFunctionName()
    }

    val qualifiedName by lazy {
        resolver.resolveQualifiedFunctionName()
    }

    val returnType by lazy {
        resolver.resolveReturnType()
    }

    val parameters by lazy {
        resolver.resolveParameters()
    }

    val meta by lazy {
        MetaData()
    }

    inner class MetaData {
        val raiseExceptionOnUndefined by lazy {
            resolver.resolveAnnotationArgument(JsObjectFunction::raiseExceptionOnUndefined.name, JsObjectFunction.RAISE_EXCEPTION_ON_UNDEFINED)
        }
    }
}
