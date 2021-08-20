package com.github.zimolab.jow.array

import com.github.zimolab.jsarray.*
import com.github.zimolab.jsarray.base.*
import netscape.javascript.JSObject

/**
 * 用于js对象包装类（java wrapper for js object）的数组类
 *注意：不要直接使用该类，该类相当于一个模板类，其内在逻辑依赖于两个模板方法：asType()、toJSObject()，应当继承该类，然后在子类中实现模板方法。
 *
 * @param T
 * @property reference JSObject
 * @property useCache Boolean
 * @property impl JsArray<JSObject?>
 * @property length Int
 * @constructor
 */
open class JsObjectWrapperArrayTemplate<T>(final override val reference: JSObject) : JsArrayInterface<T> {
    open var useCache: Boolean = false
    val impl = JsArray.jsObjectArrayOf(reference)

    /**
     * 数组元素数量
     */
    override val length: Int
        get() = impl.length


    /**
     * 该方法用于将obj对象映射为T类型。该方法是一个模板方法，必须在子类中加以实现。
     *
     * @param obj Any?
     * @return T?
     */
    open fun asT(obj: Any?): T? {
        return null
    }

    open fun asTs(vararg objects: Any?) = objects.map { asT(it) }

    /**
     * 该方法用于将T类型对象映射为JSObject。该方法是一个模板方法，必须在子类中加以实现。
     *
     * @param value T?
     * @return JSObject?
     */
    open fun toJSObject(value: T?): JSObject? {
        return null
    }

    open fun toJSObjects(vararg values: T?) =
        values.map { toJSObject(it) }

    open fun toUnTypedValue(value: Any?): Any? {
        if (value == null)
            return null
        val v = asT(value)
        return v ?: value
    }

    /**
     *
     * @param other JsArrayInterface<T>
     * @return JsArrayInterface<T>
     */
    override fun concat(other: JsArrayInterface<T>): JsObjectWrapperArrayTemplate<T> {
        val tmp = JsArray.jsObjectArrayOf(other.reference)
        return JsObjectWrapperArrayTemplate(impl.concat(tmp).reference)
    }

