package com.github.zimolab.jow.annotation

/**
 * 该注解应用于抽象类（尚未实现处理逻辑）或接口，用于生成Javascript对象的Kotlin包装类，减少重复性代码的编写。
 *
 * @property outputClassName String 生成的包装类名称
 * @property outputFilename String 生成的文件名
 * @property outputFileEncoding String 生成文件的编码字符集
 * @property primaryConstructor String 生成的主构造函数的类型
 * @property classDoc String 类文档（注释）
 * @constructor
 */
@Target(AnnotationTarget.CLASS)
annotation class JsObjectClass(
    val outputClassName: String = "",
    val outputFilename: String = "",
    val outputFileEncoding: String = DEFAULT_OUTPUT_ENCODING,
    val primaryConstructor: String = DEFAULT_PRIMARY_CONSTRUCTOR,
    val classDoc: String = ""
) {
    companion object {
        const val DEFAULT_OUTPUT_ENCODING = "UTF-8"
        const val DEFAULT_PRIMARY_CONSTRUCTOR = PrimaryConstructor.WithParameter
    }

    object PrimaryConstructor {
        const val None = "None"
        const val Blank = "Blank"
        const val WithParameter = "WithParameter"
    }
}