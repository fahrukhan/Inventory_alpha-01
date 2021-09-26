package com.example.inventory_alpha_01.helper

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    public const val DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_YMD = "yyyy-MM-dd"
    const val DATE_FORMAT_YMDH = "yyyy-MM-dd HH"
    const val DATE_FORMAT_YMDHM = "yyyy-MM-dd HH:mm"
    const val DATE_FORMAT_Y = "yyyy"
    const val DATE_FORMAT_MM = "MM"
    const val DATE_FORMAT_DD = "dd"
    const val DATE_FORMAT_HMS = "HH:mm:ss"
    const val DATE_FORMAT_HM = "HH:mm"
    const val DATE_FORMAT_HH = "HH"
    const val DATE_FORMAT_mm = "mm"
    const val DATE_FORMAT_ss = "ss"

    var CALENDAR_FIRST_DAY_OF_WEEK = Calendar.SUNDAY
    var MAX_WEEK_DAYS = 7
//    companion object{
//
//    }

    /**
     * 获取本周或本月或今年的第一天和最后一天日期（年月日）
     * @param pattern 年月日格式（如yyyy-MM-dd）
     * @param field 领域（本周Calendar.DAY_OF_WEEK，本月Calendar.DAY_OF_MONTH，本年Calendar.DAY_OF_YEAR）
     * @return 长度为2的字符串数组
     */
    private fun getDayOfWeekOrMonthOrYear(pattern: String, field: Int): Array<String> {
        val days = arrayOf<String>()
        val dateFormat: SimpleDateFormat = getSimpleDateFormat(pattern)
        val cal: Calendar = getCalendar(CALENDAR_FIRST_DAY_OF_WEEK)
        cal[field] = 1
        days[0] = dateFormat.format(cal.time)
        cal[field] = cal.getActualMaximum(field)
        days[1] = dateFormat.format(cal.time)
        return days
    }

    /**
     * 获取指定日期的那一周的所有日期
     * @param pattern 日期格式
     * @param time 日期
     * @return
     */
    fun getDaysOfWeekForDate(pattern: String, time: String): Array<String> {
        val dateFormat: SimpleDateFormat = getSimpleDateFormat(pattern)
        return try {
            val date = dateFormat.parse(time)
            val cal: Calendar = getCalendar(CALENDAR_FIRST_DAY_OF_WEEK,date)
            val weekDays = cal.getActualMaximum(Calendar.DAY_OF_WEEK)
            val days = arrayOf<String>()
            for (i in 0 until weekDays) {
                cal[Calendar.DAY_OF_WEEK] = i + 1
                days[i] = dateFormat.format(cal.time)
            }
            days
        } catch (e: ParseException) {
            arrayOf()
        }
    }

    /**
     * 获取当前时间指定格式的日期时间
     * @param pattern
     * @return
     */
    fun getCurrFormatDate(pattern: String): String? {
        return getFormatDate(pattern, System.currentTimeMillis())
    }

    /**
     * 根据时间戳获取指定格式的日期时间
     * @param pattern
     * @param time
     * @return
     */
    fun getFormatDate(pattern: String, time: Long): String? {
        val dateFormat: SimpleDateFormat = getSimpleDateFormat(pattern)
        val date = Date(time)
        return dateFormat.format(date)
    }

    private fun getSimpleDateFormat(pattern: String): SimpleDateFormat {
        val dateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
        dateFormat.applyPattern(pattern)
        return dateFormat
    }

    private fun getCalendar(firstDayOfWeek: Int): Calendar {
        return getCalendar(firstDayOfWeek, Date())
    }

    private fun getCalendar(firstDayOfWeek: Int, date: Date): Calendar {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = firstDayOfWeek // 设置周几为每周的第一天
        cal.time = date
        return cal
    }


}