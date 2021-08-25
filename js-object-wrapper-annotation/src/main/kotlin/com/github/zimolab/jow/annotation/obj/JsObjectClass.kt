package com.github.zimolab.jow.annotation.obj

/**
 * @property outputClassName String
 * @property outputFilename String
 * @property outputFileEncoding String
 * @property primaryConstructor String
 * @property classDoc String
 * @constructor
 */
@Target(AnnotationTarget.CLASS)
annotation class JsObjectClass(
    val outputClassName: String = "",
    val outputFilename: String = "",
    val outputFileEncoding: String = DEFAULT_OUTPUT_ENCODING,
    val primaryConstructor: String = DEFAULT_PRIMARY_CONSTRUCTOR,
    val classDoc: String = ""
) {
    companion object {
        const val DEFAULT_OUTPUT_ENCODING = "UTF-8"
        const val DEFAULT_PRIMARY_CONSTRUCTOR = PrimaryConstructor.WithParameter
    }

    object PrimaryConstructor {
        const val None = "None"
        const val Blank = "Blank"
        const val WithParameter = "WithParameter"
    }
}