package com.github.zimolab.jsobjectwrapper.annotation

import java.nio.charset.Charset

@Target(AnnotationTarget.CLASS)
annotation class JsObjectInterface(
    val outputClassName: String = "",
    val outputFilename: String = "",
    val ignoreUnsupportedTypes: Boolean = true,
    val outputFileEncoding: String = DEFAULT_OUTPUT_ENCODING,
    val classComment: String = ""
) {
    companion object {
        const val IGNORE_UNSUPPORTED_TYPES = true
        const val DEFAULT_OUTPUT_ENCODING = "UTF-8"
    }
}
