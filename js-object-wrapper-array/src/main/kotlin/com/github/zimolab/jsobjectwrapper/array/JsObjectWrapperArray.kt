package com.github.zimolab.jsobjectwrapper.array

import netscape.javascript.JSObject

abstract class JsObjectWrapperArray<T: JsObjectWrapper>(reference: JSObject, useCache: Boolean = true): JsObjectWrapperArrayTemplate<T>(reference) {
    private val caches = mutableMapOf<Int, T>()

    override var useCache: Boolean = useCache
        set(value) {
            field = value
            if (!value)
                clearCaches()
        }

    private fun clearCaches() {
        caches.clear()
    }

    /**
     * 该方法用于将obj对象映射为T类型。该方法是一个模板方法，必须在子类中加以实现。
     *
     * @param obj Any?
     * @return T?
     */
    override fun asT(obj: Any?): T? {
        super.asT(obj)
        if (obj == null || obj !is JSObject)
            return null
        if (!useCache)
            return createInstance(obj)
        return getOrCreate(obj)
    }

    /**
     * 该方法用于将T类型对象映射为JSObject。该方法是一个模板方法，必须在子类中加以实现。
     *
     * @param value T?
     * @return JSObject?
     */
    override fun toJSObject(value: T?): JSObject? {
        super.toJSObject(value)
        if (value == null)
            return null
        return value.source
    }

    open fun getOrCreate(source: JSObject): T {
        val id = source.hashCode()
        return if (id in caches) {
            caches[id]!!
        } else {
            val newInstance = createInstance(source)
            caches[id] = newInstance
            newInstance
        }
    }

    abstract fun createInstance(source: JSObject): T
}