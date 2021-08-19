package com.github.zimolab.jsobjectwrapper.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class JsObjectFunction(
    val jsMemberName: String = "",
    val ignored: Boolean = IGNORED,
    val raiseExceptionOnUndefined: Boolean = false
) {
    companion object {
        const val IGNORED = false
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
    }
}
