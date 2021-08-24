package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typecast.AUTO_DETERMINE


/**
 *
 * @property jsMemberName String js函数名称。默认为空，代表与kotlin中声明的函数同名。
 * @property skip Boolean 是否跳过此函数。默认为false。
 * @property undefinedAsNull Boolean 是否将“undefined”返回值转换为null。默认为true。
 * @property raiseExceptionOnUndefined Boolean 当返回值为“undefined”时是否引发异常，默认为false。注意：undefinedAsNull参数具有优先权，此参数仅在undefinedAsNull=false时生效。
 * @property returnTypeCastor String 当返回值类型为非本地类型（Int、Boolean、Double、String、JSObject）时使用的类型转换函数名称。默认为None，代表自动生成一个名称。
 * @constructor
 */
@Target(AnnotationTarget.FUNCTION) annotation class JsObjectWrapperFunction(
    val jsMemberName: String = "",
    val skip: Boolean = SKIP,
    val undefinedAsNull: Boolean = UNDEFINED_AS_NULL,
    val raiseExceptionOnUndefined: Boolean = false,
    val returnTypeCast: String = DEFAULT_RETURN_TYPE_CAST,
    val argumentTypeCastor: String = ""
) {
    companion object {
        const val SKIP = false
        const val UNDEFINED_AS_NULL = true
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
        const val DEFAULT_RETURN_TYPE_CAST = AUTO_DETERMINE
    }
}
