package com.github.zimolab.jsobjectwrapper.compiler.resolver

import com.github.zimolab.jsobjectwrapper.array.JsObjectWrapper
import com.github.zimolab.jsobjectwrapper.compiler.qualifiedName
import com.github.zimolab.jsobjectwrapper.compiler.subclassOf
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import netscape.javascript.JSObject

object TypeResolver {
    val NATIVE_TYPES = mutableListOf<String>(
        Unit::class.qualifiedName!!,
        Boolean::class.qualifiedName!!,
        Int::class.qualifiedName!!,
        Double::class.qualifiedName!!,
        String::class.qualifiedName!!,
        JSObject::class.qualifiedName!!,
        Any::class.qualifiedName!!
    )

    fun isNativeType(type: KSType): Boolean {
        return type.qualifiedName in NATIVE_TYPES
    }

    fun isJsObjectWrapperType(type: KSType): Boolean {
        return if (type.declaration is KSClassDeclaration) {
            (type.declaration as KSClassDeclaration).subclassOf(JsObjectWrapper::class)
        } else {
            false
        }
    }

    fun isNullable(type: KSType): Boolean {
        return type.isMarkedNullable
    }

    fun supportedFunctionParameterType(type: KSType): Boolean {
        return isNativeType(type) || isJsObjectWrapperType(type)
    }

    fun supportedFunctionReturnType(type: KSType): Boolean {
        return isNativeType(type) || isJsObjectWrapperType(type)
    }

}