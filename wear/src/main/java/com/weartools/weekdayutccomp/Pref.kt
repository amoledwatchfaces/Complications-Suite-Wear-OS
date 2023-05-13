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

    // TIME DIFF
    fun getTimeDiffStyle():String{ return getInstance(context).getString("time_diff_style","SHORT_DUAL_UNIT")?:"SHORT_DUAL_UNIT" }
    fun setTimeDiffStyle(value: String){ getInstance(context).edit().putString("time_diff_style",value).apply() }

    // CUSTOM TEXT
    fun setCustomText(value: String){ getInstance(context).edit().putString("custom_text",value).apply() }
    fun setCustomTitle(value: String){ getInstance(context).edit().putString("custom_title",value).apply() }
    // CUSTOM TEXT
    fun getCustomText():String{ return getInstance(context).getString("custom_text",context.getString(R.string.custom_text_p1))?:context.getString(R.string.custom_text_p1) }
    fun getCustomTitle():String{ return getInstance(context).getString("custom_title",context.getString(R.string.custom_title_p1))?:context.getString(R.string.custom_title_p1) }

    // LOCATION
    fun getCoarsePermission():Boolean { return getInstance(context).getBoolean("coarse_enabled",false) }
    fun setCoarsePermission(value: Boolean) { getInstance(context).edit().putBoolean("coarse_enabled",value).apply() }

    fun setLatitude(value: String){ getInstance(context).edit().putString("latitude_value",value).apply() }
    fun setLongitude(value: String){ getInstance(context).edit().putString("longitude_value",value).apply() }

    fun getLatitude():String{ return getInstance(context).getString("latitude_value","0.0")?:"0.0" }
    fun getLongitude():String{ return getInstance(context).getString("longitude_value","0.0")?:"0.0" }

    //fun setAltitude(value: Int){ getInstance(context).edit().putInt("altitude_value",value).apply() }
    //fun getAltitude():Int { return getInstance(context).getInt("altitude_value",0)  }

    fun forceRefresh(value: Int){ getInstance(context).edit().putInt("force_refresh",value).apply() }

    // LOCALE
    fun updateLocale(s: String) { getInstance(context).edit().putString("locale",s).apply () }
    fun getLocale(): String { return getInstance(context).getString("locale","en")?:"en" }

}