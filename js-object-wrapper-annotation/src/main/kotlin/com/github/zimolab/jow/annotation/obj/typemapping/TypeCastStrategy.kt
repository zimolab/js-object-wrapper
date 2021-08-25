package com.github.zimolab.jow.annotation.obj.typemapping

/**
 * 定义了可能的类型转换策略
 * @property name String
 * @constructor
 */
sealed class TypeCastStrategy(val name: String) {
    /**
     * 不使用类型转换
     */
    class NoCast: TypeCastStrategy(NO_CAST)

    /**
     * 自动决定类型转换策略
     */
    class AutoDetermine: TypeCastStrategy(AUTO_DETERMINE)

    /**
     * 原则上不使用类型转换，但具有内置类型转换函数的类型例外
     */
    class NoCastExceptBuiltin: TypeCastStrategy(NO_CAST_EXCEPT_BUILTIN)

    /**
     * 使用类型转换，并且使用自动生成的名称生成类型转换函数声明
     */
    class AutoGenerate: TypeCastStrategy(AUTO_GENERATE)

    /**
     * 使用类型转换，并使用用户指定的名称生成类型转换函数声明
     * @constructor
     */
    class UserSpecify(category: String): TypeCastStrategy(category)

    companion object {
        const val AUTO_DETERMINE = "auto-determine"
        const val NO_CAST = "no-cast"
        const val NO_CAST_EXCEPT_BUILTIN = "no-cast-except-builtin"
        const val AUTO_GENERATE = "auto-gen"

        fun of(category: String): TypeCastStrategy {
            return when(category) {
                AUTO_DETERMINE-> AutoDetermine()
                NO_CAST-> NoCast()
                NO_CAST_EXCEPT_BUILTIN-> NoCastExceptBuiltin()
                AUTO_GENERATE-> AutoGenerate()
                else-> UserSpecify(category)
            }
        }
    }
}