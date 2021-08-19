package com.github.zimolab.jsobjectwrapper.sample

import com.github.zimolab.jsobjectwrapper.annotation.JsObjectInterface
import java.nio.file.Path

@JsObjectInterface
interface JsDate {
}

fun main() {
    val DEFAULT_LOG_FILE = Path.of(System.getProperty("user.home"), "/.js-object-wrapper/ksp.log").toAbsolutePath().toString()
    println(DEFAULT_LOG_FILE)
}