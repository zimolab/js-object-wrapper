package com.github.zimolab.jsobjectwrapper.sample

import com.github.zimolab.jsobjectwrapper.annotation.JsObjectInterface
import com.github.zimolab.jsobjectwrapper.array.JsObjectWrapper
import java.nio.file.Path

@JsObjectInterface
interface JsDate: JsObjectWrapper {

    /** Returns a String representation of a date. The format of the String depends on the locale. */
    override fun toString(): String

    /** Returns a date as a String value. */
    fun toDateString(): String

    /** Returns a time as a String value. */
    fun toTimeString(): String

    /** Returns a value as a String value appropriate to the host environment's current locale. */
    fun toLocaleString(): String

    /** Returns a date as a String value appropriate to the host environment's current locale. */
    fun toLocaleDateString(): String

    /** Returns a time as a String value appropriate to the host environment's current locale. */
    fun toLocaleTimeString(): String

    /** Returns the stored time value in milliseconds since midnight, January 1, 1970 UTC. */
    fun valueOf(): Double

    /** Gets the time value in milliseconds. */
    fun getTime(): Double

    /** Gets the year, using local time. */
    fun getFullYear(): Double

    /** Gets the year using Universal Coordinated Time (UTC). */
    fun getUTCFullYear(): Double

    /** Gets the month, using local time. */
    fun getMonth(): Double

    /** Gets the month of a Date object using Universal Coordinated Time (UTC). */
    fun getUTCMonth(): Double

    /** Gets the day-of-the-month, using local time. */
    fun getDate(): Double

    /** Gets the day-of-the-month, using Universal Coordinated Time (UTC). */
    fun getUTCDate(): Double

    /** Gets the day of the week, using local time. */
    fun getDay(): Double

    /** Gets the day of the week using Universal Coordinated Time (UTC). */
    fun getUTCDay(): Double

    /** Gets the hours in a date, using local time. */
    fun getHours(): Double

    /** Gets the hours value in a Date object using Universal Coordinated Time (UTC). */
    fun getUTCHours(): Double

    /** Gets the minutes of a Date object, using local time. */
    fun getMinutes(): Double

    /** Gets the minutes of a Date object using Universal Coordinated Time (UTC). */
    fun getUTCMinutes(): Double

    /** Gets the seconds of a Date object, using local time. */
    fun getSeconds(): Double

    /** Gets the seconds of a Date object using Universal Coordinated Time (UTC). */
    fun getUTCSeconds(): Double

    /** Gets the milliseconds of a Date, using local time. */
    fun getMilliseconds(): Double

    /** Gets the milliseconds of a Date object using Universal Coordinated Time (UTC). */
    fun getUTCMilliseconds(): Double

    /** Gets the difference in minutes between the time on the local computer and Universal Coordinated Time (UTC). */
    fun getTimezoneOffset(): Double

    /**
     * Sets the date and time value in the Date object.
     * @param time A numeric value representing the Double of elapsed milliseconds since midnight, January 1, 1970 GMT.
     */
    fun setTime(time: Double): Double

    /**
     * Sets the milliseconds value in the Date object using local time.
     * @param ms A numeric value equal to the millisecond value.
     */
    fun setMilliseconds(ms: Double): Double

    /**
     * Sets the milliseconds value in the Date object using Universal Coordinated Time (UTC).
     * @param ms A numeric value equal to the millisecond value.
     */
    fun setUTCMilliseconds(ms: Double): Double

    /**
     * Sets the seconds value in the Date object using local time.
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setSeconds(sec: Double, ms: Double): Double

    /**
     * Sets the seconds value in the Date object using local time.
     * @param sec A numeric value equal to the seconds value.
     */
    fun setSeconds(sec: Double): Double

    /**
     * Sets the seconds value in the Date object using Universal Coordinated Time (UTC).
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setUTCSeconds(sec: Double, ms: Double): Double

    /**
     * Sets the seconds value in the Date object using Universal Coordinated Time (UTC).
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setUTCSeconds(sec: Double): Double

    /**
     * Sets the minutes value in the Date object using local time.
     * @param min A numeric value equal to the minutes value.
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setMinutes(min: Double, sec: Double, ms: Double): Double

    /**
     * Sets the minutes value in the Date object using Universal Coordinated Time (UTC).
     * @param min A numeric value equal to the minutes value.
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setUTCMinutes(min: Double, sec: Double, ms: Double): Double

    /**
     * Sets the hour value in the Date object using local time.
     * @param hours A numeric value equal to the hours value.
     * @param min A numeric value equal to the minutes value.
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setHours(hours: Double, min: Double, sec: Double, ms: Double): Double

    /**
     * Sets the hours value in the Date object using Universal Coordinated Time (UTC).
     * @param hours A numeric value equal to the hours value.
     * @param min A numeric value equal to the minutes value.
     * @param sec A numeric value equal to the seconds value.
     * @param ms A numeric value equal to the milliseconds value.
     */
    fun setUTCHours(hours: Double, min: Double, sec: Double, ms: Double): Double

    /**
     * Sets the numeric day-of-the-month value of the Date object using local time.
     * @param date A numeric value equal to the day of the month.
     */
    fun setDate(date: Double): Double

    /**
     * Sets the numeric day of the month in the Date object using Universal Coordinated Time (UTC).
     * @param date A numeric value equal to the day of the month.
     */
    fun setUTCDate(date: Double): Double

    /**
     * Sets the month value in the Date object using local time.
     * @param month A numeric value equal to the month. The value for January is 0, and other month values follow consecutively.
     * @param date A numeric value representing the day of the month. If this value is not supplied, the value from a call to the getDate method is used.
     */
    fun setMonth(month: Double, date: Double): Double

    /**
     * Sets the month value in the Date object using Universal Coordinated Time (UTC).
     * @param month A numeric value equal to the month. The value for January is 0, and other month values follow consecutively.
     * @param date A numeric value representing the day of the month. If it is not supplied, the value from a call to the getUTCDate method is used.
     */
    fun setUTCMonth(month: Double, date: Double): Double

    /**
     * Sets the year of the Date object using local time.
     * @param year A numeric value for the year.
     * @param month A zero-based numeric value for the month (0 for January, 11 for December). Must be specified if numDate is specified.
     * @param date A numeric value equal for the day of the month.
     */
    fun setFullYear(year: Double, month: Double, date: Double): Double

    /**
     * Sets the year value in the Date object using Universal Coordinated Time (UTC).
     * @param year A numeric value equal to the year.
     * @param month A numeric value equal to the month. The value for January is 0, and other month values follow consecutively. Must be supplied if numDate is supplied.
     * @param date A numeric value equal to the day of the month.
     */
    fun setUTCFullYear(year: Double, month: Double, date: Double): Double

    /** Returns a date converted to a String using Universal Coordinated Time (UTC). */
    fun toUTCString(): String

    /** Returns a date as a String value in ISO format. */
    fun toISOString(): String

    /** Used by the JSON.Stringify method to enable the transformation of an object's data for JavaScript Object Notation (JSON) serialization. */
    fun toJSON(key: Any): String
}