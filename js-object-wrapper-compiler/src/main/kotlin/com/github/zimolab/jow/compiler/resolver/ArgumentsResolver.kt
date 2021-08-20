package com.github.zimolab.jow.compiler.resolver

import com.github.zimolab.jsarray.base.JsArrayInterface
import com.github.zimolab.jow.array.JsObjectWrapper
import com.github.zimolab.jow.compiler.JsObjectWrapperProcessor
import com.github.zimolab.jow.compiler.debug
import java.util.*
import java.util.logging.Logger

@ExperimentalUnsignedTypes
object ArgumentsResolver {
    fun resolve(arguments: List<ResolvedJsObjectWrapperFunction.FunctionParameter>): MutableList<String> {
        val argumentList = mutableListOf<String>()
        arguments.forEach { arg->
            argumentList.add(resolveArgument(arg))
        }
        return argumentList
    }

    private fun resolveArgument(argument: ResolvedJsObjectWrapperFunction.FunctionParameter): String {
        return if (argument.isVarargs)
            resolveVarargs(argument)
        else
            resolveNormalArg(argument)
    }

    private fun resolveVarargs(argument: ResolvedJsObjectWrapperFunction.FunctionParameter): String {
        val mapPart = if (TypeUtils.isJsObjectWrapperType(argument.type)) {
            "_${resolveJsObjectWrapperArg(argument)}"

        } else if (TypeUtils.isJsArrayInterfaceType(argument.type)) {
            "_${resolveJsArrayArg(argument)}"
        } else {
            null
        }
        val arg = argument.name
        return when (mapPart) {
            null -> {
                val logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)
                logger.debug("arg type: ${argument.type}")
                if (TypeUtils.hasToTypedArrayFunction(argument.type)) {
                    "*(${arg}.toTypedArray())"
                } else {
                    "*(${arg})"
                }
            }
            else -> {
                "*(${arg}.map{_${arg}-> $mapPart }.toTypedArray())"
            }
        }
    }

    private fun resolveNormalArg(argument: ResolvedJsObjectWrapperFunction.FunctionParameter): String {
        return if (TypeUtils.isJsObjectWrapperType(argument.type)) {
            resolveJsObjectWrapperArg(argument)
        } else if (TypeUtils.isJsArrayInterfaceType(argument.type)) {
            resolveJsArrayArg(argument)
        } else {
            argument.name
        }

    }

    private fun resolveJsObjectWrapperArg(argument: ResolvedJsObjectWrapperFunction.FunctionParameter): String {
        return if (TypeUtils.isNullable(argument.type))
            "${argument.name}?.${JsObjectWrapper::source.name}"
        else
            "${argument.name}.${JsObjectWrapper::source.name}"
    }

    private fun resolveJsArrayArg(argument: ResolvedJsObjectWrapperFunction.FunctionParameter): String {
        return if (TypeUtils.isNullable(argument.type))
            "${argument.name}?.${JsArrayInterface<*>::reference.name}"
        else
            "${argument.name}.${JsArrayInterface<*>::reference.name}"
    }

}