    override fun concatAny(other: JsArrayInterface<T>): JsArrayInterface<Any?> {
        return impl.concatAny(JsObjectWrapperArrayTemplate(other.reference))
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Boolean>
     * @return Boolean
     */
    override fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        return if (callback is UntypedIteratorCallback) {
            impl.every(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.every(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Boolean>
     * @return Boolean
     */
    inline fun every(crossinline callback: TypedCallback2<T?, Boolean>): Boolean {
        return impl.every(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index, asT(currentValue))
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return Boolean
     */
    inline fun every(crossinline callback: UntypedCallback2<Boolean>): Boolean {
        return impl.every(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        })
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Boolean>
     * @return Boolean
     */
    override fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        return if (callback is UntypedIteratorCallback) {
            impl.some(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.some(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Boolean>
     * @return Boolean
     */
    inline fun some(crossinline callback: TypedCallback2<T?, Boolean>): Boolean {
        return impl.some(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index, asT(currentValue))
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return Boolean
     */
    inline fun some(crossinline callback: UntypedCallback2<Boolean>): Boolean {
        return impl.some(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        })
    }


    /**
     *
     * @param value T?
     * @param start Int
     * @param end Int?
     * @return JsArrayInterface<T>
     */
    override fun fill(value: T?, start: Int, end: Int?): JsObjectWrapperArrayTemplate<T> {
        impl.fill(toJSObject(value), start, end)
        return this
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Boolean>
     * @return JsArrayInterface<T>
     */
    override fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArrayInterface<T> {
        return if (callback is UntypedIteratorCallback) {
            impl.filter(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.filter(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }.let { result ->
            JsObjectWrapperArrayTemplate(result.reference)
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Boolean>
     * @return JsArrayInterface<T?>
     */
    inline fun filter(crossinline callback: TypedCallback2<T?, Boolean>): JsArrayInterface<T> {
        return impl.filter(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index, asT(currentValue))
            }
        }).let {
            JsObjectWrapperArrayTemplate(it.reference)
        }
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return JsArrayInterface<T>
     */
    inline fun filter(crossinline callback: UntypedCallback2<Boolean>): JsArrayInterface<T> {
        return impl.filter(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        }).let {
            JsObjectWrapperArrayTemplate(it.reference)
        }
    }

    /**
     *
     * @param callback UntypedIteratorCallback<Boolean>
     * @return JsArrayInterface<Any?>
     */
    override fun filterAny(callback: UntypedIteratorCallback<Boolean>): JsArrayInterface<Any?> {
        return impl.filterAny(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback.call(toUnTypedValue(currentValue), index, total, arr)
            }
        }).toJsAnyArray()
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return JsArrayInterface<Any?>
     */
    inline fun filterAny(crossinline callback: UntypedCallback2<Boolean>): JsArrayInterface<Any?> {
        return impl.filterAny(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        }).toJsAnyArray()
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Boolean>
     * @return T?
     */
    override fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T? {
        return if (callback is UntypedIteratorCallback) {
            impl.find(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.find(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }.let {
            asT(it)
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Boolean>
     * @return T?
     */
    inline fun find(crossinline callback: TypedCallback2<T?, Boolean>): T? {
        return impl.find(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index, asT(currentValue))
            }
        }).let {
            asT(it)
        }
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return T?
     */
    inline fun find(crossinline callback: UntypedCallback2<Boolean>): T? {
        return impl.find(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        }).let {
            asT(it)
        }
    }

    /**
     *
     * @param callback UntypedIteratorCallback<Boolean>
     * @return Any?
     */
    override fun findAny(callback: UntypedIteratorCallback<Boolean>): Any? {
        return impl.findAny(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback.call(toUnTypedValue(currentValue), index, total, arr)
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return Any?
     */
    inline fun findAny(crossinline callback: UntypedCallback2<Boolean>): Any? {
        return impl.findAny(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        })
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Boolean>
     * @return Int
     */
    override fun findIndex(callback: JsArrayIteratorCallback<T?, Boolean>): Int {
        return if (callback is UntypedIteratorCallback) {
            impl.findIndex(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.findIndex(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Boolean>
     * @return Int
     */
    inline fun findIndex(crossinline callback: TypedCallback2<T?, Boolean>): Int {
        return impl.findIndex(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index, asT(currentValue))
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     * @return Int
     */
    inline fun findIndex(crossinline callback: UntypedCallback2<Boolean>): Int {
        return impl.findIndex(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        })
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Unit>
     */
    override fun forEach(callback: JsArrayIteratorCallback<T?, Unit>) {
        if (callback is UntypedIteratorCallback) {
            impl.forEach(object : UntypedIteratorCallback<Unit> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?) {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.forEach(object : UntypedIteratorCallback<Unit> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?) {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Unit>
     */
    inline fun forEach(crossinline callback: TypedCallback2<T?, Unit>) {
        return impl.forEach(object : UntypedIteratorCallback<Unit> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?) {
                callback(index, asT(currentValue))
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Unit>
     */
    inline fun forEach(crossinline callback: UntypedCallback2<Unit>) {
        return impl.forEach(object : UntypedIteratorCallback<Unit> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?) {
                callback(index to toUnTypedValue(currentValue))
            }
        })
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, Boolean>
     * @param startIndex Int
     * @param stopIndex Int
     * @param step Int
     */
    override fun forLoop(
        callback: JsArrayIteratorCallback<T?, Boolean>,
        startIndex: Int,
        stopIndex: Int,
        step: Int
    ) {
        if (callback is UntypedIteratorCallback) {
            impl.forLoop(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(toUnTypedValue(currentValue), index, null, arr)
                }
            })
        } else {
            impl.forLoop(object : UntypedIteratorCallback<Boolean> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                    return callback.call(asT(currentValue), index, null, arr)
                }
            })
        }
    }

    /**
     *
     * @param startIndex Int
     * @param stopIndex Int
     * @param step Int
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, Boolean>
     */
    inline fun forLoop(
        startIndex: Int = 0,
        stopIndex: Int = -1,
        step: Int = 1,
        crossinline callback: TypedCallback2<T?, Boolean>
    ) {
        return impl.forLoop(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index, asT(currentValue))
            }
        })
    }

    /**
     *
     * @param startIndex Int
     * @param stopIndex Int
     * @param step Int
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Boolean>
     */
    inline fun forLoop(
        startIndex: Int = 0,
        stopIndex: Int = -1,
        step: Int = 1,
        crossinline callback: UntypedCallback2<Boolean>
    ) {
        return impl.forLoop(object : UntypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to toUnTypedValue(currentValue))
            }
        }, startIndex, stopIndex, step)
    }

    /**
     *
     * @param index Int
     * @return T?
     */
    override operator fun get(index: Int): T? {
        return asT(impl[index])
    }

    /**
     *
     * @param index Int
     * @return Any?
     */
    override fun getAny(index: Int): Any? {
        return toUnTypedValue(impl.getAny(index))
    }

    /**
     *
     * @param element T?
     * @param start Int
     * @return Boolean
     */
    override fun includes(element: T, start: Int): Boolean {
        return impl.includes(toJSObject(element), start)
    }

    override fun includesAny(element: Any?, start: Int): Boolean {
        return impl.includesAny(element, start)
    }

    /**
     *
     * @param element T?
     * @param start Int
     * @return Int
     */
    override fun indexOf(element: T, start: Int): Int {
        return impl.indexOf(toJSObject(element), start)
    }

    override fun indexOfAny(element: Any?, start: Int): Int {
        return impl.indexOfAny(element, start)
    }

    /**
     *
     * @param separator String
     * @return String
     */
    override fun join(separator: String): String {
        return impl.join(separator)
    }

    /**
     *
     * @param element T
     * @param start Int
     * @return Int
     */
    override fun lastIndexOf(element: T, start: Int): Int {
        return impl.lastIndexOf(toJSObject(element))
    }

    /**
     *
     * @param element Any?
     * @param start Int
     * @return Int
     */
    override fun lastIndexOfAny(element: Any?, start: Int): Int {
        return impl.lastIndexOfAny(element, start)
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, T?>
     * @return JsWrapperArray<T>
     */
    override fun map(callback: JsArrayIteratorCallback<T?, T?>): JsObjectWrapperArrayTemplate<T> {
        return if (callback is UntypedIteratorCallback) {
            impl.map(object : UntypedIteratorCallback<JSObject?> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                    return toJSObject(callback.call(toUnTypedValue(currentValue), index, total, arr))
                }
            })
        } else {
            impl.map(object : UntypedIteratorCallback<JSObject?> {
                override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                    return toJSObject(callback.call(asT(currentValue), index, null, arr))
                }
            })
        }.let {
            JsObjectWrapperArrayTemplate(it.reference)
        }
    }

    /**
     *
     * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, T?>
     * @return JsWrapperArray<T>
     */
    inline fun map(crossinline callback: TypedCallback2<T?, T?>): JsObjectWrapperArrayTemplate<T> {
        return impl.map(object : UntypedIteratorCallback<JSObject?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                return toJSObject(callback(index, asT(currentValue)))
            }
        }).let {
            JsObjectWrapperArrayTemplate(it.reference)
        }
    }

    /**
     *
     * @param callback UntypedIteratorCallback<Any?>
     * @return JsArrayInterface<Any?>
     */
    override fun mapAny(callback: UntypedIteratorCallback<Any?>): JsArrayInterface<Any?> {
        return impl.mapAny(object : UntypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback.call(toUnTypedValue(currentValue), index, total, arr)
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Pair<Int, Any?>, Any?>
     * @return JsArrayInterface<Any?>
     */
    inline fun mapAny(crossinline callback: UntypedCallback2<Any?>): JsArrayInterface<Any?> {
        return impl.mapAny(object : UntypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback(index to toUnTypedValue(currentValue))
            }
        })
    }

    /**
     *
     * @return T?
     */
    override fun pop(): T? {
        return asT(impl.pop())
    }

    /**
     *
     * @return Any?
     */
    override fun popAny(): Any? {
        return toUnTypedValue(impl.popAny())
    }

    /**
     *
     * @param elements Array<out T?>
     * @return Int
     */
    override fun push(vararg elements: T?): Int {
        return impl.push(*toJSObjects(*elements).toTypedArray())
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, T?>
     * @return T?
     */
    override fun reduce(callback: JsArrayIteratorCallback<T?, T?>): T? {
        return impl.reduce(object : JsArrayIteratorCallback<Any?, JSObject?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                return toJSObject(callback.call(asT(currentValue), index, asT(total), arr))
            }
        }).let {
            asT(it)
        }
    }

    /**
     *
     * @param callback Function3<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, [@kotlin.ParameterName] T?, T?>
     * @return T?
     */
    inline fun reduce(crossinline callback: TypedCallback3<T?, T?>): T? {
        return impl.reduce(object : JsArrayIteratorCallback<Any?, JSObject?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                return toJSObject(callback(index, asT(currentValue), asT(total)))
            }
        }).let {
            asT(it)
        }
    }

    /**
     *
     * @param callback UntypedIteratorCallback<Any?>
     * @return Any?
     */
    override fun reduceAny(callback: UntypedIteratorCallback<Any?>): Any? {
        return impl.reduceAny(object : UntypedIteratorCallback<Any?> {
            var count = 0
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return if (count==0) {
                    count++
                    callback.call(toUnTypedValue(currentValue), index, toUnTypedValue(total), arr)
                } else {
                    count++
                    callback.call(toUnTypedValue(currentValue), index, total, arr)
                }
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Triple<Int, Any?, Any?>, Any?>
     * @return Any?
     */
    inline fun reduceAny(crossinline callback: UntypedCallback3<Any?>): Any? {
        return impl.reduceAny(object : UntypedIteratorCallback<Any?> {
            var count = 0
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return if (count == 0) {
                    count++
                    callback(Triple(index, toUnTypedValue(currentValue), toUnTypedValue(total)))
                } else {
                    count++
                    callback(Triple(index, toUnTypedValue(currentValue), total))
                }
            }
        })
    }

    /**
     *
     * @param callback JsArrayIteratorCallback<T?, T?>
     * @return T?
     */
    override fun reduceRight(callback: JsArrayIteratorCallback<T?, T?>): T? {
        return impl.reduceRight(object : JsArrayIteratorCallback<Any?, JSObject?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                return toJSObject(callback.call(asT(currentValue), index, asT(total), arr))
            }
        }).let {
            asT(it)
        }
    }

    /**
     *
     * @param callback Function3<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] T?, [@kotlin.ParameterName] T?, T?>
     * @return T?
     */
    inline fun reduceRight(crossinline callback: TypedCallback3<T?, T?>): T? {
        return impl.reduceRight(object : JsArrayIteratorCallback<Any?, JSObject?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): JSObject? {
                return toJSObject(callback(index, asT(currentValue), asT(total)))
            }
        }).let {
            asT(it)
        }
    }

    /**
     *
     * @param callback UntypedIteratorCallback<Any?>
     * @return Any?
     */
    override fun reduceRightAny(callback: UntypedIteratorCallback<Any?>): Any? {
        return impl.reduceRightAny(object : UntypedIteratorCallback<Any?> {
            var count = 0
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return if (count == 0) {
                    count++
                    callback.call(toUnTypedValue(currentValue), index, toUnTypedValue(total), arr)
                } else {
                    count++
                    callback.call(toUnTypedValue(currentValue), index, total, arr)
                }
            }
        })
    }

    /**
     *
     * @param callback Function1<[@kotlin.ParameterName] Triple<Int, Any?, Any?>, Any?>
     * @return Any?
     */
    inline fun reduceRightAny(crossinline callback: UntypedCallback3<Any?>): Any? {
        return impl.reduceRightAny(object : UntypedIteratorCallback<Any?> {
            var count = 0
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return if (count == 0) {
                    count++
                    callback(Triple(index, toUnTypedValue(currentValue),toUnTypedValue(total)))
                } else {
                    count++
                    callback(Triple(index, toUnTypedValue(currentValue), total))
                }
            }
        })
    }


    /**
     *
     * @return JsArrayInterface<T>
     */
    override fun reverse(): JsObjectWrapperArrayTemplate<T> {
        impl.reverse()
        return this
    }

    /**
     *
     * @param index Int
     * @param value T?
     */
    override operator fun set(index: Int, value: T?) {
        impl[index] = toJSObject(value)
    }

    /**
     *
     * @return T?
     */
    override fun shift(): T? {
        return asT(impl.shift())
    }

    /**
     *
     * @return Any?
     */
    override fun shiftAny(): Any? {
        return toUnTypedValue(impl.shiftAny())
    }

    /**
     *
     * @param start Int
     * @param end Int?
     * @return JsArrayInterface<T>
     */
    override fun slice(start: Int, end: Int?): JsArrayInterface<T> {
        return JsObjectWrapperArrayTemplate(impl.slice(start, end).reference)
    }

    override fun sort(sortFunction: JsArraySortFunction<T?>?): JsObjectWrapperArrayTemplate<T> {
        if (sortFunction == null) {
            impl.sort()
            return this
        }
//        impl.reference.inject("sort_cb", sortFunction)
//        val result = impl.reference.execute("""
//            this.sort((a,b)=>{
//                a=(a==undefined? null : a);
//                b=(b==undefined? null : b);
//                return this.sort_cb.compare(a, b)
//            })
//        """.trimIndent())
//        impl.reference.uninject("sort_cb")
//        if (result !is JSObject)
//            throw RuntimeException("")
//        return this
        if (sortFunction is UnTypedSortFunction) {
            impl.sort(object : UnTypedSortFunction {
                override fun compare(a: Any?, b: Any?): Int {
                    return sortFunction.compare(toUnTypedValue(a), toUnTypedValue(b))
                }
            })
        } else {
            impl.sort(object : UnTypedSortFunction {
                override fun compare(a: Any?, b: Any?): Int {
                    return sortFunction.compare(asT(a), asT(b))
                }
            })
        }
        return this
    }

    inline fun sort(crossinline comparator: TypedSortComparator<T?>): JsObjectWrapperArrayTemplate<T> {
        impl.sort(object : UnTypedSortFunction {
            override fun compare(a: Any?, b: Any?): Int {
                return comparator(asT(a), asT(b))
            }
        })
        return this
    }

    inline fun sort(crossinline comparator: UntypedSortComparator): JsObjectWrapperArrayTemplate<T> {
        impl.sort(object : UnTypedSortFunction {
            override fun compare(a: Any?, b: Any?): Int {
                return comparator(toUnTypedValue(a) to toUnTypedValue(b))
            }
        })
        return this
    }

    override fun splice(index: Int, count: Int, vararg items: T?): JsArrayInterface<T> {
        return JsObjectWrapperArrayTemplate(impl.splice(index, count, *toJSObjects(*items).toTypedArray()).reference)
    }

    override fun unshift(vararg elements: T?): Int {
        return impl.unshift(*(toJSObjects(*elements).toTypedArray()))
    }

    override fun toJsAnyArray(): JsArrayInterface<Any?> {
        return impl.toJsAnyArray()
    }

    override fun toString(): String {
        return "[" + impl.join(",") + "]"
    }
}