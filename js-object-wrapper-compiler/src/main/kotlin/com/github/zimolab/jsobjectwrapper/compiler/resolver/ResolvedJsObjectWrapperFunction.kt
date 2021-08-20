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
        val isVarargs: Boolean = false
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
        val undefinedAsNull by lazy {
            resolver.resolveAnnotationArgument(JsObjectFunction::undefinedAsNull.name, JsObjectFunction.UNDEFINED_AS_NULL)
        }

        val raiseExceptionOnUndefined by lazy {
            if (undefinedAsNull)
                false
            else
                resolver.resolveAnnotationArgument(JsObjectFunction::raiseExceptionOnUndefined.name, JsObjectFunction.RAISE_EXCEPTION_ON_UNDEFINED)
        }

        val skipped by lazy {
            resolver.resolveAnnotationArgument(JsObjectFunction::skip.name, JsObjectFunction.SKIP)
        }

        val jsMemberName by lazy {
            resolver.resolveAnnotationArgument(JsObjectFunction::jsMemberName.name, simpleName).ifEmpty { simpleName }
        }

        var returnTypeCastor: String? = resolver.resolveReturnTypeCastor()
            set(value) {
                field = if (value.equals("None", true) || value == "")
                    null
                else
                    value
            }
    }
}
