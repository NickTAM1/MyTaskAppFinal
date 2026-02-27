package com.vfs.mytaskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TasksActivity : AppCompatActivity(), TaskListener {
    private lateinit var tasksAdapter: TasksAdapter
    private var tasks: MutableList<Task> = mutableListOf()
    private lateinit var groupId: String
    private lateinit var ownerUid: String
    private var tasksListener: ValueEventListener? = null
    private lateinit var newTaskEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tasks_layout)

        groupId = intent.getStringExtra("groupId") ?: ""
        ownerUid = intent.getStringExtra("ownerUid") ?: ""
        val groupName = intent.getStringExtra("groupName") ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = groupName
        toolbar.setNavigationOnClickListener { finish() }

        newTaskEditText = findViewById(R.id.editTextText)
        newTaskEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newTaskName = newTaskEditText.text.toString()
                if (newTaskName.isNotEmpty()) {
                    val newTask = Task(name = newTaskName)
                    Cloud.db.child("groups").child(ownerUid).child("groups")
                        .child(groupId).child("items").push().setValue(newTask)
                    newTaskEditText.text.clear()
                }
                true
            } else {
                false
            }
        }

        val tasksRv = findViewById<RecyclerView>(R.id.tasksRv_id)
        tasksRv.layoutManager = LinearLayoutManager(this)
        tasksAdapter = TasksAdapter(this, tasks)
        tasksRv.adapter = tasksAdapter

        listenForTasks()
    }

    private fun listenForTasks() {
        val tasksRef = Cloud.db.child("groups").child(ownerUid).child("groups")
            .child(groupId).child("items")

        tasksListener?.let { tasksRef.removeEventListener(it) }

        tasksListener = tasksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tasks.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null) {
                        task.id = taskSnapshot.key!!
                        tasks.add(task)
                    }
                }
                tasksAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        tasksListener?.let {
            Cloud.db.child("groups").child(ownerUid).child("groups")
                .child(groupId).child("items").removeEventListener(it)
        }
    }

    override fun taskClicked(index: Int) {
        showEditTaskDialog(tasks[index])
    }

    override fun taskLongClicked(index: Int) {
        val taskId = tasks[index].id
        Cloud.db.child("groups").child(ownerUid).child("groups")
            .child(groupId).child("items").child(taskId).removeValue()
    }

    override fun onTaskDoneChanged(index: Int, isDone: Boolean) {
        val task = tasks[index]
        task.done = isDone
        Cloud.db.child("groups").child(ownerUid).child("groups")
            .child(groupId).child("items").child(task.id).setValue(task)
    }

    private fun showEditTaskDialog(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Task")

        val layout = layoutInflater.inflate(R.layout.edit_task_dialog, null)
        val nameEditText = layout.findViewById<EditText>(R.id.et_task_name)
        val contentEditText = layout.findViewById<EditText>(R.id.et_task_content)
        val prioritySpinner = layout.findViewById<Spinner>(R.id.spinner_priority)

        nameEditText.setText(task.name)
        contentEditText.setText(task.content)
        prioritySpinner.setSelection(task.priority)

        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            val newName = nameEditText.text.toString()
            val newContent = contentEditText.text.toString()
            val newPriority = prioritySpinner.selectedItemPosition
            if (newName.isNotEmpty()) {
                task.name = newName
                task.content = newContent
                task.priority = newPriority
                Cloud.db.child("groups").child(ownerUid).child("groups")
                    .child(groupId).child("items").child(task.id).setValue(task)
            }
        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }
}
