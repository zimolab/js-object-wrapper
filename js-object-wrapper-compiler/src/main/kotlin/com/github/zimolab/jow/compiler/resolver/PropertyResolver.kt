package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectProperty
import com.github.zimolab.jow.annotation.obj.typecast.TypeCastStrategy
import com.github.zimolab.jow.compiler.*
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import java.util.logging.Logger

@ExperimentalUnsignedTypes
class PropertyResolver(
    val declaration: KSPropertyDeclaration,
    val annotation: KSAnnotation?
) {

    private val logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

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

    fun resolveMutable(): Boolean {
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
        return resolveAnnotationArgument(JsObjectProperty::jsMemberName.name, "").ifEmpty {
            null
        }
    }

    fun resolveGetterTypeCastCategory(): TypeCastStrategy {
        val category =
            resolveAnnotationArgument(JsObjectProperty::getterTypeCast.name, JsObjectProperty.DEFAULT_TYPE_CAST_STRATEGY)
        category.ifEmpty {
            AnnotationProcessingError(
                "@${JsObjectProperty::class.simpleName}注解的${JsObjectProperty::getterTypeCast.name}参数不可为空"
            ).let {
                logger.error(it)
            }
        }
        return TypeCastStrategy.of(category)
    }

    fun resolveSetterTypeCastCategory(): TypeCastStrategy {
        val category =
            resolveAnnotationArgument(JsObjectProperty::setterTypeCast.name, JsObjectProperty.DEFAULT_TYPE_CAST_STRATEGY)
        category.ifEmpty {
            AnnotationProcessingError(
                "@${JsObjectProperty::class.simpleName}注解的${JsObjectProperty::setterTypeCast.name}参数不可为空"
            ).let {
                logger.error(it)
            }
        }
        return TypeCastStrategy.of(category)
    }

}