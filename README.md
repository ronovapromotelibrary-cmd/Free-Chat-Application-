Free Chat 💬

A secure, lightweight, real-time private 1-to-1 messaging application built with Firebase.

Free Chat is designed around one simple concept:

«Find a real person using their permanent Free Chat ID and communicate through secure real-time messaging.»

---

🚀 Overview

Free Chat is a messaging-only application for Android and iOS.

The application uses 100% real Firebase backend data and does not use fake, demo, placeholder, or hard-coded chat data.

Core Experience

Register
   ↓
Automatic Permanent Free Chat ID
   ↓
Email Verification
   ↓
Login
   ↓
Search Free Chat ID
   ↓
View Real Profile
   ↓
Start 1-to-1 Chat
   ↓
Send Text / Image
   ↓
Real-Time Delivery
   ↓
Push Notification
   ↓
Delivered
   ↓
Seen

---

✨ Features

🔐 Authentication

- Email & Password registration
- Email & Password login
- Mandatory email verification
- Forgot password
- Change password
- Change email
- Secure Firebase Authentication
- Re-authentication where required
- Secure account deletion
- Logout

🆔 Permanent Free Chat ID

Every registered user receives a unique Free Chat ID automatically.

Example:

FC8K4M2P7X

The ID is:

- Unique
- Permanent
- Searchable
- Shareable
- Case-normalized
- Firebase-backed
- Non-sensitive

The Free Chat ID cannot be changed or regenerated after registration.

---

👤 User Discovery

Users can find other users using their Free Chat ID.

Enter Free Chat ID
        ↓
Normalize ID
        ↓
Search userIdIndex
        ↓
Find UID
        ↓
Load authorized profile
        ↓
Display real user

The application does not scan the entire users database to perform searches.

---

💬 Real-Time 1-to-1 Messaging

Free Chat supports private communication between two users.

Supported message types:

- Text
- Image

Every conversation uses a deterministic conversation ID to prevent duplicate conversations.

Message Lifecycle

Compose
   ↓
Local Message
   ↓
Unique Message ID
   ↓
Pending
   ↓
Firebase
   ↓
Sent
   ↓
Delivered
   ↓
Seen

Message states:

PENDING
SENT
DELIVERED
SEEN

---

📴 Offline Messaging

Free Chat is designed to support offline text messaging.

When the device is offline:

Write Message
     ↓
Local Database
     ↓
Pending
     ↓
Display Immediately
     ↓
Network Restored
     ↓
Automatic Synchronization
     ↓
Firebase
     ↓
Sent

Users do not need to press Send again after reconnecting.

Message IDs remain consistent during retries to prevent duplicate messages.

---

🖼️ Image Messaging

Users can send images through Firebase Storage.

Supported formats:

- JPEG
- PNG
- WebP

Image flow:

Select Image
     ↓
Preview
     ↓
Compress
     ↓
Upload to Firebase Storage
     ↓
Create Message Metadata
     ↓
Realtime Database
     ↓
Recipient

Image access is protected using Firebase Storage Rules.

---

👀 Message Status

Free Chat supports real message status tracking.

Sent

✓

Delivered

✓✓

Seen

✓✓

Actual Firebase server timestamps are used for message status.

---

🟢 Presence

The application supports real user presence.

Possible states:

- Online
- Offline
- Last seen

Presence is based on Firebase Realtime Database connection state and server-side disconnect handling.

No fake online status is used.

---

✍️ Typing Indicator

Users can see when the other person is typing.

Example:

Rony is typing...

Typing state is synchronized through Firebase Realtime Database.

Debouncing/throttling is used to avoid unnecessary database writes.

---

🔔 Push Notifications

Free Chat uses Firebase Cloud Messaging (FCM).

Example notification:

Free Chat

Rony Hassan sent you a message

Notification navigation is securely validated before opening a conversation.

Foreground notifications are coordinated with realtime listeners to avoid unnecessary duplicate notifications.

---

🔒 Security

Security is a core part of Free Chat.

The application uses:

- Firebase Authentication
- Email Verification
- Firebase Realtime Database Rules
- Firebase Storage Rules
- Firebase App Check
- Secure client architecture

Security Principles

Unauthenticated users:

DENY

