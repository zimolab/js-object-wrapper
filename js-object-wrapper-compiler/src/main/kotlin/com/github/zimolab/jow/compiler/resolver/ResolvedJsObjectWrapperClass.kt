package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.`object`.JsObjectInterface
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

class ResolvedJsObjectWrapperClass(
    val originDeclaration: KSClassDeclaration,
    val originAnnotation: KSAnnotation,
    val options: Map<String, String>
) {

    val functions = mutableListOf<ResolvedJsObjectWrapperFunction>()
    val fields = mutableListOf<ResolvedJsObjectWrapperField>()

    val resolver: JsObjectWrapperClassResolver = JsObjectWrapperClassResolver(originDeclaration, originAnnotation, options)

    val packageName by lazy {
        resolver.resolvePackageName()
    }

    val simpleName by lazy {
        resolver.resolveClassName()
    }

    val qualifiedName by lazy {
        resolver.resolveQualifiedClassName()
    }

    val ksType by lazy {
        resolver.resolveClassType()
    }

    val containingFile by lazy {
        resolver.resolveContainingFile()
    }

    val classComment by lazy {
        resolver.resolveClassComment()
    }

    val meta by lazy {
        MetaData()
    }

    fun addField(resolvedField: ResolvedJsObjectWrapperField) {
        fields.add(resolvedField)
    }

    fun addFunction(resolvedFunction: ResolvedJsObjectWrapperFunction) {
        functions.add(resolvedFunction)
    }

    inner class MetaData {
        val outputClassName by lazy {
            resolver.resolveOutputClassName()
        }

        val outputFilename by lazy {
            resolver.resolveOutputFilename()
        }

        val outputFileEncoding by lazy {
            resolver.resolveOutputFileEncoding()
        }

        val generateArrayType by lazy {
            resolver.resolveAnnotationArgument(JsObjectInterface::generateArrayType.name, JsObjectInterface.GENERATE_ARRAY_TYPE)
        }

        val ignoreUnsupportedTypes by lazy {
           resolver.resolveAnnotationArgument(JsObjectInterface::ignoreUnsupportedTypes.name, JsObjectInterface.IGNORE_UNSUPPORTED_TYPES)
        }

    }

}