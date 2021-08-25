package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typecast.TypeCastStrategyConstants.AUTO_DETERMINE

/**
 *
 * @property jsMemberName String
 * @property skip Boolean
 * @property undefinedAsNull Boolean
 * @property raiseExceptionOnUndefined Boolean
 * @property getterTypeCast String
 * @property setterTypeCast String
 * @constructor
 */
@Target(AnnotationTarget.PROPERTY)
annotation class JsObjectProperty(
    val jsMemberName: String = "",
    val skip: Boolean = SKIP,
    val undefinedAsNull: Boolean = UNDEFINED_AS_NULL,
    val raiseExceptionOnUndefined: Boolean = RAISE_EXCEPTION_ON_UNDEFINED,
    val getterTypeCast: String = DEFAULT_TYPE_CAST_STRATEGY,
    val setterTypeCast: String = DEFAULT_TYPE_CAST_STRATEGY

) {
    companion object {
        const val SKIP = false
        const val UNDEFINED_AS_NULL = true
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
        const val DEFAULT_TYPE_CAST_STRATEGY = AUTO_DETERMINE
    }
}

