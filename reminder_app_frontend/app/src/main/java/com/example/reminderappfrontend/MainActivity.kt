package com.example.reminderappfrontend

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

/**
 * MainActivity: Displays calendar, supports add/edit/delete, uses clean modern UI and placeholder for notifications.
 */
class MainActivity : AppCompatActivity(), CalendarView.OnDateSelectedListener {

    private lateinit var calendarView: CalendarView
    private lateinit var reminderList: MutableList<Reminder>
    private var selectedDate: Calendar = Calendar.getInstance()
    private var isMonthView: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        reminderList = mutableListOf()

        // Setup Calendar Container with custom CalendarView
        val calendarContainer: FrameLayout = findViewById(R.id.calendarContainer)
        calendarView = CalendarView(this)
        calendarView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        calendarView.setReminders(reminderList)
        calendarView.setOnDateSelectedListener(this)
        calendarContainer.removeAllViews()
        calendarContainer.addView(calendarView)

        // Toggle buttons for Month/Day view
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.calendarViewToggle)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isMonthView = checkedId == R.id.monthViewButton
                calendarView.setMonthView(isMonthView)
            }
        }

        // FAB for new reminder
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showReminderDialog(null)
        }
    }

    // PUBLIC_INTERFACE
    /**
     * Triggered when a date is selected in the calendar.
     */
    override fun onDateSelected(date: Calendar) {
        // Optionally, filter reminders for this day and show list or prompt
        selectedDate = date
        val remindersToday = reminderList.filter {
            val rCal = Calendar.getInstance().apply { timeInMillis = it.timeMillis }
            rCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            rCal.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
            rCal.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
        if (remindersToday.isEmpty()) {
            showReminderDialog(null)
        } else {
            // Pick first for edit as placeholder, show list if >1 in future
            showReminderDialog(remindersToday.first())
        }
    }

    // PUBLIC_INTERFACE
    /**
     * Triggered when clicking a reminder badge.
     */
    override fun onReminderClicked(reminder: Reminder) {
        showReminderDialog(reminder)
    }

    /**
     * Shows dialog to create/edit a reminder. If reminder is null, creates new. Otherwise, edit existing.
     */
    private fun showReminderDialog(reminder: Reminder?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reminder, null, false)
        val titleField = dialogView.findViewById<EditText>(R.id.reminderTitle)
        val detailsField = dialogView.findViewById<EditText>(R.id.reminderDetails)
        val dateButton = dialogView.findViewById<Button>(R.id.reminderDateButton)
        val timeButton = dialogView.findViewById<Button>(R.id.reminderTimeButton)
        var dateTime = Calendar.getInstance()

        if (reminder != null) {
            titleField.setText(reminder.title)
            detailsField.setText(reminder.details)
            dateTime.timeInMillis = reminder.timeMillis
        } else {
            dateTime.timeInMillis = selectedDate.timeInMillis
        }
        updateDateTimeButtons(dateButton, timeButton, dateTime)

        dateButton.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                dateTime.set(year, month, dayOfMonth)
                updateDateTimeButtons(dateButton, timeButton, dateTime)
            }, dateTime.get(Calendar.YEAR), dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH)).show()
        }
        timeButton.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                dateTime.set(Calendar.HOUR_OF_DAY, hour)
                dateTime.set(Calendar.MINUTE, minute)
                updateDateTimeButtons(dateButton, timeButton, dateTime)
            }, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show()
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (reminder == null) getString(R.string.add_reminder) else getString(R.string.edit_reminder))
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel)) { dlg, _ -> dlg.dismiss() }

        if (reminder != null) {
            dialogBuilder.setNeutralButton(getString(R.string.delete_reminder)) { dlg, _ ->
                reminderList.removeIf { it.id == reminder.id }
                calendarView.setReminders(reminderList)
                dlg.dismiss()
            }
        }

        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val title = titleField.text.toString().trim()
                if (title.isEmpty()) {
                    titleField.error = getString(R.string.reminder_title)
                    return@setOnClickListener
                }
                val detail = detailsField.text.toString()
                if (reminder == null) {
                    reminderList.add(
                        Reminder(
                            title = title,
                            details = detail,
                            timeMillis = dateTime.timeInMillis
                        )
                    )
                } else {
                    reminder.title = title
                    reminder.details = detail
                    reminder.timeMillis = dateTime.timeInMillis
                }
                calendarView.setReminders(reminderList)
                dialog.dismiss()
                // TODO: Local notification scheduling goes here
                // scheduleNotification(reminder)
            }
        }
        dialog.show()
    }

    private fun updateDateTimeButtons(dateButton: Button, timeButton: Button, calendar: Calendar) {
        dateButton.text = String.format(
            "%d-%02d-%02d", calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)
        )
        timeButton.text = String.format(
            "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)
        )
    }

    // Placeholder for local notification (to be connected)
    @Suppress("UNUSED")
    private fun scheduleNotification() {
        // Implementation will use WorkManager or AlarmManager in production
        // Example:
        // NotificationUtil.scheduleNotification(this, reminder)
    }
}
