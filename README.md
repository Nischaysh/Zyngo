# Zyngo - A Modern Social Media App 🟣

**Zyngo** is a full-featured, real-time social media application built using **Kotlin**, **Firebase**, and modern Android UI principles. Designed for seamless interactions, Vibin brings together posts, stories, messaging, and user presence into one beautiful experience.

<img src="https://raw.githubusercontent.com/Nischaysh/Zyngo/d496adb41d701c2ff7ac1926f87a948d55c76abb/Frame%202.png" alt="Vibin Banner" />
<img src="https://raw.githubusercontent.com/Nischaysh/Zyngo/dd081946b781036b8bf90db200db7aea2f005ded/Frame%202%20(1).png" alt="Vibin Banner" />

---

## 🚀 Features

### 📸 Create & Share Posts
- Users can create image + caption posts (like Instagram).
- Optionally share text-only posts (like Twitter).
- Posts support:
  - Likes 👍
  - Comments 💬
  - Real-time updates on post interactions

### 💬 Real-Time Chat System
- One-on-one private messaging (WhatsApp style).
- Built with **Firestore real-time updates**.
- Automatically creates chat tiles for recent conversations.
- Messages are delivered and shown instantly.
- Chat layout includes:
  - Sent & received message bubbles
  - Timestamps
  - Scroll to latest message on new entry

### 🟢 Online/Offline User Status
- Shows **online** status of users you follow.
- Last seen is updated when a user goes offline.
- Uses **Firebase Realtime Database** for presence management.

### 👤 Follow/Unfollow Users
- Users can follow others to see their posts and stories.
- Following is stored in Firestore under each user's data.
- Explore screen shows all registered users with follow option.

### 🔄 Stories (Status) Feature
- Like WhatsApp stories or Instagram stories:
  - Upload temporary photo stories
  - Stories expire after 24 hours
- Users can view stories of people they follow.
- Real-time story updates with smooth UI.

### 📩 Notifications Tab (Coming Soon)
- A section to show all activity related to the user:
  - Who followed you
  - Who liked your post
  - New comments/messages

### 🔍 Explore Screen
- Discover new users to follow
- Displays profile picture, username, and follow button
- Search functionality (planned)

### 👥 Profile Management
- Update profile picture, bio, and username
- View all personal posts
- Easy logout option

### 💾 Image Cropping Before Upload
- Integrated **uCrop** for better image control
- Crops image before upload to maintain UI consistency

---

## 🛠️ Built With

- **Language**: Kotlin  
- **UI**: XML, Material Design 3  
- **Backend**: Firebase Firestore, Firebase Auth, Firebase Storage, Realtime DB  
- **Libraries**:
  - Glide (for image loading)
  - uCrop (for image cropping)
  - FirebaseUI
  - Material Components

---

## 🔐 Authentication

- Users can register with email & password.
- Login session is persisted using FirebaseAuth.
- Secure sign-in & sign-out process.
- Auth guards ensure only authenticated users access the app.

---

## ✅ Upcoming Features

- Push notifications using **Firebase Cloud Messaging (FCM)**
- Story reactions and emojis
- Typing indicator in chats
- Message read receipts
- In-app theme switcher (light/dark mode)

---

## 📲 Download the App

🎉 **Try out my App on your Android device!**

**Click download link**

<br>
<p align="center">
  <a href="https://github.com/Nischaysh/Zyngo/releases/download/v1.0.0/app-debug.apk" download>
    <img src="https://img.shields.io/badge/Download%20APK-blue?style=for-the-badge&logo=android" alt="Download APK">
  </a>
</p>
> No Play Store needed – just download, install, and enjoy the clean UI experience!
> 💡 Tip: Enable "Install from unknown sources" in your phone settings if needed.

---

## 🙋‍♂️ Author

**Lalit Sharma**  
Android Developer | UI/UX Enthusiast  
[LinkedIn](https://linkedin.com/in/lalit-sharma_x) • [Twitter](https://twitter.com/nischayyy_y))



