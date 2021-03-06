package com.github.zimolab.jow.compiler

import com.github.zimolab.jow.annotation.JsObjectClass
import com.github.zimolab.jow.annotation.JsObjectFunction
import com.github.zimolab.jow.annotation.JsObjectProperty
import com.github.zimolab.jow.core.JsObjectWrapper
import com.github.zimolab.jow.compiler.generator.JsObjectWrapperClassGenerator
import com.github.zimolab.jow.compiler.resolve.ResolvedClass
import com.github.zimolab.jow.compiler.resolve.ResolvedFunction
import com.github.zimolab.jow.compiler.resolve.ResolvedProperty
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime.now
import java.util.logging.FileHandler
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.io.path.Path

@ExperimentalUnsignedTypes
class JsObjectWrapperProcessor(
    private val options: Map<String, String>,
    private val kotlinVersion: KotlinVersion,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    companion object {
        val DEFAULT_LOG_FILE = Path(System.getProperty("user.home"), ".js-object-wrapper", "ksp").toAbsolutePath()
    }

    private val logger: Logger
    private val classFileGenerator: JsObjectWrapperClassGenerator

    init {
        logger = initLogger()
        classFileGenerator = JsObjectWrapperClassGenerator(options, codeGenerator, logger)
    }

    private fun initLogger(): Logger {
        // 创建日志文件
        val logFile = if (options.containsKey("log_file")) {
            "${options["log_file"]}-${now().toString().replace(":", ".")}.log"
        } else {
            "$DEFAULT_LOG_FILE-${now().toString().replace(":", ".")}.log"
        }
        val tmp = File(logFile).parentFile
        if (tmp!=null && !tmp.exists()) {
            tmp.mkdirs()
        }
        // 创建日志器
        LogManager.getLogManager().readConfiguration(javaClass.getResourceAsStream("/log.properties"))
        val logger = Logger.getLogger(JsObjectWrapperProcessor::class.java.canonicalName)
        logger.addHandler(FileHandler(logFile))
        return logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.debug("开始自动处理注解")
        val symbols = resolver.getSymbolsWithAnnotation(JsObjectClass::class.qualifiedName!!)
        val notProcessedSymbols = symbols.filter { !it.validate() }.toList()
        logger.debug("找到${symbols.count()}个被注解的符号")
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it.accept(SymbolVisitor(), Unit)
            }
        return notProcessedSymbols
    }

    inner class SymbolVisitor: KSVisitorVoid() {
        @Suppress("ThrowableNotThrown")
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            super.visitClassDeclaration(classDeclaration, data)
            logger.debug("正在处理${classDeclaration}")
            if (!classDeclaration.subclassOf(JsObjectWrapper::class)) {
                AnnotationProcessingError("被@${JsObjectClass::class.simpleName}注解的接口或者类必须实现${JsObjectWrapper::class.simpleName}接口").let {
                    logger.error(it)
                }
            }
            // 解析被注解的接口或抽象类
            val resolvedClass = if (classDeclaration.classKind == ClassKind.INTERFACE) {
                processAnnotatedInterface(annotatedInterface = classDeclaration)
            } else {
                if (classDeclaration.isAbstract())
                    processAnnotatedClass(annotatedClass = classDeclaration)
                else {
                    AnnotationProcessingError("@${JsObjectClass::class.simpleName}注解不能注解非抽象类").let {
                        logger.error(it, throws = false)
                        throw it
                    }
                }
            }
            resolvedClass?.let {
                classFileGenerator.submit(it)
            }
        }
    }

    fun processAnnotatedInterface(annotatedInterface: KSClassDeclaration): ResolvedClass? {
        val annotation = annotatedInterface.findAnnotations(JsObjectClass::class).firstOrNull()
            ?: AnnotationProcessingError("无法找到${JsObjectClass::class.simpleName}注解").let {
                logger.error(it)
                return null
            }
        val resolvedClass = ResolvedClass(
            originDeclaration = annotatedInterface,
            originAnnotation = annotation,
            options = options)
        // 解析全部函数
        annotatedInterface.getDeclaredFunctions().forEach {functionDeclaration->
            val functionAnnotation = functionDeclaration.findAnnotations(JsObjectFunction::class).firstOrNull()
            resolvedClass.addFunction(ResolvedFunction(functionDeclaration, functionAnnotation))
        }

        //TODO 解析全部属性
        annotatedInterface.getDeclaredProperties().forEach { propertyDeclaration->
            val propertyAnnotation = propertyDeclaration.findAnnotations(JsObjectProperty::class).firstOrNull()
            resolvedClass.addProperty(ResolvedProperty(propertyDeclaration, propertyAnnotation))
        }
        return resolvedClass
    }

    fun processAnnotatedClass(annotatedClass: KSClassDeclaration): ResolvedClass? {
        logger.warning("对抽象类的处理程序尚未被实现，${annotatedClass}将被略过！")
        return null
    }
}