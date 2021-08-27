package com.github.zimolab.jow.sample.simpleobject.jsinterface

import com.github.zimolab.jow.annotation.JsObjectClass
import com.github.zimolab.jow.core.JsObjectWrapper

@JsObjectClass
interface JsLine: JsObjectWrapper {
    val start: JsPoint
    val end: JsPoint
    fun length(): Double
    fun contains(point: JsPoint): Boolean
    override fun toString(): String
}