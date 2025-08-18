package com.textalarm.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.textalarm.app.data.Alarm
import com.textalarm.app.databinding.ItemAlarmBinding

class AlarmAdapter(
    private val onAlarmToggle: (Alarm, Boolean) -> Unit,
    private val onAlarmEdit: (Alarm) -> Unit,
    private val onAlarmDelete: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(
        private val binding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: Alarm) {
            binding.apply {
                timeText.text = alarm.getTimeString()
                labelText.text = if (alarm.label.isNotEmpty()) alarm.label else "Alarm"
                daysText.text = alarm.getDaysOfWeekString()
                
                // Set switch state without triggering listener
                enableSwitch.setOnCheckedChangeListener(null)
                enableSwitch.isChecked = alarm.isEnabled
                enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                    onAlarmToggle(alarm, isChecked)
                }
                
                editButton.setOnClickListener {
                    onAlarmEdit(alarm)
                }
                
                deleteButton.setOnClickListener {
                    onAlarmDelete(alarm)
                }
                
                // Apply visual state based on enabled status
                val alpha = if (alarm.isEnabled) 1.0f else 0.6f
                timeText.alpha = alpha
                labelText.alpha = alpha
                daysText.alpha = alpha
            }
        }
    }

    private class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem == newItem
        }
    }
}