package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectWrapperProperty
import com.github.zimolab.jow.compiler.findArgument
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

class PropertyResolver(
    val declaration: KSPropertyDeclaration,
    val annotation: KSAnnotation?
) {
    private val type by lazy {
        declaration.type.resolve()
    }

    fun resolveName(): String {
        return declaration.simpleNameStr
    }

    fun resolveQualifiedName(): String {
        return declaration.qualifiedNameStr
    }

    fun resolveType(): KSType {
        return type
    }

    fun resolveNullable(): Boolean {
        return type.isMarkedNullable
    }

    fun  resolveMutable(): Boolean {
        return declaration.isMutable
    }

    fun resolveAbstract(): Boolean {
        return declaration.isAbstract()
    }

    inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }

    fun resolveJsMemberName(): String? {
        return resolveAnnotationArgument(JsObjectWrapperProperty::jsMemberName.name, "").ifEmpty {
            null
        }
    }

    fun resolveGetterTypeCastorName(): String {
        return resolveAnnotationArgument(JsObjectWrapperProperty::getterTypeCast.name, JsObjectWrapperProperty.DEFAULT_TYPE_CAST)
    }

    fun resolveSetterTypeCastCategory(): String {
        return resolveAnnotationArgument(JsObjectWrapperProperty::setterTypeCast.name, JsObjectWrapperProperty.DEFAULT_TYPE_CAST)
    }

}