Authenticated users can access only the data they are authorized to access.

Users cannot:

- Read unauthorized conversations
- Modify another user's profile
- Modify another user's Free Chat ID
- Access unauthorized images
- Write unauthorized messages
- Bypass blocking
- Access private user data

---

🛡️ Firebase App Check

Firebase App Check provides an additional abuse-protection layer.

It does not replace:

- Authentication
- Database Rules
- Storage Rules
- Authorization

---

🗄️ Firebase Architecture

Free Chat uses Firebase as its production backend.

Service| Purpose
Firebase Authentication| User authentication
Firebase Realtime Database| Users, conversations, messages, presence
Firebase Storage| Image storage
Firebase Cloud Messaging| Push notifications
Firebase Crashlytics| Crash monitoring
Firebase App Check| Application protection

---

📂 Database Structure

Conceptual production structure:

users/
userIdIndex/
friends/
blockedUsers/
conversations/
userConversations/
messages/
presence/
typing/
userDevices/
userSettings/

Users

users/
  {uid}/
    uid
    userId
    name
    email
    photoUrl
    createdAt
    updatedAt

User ID Index

userIdIndex/
  {normalizedUserId}/
    uid

Conversations

conversations/
  {conversationId}/
    participants/
      {uidA}: true
      {uidB}: true
    lastMessage
    lastMessageType
    lastMessageSenderId
    lastMessageTime
    updatedAt

Messages

messages/
  {conversationId}/
    {messageId}/
      senderId
      receiverId
      type
      text
      imageUrl
      thumbnailUrl
      timestamp
      sentAt
      deliveredAt
      readAt
      status

---

⚡ Performance

Free Chat prioritizes:

- Fast startup
- Efficient Firebase queries
- Pagination
- Lazy loading
- Local caching
- Image compression
- Limited realtime listeners
- Minimal database writes
- Optimized indexes

The application must never download all users or all messages unnecessarily.

---

🎨 Design System

Free Chat uses a soft pink and white visual language.

Primary Colors

Primary Pink:    #FF4F91
Light Pink:      #FFE6F0
Background:      #FFFFFF
Soft Background: #FFF8FB
Primary Text:    #222222
Secondary Text:  #777777
Divider:         #F1E7EC
Success:         #22C55E
Error:           #EF4444

Typography

Primary font:

Alata

The UI uses:

- Rounded cards
- Soft borders
- Comfortable spacing
- Clean icons
- Subtle shadows
- Pink accents
- White surfaces
- Smooth transitions
- Accessible touch targets

---

📱 Main Screens

Authentication

1. Splash
2. Login
3. Registration
4. Email Verification
5. Forgot Password

Main Application

1. Chats
2. Friends
3. Search User ID
4. User Profile
5. Chat
6. Image Preview
7. Profile
8. Settings
9. Notification Settings
10. Privacy Settings
11. Blocked Users
12. Change Password
13. Change Email
14. Message Info
15. Account Deletion
16. About
17. Privacy Policy
18. Help & Support

---

🔐 Privacy

Private data must remain private.

The application must never publicly expose:

- Passwords
- Authentication tokens
- Refresh tokens
- Private conversations
- Private images
- Internal security information

Passwords are managed exclusively through Firebase Authentication.

---

🚫 Out of Scope

Free Chat is strictly a messaging application.

The following features are intentionally excluded:

- ❌ Voice calls
- ❌ Video calls
- ❌ Audio calls
- ❌ WebRTC calling
- ❌ Group chat
- ❌ Public social feed
- ❌ Stories
- ❌ Reels
- ❌ Marketplace
- ❌ Payments
- ❌ Wallet
- ❌ Credits
- ❌ Recharge
- ❌ Subscriptions
- ❌ Call history
- ❌ Cryptocurrency
- ❌ Mobile-number authentication
- ❌ OTP
- ❌ Fake users
- ❌ Demo users
- ❌ Fake messages
- ❌ Hard-coded chat data

---

🧪 Testing

The application must be tested for:

Authentication

- Registration
- Login
- Email verification
- Forgot password
- Password change
- Email change
- Logout
- Account deletion

Free Chat ID

- Automatic generation
- Uniqueness
- Normalization
- Search
- Case-insensitive search
- Invalid IDs
- Permanent ID protection
- Unauthorized ID modification

