package com.vfs.mytaskapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class InvitationsActivity : AppCompatActivity(), InvitationListener {

    private lateinit var invitationsAdapter: InvitationsAdapter
    private var invitations = mutableListOf<Invitation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val invitationsRv = findViewById<RecyclerView>(R.id.invitationsRv_id)
        invitationsRv.layoutManager = LinearLayoutManager(this)
        invitationsAdapter = InvitationsAdapter(this, invitations)
        invitationsRv.adapter = invitationsAdapter

        listenForInvitations()
    }

    private fun listenForInvitations() {
        val user = Cloud.auth.currentUser
        if (user == null) return

        val invitationsRef = Cloud.db.child("invitations").child(user.uid)
        invitationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                invitations.clear()
                for (invitationSnapshot in snapshot.children) {
                    val invitation = invitationSnapshot.getValue(Invitation::class.java)
                    if (invitation != null) {
                        invitation.id = invitationSnapshot.key!!
                        invitations.add(invitation)
                    }
                }
                invitationsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onInvitationAccepted(invitation: Invitation) {
        val user = Cloud.auth.currentUser
        if (user == null) return

        // 1. Add group to user's groups: {uid}/groups/{groupId} -> ownerUid
        Cloud.db.child(user.uid).child("groups").child(invitation.groupId).setValue(invitation.ownerUid)

        // 2. Update invitation status: invitations/{uid}/{invitationId}/status -> "accepted"
        Cloud.db.child("invitations").child(user.uid).child(invitation.id).child("status").setValue("accepted")

        // 3. Update group tracking: groups/{ownerUid}/groups/{groupId}/invitations/{uid} -> "accepted"
        Cloud.db.child("groups").child(invitation.ownerUid).child("groups")
            .child(invitation.groupId).child("invitations").child(user.uid).setValue("accepted")
    }

    override fun onInvitationRejected(invitation: Invitation) {
        val user = Cloud.auth.currentUser
        if (user == null) return

        // 1. Update invitation status: invitations/{uid}/{invitationId}/status -> "rejected"
        Cloud.db.child("invitations").child(user.uid).child(invitation.id).child("status").setValue("rejected")

        // 2. Update group tracking: groups/{ownerUid}/groups/{groupId}/invitations/{uid} -> "rejected"
        Cloud.db.child("groups").child(invitation.ownerUid).child("groups")
            .child(invitation.groupId).child("invitations").child(user.uid).setValue("rejected")
    }
}