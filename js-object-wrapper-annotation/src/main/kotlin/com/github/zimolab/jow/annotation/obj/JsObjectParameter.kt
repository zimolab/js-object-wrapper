package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typecast.TypeCastStrategyConstants.NO_CAST_EXCEPT_BUILTIN

/**
 * @property typeCast String
 * @constructor
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsObjectParameter(
    val typeCast: String = DEFAULT_TYPE_CAST_STRATEGY
) {
    companion object {
        const val DEFAULT_TYPE_CAST_STRATEGY = NO_CAST_EXCEPT_BUILTIN
    }
}