package com.github.zimolab.jow.annotation.obj

@Target(AnnotationTarget.CLASS)
annotation class JsObjectWrapperClass(
    val outputClassName: String = "",
    val outputFilename: String = "",
    val ignoreUnsupportedTypes: Boolean = true,
    val outputFileEncoding: String = DEFAULT_OUTPUT_ENCODING,
    val primaryConstructor: String = DEFAULT_PRIMARY_CONSTRUCTOR,
    val classComment: String = ""
) {
    companion object {
        const val IGNORE_UNSUPPORTED_TYPES = true
        const val DEFAULT_OUTPUT_ENCODING = "UTF-8"
        const val DEFAULT_PRIMARY_CONSTRUCTOR = PrimaryConstructor.WithParameter
    }

    object PrimaryConstructor {
        const val None = "None"
        const val Blank = "Blank"
        const val WithParameter = "WithParameter"
    }
}