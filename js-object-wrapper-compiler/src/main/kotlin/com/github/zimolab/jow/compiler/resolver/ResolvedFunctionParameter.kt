package com.github.zimolab.jow.compiler.resolver

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueParameter

class ResolvedFunctionParameter(
    val declaration: KSValueParameter,
    val annotation: KSAnnotation? = null
) {
    private val resolver = FunctionParameterResolver(declaration, annotation)
    val name by lazy {
        resolver.resolveName()
    }

    val type by lazy {
        resolver.resolveType()
    }

    val isVararg by lazy {
        resolver.resolveIsVararg()
    }

    val typeCastCategory by lazy {
        resolver.resolveTypeCastCategory()
    }
}