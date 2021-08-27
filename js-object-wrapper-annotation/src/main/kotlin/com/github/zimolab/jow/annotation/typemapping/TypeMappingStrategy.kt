package com.github.zimolab.jow.annotation.typemapping

/**
 * 定义了可能的类型映射策略
 * @property name String
 * @constructor
 */
sealed class TypeMappingStrategy(val name: String) {
    /**
     * 不使用类型映射
     */
    class NoMapping: TypeMappingStrategy(NO_MAPPING)

    /**
     * 自动决定类型映射策略
     */
    class AutoDetermine: TypeMappingStrategy(AUTO_DETERMINE)

    /**
     * 原则上不使用类型映射，但具有内置类型映射函数的类型例外
     */
    class NoMappingExceptBuiltin: TypeMappingStrategy(NO_MAPPING_EXCEPT_BUILTIN)

    /**
     * 使用类型映射，并且使用自动生成的名称生成类型映射函数声明
     */
    class AutoGenerate: TypeMappingStrategy(AUTO_GENERATE)

    /**
     * 使用类型映射，并使用用户指定的名称生成类型映射函数声明
     * @constructor
     */
    class UserSpecify(category: String): TypeMappingStrategy(category)

    companion object {
        const val AUTO_DETERMINE = "auto-determine"
        const val NO_MAPPING = "no-mapping"
        const val NO_MAPPING_EXCEPT_BUILTIN = "no-mapping-except-builtin"
        const val AUTO_GENERATE = "auto-gen"

        fun of(category: String): TypeMappingStrategy {
            return when(category) {
                AUTO_DETERMINE -> AutoDetermine()
                NO_MAPPING -> NoMapping()
                NO_MAPPING_EXCEPT_BUILTIN -> NoMappingExceptBuiltin()
                AUTO_GENERATE -> AutoGenerate()
                else-> UserSpecify(category)
            }
        }
    }
}