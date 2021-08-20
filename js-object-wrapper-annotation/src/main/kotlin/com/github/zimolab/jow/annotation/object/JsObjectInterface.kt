package com.github.zimolab.jow.annotation.`object`

@Target(AnnotationTarget.CLASS)
annotation class JsObjectInterface(
    val outputClassName: String = "",
    val outputFilename: String = "",
    val ignoreUnsupportedTypes: Boolean = true,
    val outputFileEncoding: String = DEFAULT_OUTPUT_ENCODING,
    val primaryConstructor: String = PrimaryConstructor.WithSourceParameter,
    val generateArrayType: Boolean = GENERATE_ARRAY_TYPE,
    val classComment: String = ""
) {
    companion object {
        const val IGNORE_UNSUPPORTED_TYPES = true
        const val DEFAULT_OUTPUT_ENCODING = "UTF-8"
        const val GENERATE_ARRAY_TYPE = false

        object PrimaryConstructor {
            const val None = "None"
            const val Blank = "Blank"
            const val WithSourceParameter = "withSourceParameter"
        }
    }
}
