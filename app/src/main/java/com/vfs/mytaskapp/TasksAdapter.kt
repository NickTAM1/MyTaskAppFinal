package com.vfs.mytaskapp

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TasksAdapter(private val listener: TaskListener, private val tasks: List<Task>) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_row, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.nameTextView.text = task.name
        holder.contentTextView.text = task.content
        holder.completionCheckBox.isChecked = task.done

        // Set priority color
        val priorityColor = when (task.priority) {
            0 -> R.color.priority_low
            2 -> R.color.priority_high
            else -> R.color.priority_medium
        }
        holder.priorityIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, priorityColor))

        if (task.done) {
            holder.nameTextView.paintFlags = holder.nameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.contentTextView.paintFlags = holder.contentTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.nameTextView.paintFlags = holder.nameTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.contentTextView.paintFlags = holder.contentTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.itemView.setOnClickListener {
            listener.taskClicked(position)
        }

        holder.completionCheckBox.setOnClickListener {
            listener.onTaskDoneChanged(position, holder.completionCheckBox.isChecked)
        }

        holder.itemView.setOnLongClickListener {
            listener.taskLongClicked(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.taskTextView_id)
        val contentTextView: TextView = itemView.findViewById(R.id.taskContentTextView_id)
        val completionCheckBox: CheckBox = itemView.findViewById(R.id.taskCompletionCheckBox_id)
        val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator_id)
    }
}

interface TaskListener {
    fun taskClicked(index: Int)
    fun taskLongClicked(index: Int)
    fun onTaskDoneChanged(index: Int, isDone: Boolean)
}
