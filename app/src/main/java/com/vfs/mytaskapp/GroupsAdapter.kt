package com.vfs.mytaskapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupsAdapter(private val listener: GroupListener, private val groups: List<Group>) :
    RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_row, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.nameTextView.text = group.name

        holder.itemView.setOnClickListener {
            listener.groupClicked(position)
        }

        holder.itemView.setOnLongClickListener {
            listener.groupLongClicked(position)
            true
        }

        holder.shareIconButton.setOnClickListener {
            listener.groupShareClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.groupNameTextView_id)
        val shareIconButton: ImageButton = itemView.findViewById(R.id.btn_share_group)
    }
}

interface GroupListener {
    fun groupClicked(index: Int)
    fun groupLongClicked(index: Int)
    fun groupShareClicked(index: Int)
}
