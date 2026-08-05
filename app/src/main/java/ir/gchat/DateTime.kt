package ir.gchat

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun getCurrentUtcTimestamp(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")//.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.now(ZoneOffset.UTC).format(formatter)
}

fun formatMessageTime(timestamp: String): String {
    try {
        //val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val dateTime = LocalDateTime.parse(timestamp, inputFormatter)
            .atZone(ZoneOffset.UTC)

        val today = LocalDate.now(ZoneOffset.UTC)
        val date = dateTime.toLocalDate()

        return when {
            date == today -> {
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }

            date == today.minusDays(1) -> {
                "Yesterday, ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }

            date.isAfter(today.minusDays(7)) -> {
                val dayName = when (date.dayOfWeek.value) {
                    1 -> "Monday"
                    2 -> "Tuesday"
                    3 -> "Wednesday"
                    4 -> "Thursday"
                    5 -> "Friday"
                    6 -> "Saturday"
                    7 -> "Sunday"
                    else -> ""
                }
                "$dayName، ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }

            date.year == today.year -> {
                val monthName = when (date.monthValue) {
                    1 -> "January"
                    2 -> "February"
                    3 -> "March"
                    4 -> "April"
                    5 -> "May"
                    6 -> "June"
                    7 -> "July"
                    8 -> "August"
                    9 -> "September"
                    10 -> "October"
                    11 -> "November"
                    12 -> "December"
                    else -> ""
                }
                "${date.dayOfMonth} $monthName، ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }

            else -> {
                val monthName = when (date.monthValue) {
                    1 -> "January"
                    2 -> "February"
                    3 -> "March"
                    4 -> "April"
                    5 -> "May"
                    6 -> "June"
                    7 -> "July"
                    8 -> "August"
                    9 -> "September"
                    10 -> "October"
                    11 -> "November"
                    12 -> "December"
                    else -> ""
                }
                "${date.dayOfMonth} $monthName ${date.year}, ${
                    dateTime.format(
                        DateTimeFormatter.ofPattern(
                            "HH:mm"
                        )
                    )
                }"
            }
        }
    }  catch (_: Exception) {
         return timestamp
    }
}

fun formatGregorianDate(date: String): String {
    val parts = date.substring(0, 10).split("-")

    val year = parts[0]
    val month = parts[1].toInt()
    val day = parts[2].toInt()

    val monthName = when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
    }

    return "%s %02d %s".format(year, monthName, day)
}

fun formatMessageTimeJalali(timestamp: String): String {
    if (timestamp.isBlank()) return ""

    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(timestamp, inputFormatter)
        .atZone(ZoneOffset.UTC)
    val today = LocalDate.now(ZoneOffset.UTC)
    val yesterday = today.minusDays(1)
    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1
        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) gDayNo += gDaysInMonth[i]
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) gDayNo++
        gDayNo += gd2
        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        return Triple(jy, jm + 1, jDayNo + 1)
    }

    val messageJalali = toJalali(dateTime.year, dateTime.monthValue, dateTime.dayOfMonth)
    val todayJalali = toJalali(today.year, today.monthValue, today.dayOfMonth)
    val yesterdayJalali = toJalali(yesterday.year, yesterday.monthValue, yesterday.dayOfMonth)

    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    return when {
        messageJalali == todayJalali -> time
        messageJalali == yesterdayJalali -> "دیروز، $time"
        dateTime.toLocalDate().isAfter(today.minusDays(7)) -> {
            val dayName = when (dateTime.dayOfWeek.value) {
                1 -> "دوشنبه"
                2 -> "سه‌شنبه"
                3 -> "چهارشنبه"
                4 -> "پنجشنبه"
                5 -> "جمعه"
                6 -> "شنبه"
                7 -> "یکشنبه"
                else -> ""
            }
            "$dayName، $time"
        }
        messageJalali.first == todayJalali.first -> {
            "${convertDigits(messageJalali.third.toString(), persianDigits)} ${monthNames[messageJalali.second - 1]}، $time"
        }
        else -> {
            "${convertDigits(messageJalali.third.toString(), persianDigits)} ${monthNames[messageJalali.second - 1]} ${convertDigits(messageJalali.first.toString(), persianDigits)}، $time"
        }
    }
}

fun gregorianToJalali(date: String): String {
    val parts = date.substring(0, 10).split("-")
    val gy = parts[0].toInt()
    val gm = parts[1].toInt()
    val gd = parts[2].toInt()

    val gDaysInMonth = intArrayOf(
        31, 28, 31, 30, 31, 30,
        31, 31, 30, 31, 30, 31
    )

    val gy2 = gy - 1600
    val gm2 = gm - 1
    val gd2 = gd - 1

    var gDayNo = 365 * gy2 +
            (gy2 + 3) / 4 -
            (gy2 + 99) / 100 +
            (gy2 + 399) / 400

    for (i in 0 until gm2)
        gDayNo += gDaysInMonth[i]

    if (gm2 > 1 &&
        ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))
    ) {
        gDayNo++
    }

    gDayNo += gd2

    var jDayNo = gDayNo - 79

    val jNp = jDayNo / 12053
    jDayNo %= 12053

    var jy = 979 + 33 * jNp

    jy += 4 * (jDayNo / 1461)
    jDayNo %= 1461

    if (jDayNo >= 366) {
        jy += (jDayNo - 1) / 365
        jDayNo = (jDayNo - 1) % 365
    }

    val jDaysInMonth = intArrayOf(
        31, 31, 31, 31, 31, 31,
        30, 30, 30, 30, 30, 29
    )

    var jm = 0
    while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
        jDayNo -= jDaysInMonth[jm]
        jm++
    }

    val jd = jDayNo + 1

    val monthName = when (jm + 1) {
        1 -> "فروردین"
        2 -> "اردیبهشت"
        3 -> "خرداد"
        4 -> "تیر"
        5 -> "مرداد"
        6 -> "شهریور"
        7 -> "مهر"
        8 -> "آبان"
        9 -> "آذر"
        10 -> "دی"
        11 -> "بهمن"
        12 -> "اسفند"
        else -> ""
    }

    val persianDigits = charArrayOf(
        '۰', '۱', '۲', '۳', '۴',
        '۵', '۶', '۷', '۸', '۹'
    )
    val persianText = convertDigits("%04d %s %02d".format(jy, monthName, jd), persianDigits)

    return persianText
}

