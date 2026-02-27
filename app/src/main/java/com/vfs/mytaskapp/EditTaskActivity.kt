package com.vfs.mytaskapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class EditTaskActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var doneCheckBox: CheckBox
    private lateinit var prioritySpinner: Spinner
    
    private var taskId: String? = null
    private var groupId: String? = null
    private var ownerUid: String? = null
    private var isTaskDone: Boolean = false
    private var taskPriority: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        nameEditText = findViewById(R.id.et_task_name_full)
        contentEditText = findViewById(R.id.et_task_content_full)
        saveButton = findViewById(R.id.btn_save_task)
        cancelButton = findViewById(R.id.btn_cancel_task)
        doneCheckBox = findViewById(R.id.cb_task_done)
        prioritySpinner = findViewById(R.id.spinner_priority_full)

        taskId = intent.getStringExtra("taskId")
        groupId = intent.getStringExtra("groupId")
        ownerUid = intent.getStringExtra("ownerUid")
        
        val taskName = intent.getStringExtra("taskName")
        val taskContent = intent.getStringExtra("taskContent")
        isTaskDone = intent.getBooleanExtra("taskDone", false)
        taskPriority = intent.getIntExtra("taskPriority", 1)

        nameEditText.setText(taskName)
        contentEditText.setText(taskContent)
        doneCheckBox.isChecked = isTaskDone
        prioritySpinner.setSelection(taskPriority)

        if (taskId == null) {
            // New task mode
            doneCheckBox.visibility = View.GONE
        } else {
            // Edit task mode
            doneCheckBox.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {
            saveTask()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveTask() {
        val name = nameEditText.text.toString()
        val content = contentEditText.text.toString()
        val isDone = doneCheckBox.isChecked
        val priority = prioritySpinner.selectedItemPosition
        
        val currentGroupId = groupId
        val currentOwnerUid = ownerUid

        if (name.isNotEmpty() && currentGroupId != null && currentOwnerUid != null) {
            val task = Task(
                id = taskId ?: "", 
                name = name, 
                content = content, 
                done = isDone,
                priority = priority
            )
            val itemsRef = Cloud.db.child("groups").child(currentOwnerUid).child("groups")
                .child(currentGroupId).child("items")

            if (taskId == null) {
                // New task
                itemsRef.push().setValue(task)
            } else {
                // Existing task
                itemsRef.child(taskId!!).setValue(task)
            }
            finish()
        }
    }
}
