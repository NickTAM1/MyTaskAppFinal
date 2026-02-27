package com.vfs.mytaskapp

import com.google.firebase.database.Exclude

data class Group(
    @get:Exclude
    var id: String = "",
    @get:Exclude
    var ownerUid: String = "", // Used for DB path construction
    var name: String = "",
    var owner: String = "" // UID of the group's owner
)
