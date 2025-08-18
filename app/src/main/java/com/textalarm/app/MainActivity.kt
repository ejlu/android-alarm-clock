package com.textalarm.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.textalarm.app.data.Alarm
import com.textalarm.app.databinding.ActivityMainBinding
import com.textalarm.app.ui.AlarmAdapter
import com.textalarm.app.ui.AlarmDetailsDialog
import com.textalarm.app.ui.AlarmViewModel
import com.textalarm.app.utils.TimeParser
import java.util.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val viewModel: AlarmViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        setupRecyclerView()
        setupClickListeners()
        observeAlarms()
    }
    
    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onAlarmToggle = { alarm, enabled ->
                viewModel.toggleAlarm(alarm, enabled)
            },
            onAlarmEdit = { alarm ->
                showAlarmDialog(alarm)
            },
            onAlarmDelete = { alarm ->
                showDeleteConfirmation(alarm)
            }
        )
        
        binding.alarmsRecyclerView.apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
    
    private fun setupClickListeners() {
        binding.setAlarmButton.setOnClickListener {
            createAlarmFromInput()
        }
        
        binding.fabAddAlarm.setOnClickListener {
            showAlarmDialog()
        }
    }
    
    private fun observeAlarms() {
        viewModel.allAlarms.observe(this) { alarms ->
            alarmAdapter.submitList(alarms)
        }
    }
    
    private fun createAlarmFromInput() {
        val timeText = binding.timeInput.text?.toString()?.trim()
        
        if (timeText.isNullOrEmpty()) {
            binding.timeInput.error = "Please enter a time"
            return
        }
        
        try {
            val parsedTime = TimeParser.parseTime(timeText)
            if (parsedTime != null) {
                val alarm = Alarm(
                    hour = parsedTime.first,
                    minute = parsedTime.second,
                    label = "Alarm",
                    isEnabled = true
                )
                
                viewModel.insertAlarm(alarm)
                binding.timeInput.text?.clear()
                binding.timeInput.error = null
                
                Toast.makeText(
                    this,
                    "Alarm set for ${alarm.getTimeString()}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.timeInput.error = "Invalid time format"
            }
        } catch (e: Exception) {
            binding.timeInput.error = "Invalid time format"
        }
    }
    
    private fun showAlarmDialog(existingAlarm: Alarm? = null) {
        val dialog = AlarmDetailsDialog.newInstance(existingAlarm) { alarm ->
            if (existingAlarm != null) {
                viewModel.updateAlarm(alarm)
                Toast.makeText(this, "Alarm updated", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.insertAlarm(alarm)
                Toast.makeText(this, "Alarm saved for ${alarm.getTimeString()}", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show(supportFragmentManager, "AlarmDetailsDialog")
    }
    
    private fun showDeleteConfirmation(alarm: Alarm) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Alarm")
            .setMessage("Delete alarm for ${alarm.getTimeString()}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAlarm(alarm)
                Toast.makeText(this, "Alarm deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}