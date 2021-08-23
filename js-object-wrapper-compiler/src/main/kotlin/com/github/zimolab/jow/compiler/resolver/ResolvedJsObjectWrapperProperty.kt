package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperProperty
import com.github.zimolab.jow.compiler.generator.TypeCast
import com.github.zimolab.jow.compiler.simpleName
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

class ResolvedJsObjectWrapperProperty(
    val declaration: KSPropertyDeclaration,
    val annotation: KSAnnotation?
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
                JsObjectWrapperProperty::skip.name,
                JsObjectWrapperProperty.SKIP
            )
        }

        val undefinedAsNull by lazy {
            resolver.resolveAnnotationArgument(
                JsObjectWrapperProperty::undefinedAsNull.name,
                JsObjectWrapperProperty.UNDEFINED_AS_NULL
            )
        }

        val raiseExceptionOnUndefined by lazy {
            if (undefinedAsNull)
                false
            else
                resolver.resolveAnnotationArgument(
                    JsObjectWrapperProperty::raiseExceptionOnUndefined.name,
                    JsObjectWrapperProperty.RAISE_EXCEPTION_ON_UNDEFINED
                )
        }

        val getterTypeCast by lazy {
           TypeCast.ofProperty(false, this@ResolvedJsObjectWrapperProperty)
        }

        val setterTypeCast by lazy {
            TypeCast.ofProperty(true, this@ResolvedJsObjectWrapperProperty)
        }
    }

}