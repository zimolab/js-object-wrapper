package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jow.annotation.obj.JsObjectParameter
import com.github.zimolab.jow.annotation.obj.typemapping.TypeMappingStrategy
import com.github.zimolab.jow.compiler.AnnotationProcessingError
import com.github.zimolab.jow.compiler.JsObjectWrapperProcessor
import com.github.zimolab.jow.compiler.error
import com.github.zimolab.jow.compiler.findArgument
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import java.util.logging.Logger

@ExperimentalUnsignedTypes
class FunctionParameterResolver(
    private val declaration: KSValueParameter,
    private val annotation: KSAnnotation?
) {
    private val logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)

    fun resolveName(): String {
        val name = declaration.name?.asString()
            ?: AnnotationProcessingError("在解析函数参数返回值时出现一个错误").let {
                logger.error(it, throws = false)
                throw it
            }
        return name
    }

    fun resolveType(): KSType {
        return declaration.type.resolve()
    }

    fun resolveIsVararg(): Boolean {
        return declaration.isVararg
    }

    private inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }

    fun resolveTypeCastCategory(): TypeMappingStrategy {
        val category = resolveAnnotationArgument(JsObjectParameter::typeCast.name, JsObjectParameter.DEFAULT_TYPE_CAST_STRATEGY)
        category.ifEmpty {
            AnnotationProcessingError("@${JsObjectParameter::class.simpleName}注解的的${JsObjectParameter::typeCast.name}参数不可为空").let {
                logger.error(it, throws = false)
                throw it
            }
        }
        return TypeMappingStrategy.of(category)
    }
}