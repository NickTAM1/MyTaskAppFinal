package com.vfs.mytaskapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupsActivity : AppCompatActivity(), GroupListener {

    private lateinit var groupsAdapter: GroupsAdapter
    private var groups = mutableListOf<Group>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val groupsRv = findViewById<RecyclerView>(R.id.groupsRv_id)
        groupsRv.layoutManager = LinearLayoutManager(this)
        groupsAdapter = GroupsAdapter(this, groups)
        groupsRv.adapter = groupsAdapter

        listenForGroups()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.groups_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.invitations_menu_item -> {
                val intent = Intent(this, InvitationsActivity::class.java)
                startActivity(intent)
            }
            R.id.logout_menu_item -> {
                logoutUser()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        Cloud.auth.signOut()
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun listenForGroups() {
        val user = Cloud.auth.currentUser
        if (user == null) return

        // Groups reference: {uid}/groups/{groupId} -> ownerUid
        val userGroupsRef = Cloud.db.child(user.uid).child("groups")
        userGroupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groups.clear()
                val groupIds = snapshot.children.mapNotNull { child ->
                    val key = child.key
                    val value = child.getValue(String::class.java)
                    if (key != null && value != null) key to value else null
                }
                
                if (groupIds.isEmpty()) {
                    groupsAdapter.notifyDataSetChanged()
                    return
                }

                var count = groupIds.size
                groupIds.forEach { (groupId, ownerUid) ->
                    Cloud.db.child("groups").child(ownerUid).child("groups").child(groupId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snap: DataSnapshot) {
                                snap.getValue(Group::class.java)?.let { group ->
                                    group.id = groupId
                                    group.ownerUid = ownerUid
                                    groups.add(group)
                                }
                                count--
                                if (count == 0) groupsAdapter.notifyDataSetChanged()
                            }
                            override fun onCancelled(error: DatabaseError) { count-- }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun onAddNewGroupClicked(view: View) {
        val user = Cloud.auth.currentUser ?: return
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Group")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Create") { _, _ ->
            val groupName = input.text.toString()
            if (groupName.isNotEmpty()) {
                val newGroup = Group(name = groupName, owner = user.uid)
                val groupRef = Cloud.db.child("groups").child(user.uid).child("groups").push()
                val groupId = groupRef.key ?: return@setPositiveButton
                
                groupRef.setValue(newGroup)
                Cloud.db.child(user.uid).child("groups").child(groupId).setValue(user.uid)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun groupClicked(index: Int) {
        val group = groups[index]
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("groupId", group.id)
        intent.putExtra("ownerUid", group.ownerUid)
        intent.putExtra("groupName", group.name)
        startActivity(intent)
    }

    override fun groupLongClicked(index: Int) {
        val group = groups[index]
        val user = Cloud.auth.currentUser ?: return
        
        if (group.owner != user.uid) {
            Toast.makeText(this, "Only the group owner can delete", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete ${group.name}?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete group data
                Cloud.db.child("groups").child(user.uid).child("groups").child(group.id).removeValue()
                // Delete user's link to the group
                Cloud.db.child(user.uid).child("groups").child(group.id).removeValue()
                Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun groupShareClicked(index: Int) {
        val group = groups[index]
        val user = Cloud.auth.currentUser ?: return
        
        if (group.owner != user.uid) {
            Toast.makeText(this, "Only the group owner can invite users", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invite User to ${group.name}")
        val input = EditText(this)
        input.hint = "Enter user email"
        builder.setView(input)

        builder.setPositiveButton("Send Invite") { _, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                if (email == user.email) {
                    Toast.makeText(this, "You can't invite yourself", Toast.LENGTH_SHORT).show()
                } else {
                    inviteUser(email, group)
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun inviteUser(email: String, group: Group) {
        val sanitizedEmail = Cloud.sanitizeEmail(email)
        Cloud.db.child("users").child(sanitizedEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val targetUid = snapshot.getValue(String::class.java)
                    if (targetUid != null) {
                        val invitation = Invitation(
                            groupId = group.id,
                            groupName = group.name,
                            fromUserId = Cloud.auth.currentUser!!.uid,
                            fromUserName = Cloud.auth.currentUser!!.displayName ?: "Someone",
                            ownerUid = group.ownerUid
                        )
                        Cloud.db.child("invitations").child(targetUid).push().setValue(invitation)
                        Cloud.db.child("groups").child(group.ownerUid).child("groups")
                            .child(group.id).child("invitations").child(targetUid).setValue("pending")
                        Toast.makeText(this@GroupsActivity, "Invitation sent to $email!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@GroupsActivity, "User not found with that email", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
