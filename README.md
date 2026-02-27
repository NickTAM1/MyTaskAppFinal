# MyTaskApp

A shared task management app built with Kotlin and Firebase.

---

## Features

- Register / Login / Logout with email and password
- Auto-login on app relaunch
- Create and delete task groups
- Add, edit, delete, and complete tasks
- Invite other users to a group by email
- Accept or reject group invitations
- Shared groups sync tasks in real-time between all members

---

## Tech Stack

- Kotlin
- Firebase Authentication
- Firebase Realtime Database
- RecyclerView + CardView
- Material Design Components

---

## Screens

1. **Login / Register** — email + password auth with toggle
2. **Groups** — list of groups, add new, share, delete
3. **Tasks** — list of tasks per group, add, edit, check off, delete
4. **Invitations** — view and accept/reject pending invites

---

## How Sharing Works

1. Owner taps share icon on a group → enters another user's email
2. Receiver opens Invitations → taps Accept
3. Both users now see the same tasks in real-time

---

## Firebase DB Structure

```
users/{email}            → uid
{uid}/email              → email
{uid}/groups/{groupId}   → ownerUid
groups/{ownerUid}/groups/{groupId}/
  name, owner, items/, invitations/
invitations/{receiverUid}/{id}/
  groupId, groupName, fromUserId, fromUserName, ownerUid, status
```

---

## Setup

1. Create a Firebase project
2. Enable Email/Password authentication
3. Add `google-services.json` to the app module
4. Publish `firebase_rules.json` to Realtime Database → Rules
5. Build and run

Github:
https://github.com/NickTAM1/MyTaskAppFinal.git
