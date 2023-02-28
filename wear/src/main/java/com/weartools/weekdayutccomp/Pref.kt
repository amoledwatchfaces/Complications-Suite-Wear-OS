package com.weartools.weekdayutccomp

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Pref(val context: Context) {
    // to make sure there's only one instance

    companion object{
        var data :SharedPreferences?=null
        fun getInstance(context:Context):SharedPreferences{
            if (data==null)
                data = PreferenceManager.getDefaultSharedPreferences(context)
            return data!!
        }
    }
    // WORLD CLOCK
    fun getCity():String { return getInstance(context).getString("citiesid","UTC")?:"UTC" }
    fun setCity(value: String) { getInstance(context).edit().putString("citiesid",value).apply() }
    fun getCity2():String { return getInstance(context).getString("citiesid2","UTC")?:"UTC" }
    fun setCity2(value:String) { getInstance(context).edit().putString("citiesid2",value).apply() }
    fun getIsMilitary():Boolean { return getInstance(context).getBoolean("is_military",true) }
    fun setIsMilitary(value: Boolean) { getInstance(context).edit().putBoolean("is_military",value).apply() }
    fun getIsLeadingZero():Boolean { return getInstance(context).getBoolean("leading_zero",true) }
    fun setIsLeadingZero(value: Boolean) { getInstance(context).edit().putBoolean("leading_zero",value).apply() }

    // MOON
    fun getIsHemisphere():Boolean { return getInstance(context).getBoolean("is_northern",true) }
    fun getIsSimpleIcon():Boolean { return getInstance(context).getBoolean("is_simple_icon",false) }
    fun setIsHemisphere(value: Boolean) { getInstance(context).edit().putBoolean("is_northern",value).apply() }
    fun setIsSimpleIcon(value: Boolean) { getInstance(context).edit().putBoolean("is_simple_icon",value).apply() }

    //TIME
    fun getIsMilitaryTime():Boolean { return getInstance(context).getBoolean("is_military_time",true) }
    fun setIsMilitaryTime(value: Boolean) { getInstance(context).edit().putBoolean("is_military_time",value).apply() }
    fun getIsLeadingZeroTime():Boolean { return getInstance(context).getBoolean("leading_zero_time",true) }
    fun setIsLeadingZeroTime(value: Boolean) { getInstance(context).edit().putBoolean("leading_zero_time",value).apply() }

    // WEEK OF YEAR
    fun getIsISO():Boolean { return getInstance(context).getBoolean("is_iso_week",true) }
    fun setIsISO(value: Boolean) { getInstance(context).edit().putBoolean("is_iso_week",value).apply() }

    // DATE
    fun getLongText():String{ return getInstance(context).getString("date_format","EEE, d MMM")?:"EEE, d MMM" }
    fun getShortText():String{ return getInstance(context).getString("short_text_format","d")?:"d" }
    fun getShortTitle():String{ return getInstance(context).getString("short_title_format","MMM")?:"MMM" }
    fun setLongText(value: String){ getInstance(context).edit().putString("date_format",value).apply() }
    fun setShortText(value: String){ getInstance(context).edit().putString("short_text_format",value).apply() }
    fun setShortTitle(value: String){ getInstance(context).edit().putString("short_title_format",value).apply() }

    // LOCALE
    fun updateLocale(s: String) { getInstance(context).edit().putString("locale",s).apply () }
    fun getLocale(): String { return getInstance(context).getString("locale","en")?:"en" }

}