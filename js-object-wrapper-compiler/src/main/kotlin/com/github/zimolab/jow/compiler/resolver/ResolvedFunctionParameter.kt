package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.compiler.generator.TypeMapper
import com.github.zimolab.jow.compiler.generator.TypeMappingMethod
import com.github.zimolab.jow.compiler.utils.TypeUtils
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueParameter

@ExperimentalUnsignedTypes
class ResolvedFunctionParameter(
    declaration: KSValueParameter,
    annotation: KSAnnotation? = null
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

        val typeMappingStrategy by lazy {
            resolver.resolveTypeMappingStrategy()
        }

        fun asArgumentString(typeMapper: TypeMapper): String {
            return if (isVararg) {
                val args = name
                val mapped = if (typeMapper.method == TypeMappingMethod.USE_MAPPING_FUNCTION) {
                    "${typeMapper.functionName}(it)"
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
                if (typeMapper.method == TypeMappingMethod.USE_MAPPING_FUNCTION) {
                    "${typeMapper.functionName}($arg)"
                } else {
                    arg
                }
            }
        }
    }
}