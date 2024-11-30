package com.weartools.weekdayutccomp.utils

import kotlinx.serialization.Serializable

@Serializable
data class WorldClock(
    val zoneId: String,
    val cityId: String,
    val cityName: String
)
object WorldClockLists{
    val africa = listOf(
        WorldClock("Africa/Accra", "ACC", "Accra"),
        WorldClock("Africa/Addis_Ababa", "ADD", "Addis Ababa"),
        WorldClock("Africa/Algiers", "ALG", "Algiers"),
        WorldClock("Africa/Asmara", "ASM", "Asmara"),
        WorldClock("Africa/Bamako", "BKO", "Bamako"),
        WorldClock("Africa/Bangui", "BGF", "Bangui"),
        WorldClock("Africa/Cairo", "CAI", "Cairo"),
        WorldClock("Africa/Johannesburg", "CPT", "Cape Town"),
        WorldClock("Africa/Conakry", "CKY", "Conakry"),
        WorldClock("Africa/Dakar", "DKR", "Dakar"),
        WorldClock("Africa/Dar_es_Salaam", "DAR", "Dar es Salaam"),
        WorldClock("Africa/Djibouti", "JIB", "Djibouti"),
        WorldClock("Africa/Douala", "DLA", "Douala"),
        WorldClock("Africa/Freetown", "FNA", "Freetown"),
        WorldClock("Africa/Harare", "HRE", "Harare"),
        WorldClock("Africa/Khartoum", "KRT", "Khartoum"),
        WorldClock("Africa/Kampala", "KLA", "Kampala"),
        WorldClock("Africa/Lagos", "LOS", "Lagos"),
        WorldClock("Africa/Luanda", "LAD", "Luanda"),
        WorldClock("Africa/Lusaka", "LUN", "Lusaka"),
        WorldClock("Africa/Nairobi", "NBO", "Nairobi"),
        WorldClock("Africa/Ndjamena", "NDJ", "Ndjamena"),
        WorldClock("Africa/Nouakchott", "NKC", "Nouakchott"),
        WorldClock("Africa/Ouagadougou", "OUA", "Ouagadougou"),
        WorldClock("Africa/Rabat", "RBA", "Rabat"),
        WorldClock("Africa/Tripoli", "TIP", "Tripoli"),
        WorldClock("Africa/Tunis", "TUN", "Tunis")
    )
    val asia = listOf(
        WorldClock("Asia/Almaty", "ALMT", "Almaty"),
        WorldClock("Asia/Baghdad", "BGW", "Baghdad"),
        WorldClock("Asia/Bahrain", "BAH", "Bahrain"),
        WorldClock("Asia/Baku", "BAK", "Baku"),
        WorldClock("Asia/Bangkok", "BKK", "Bangkok"),
        WorldClock("Asia/Brunei", "BWN", "Brunei"),
        WorldClock("Asia/Colombo", "CMB", "Colombo"),
        WorldClock("Asia/Dhaka", "DAC", "Dhaka"),
        WorldClock("Asia/Qatar", "DOH", "Doha"),
        WorldClock("Asia/Dubai", "DXB", "Dubai"),
        WorldClock("Asia/Ho_Chi_Minh", "SGN", "Ho Chi Minh"),
        WorldClock("Asia/Hong_Kong", "HKG", "Hong Kong"),
        WorldClock("Asia/Jakarta", "JKT", "Jakarta"),
        WorldClock("Asia/Jerusalem", "JER", "Jerusalem"),
        WorldClock("Asia/Kabul", "KBL", "Kabul"),
        WorldClock("Asia/Kamchatka", "PKC", "Kamchatka"),
        WorldClock("Asia/Karachi", "KHI", "Karachi"),
        WorldClock("Asia/Kathmandu", "KTM", "Kathmandu"),
        WorldClock("Asia/Kolkata", "CCU", "Kolkata"),
        WorldClock("Asia/Kuala_Lumpur", "KUL", "Kuala Lumpur"),
        WorldClock("Asia/Kuwait", "KWI", "Kuwait"),
        WorldClock("Asia/Rangoon", "RGN", "Rangoon"),
        WorldClock("Asia/Riyadh", "RUH", "Riyadh"),
        WorldClock("Asia/Seoul", "SEL", "Seoul"),
        WorldClock("Asia/Shanghai", "SGH", "Shanghai"),
        WorldClock("Asia/Singapore", "SIN", "Singapore"),
        WorldClock("Asia/Taipei", "TPE", "Taipei"),
        WorldClock("Asia/Tehran", "THR", "Tehran"),
        WorldClock("Asia/Tel_Aviv", "TAV", "Tel Aviv"),
        WorldClock("Asia/Tokyo", "TYO", "Tokyo"),
        WorldClock("Asia/Vladivostok", "VVO", "Vladivostok"),
        WorldClock("Asia/Volgograd", "VOG", "Volgograd")
    )
    val atlantic = listOf(
        WorldClock("Atlantic/South_Georgia", "SGS", "Grytviken"),
        WorldClock("Atlantic/Azores", "PDL", "Ponta Delgada"),
        WorldClock("Atlantic/Reykjavik", "REY", "Reykjavik")
    )
    val australia = listOf(
        WorldClock("Australia/Adelaide", "ADL", "Adelaide"),
        WorldClock("Australia/Brisbane", "BNE", "Brisbane"),
        WorldClock("Australia/Canberra", "CBR", "Canberra"),
        WorldClock("Australia/Darwin", "DRW", "Darwin"),
        WorldClock("Australia/Hobart", "HBA", "Hobart"),
        WorldClock("Australia/Melbourne", "MEL", "Melbourne"),
        WorldClock("Australia/Perth", "PER", "Perth"),
        WorldClock("Australia/Sydney", "SYD", "Sydney")
    )
    val europe = listOf(
        WorldClock("Europe/Amsterdam", "AMS", "Amsterdam"),
        WorldClock("Europe/Athens", "ATH", "Athens"),
        WorldClock("Europe/Belgrade", "BEG", "Belgrade"),
        WorldClock("Europe/Berlin", "BER", "Berlin"),
        WorldClock("Europe/Brussels", "BRU", "Brussels"),
        WorldClock("Europe/Budapest", "BUD", "Budapest"),
        WorldClock("Europe/Bucharest", "BUH", "Bucharest"),
        WorldClock("Europe/Copenhagen", "CPH", "Copenhagen"),
        WorldClock("Europe/Dublin", "DUB", "Dublin"),
        WorldClock("Europe/London", "EDI", "Edinburgh"),
        WorldClock("Europe/Zurich", "GVA", "Geneva"),
        WorldClock("Europe/Helsinki", "HEL", "Helsinki"),
        WorldClock("Europe/Istanbul", "IST", "Istanbul"),
        WorldClock("Europe/Kiev", "IEV", "Kiev"),
        WorldClock("Europe/Lisbon", "LIS", "Lisbon"),
        WorldClock("Europe/Ljubljana", "LJU", "Ljubljana"),
        WorldClock("Europe/London", "LON", "London"),
        WorldClock("Europe/Madrid", "MAD", "Madrid"),
        WorldClock("Europe/Berlin", "MUC", "Munich"),
        WorldClock("Europe/Oslo", "OSL", "Oslo"),
        WorldClock("Europe/Paris", "PAR", "Paris"),
        WorldClock("Europe/Podgorica", "TGD", "Podgorica"),
        WorldClock("Europe/Prague", "PRG", "Prague"),
        WorldClock("Europe/Rome", "ROM", "Rome"),
        WorldClock("Europe/Sofia", "SOF", "Sofia"),
        WorldClock("Europe/Stockholm", "STO", "Stockholm"),
        WorldClock("Europe/Vienna", "VIE", "Vienna"),
        WorldClock("Europe/Vilnius", "VNO", "Vilnius"),
        WorldClock("Europe/Warsaw", "WAW", "Warsaw"),
        WorldClock("Europe/Zagreb", "ZAG", "Zagreb"),
        WorldClock("Europe/Zurich", "ZRH", "Zurich")
    )
    val northAmerica = listOf(
        WorldClock("America/Adak", "ADK", "Adak"),
        WorldClock("America/Anchorage", "ANC", "Anchorage"),
        WorldClock("America/New_York", "ATL", "Atlanta"),
        WorldClock("America/Chicago", "AUS", "Austin"),
        WorldClock("America/New_York", "BOS", "Boston"),
        WorldClock("America/Edmonton", "CAL", "Calgary"),
        WorldClock("America/Chicago", "CHI", "Chicago"),
        WorldClock("America/New_York", "CMH", "Columbus"),
        WorldClock("America/Los_Angeles", "CUO", "Cupertino"),
        WorldClock("America/Chicago", "DAL", "Dallas"),
        WorldClock("America/Denver", "DEN", "Denver"),
        WorldClock("America/Detroit", "DET", "Detroit"),
        WorldClock("Canada/Atlantic", "HAL", "Halifax"),
        WorldClock("America/Indiana/Indianapolis", "IND", "Indianapolis"),
        WorldClock("America/New_York", "TYS", "Knoxville"),
        WorldClock("America/Los_Angeles", "LAX", "Los Angeles"),
        WorldClock("America/Chicago", "MEM", "Memphis"),
        WorldClock("Mexico/General", "DFL", "Mexico City"),
        WorldClock("America/New_York", "MIA", "Miami"),
        WorldClock("America/Chicago", "MES", "Minneapolis"),
        WorldClock("America/Montreal", "MTR", "Montreal"),
        WorldClock("America/New_York", "NYC", "New York"),
        WorldClock("America/Toronto", "OTT", "Ottawa"),
        WorldClock("America/New_York", "PHL", "Philadelphia"),
        WorldClock("America/Phoenix", "PHX", "Phoenix"),
        WorldClock("America/Los_Angeles", "PDX", "Portland"),
        WorldClock("America/Denver", "SLC", "Salt Lake City"),
        WorldClock("America/Los_Angeles", "SAN", "San Diego"),
        WorldClock("America/Los_Angeles", "SFO", "San Francisco"),
        WorldClock("America/Los_Angeles", "SJC", "San Jose"),
        WorldClock("America/Los_Angeles", "SEA", "Seattle"),
        WorldClock("America/Chicago", "SYT", "St. Louis"),
        WorldClock("America/Toronto", "TOR", "Toronto"),
        WorldClock("America/Vancouver", "VAN", "Vancouver"),
        WorldClock("America/New_York", "WAS", "Washington, D.C."),
        WorldClock("America/Winnipeg", "WNP", "Winnipeg")
    )
    val pacific = listOf(
        WorldClock("Pacific/Auckland", "AKL", "Auckland"),
        WorldClock("Pacific/Guam", "GUM", "Guam"),
        WorldClock("Pacific/Honolulu", "HNL", "Honolulu"),
        WorldClock("Pacific/Noumea", "NOU", "Noumea"),
        WorldClock("Pacific/Pago_Pago", "PPG", "Pago Pago")
    )
    val southAmerica = listOf(
        WorldClock("America/Asuncion", "ASU", "Asuncion"),
        WorldClock("America/Bogota", "BOG", "Bogota"),
        WorldClock("America/Sao_Paulo", "BSB", "Brasilia"),
        WorldClock("America/Buenos_Aires", "BUE", "Buenos Aires"),
        WorldClock("America/Caracas", "CCS", "Caracas"),
        WorldClock("America/Cayenne", "CAY", "Cayenne"),
        WorldClock("America/Guyana", "GEO", "Georgetown"),
        WorldClock("America/Guatemala", "GUA", "Guatemala"),
        WorldClock("America/Havana", "HAV", "Havana"),
        WorldClock("America/La_Paz", "LPB", "La Paz"),
        WorldClock("America/Lima", "LIM", "Lima"),
        WorldClock("America/Managua", "MGA", "Managua"),
        WorldClock("America/Montevideo", "MVD", "Montevideo"),
        WorldClock("America/Panama", "PTY", "Panama"),
        WorldClock("America/Paramaribo", "PBM", "Paramaribo"),
        WorldClock("America/Port-au-Prince", "PAP", "Port-au-Prince"),
        WorldClock("America/Quito", "UIO", "Quito"),
        WorldClock("America/Recife", "REC", "Recife"),
        WorldClock("America/Sao_Paulo", "RIO", "Rio de Janeiro"),
        WorldClock("America/Argentina/San_Juan", "SJU", "San Juan"),
        WorldClock("America/El_Salvador", "SAL", "San Salvador"),
        WorldClock("America/Santiago", "SCL", "Santiago"),
        WorldClock("America/Santo_Domingo", "SDQ", "Santo Domingo"),
        WorldClock("America/Sao_Paulo", "SAO", "Sao Paulo")
    )
    val universal = listOf(
        WorldClock("Etc/UTC", "UTC", "UTC")
    )
}




