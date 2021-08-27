package com.github.zimolab.jow.annotation

import com.github.zimolab.jow.annotation.typemapping.TypeMappingStrategy.Companion.AUTO_DETERMINE

/**
 * 该注解应用于抽象属性，被注解的属性对应Javascript对象中的一个属性（字段）。
 *
 * @property jsMemberName String 对应Javascript属性名称，为空表示与kotlin中定义的属性同名
 * @property skip Boolean 是否跳过该属性
 * @property undefinedAsNull Boolean 是否将undefined值转换为null
 * @property raiseExceptionOnUndefined Boolean 是否在返回undefined时引发一个异常。该参数仅在undefinedAsNull=false时生效
 * @property getterTypeMappingStrategy String 属性getter的类型映射策略
 * @property setterTypeMappingStrategy String 属性setter的类型映射策略
 * @constructor
 */
@Target(AnnotationTarget.PROPERTY)
annotation class JsObjectProperty(
    val jsMemberName: String = "",
    val skip: Boolean = SKIP,
    val undefinedAsNull: Boolean = UNDEFINED_AS_NULL,
    val raiseExceptionOnUndefined: Boolean = RAISE_EXCEPTION_ON_UNDEFINED,
    val getterTypeMappingStrategy: String = DEFAULT_TYPE_MAPPING_STRATEGY,
    val setterTypeMappingStrategy: String = DEFAULT_TYPE_MAPPING_STRATEGY

) {
    companion object {
        const val SKIP = false
        const val UNDEFINED_AS_NULL = true
        const val RAISE_EXCEPTION_ON_UNDEFINED = false
        const val DEFAULT_TYPE_MAPPING_STRATEGY = AUTO_DETERMINE
    }
}

