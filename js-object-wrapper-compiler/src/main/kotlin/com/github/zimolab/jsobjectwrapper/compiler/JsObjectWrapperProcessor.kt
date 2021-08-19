package com.github.zimolab.jsobjectwrapper.compiler

import com.github.zimolab.jsobjectwrapper.annotation.JsObjectInterface
import com.github.zimolab.jsobjectwrapper.array.JsObjectWrapper
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime.now
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger


class JsObjectWrapperProcessor(
    val options: Map<String, String>,
    val kotlinVersion: KotlinVersion,
    val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    companion object {
        val DEFAULT_LOG_FILE = Path.of(System.getProperty("user.home"), ".js-object-wrapper", "ksp").toAbsolutePath()
    }

    private val logger: Logger

    init {
        logger = initLogger()
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
        val logger = Logger.getLogger(javaClass.canonicalName)
        logger.addHandler(FileHandler(logFile))
        return logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.debug("开始自动处理注解")
        val symbols = resolver.getSymbolsWithAnnotation(JsObjectInterface::class.qualifiedName!!)
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
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            super.visitClassDeclaration(classDeclaration, data)
            logger.debug("正在处理${classDeclaration}")
            if (!classDeclaration.subclassOf(JsObjectWrapper::class)) {
                AnnotationProcessorError("被@${JsObjectInterface::class.simpleName}注解的接口或者类必须实现${JsObjectWrapper::class.simpleName}接口").let {
                    logger.error(it)
                }
            }

            if (classDeclaration.classKind == ClassKind.INTERFACE) {
                if (classDeclaration.subclassOf(JsObjectInterface::class))
                processAnnotatedInterface(annotatedInterface = classDeclaration)
            } else {
                if (classDeclaration.isAbstract())
                    processAnnotatedClass(annotatedClass = classDeclaration)
                else {
                    AnnotationProcessorError("@${JsObjectInterface::class.simpleName}注解不能注解非抽象类").let {
                        logger.error(it)
                    }
                }
            }
        }
    }

    fun processAnnotatedInterface(annotatedInterface: KSClassDeclaration) {

    }

    fun processAnnotatedClass(annotatedClass: KSClassDeclaration) {
        logger.warning("对抽象类的处理程序尚未被实现，${annotatedClass}将被略过！")
    }
}