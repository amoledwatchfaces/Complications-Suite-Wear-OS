package com.weartools.weekdayutccomp

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.map

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



//    companion object {
//        private val Context.dataStoree: DataStore<Preferences> by preferencesDataStore("userEmail")

//
//        //MOON PHASE
//        val  key_pref_phase_icon= intPreferencesKey("key_pref_phase_icon")
//        val  key_pref_moon_visibility_int= intPreferencesKey("key_pref_moon_visibility")
//        val  key_pref_moon_visibility_float= floatPreferencesKey("key_pref_moon_visibility")
//        val moon_setting_hemi_key = booleanPreferencesKey("moon_setting_hemi_key")
//        val moon_setting_simple_icon_key = booleanPreferencesKey("moon_setting_simple_icon_key")
//        //BITCOIN
//        val ticker_1 = stringPreferencesKey("ticker_1")
//        val price_1 = floatPreferencesKey("price_1")
//        val price_1_string = stringPreferencesKey("price_1_string")
//        //DATE COMPILATION
//        val date_long_text_key = stringPreferencesKey("date_long_text_key")
//        val date_short_text_key = stringPreferencesKey("date_short_text_key")
//        val date_short_title_key = stringPreferencesKey("date_short_title_key")
//        //ETHRIUM
//        val ticker_2 = stringPreferencesKey("ticker_2")
//        val price_2_string = stringPreferencesKey("price_2_string")
//        val price_2 = floatPreferencesKey("price_2")
//        //TIME
//        val time_ampm_setting_key= booleanPreferencesKey("time_ampm_setting_key")
//        val time_setting_leading_zero_key= booleanPreferencesKey("time_setting_leading_zero_key")
//        val wc2_setting_key= stringPreferencesKey("wc2_setting_key")
//        val woy_setting_key= booleanPreferencesKey("is_iso_week")
//    }


    fun setIsHemisphere(value: Boolean) {
        getInstance(context).edit().putBoolean("is_northern",value).apply()
    }


    fun setIsMilitary(value: Boolean) {
        getInstance(context).edit().putBoolean("is_military",value).apply()
    }

    fun setIsMilitaryTime(value: Boolean) {
        getInstance(context).edit().putBoolean("is_military_time",value).apply()
    }


    fun setIsSimpleIcon(value: Boolean) {
        getInstance(context).edit().putBoolean("is_simple_icon",value).apply()
    }

    fun setIsLeadingZero(value: Boolean) {
        getInstance(context).edit().putBoolean("leading_zero",value).apply()
    }


    fun setIsLeadingZeroTime(value: Boolean) {
        getInstance(context).edit().putBoolean("leading_zero_time",value).apply()
    }


    fun setIsISO(value: Boolean) {
        getInstance(context).edit().putBoolean("is_iso_week",value).apply()
    }

    fun getIsHemisphere():Boolean {
        return getInstance(context).getBoolean("is_northern",true)
    }


    fun getIsMilitary():Boolean {
        return getInstance(context).getBoolean("is_military",true)
    }

    fun getIsMilitaryTime():Boolean {
        return getInstance(context).getBoolean("is_military_time",true)
    }


    fun getIsSimpleIcon():Boolean {
        return getInstance(context).getBoolean("is_simple_icon",true)
    }

    fun getIsLeadingZero():Boolean {
        return getInstance(context).getBoolean("leading_zero",true)
    }


    fun getIsLeadingZeroTime():Boolean {
        return getInstance(context).getBoolean("leading_zero_time",true)
    }


    fun getIsISO():Boolean {
        return getInstance(context).getBoolean("is_iso_week",false)
    }

    fun getCity():String {
        return getInstance(context).getString("citiesid","")?:""
    }

    fun getCity2():String {
        return getInstance(context).getString("citiesid2","")?:""
    }

    fun getLongText():String{
        return getInstance(context).getString("date_format","EEE, d MMM")?:"EEE, d MMM"
    }

    fun getShortText():String{
        return getInstance(context).getString("short_text_format","d")?:"d"
    }

    fun getShortTitle():String{
        return getInstance(context).getString("short_title_format","MMM")?:"MMM"
    }


    fun setLongText(value: String){
        getInstance(context).edit().putString("date_format",value).apply()
    }

    fun setShortText(value: String){
        getInstance(context).edit().putString("short_text_format",value).apply()
    }

    fun setShortTitle(value: String){
        getInstance(context).edit().putString("short_title_format",value).apply()
    }

    fun setCity(value: String) {
        getInstance(context).edit().putString("citiesid",value).apply()

    }

    fun setCity2(value:String) {
        getInstance(context).edit().putString("citiesid2",value).apply()
    }

//    suspend fun setTicket1(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[ticker_1] = value
//        }
//    }
//
//
//    suspend fun setFloatPrice1(value: Float) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[price_1] = value
//        }
//    }
//
//
//    suspend fun setStringPrice1(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[price_1_string] = value
//        }
//    }
//
//
//    suspend fun setTicket2(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[ticker_2] = value
//        }
//    }
//
//
//    suspend fun setFloatPrice2(value: Float) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[price_2] = value
//        }
//    }
//
//    suspend fun setStringPrice2(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[price_2_string] = value
//        }
//    }
//
//
//    suspend fun setLongDate(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[date_long_text_key] = value
//        }
//    }

//
//    suspend fun setShortDate(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[date_short_text_key] = value
//        }
//    }
//
//
//    suspend fun setShortDateTitle(value: String) {
//        context?.dataStoree?.edit { preferences ->
//            preferences[date_short_title_key] = value
//        }
//    }


}