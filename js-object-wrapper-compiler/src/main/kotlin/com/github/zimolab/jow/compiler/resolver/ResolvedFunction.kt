package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectFunction
import com.github.zimolab.jow.compiler.generator.TypeCast
import com.github.zimolab.jow.compiler.generator.TypeCastMethod
import com.github.zimolab.jow.compiler.generator.TypeCastTarget
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeSpec

@ExperimentalUnsignedTypes
class ResolvedFunction(
    val originDeclaration: KSFunctionDeclaration,
    val originAnnotation: KSAnnotation?
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

        val returnTypeCastCategory by lazy {
            resolver.resolveReturnTypeCastCategory()
        }

        val parameters by lazy {
            resolver.resolveParameters2()
        }
    }
}
