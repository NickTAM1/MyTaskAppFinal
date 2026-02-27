package com.vfs.mytaskapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InvitationsAdapter(
    private val listener: InvitationListener,
    private val invitations: List<Invitation>
) : RecyclerView.Adapter<InvitationsAdapter.InvitationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.invitation_row, parent, false)
        return InvitationViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val invitation = invitations[position]
        holder.groupNameTextView.text = invitation.groupName
        holder.fromUserNameTextView.text = "From: ${invitation.fromUserName}"

        holder.acceptButton.setOnClickListener {
            listener.onInvitationAccepted(invitation)
        }

        holder.rejectButton.setOnClickListener {
            listener.onInvitationRejected(invitation)
        }
    }

    override fun getItemCount(): Int {
        return invitations.size
    }

    class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView = itemView.findViewById(R.id.groupNameTextView_id)
        val fromUserNameTextView: TextView = itemView.findViewById(R.id.fromUserNameTextView_id)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton_id)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton_id)
    }
}

interface InvitationListener {
    fun onInvitationAccepted(invitation: Invitation)
    fun onInvitationRejected(invitation: Invitation)
}
