package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperProperty
import com.github.zimolab.jow.compiler.TypeUtils
import com.github.zimolab.jow.compiler.simpleName
import com.google.devtools.ksp.symbol.KSType

class TypeCastor private constructor(
    val target: Target,
    val category: Category,
    val castorName: String?
) {
    enum class Target {
        PROPERTY_SETTER,
        PROPERTY_GETTER,
        FUNCTION_RETURN_VALUE,
    }

    enum class Category {
        AUTO_GENERATED,
        AS_OPERATOR,
        USER_SPECIFIED,
        NOT_APPLICABLE
    }

    companion object {

        fun autoGenerateCastorName(
            target: Target,
            type: KSType,
            prefix: String = "__",
            suffix: String = "__",
        ): String {
            return when(target) {
                Target.PROPERTY_SETTER -> "${prefix}cast${type.simpleName}${if (TypeUtils.isNullable(type))"Nullable" else ""}$suffix"
                Target.PROPERTY_GETTER -> "${prefix}as${type.simpleName}${if (TypeUtils.isNullable(type))"Nullable" else ""}$suffix"
                Target.FUNCTION_RETURN_VALUE -> "${prefix}as${type.simpleName}${if (TypeUtils.isNullable(type))"Nullable" else ""}$suffix"
            }
        }


        fun forSetter(property: ResolvedJsObjectWrapperProperty): TypeCastor {
            val originSetterTypeCastValue = property.resolver.resolveSetterTypeCastorName()
            val type = property.type
            // 以下类型不需要进行类型转换
            // 对于JsObjectWrapper、JsArrayInterface对象而言，内部已经实现了固定的类型转换程序，换言之，用户定义的类型转换器是无效的。
            if (
                TypeUtils.isNativeType(type) ||
                TypeUtils.isVoidType(type) ||
                TypeUtils.isAnyType(type) ||
                TypeUtils.isJsObjectWrapperType(type) ||
                TypeUtils.isJsArrayInterfaceType(type)
            ) {
                return TypeCastor(
                    Target.PROPERTY_SETTER,
                    Category.NOT_APPLICABLE,
                    null
                )
            }

            // 对于setter而言，使用as运算符作为类型转换的方法是不适用的
            if (
                originSetterTypeCastValue == JsObjectWrapperProperty.TYPE_CAST_NOT_APPLICABLE ||
                originSetterTypeCastValue == JsObjectWrapperProperty.TYPE_CAST_USE_AS_OPERATOR
            ) {
                return TypeCastor(
                    Target.PROPERTY_SETTER,
                    Category.NOT_APPLICABLE,
                    null
                )
            }

            // 判断是否自动生成类型转换函数名称
            return if (originSetterTypeCastValue == JsObjectWrapperProperty.TYPE_CAST_USE_AUTO_GEN_CASTOR) {
                TypeCastor(
                    Target.PROPERTY_SETTER,
                    Category.AUTO_GENERATED,
                    autoGenerateCastorName(Target.PROPERTY_SETTER, type)
                )
            } else {
                TypeCastor(
                    Target.PROPERTY_SETTER,
                    Category.USER_SPECIFIED,
                    originSetterTypeCastValue
                )
            }
        }

        fun forGetter(property: ResolvedJsObjectWrapperProperty): TypeCastor {
            val originGetterTypeCastValue = property.resolver.resolveGetterTypeCastorName()
            val type = property.type
            // 以下类型不需要进行类型转换
            if (
                TypeUtils.isNativeType(type) ||
                TypeUtils.isVoidType(type) ||
                TypeUtils.isAnyType(type)
            ) {
                return TypeCastor(
                    Target.PROPERTY_GETTER,
                    Category.NOT_APPLICABLE,
                    null
                )
            }

            // 不使用类型转换
            if (originGetterTypeCastValue == JsObjectWrapperProperty.TYPE_CAST_NOT_APPLICABLE) {
                return TypeCastor(
                    Target.PROPERTY_GETTER,
                    Category.NOT_APPLICABLE,
                    null
                )
            }

            // 使用as运算符进行类型转换
            if (originGetterTypeCastValue == JsObjectWrapperProperty.TYPE_CAST_USE_AS_OPERATOR) {
                return TypeCastor(
                    Target.PROPERTY_GETTER,
                    Category.AS_OPERATOR,
                    null
                )
            }

            // 判断是否使用自动生成的类型转换函数名称
            return if (originGetterTypeCastValue == JsObjectWrapperProperty.TYPE_CAST_USE_AUTO_GEN_CASTOR) {
                TypeCastor(
                    Target.PROPERTY_GETTER,
                    Category.AUTO_GENERATED,
                    autoGenerateCastorName(Target.PROPERTY_GETTER, type)
                )
            } else {
                TypeCastor(
                    Target.PROPERTY_GETTER,
                    Category.AUTO_GENERATED,
                    originGetterTypeCastValue
                )
            }

        }

        fun forFunctionReturnValue(func: ResolvedJsObjectWrapperFunction): TypeCastor {
            TODO()
        }
    }
}