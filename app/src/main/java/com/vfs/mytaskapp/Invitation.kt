package com.vfs.mytaskapp

import com.google.firebase.database.Exclude

data class Invitation(
    @get:Exclude
    var id: String = "", // Invitation ID (push key)
    var groupId: String = "",
    var groupName: String = "",
    var fromUserId: String = "",
    var fromUserName: String = "",
    var ownerUid: String = "", // for resolving group DB path
    var status: String = "pending" // pending, accepted, rejected
)
