package com.github.zimolab.jow.compiler.resolver;

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperFunction
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

class ResolvedJsObjectWrapperFunction(
    val originDeclaration: KSFunctionDeclaration,
    val originAnnotation: KSAnnotation?
) {
    private val resolver = FunctionResolver(originDeclaration, originAnnotation)

    data class FunctionParameter(
        val name: String,
        val type: KSType,
        val isVarargs: Boolean = false
    )

    val simpleName by lazy {
        resolver.resolveName()
    }

    val qualifiedName by lazy {
        resolver.resolveQualifiedName()
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
            resolver.resolveAnnotationArgument(JsObjectWrapperFunction::undefinedAsNull.name, JsObjectWrapperFunction.UNDEFINED_AS_NULL)
        }

        val raiseExceptionOnUndefined by lazy {
            if (undefinedAsNull)
                false
            else
                resolver.resolveAnnotationArgument(JsObjectWrapperFunction::raiseExceptionOnUndefined.name, JsObjectWrapperFunction.RAISE_EXCEPTION_ON_UNDEFINED)
        }

        val skipped by lazy {
            resolver.resolveAnnotationArgument(JsObjectWrapperFunction::skip.name, JsObjectWrapperFunction.SKIP)
        }

        val jsMemberName by lazy {
            resolver.resolveAnnotationArgument(JsObjectWrapperFunction::jsMemberName.name, simpleName).ifEmpty { simpleName }
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
