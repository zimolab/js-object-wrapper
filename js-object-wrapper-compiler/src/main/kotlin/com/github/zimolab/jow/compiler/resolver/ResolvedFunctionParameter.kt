package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.compiler.generator.TypeCast
import com.github.zimolab.jow.compiler.generator.TypeCastMethod
import com.github.zimolab.jow.compiler.utils.TypeUtils
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueParameter

@ExperimentalUnsignedTypes
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


    val meta by lazy {
        MetaData()
    }

    inner class MetaData {
        val isNativeType by lazy {
            TypeUtils.isNativeType(type) || TypeUtils.isAnyType(type) || TypeUtils.isVoidType(type)
        }

        val typeCastCategory by lazy {
            resolver.resolveTypeCastCategory()
        }

        fun asArgumentString(typeCast: TypeCast): String {
            return if (isVararg) {
                val args = name
                val mapped = if (typeCast.typeCastMethod == TypeCastMethod.CAST_FUNCTION) {
                    "${typeCast.functionName}(it)"
                } else {
                    null
                }

                when(mapped) {
                    null-> {
                        if (TypeUtils.hasToTypedArrayFunction(type)) {
                            "*(${args}.toTypedArray())"
                        } else {
                            "*($args)"
                        }
                    }
                    else-> {
                        "*(${args}.map{${mapped}}.toTypedArray())"
                    }

                }
            } else {
                val arg = name
                if (typeCast.typeCastMethod == TypeCastMethod.CAST_FUNCTION) {
                    "${typeCast.functionName}($arg)"
                } else {
                    arg
                }
            }
        }
    }
}