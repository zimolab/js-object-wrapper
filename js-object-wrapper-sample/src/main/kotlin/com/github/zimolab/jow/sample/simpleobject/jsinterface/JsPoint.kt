package com.github.zimolab.jow.sample.simpleobject.jsinterface

import com.github.zimolab.jow.annotation.JsObjectClass
import com.github.zimolab.jow.core.JsObjectWrapper

@JsObjectClass
interface JsPoint: JsObjectWrapper {
    var x: Double
    var y: Double
    fun isOrigin(): Double
    fun plus(other: JsPoint): JsPoint
    fun move(x: Double, y: Double): JsPoint
    override fun toString(): String
}