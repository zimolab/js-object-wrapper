package com.github.zimolab.jow.annotation.obj.typecast

const val AUTO_DETERMINE = "auto-determine"
const val NO_CAST = "no-cast"
const val NO_CAST_EXCEPT_BUILTIN = "no-cast-except-builtin"
const val AUTO_GEN = "auto-gen"


sealed class TypeCastCategory(val name: String) {
    class NoCast: TypeCastCategory(NO_CAST)
    class AutoDetermine: TypeCastCategory(AUTO_DETERMINE)
    class NoCastExceptBuiltin: TypeCastCategory(NO_CAST_EXCEPT_BUILTIN)
    class AutoGenerate: TypeCastCategory(AUTO_GEN)
    class UserSpecify(category: String): TypeCastCategory(category)
    companion object {
        fun of(category: String): TypeCastCategory {
            return when(category) {
                AUTO_DETERMINE-> AutoDetermine()
                NO_CAST-> NoCast()
                NO_CAST_EXCEPT_BUILTIN-> NoCastExceptBuiltin()
                AUTO_GEN-> AutoGenerate()
                else-> UserSpecify(category)
            }
        }
    }
}