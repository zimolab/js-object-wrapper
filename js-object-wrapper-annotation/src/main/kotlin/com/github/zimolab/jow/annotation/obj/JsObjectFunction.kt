package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typemapping.TypeMappingStrategy.Companion.AUTO_DETERMINE


/**
 * 该注解应用于抽象函数，被注解的函数对应Javascript对象中的一个函数。
 *
 * @property jsMemberName String Javascript对象中对应函数名称。默认为空，代表与kotlin中声明的函数同名。
 * @property skip Boolean 是否跳过此函数。为true时不为该函数生成相关代码。默认为false。
 * @property undefinedAsNull Boolean 是否将“undefined”返回值转换为null。默认为true。当函数返回值为非空类型时，该参数不生效。
 * @property raiseExceptionOnUndefined Boolean 当返回值为“undefined”时是否引发异常，默认为false。注意：undefinedAsNull参数具有优先权，此参数仅在undefinedAsNull=false时生效。
 * @property returnTypeMappingStrategy String 函数返回值的类型映射策略，可取值参照TypeMappingStrategy类。
 */
@Target(AnnotationTarget.FUNCTION)
annotation class JsObjectFunction(
    val jsMemberName: String = "",
    val skip: Boolean = SKIP,
    val undefinedAsNull: Boolean = UNDEFINED_AS_NULL,
    val raiseExceptionOnUndefined: Boolean = false,
    val returnTypeMappingStrategy: String = DEFAULT_RETURN_TYPE_MAPPING_STRATEGY,
) {
    companion object {
        const val SKIP = false
        const val UNDEFINED_AS_NULL = true
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
        const val DEFAULT_RETURN_TYPE_MAPPING_STRATEGY = AUTO_DETERMINE
    }
}
