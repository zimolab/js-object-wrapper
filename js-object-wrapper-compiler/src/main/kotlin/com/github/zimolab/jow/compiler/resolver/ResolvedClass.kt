package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectClass
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

@ExperimentalUnsignedTypes
class ResolvedClass(
    val originDeclaration: KSClassDeclaration,
    val originAnnotation: KSAnnotation,
    val options: Map<String, String>
) {

    val functions = mutableListOf<ResolvedFunction>()
    val properties = mutableListOf<ResolvedProperty>()

    val resolver: ClassResolver = ClassResolver(originDeclaration, originAnnotation, options)

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

    fun addProperty(resolvedField: ResolvedProperty) {
        properties.add(resolvedField)
    }

    fun addFunction(resolvedFunction: ResolvedFunction) {
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

        val primaryConstructor by lazy {
            resolver.resolvePrimaryConstructor()
        }

        val ignoreUnsupportedTypes by lazy {
           resolver.resolveAnnotationArgument(JsObjectClass::ignoreUnsupportedTypes.name, JsObjectClass.IGNORE_UNSUPPORTED_TYPES)
        }

    }

}