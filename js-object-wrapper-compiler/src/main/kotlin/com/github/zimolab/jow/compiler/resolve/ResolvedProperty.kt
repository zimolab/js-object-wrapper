package com.github.zimolab.jow.compiler.resolve

import com.github.zimolab.jow.annotation.JsObjectProperty
import com.github.zimolab.jow.compiler.utils.TypeUtils
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

@Suppress("unused")
@ExperimentalUnsignedTypes
class ResolvedProperty(
    declaration: KSPropertyDeclaration,
    annotation: KSAnnotation?
) {
    val resolver: PropertyResolver = PropertyResolver(declaration, annotation)

    val simpleName by lazy {
        resolver.resolveName()
    }

    val qualifiedName by lazy {
        resolver.resolveQualifiedName()
    }

    val type by lazy {
        resolver.resolveType()
    }

    val nullable by lazy {
        resolver.resolveNullable()
    }

    val mutable by lazy {
        resolver.resolveMutable()
    }

    val meta by lazy {
        MetaData()
    }

    inner class MetaData {
        val jsMemberName by lazy {
            resolver.resolveJsMemberName() ?: let {
                simpleName
            }
        }

        val skipped by lazy {
            resolver.resolveAnnotationArgument(
                JsObjectProperty::skip.name,
                JsObjectProperty.SKIP
            )
        }

        val undefinedAsNull by lazy {
            resolver.resolveAnnotationArgument(
                JsObjectProperty::undefinedAsNull.name,
                JsObjectProperty.UNDEFINED_AS_NULL
            )
        }

        val raiseExceptionOnUndefined by lazy {
            if (undefinedAsNull)
                false
            else
                resolver.resolveAnnotationArgument(
                    JsObjectProperty::raiseExceptionOnUndefined.name,
                    JsObjectProperty.RAISE_EXCEPTION_ON_UNDEFINED
                )
        }

        val isNativeType by lazy {
            TypeUtils.isNativeType(type) || TypeUtils.isVoidType(type) || TypeUtils.isAnyType(type)
        }

        val getterTypeMappingStrategy by lazy {
            resolver.resolveGetterTypeMappingStrategy()
        }

        val setterTypeMappingStrategy by lazy {
            resolver.resolveSetterTypeMappingStrategy()
        }
    }

}