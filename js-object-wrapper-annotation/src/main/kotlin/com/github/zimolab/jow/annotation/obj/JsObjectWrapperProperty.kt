package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typecast.AUTO_DETERMINE

@Target(AnnotationTarget.PROPERTY)
annotation class JsObjectWrapperProperty(
    val jsMemberName: String = "",
    val skip: Boolean = SKIP,
    val undefinedAsNull: Boolean = UNDEFINED_AS_NULL,
    val raiseExceptionOnUndefined: Boolean = RAISE_EXCEPTION_ON_UNDEFINED,
    val getterTypeCast: String = DEFAULT_TYPE_CAST,
    val setterTypeCast: String = DEFAULT_TYPE_CAST

) {
    companion object {
        const val SKIP = false
        const val UNDEFINED_AS_NULL = true
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
        const val DEFAULT_TYPE_CAST = AUTO_DETERMINE
    }
}

