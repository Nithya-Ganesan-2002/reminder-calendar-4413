package com.example.reminderappfrontend

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import android.view.View
import android.view.ViewGroup

// PUBLIC_INTERFACE
/**
 * Custom calendar view supporting month/day display and reminders badge.
 */
class CalendarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
): FrameLayout(context, attrs, defStyle) {

    interface OnDateSelectedListener {
        fun onDateSelected(date: Calendar)
        fun onReminderClicked(reminder: Reminder)
    }

    private val calendar = Calendar.getInstance()
    private var reminders: List<Reminder> = emptyList()
    private var listener: OnDateSelectedListener? = null

    private val recyclerView: RecyclerView

    private var isMonthView: Boolean = true

    init {
        LayoutInflater.from(context).inflate(R.layout.calendar_month_view, this, true)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 7)
        recyclerView.adapter = CalendarAdapter()
    }

    // PUBLIC_INTERFACE
    /**
     * Set the reminders to be displayed on the calendar.
     */
    fun setReminders(reminders: List<Reminder>) {
        this.reminders = reminders
        (recyclerView.adapter as CalendarAdapter).notifyDataSetChanged()
    }

    // PUBLIC_INTERFACE
    /**
     * Set a listener for date selection.
     */
    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        this.listener = listener
    }

    // PUBLIC_INTERFACE
    /**
     * Switch between month and day views (only month is implemented as full, day as placeholder).
     */
    fun setMonthView(monthView: Boolean) {
        isMonthView = monthView
        (recyclerView.adapter as CalendarAdapter).notifyDataSetChanged()
    }

    private inner class CalendarAdapter: RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.calendar_day_cell, parent, false)
            return CalendarViewHolder(view)
        }

        override fun getItemCount(): Int {
            if (isMonthView) {
                // 6 rows x 7 = 42
                return 42
            }
            // 24 = 24 hours for a day view (placeholder)
            return 24
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            if (!isMonthView) {
                holder.bindDayView(position)
            } else {
                holder.bindMonthView(position)
            }
        }

        inner class CalendarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun bindMonthView(position: Int) {
                val today = Calendar.getInstance()
                val cellText = itemView.findViewById<TextView>(R.id.dayNumber)
                val badge = itemView.findViewById<View>(R.id.reminderBadge)
                val cal = calendar.clone() as Calendar
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
                val day = position - firstDayIndex + 1
                if (day < 1 || day > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    cellText.text = ""
                    badge.visibility = View.GONE
                    itemView.setBackgroundColor(0x00000000)
                } else {
                    cellText.text = day.toString()
                    val thisDate = Calendar.getInstance()
                    thisDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day, 0, 0, 0)
                    // highlight today
                    if (today.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) == thisDate.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) == thisDate.get(Calendar.DAY_OF_MONTH)
                    ) {
                        itemView.setBackgroundResource(R.drawable.day_cell_selected)
                    } else {
                        itemView.setBackgroundColor(0x00000000)
                    }
                    // Show badge if has reminders
                    val remindersOnThisDay = reminders.filter {
                        val rCal = Calendar.getInstance().apply { timeInMillis = it.timeMillis }
                        rCal.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
                        rCal.get(Calendar.MONTH) == thisDate.get(Calendar.MONTH) &&
                        rCal.get(Calendar.DAY_OF_MONTH) == thisDate.get(Calendar.DAY_OF_MONTH)
                    }
                    badge.visibility = if (remindersOnThisDay.isNotEmpty()) View.VISIBLE else View.GONE
                    itemView.setOnClickListener {
                        listener?.onDateSelected(thisDate)
                    }
                    badge.setOnClickListener {
                        remindersOnThisDay.firstOrNull()?.let {reminder ->
                            listener?.onReminderClicked(reminder)
                        }
                    }
                }
            }

            fun bindDayView(position: Int) {
                // Placeholder: display hours
                val cellText = itemView.findViewById<TextView>(R.id.dayNumber)
                val badge = itemView.findViewById<View>(R.id.reminderBadge)
                cellText.text = "%02d:00".format(position)
                badge.visibility = View.GONE
                itemView.setBackgroundColor(0x00000000)
            }
        }
    }
}
