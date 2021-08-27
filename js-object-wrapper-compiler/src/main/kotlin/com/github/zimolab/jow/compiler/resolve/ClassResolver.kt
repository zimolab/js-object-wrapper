package com.github.zimolab.jow.compiler.resolve

import com.github.zimolab.jow.annotation.JsObjectClass
import com.github.zimolab.jow.compiler.findArgument
import com.github.zimolab.jow.compiler.packageNameStr
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType

class ClassResolver(
    private val declaration: KSClassDeclaration,
    private val annotation: KSAnnotation?,
    private val options: Map<String, String>
) {

    companion object {
        const val KEY_OUTPUT_CLASS_PREFIX = "output_class_prefix"
        const val KEY_OUTPUT_CLASS_SUFFIX = "output_class_suffix"

        const val DEFAULT_OUTPUT_CLASS_PREFIX = "Abs"
        const val DEFAULT_OUTPUT_CLASS_SUFFIX = ""
    }

    fun resolvePackageName(): String {
        return declaration.packageNameStr
    }

    fun resolveClassName(): String {
        return declaration.simpleNameStr
    }

    fun resolveQualifiedClassName(): String {
        return declaration.qualifiedNameStr
    }

    //TODO 暂时不支持泛型的解析
    fun resolveClassType(): KSType {
        return declaration.asType(emptyList())
    }

    fun resolveOutputClassName(): String {
        return resolveAnnotationArgument(JsObjectClass::outputClassName.name, "").ifEmpty {
            if (KEY_OUTPUT_CLASS_PREFIX in options || KEY_OUTPUT_CLASS_SUFFIX in options) {
                val prefix = options[KEY_OUTPUT_CLASS_PREFIX]?.let {
                    it.ifEmpty { DEFAULT_OUTPUT_CLASS_PREFIX }
                }?: DEFAULT_OUTPUT_CLASS_PREFIX
                val suffix = options[KEY_OUTPUT_CLASS_SUFFIX]?.let {
                    it.ifEmpty { DEFAULT_OUTPUT_CLASS_PREFIX }
                }?: DEFAULT_OUTPUT_CLASS_PREFIX

                "$prefix${resolveClassName()}$suffix"
            } else {
                "$DEFAULT_OUTPUT_CLASS_PREFIX${resolveClassName()}$DEFAULT_OUTPUT_CLASS_SUFFIX"
            }
        }
    }

    fun resolveOutputFilename(): String {
        val filename = resolveOutputClassName()
        return resolveAnnotationArgument(JsObjectClass::outputFilename.name, filename).ifEmpty {
            filename
        }
    }

    fun resolveOutputFileEncoding(): String {
        return resolveAnnotationArgument(
            JsObjectClass::outputFileEncoding.name,
            JsObjectClass.DEFAULT_OUTPUT_ENCODING
        ).ifEmpty { JsObjectClass.DEFAULT_OUTPUT_ENCODING }
    }

    fun resolveContainingFile(): KSFile? {
        return declaration.containingFile
    }

    fun resolveClassComment(): String {
        return resolveAnnotationArgument(JsObjectClass::classDoc.name, "")
    }

    private inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }

    fun resolvePrimaryConstructor(): String {
        return resolveAnnotationArgument(JsObjectClass::primaryConstructor.name, JsObjectClass.DEFAULT_PRIMARY_CONSTRUCTOR).ifEmpty {
            JsObjectClass.DEFAULT_PRIMARY_CONSTRUCTOR
        }
    }
}
