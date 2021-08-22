package com.github.zimolab.jow.annotation.obj

@Target(AnnotationTarget.PROPERTY)
annotation class JsObjectWrapperProperty(
    val jsMemberName: String = "",
    val skip: Boolean = SKIP,
    val undefinedAsNull: Boolean = UNDEFINED_AS_NULL,
    val raiseExceptionOnUndefined: Boolean = RAISE_EXCEPTION_ON_UNDEFINED,
    val getterTypeCast: String = TYPE_CAST_USE_AUTO_GEN_CASTOR,
    val setterTypeCast: String = TYPE_CAST_USE_AUTO_GEN_CASTOR

) {
    companion object {
        const val SKIP = false
        const val UNDEFINED_AS_NULL = true
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
        const val TYPE_CAST_USE_AUTO_GEN_CASTOR = ""
        const val TYPE_CAST_USE_AS_OPERATOR = "as"
        const val TYPE_CAST_NOT_APPLICABLE = "None"
    }
}

