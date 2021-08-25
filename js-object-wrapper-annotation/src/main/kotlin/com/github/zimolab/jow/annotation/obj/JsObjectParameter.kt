package com.github.zimolab.jow.annotation.obj

import com.github.zimolab.jow.annotation.obj.typemapping.TypeMappingStrategy.Companion.NO_MAPPING_EXCEPT_BUILTIN

/**
 * 该注解应用于函数参数，主要作用在于为函数参数指定类型映射策略
 *
 * @property typeMappingStrategy String 类型映射策略，默认为NO_MAPPING_EXCEPT_BUILTIN（即除内置了映射函数的类型外不进行映射）。
 * @constructor
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsObjectParameter(
    val typeMappingStrategy: String = DEFAULT_TYPE_MAPPING_STRATEGY
) {
    companion object {
        const val DEFAULT_TYPE_MAPPING_STRATEGY = NO_MAPPING_EXCEPT_BUILTIN
    }
}