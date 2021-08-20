package com.github.zimolab.jow.compiler

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class JsObjectWrapperProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return JsObjectWrapperProcessor(
            environment.options,
            environment.kotlinVersion,
            environment.codeGenerator)
    }
}