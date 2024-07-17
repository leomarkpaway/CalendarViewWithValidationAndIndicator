package com.leomarkpaway.calendarviewwithvalidationandindicator

import android.os.Build
import android.os.Bundle
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.leomarkpaway.calendarviewwithvalidationandindicator.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { DataBindingUtil.setContentView(this, R.layout.activity_main) }
    private val calendar: Calendar by lazy { Calendar.getInstance() }
    private lateinit var calendarView: CalendarView
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM/d/yyyy")
    private val dateStringPattern = "MM/d/yyyy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCalendar()
        onResetAction()
    }

    private fun setupCalendar() = with(binding) {
        calendarView = cvDate
        cvDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                calendar.set(year, month, dayOfMonth)
                val dateString = calendar.timeInMillis.convertMillis(calendar, dateStringPattern)
                val date = dateString.dateStringToLocalDate(dateTimeFormatter)
                tvDateIndicator.text = getDateIndicator(date)
            }
        }
    }

    private fun onResetAction() = with(binding) {
        btnReset.setOnClickListener {
            calendarView.date = System.currentTimeMillis()
            tvDateIndicator.text = "Today"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateIndicator(date: LocalDate): String {
        val today = LocalDate.now()
        val currentWeek = getCurrentWeekDates()
        val nextWeek = currentWeek[0].plusWeeks(1)
        val nextMonth = today.plusMonths(1).withDayOfMonth(1).withDayOfMonth(1)
        val firstOfJanuary = today.withDayOfYear(1)
        val remainingMonths = ChronoUnit.MONTHS.between(firstOfJanuary, today.withDayOfMonth(1).minusMonths(1))
        val dateFormat = DateTimeFormatter.ofPattern("MMM d")

        return when {
            date.isEqual(today) -> "Today"
            date.isEqual(today.plusDays(1)) -> "Tomorrow"
            date.isEqual(today.plusDays(2)) && !date.isEqual(currentWeek[5]) && !date.isEqual(currentWeek[6]) -> "2 days later"
            date.dayOfWeek == DayOfWeek.SATURDAY && date.isEqual(currentWeek[5]) -> "Saturday"
            date.dayOfWeek == DayOfWeek.SUNDAY && date.isEqual(currentWeek[6]) -> "Sunday"
            date.isBefore(nextWeek.plusWeeks(1)) && date.isAfter(today) -> {
                "Next ${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
            }
            date.isAfter(nextWeek.minusDays(1)) && date.isBefore(nextMonth.plusMonths(remainingMonths)) -> {
                "${date.format(dateFormat)}, ${ChronoUnit.DAYS.between(today, date)} days left"
            }
            date.year != today.year -> { "$${date.format(dateFormat)}, ${date.year}" }
            else -> "Invalid"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentWeekDates(): List<LocalDate> {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        return (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    fun Long.convertMillis(calendar: Calendar, pattern: String): String {
        calendar.timeInMillis = this
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(calendar.time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.dateStringToLocalDate(format: DateTimeFormatter): LocalDate {
        return LocalDate.parse(this, format)
    }

}