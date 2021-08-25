package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

@Suppress("unused")
@ExperimentalUnsignedTypes
class ResolvedFunction(
    originDeclaration: KSFunctionDeclaration,
    originAnnotation: KSAnnotation?
) {
    val resolver = FunctionResolver(originDeclaration, originAnnotation)

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
            resolver.resolveUndefinedAsNull()
        }

        val raiseExceptionOnUndefined by lazy {
            if (undefinedAsNull)
                false
            else
                resolver.resolveRaiseExceptionOnUndefined()
        }

        val skipped by lazy {
            resolver.resolveSkipped()
        }

        val jsMemberName by lazy {
            resolver.resolveJsMemberName().ifEmpty { simpleName }
        }

        val returnTypeMappingStrategy by lazy {
            resolver.resolveReturnTypeMappingStrategy()
        }
    }
}
