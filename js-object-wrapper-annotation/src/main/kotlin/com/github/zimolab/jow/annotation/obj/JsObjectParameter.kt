package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typecast.NO_CAST_EXCEPT_BUILTIN

annotation class JsObjectParameter(
    val typeCast: String = DEFAULT_TYPE_CAST
) {
    companion object {
        const val DEFAULT_TYPE_CAST = NO_CAST_EXCEPT_BUILTIN
    }
}