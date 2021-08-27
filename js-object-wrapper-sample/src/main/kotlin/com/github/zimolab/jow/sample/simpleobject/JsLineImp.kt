package com.github.zimolab.jow.sample.simpleobject

import com.github.zimolab.jow.sample.simpleobject.jsinterface.AbsJsLine
import com.github.zimolab.jow.sample.simpleobject.jsinterface.JsPoint
import netscape.javascript.JSObject

class JsLineImp(source: JSObject): AbsJsLine(source) {
    /**
     * This is an auto-generated type mapping function, which is used in the underlying web engine
     * calls.
     * This is an abstract function please implement it with your own type mapping logic.
     *
     * 	(src:Any?) -> JsPoint
     *
     * @param src Any?
     * @return JsPoint
     */
    override fun asJsPoint(src: Any?): JsPoint {
        if (src !is JSObject) {
            throw RuntimeException("js invoke failed")
        }
        return JsPointImp(src)
    }
}