Messaging

- Online messaging
- Offline messaging
- Automatic synchronization
- Retry
- Duplicate prevention
- Sent status
- Delivered status
- Seen status
- Unread count
- Realtime receiving

Images

- Permissions
- JPEG
- PNG
- WebP
- Preview
- Compression
- Upload
- Progress
- Failure
- Retry
- Unauthorized access

Presence

- Online
- Offline
- Last seen
- Connection loss
- Reconnection
- Background state
- App termination

Notifications

- Foreground
- Background
- Closed application
- Permission granted
- Permission denied
- Correct sender
- Correct conversation
- Notification navigation
- Duplicate prevention

---

🚀 Production Requirements

Before production release, verify:

- [ ] Firebase Authentication configured
- [ ] Email verification working
- [ ] Permanent Free Chat ID generation working
- [ ] ID uniqueness guaranteed
- [ ] ID immutability enforced
- [ ] User ID search optimized
- [ ] Database Rules deployed
- [ ] Storage Rules deployed
- [ ] Firebase App Check configured
- [ ] FCM configured
- [ ] Crashlytics configured
- [ ] Offline queue tested
- [ ] Duplicate prevention tested
- [ ] Presence tested
- [ ] Typing tested
- [ ] Delivery tested
- [ ] Seen status tested
- [ ] Notifications tested
- [ ] Permissions tested
- [ ] Account deletion tested
- [ ] Security scenarios tested
- [ ] No fake data
- [ ] No demo data
- [ ] No forbidden features

---

📌 Definition of Done

Free Chat is considered production-ready when:

- A real user can register.
- A permanent Free Chat ID is automatically generated.
- The ID is unique and immutable.
- Email verification is mandatory.
- Verified users can log in.
- Users can find other users using Free Chat ID.
- Real profiles are displayed.
- Exactly one conversation exists per user pair.
- Text messaging works in real time.
- Images work securely.
- Offline messages are queued and synchronized automatically.
- Retries do not create duplicate messages.
- Sent, Delivered and Seen statuses work.
- Unread counts work.
- Presence and Last Seen work.
- Typing indicators work.
- Push notifications work.
- Privacy settings work.
- Blocking is enforced.
- Firebase Rules enforce authorization.
- Storage Rules enforce authorization.
- App Check is configured.
- Crashlytics is configured.
- Account deletion works.
- No passwords are stored in the database.
- No mobile number is required.
- No fake or demo data exists.
- No calling, payment, wallet, credit or subscription features exist.

---

🔑 Non-Negotiable Principles

REAL USERS ONLY
REAL FIREBASE DATA ONLY
REAL-TIME DATA ONLY
REAL NOTIFICATIONS ONLY
REAL PRESENCE ONLY
REAL TYPING STATUS ONLY
REAL MESSAGE STATUS ONLY

NO FAKE DATA
NO DEMO DATA
NO MOBILE NUMBER
NO EDITABLE USER ID
NO DUPLICATE CONVERSATIONS
NO DUPLICATE MESSAGES
NO CLIENT-ONLY SECURITY
NO UNAUTHORIZED DATA ACCESS
NO PASSWORD STORAGE

NO CALLING
NO PAYMENT
NO WALLET
NO CREDIT
NO SUBSCRIPTION
NO GROUP CHAT

---

🎯 Product Goal

«Free Chat = Permanent User ID + Real Profiles + 1-to-1 Messaging + Images + Offline Queue + Automatic Sync + Presence + Typing + Delivered + Seen + Push Notifications + Strong Firebase Security.»

No calls. No payments. No wallet. No credits. No mobile numbers. No editable IDs. No fake data. Only real messaging.

---

📄 License

This project is proprietary unless a separate open-source license is provided by the project owner.

---

👨‍💻 Project

Product: Free Chat
Platform: Android + iOS
Category: Real-Time Private 1-to-1 Messenger
Backend: Firebase
Database: Firebase Realtime Database
Storage: Firebase Storage
Authentication: Firebase Authentication
Notifications: Firebase Cloud Messaging
Crash Monitoring: Firebase Crashlytics
Protection: Firebase App Check
Design: Pink + White Soft UI
Font: Alata