fun formatMessageTimeHijri(timestamp: String): String {
    if (timestamp.isBlank()) return ""

    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(timestamp, inputFormatter)
        .atZone(ZoneOffset.UTC)
    val today = LocalDate.now(ZoneOffset.UTC)
    val yesterday = today.minusDays(1)
    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun toHijri(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
            val a = (14 - month) / 12
            val y = year + 4800 - a
            val m = month + 12 * a - 3
            return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        }

        val jd = gregorianToJulianDay(gy, gm, gd)
        val l = jd - 1948440 + 10632
        val n = (l - 1) / 10631
        var ll = l - 10631 * n + 354
        val j = ((10985 - ll) / 5316) * ((50 * ll) / 17719) +
                (ll / 5670) * ((43 * ll) / 15238)
        ll = ll -
                ((30 - j) / 15) * ((17719 * j) / 50) -
                (j / 16) * ((15238 * j) / 43) + 29
        val monthH = (24 * ll) / 709
        val dayH = ll - (709 * monthH) / 24
        val yearH = 30 * n + j - 30
        return Triple(yearH, monthH, dayH)
    }

    val messageHijri = toHijri(dateTime.year, dateTime.monthValue, dateTime.dayOfMonth)
    val todayHijri = toHijri(today.year, today.monthValue, today.dayOfMonth)
    val yesterdayHijri = toHijri(yesterday.year, yesterday.monthValue, yesterday.dayOfMonth)

    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val monthNames = arrayOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی",
        "جمادی‌الاول", "جمادی‌الثانی", "رجب", "شعبان",
        "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )

    return when {
        messageHijri == todayHijri -> time
        messageHijri == yesterdayHijri -> "دیروز، $time"
        dateTime.toLocalDate().isAfter(today.minusDays(7)) -> {
            val dayName = when (dateTime.dayOfWeek.value) {
                1 -> "دوشنبه"
                2 -> "سه‌شنبه"
                3 -> "چهارشنبه"
                4 -> "پنجشنبه"
                5 -> "جمعه"
                6 -> "شنبه"
                7 -> "یکشنبه"
                else -> ""
            }
            "$dayName، $time"
        }
        messageHijri.first == todayHijri.first -> {
            "${convertDigits(messageHijri.third.toString(), arabicDigits)} ${monthNames[messageHijri.second - 1]}، $time"
        }
        else -> {
            "${convertDigits(messageHijri.third.toString(), arabicDigits)} ${monthNames[messageHijri.second - 1]} ${convertDigits(messageHijri.first.toString(), arabicDigits)}، $time"
        }
    }
}

fun gregorianToHijri(date: String): String {
    val parts = date.substring(0, 10).split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()

    val a = (14 - month) / 12
    val y2 = year + 4800 - a
    val m2 = month + 12 * a - 3

    val jd = day +
            (153 * m2 + 2) / 5 +
            365 * y2 +
            y2 / 4 -
            y2 / 100 +
            y2 / 400 -
            32045

    val l = jd - 1948440 + 10632
    val n = (l - 1) / 10631
    var ll = l - 10631 * n + 354

    val j = ((10985 - ll) / 5316) *
            ((50 * ll) / 17719) +
            (ll / 5670) *
            ((43 * ll) / 15238)

    ll = ll -
            ((30 - j) / 15) *
            ((17719 * j) / 50) -
            (j / 16) *
            ((15238 * j) / 43) +
            29

    val monthH = (24 * ll) / 709
    val dayH = ll - (709 * monthH) / 24
    val yearH = 30 * n + j - 30

    val monthName = when (monthH) {
        1 -> "محرم"
        2 -> "صفر"
        3 -> "ربیع‌الاول"
        4 -> "ربیع‌الثانی"
        5 -> "جمادی‌الاول"
        6 -> "جمادی‌الثانی"
        7 -> "رجب"
        8 -> "شعبان"
        9 -> "رمضان"
        10 -> "شوال"
        11 -> "ذی‌القعده"
        12 -> "ذی‌الحجه"
        else -> ""
    }

    val arabicDigits = charArrayOf(
        '٠', '١', '٢', '٣', '٤',
        '٥', '٦', '٧', '٨', '٩'
    )

    val arabicText = convertDigits("%04d %s %02d".format(yearH, monthName, dayH), arabicDigits)

    return arabicText
}