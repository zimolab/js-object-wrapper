package com.github.zimolab.jow.compiler.resolve

import com.github.zimolab.jow.annotation.JsObjectProperty
import com.github.zimolab.jow.annotation.typemapping.TypeMappingStrategy
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

    fun resolveGetterTypeMappingStrategy(): TypeMappingStrategy {
        val strategy =
            resolveAnnotationArgument(JsObjectProperty::getterTypeMappingStrategy.name, JsObjectProperty.DEFAULT_TYPE_MAPPING_STRATEGY)
        strategy.ifEmpty {
            AnnotationProcessingError(
                "@${JsObjectProperty::class.simpleName}注解的${JsObjectProperty::getterTypeMappingStrategy.name}参数不可为空"
            ).let {
                logger.error(it)
            }
        }
        return TypeMappingStrategy.of(strategy)
    }

    fun resolveSetterTypeMappingStrategy(): TypeMappingStrategy {
        val strategy =
            resolveAnnotationArgument(JsObjectProperty::setterTypeMappingStrategy.name, JsObjectProperty.DEFAULT_TYPE_MAPPING_STRATEGY)
        strategy.ifEmpty {
            AnnotationProcessingError(
                "@${JsObjectProperty::class.simpleName}注解的${JsObjectProperty::setterTypeMappingStrategy.name}参数不可为空"
            ).let {
                logger.error(it)
            }
        }
        return TypeMappingStrategy.of(strategy)
    }

}