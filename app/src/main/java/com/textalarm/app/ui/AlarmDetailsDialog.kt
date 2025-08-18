package com.textalarm.app.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.textalarm.app.R
import com.textalarm.app.data.Alarm
import com.textalarm.app.databinding.DialogAlarmDetailsBinding
import com.textalarm.app.utils.TimeParser

class AlarmDetailsDialog(
    private val existingAlarm: Alarm? = null,
    private val onAlarmSaved: (Alarm) -> Unit
) : DialogFragment() {

    private var _binding: DialogAlarmDetailsBinding? = null
    private val binding get() = _binding!!
    
    private val dayChips by lazy {
        listOf(
            binding.chipSunday,
            binding.chipMonday,
            binding.chipTuesday,
            binding.chipWednesday,
            binding.chipThursday,
            binding.chipFriday,
            binding.chipSaturday
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAlarmDetailsBinding.inflate(layoutInflater)
        
        setupViews()
        populateExistingAlarm()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                saveAlarm()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupViews() {
        // Show/hide recurring options
        binding.recurringSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.daysOfWeekContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Quick select buttons
        binding.btnWeekdays.setOnClickListener {
            selectWeekdays()
        }
        
        binding.btnWeekends.setOnClickListener {
            selectWeekends()
        }
    }
    
    private fun populateExistingAlarm() {
        existingAlarm?.let { alarm ->
            binding.timeInput.setText(alarm.getTimeString())
            binding.labelInput.setText(alarm.label)
            binding.recurringSwitch.isChecked = alarm.isRecurring
            
            if (alarm.isRecurring) {
                binding.daysOfWeekContainer.visibility = View.VISIBLE
                updateDayChips(alarm.daysOfWeek)
            }
        }
    }
    
    private fun updateDayChips(daysOfWeek: Int) {
        dayChips.forEachIndexed { index, chip ->
            chip.isChecked = (daysOfWeek and (1 shl index)) != 0
        }
    }
    
    private fun selectWeekdays() {
        // Monday to Friday (indices 1-5)
        dayChips.forEachIndexed { index, chip ->
            chip.isChecked = index in 1..5
        }
    }
    
    private fun selectWeekends() {
        // Saturday and Sunday (indices 0 and 6)
        dayChips.forEachIndexed { index, chip ->
            chip.isChecked = index == 0 || index == 6
        }
    }
    
    private fun getSelectedDays(): Int {
        var daysOfWeek = 0
        dayChips.forEachIndexed { index, chip ->
            if (chip.isChecked) {
                daysOfWeek = daysOfWeek or (1 shl index)
            }
        }
        return daysOfWeek
    }
    
    private fun saveAlarm() {
        val timeText = binding.timeInput.text?.toString()?.trim()
        val label = binding.labelInput.text?.toString()?.trim() ?: ""
        val isRecurring = binding.recurringSwitch.isChecked
        
        if (timeText.isNullOrEmpty()) {
            return
        }
        
        val parsedTime = TimeParser.parseTime(timeText)
        if (parsedTime == null) {
            return
        }
        
        val daysOfWeek = if (isRecurring) {
            val selectedDays = getSelectedDays()
            if (selectedDays == 0) {
                // No days selected, default to daily
                127 // All days (1111111 in binary)
            } else {
                selectedDays
            }
        } else {
            0
        }
        
        val alarm = if (existingAlarm != null) {
            existingAlarm.copy(
                hour = parsedTime.first,
                minute = parsedTime.second,
                label = label.ifEmpty { "Alarm" },
                isRecurring = isRecurring,
                daysOfWeek = daysOfWeek
            )
        } else {
            Alarm(
                hour = parsedTime.first,
                minute = parsedTime.second,
                label = label.ifEmpty { "Alarm" },
                isEnabled = true,
                isRecurring = isRecurring,
                daysOfWeek = daysOfWeek
            )
        }
        
        onAlarmSaved(alarm)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(
            existingAlarm: Alarm? = null,
            onAlarmSaved: (Alarm) -> Unit
        ): AlarmDetailsDialog {
            return AlarmDetailsDialog(existingAlarm, onAlarmSaved)
        }
    }
}