package com.github.zimolab.jow.compiler.resolver;

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperClass
import com.github.zimolab.jow.compiler.findArgument
import com.github.zimolab.jow.compiler.packageNameStr
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType

class JsObjectWrapperClassResolver(
    val declaration: KSClassDeclaration,
    val annotation: KSAnnotation?,
    val options: Map<String, String>
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
        return resolveAnnotationArgument(JsObjectWrapperClass::outputClassName.name, "").ifEmpty {
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
        return resolveAnnotationArgument(JsObjectWrapperClass::outputFilename.name, filename).ifEmpty {
            filename
        }
    }

    fun resolveOutputFileEncoding(): String {
        return resolveAnnotationArgument(
            JsObjectWrapperClass::outputFileEncoding.name,
            JsObjectWrapperClass.DEFAULT_OUTPUT_ENCODING
        ).ifEmpty { JsObjectWrapperClass.DEFAULT_OUTPUT_ENCODING }
    }

    fun resolveContainingFile(): KSFile? {
        return declaration.containingFile
    }

    fun resolveClassComment(): String {
        return resolveAnnotationArgument(JsObjectWrapperClass::classComment.name, "")
    }

    inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }

    fun resolvePrimaryConstructor(): String {
        return resolveAnnotationArgument(JsObjectWrapperClass::primaryConstructor.name, JsObjectWrapperClass.DEFAULT_PRIMARY_CONSTRUCTOR).ifEmpty {
            JsObjectWrapperClass.DEFAULT_PRIMARY_CONSTRUCTOR
        }
    }
}
