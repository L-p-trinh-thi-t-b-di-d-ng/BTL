[English](#english) | [Tiếng Việt](#tiếng-việt)

---

# English

# LingBook - A Comprehensive English Learning App 🚀

**LingBook** is a mobile application project for the Android OS, built with the goal of providing users with a comprehensive and personalized English learning tool. The application allows users to learn through structured lessons, practice vocabulary with flashcards, and track their progress in detail.

## 🖼️ Screenshots

| Home Screen | Lesson List | Vocabulary Topic | Flashcards | Progress | Practice | Social |
|---|---|---|---|---|---|---|
| ![image](https://github.com/user-attachments/assets/06bbc305-b62b-433a-8bd3-6ed17f38449b) |![image](https://github.com/user-attachments/assets/97c3a480-d2f3-4a6d-8fb5-677c2339e575) |![image](https://github.com/user-attachments/assets/6cc1e592-1ba9-4ae3-8e3c-637363bda0cb) |![image](https://github.com/user-attachments/assets/b33ff8b5-c9d5-4794-8a34-c5794f87da2c) |![image](https://github.com/user-attachments/assets/f9229106-9e59-4d12-bf8e-f34737f2f078) |![image](https://github.com/user-attachments/assets/5ca8492c-2d74-4b2d-ae85-59bf4e18866c) |![image](https://github.com/user-attachments/assets/5908b6d3-6f48-40fe-ae89-717e3772f199) |

## ✨ Features

The LingBook application includes the following core feature flows:

#### 👤 **User Authentication & Management**
* Register, Login, and Auto-Login sessions.
* Full Profile Management:
    * View user information.
    * Update display name.
    * Change password (with re-authentication for security).
    * Permanently delete account (includes Firestore data cleanup).
    * Logout functionality.

#### 📚 **Structured Lessons**
* Lessons categorized into 3 levels: Beginner, Intermediate, and Advanced.
* **Diverse Exercise Types:**
    * Translation (Vietnamese-English & English-Vietnamese).
    * Listen & Fill-in-the-blank.
    * Listen & Choose the correct answer.
* Automatically saves scores and progress after completing each lesson.

#### 📇 **Vocabulary Learning with Flashcards**
* Learn vocabulary organized by topics (Animals, Food, Jobs, etc.).
* Modern flashcard interface with **3D flip animation** and **swipe navigation**.
* Integrated **Text-to-Speech** to listen to standard pronunciation of words.
* Saves learning progress: Users can mark words as **"I know"** or **"I don't know"**.

#### 📈 **Progress Tracking**
* **Overview Dashboard:** Displays statistics on completed lessons and learned words.
* **Detailed Lesson History:** Shows a list of completed lessons with scores and completion dates.
* **Detailed Vocabulary History:** Displays a list of learned words.
* **Interactive:** Allows users to tap on a lesson in their history to redo it.
* (Future plan) Allows users to tap on a voca in their history to see overview of it such as word, meaning, definition, pronunciation, mark word as learned or for learning, ...

## 🛠️ Tech Stack & Architecture

This project was built using modern technologies and architecture patterns recommended by Google.

* **Language:** **Kotlin** (Official language for Android development).
* **Architecture:**
    * **Single-Activity Architecture:** Utilizes a main Activity to host all Fragments.
    * **Lean MVVM (Model-View-ViewModel):** Separates UI (View) from data (Model), using a `SharedViewModel` to share user state across the app.
* **UI:**
    * **Android XML** with ViewBinding.
    * **Material Design 3:** Implements modern components like `MaterialCardView`, `MaterialButton`, `BottomNavigationView`.
* **Jetpack Components:**
    * **Navigation Component:** Manages all navigation flows between screens visually and efficiently.
    * **ViewModel & LiveData:** Manages UI-related data in a lifecycle-aware way.
* **Backend & Data:**
    * **Firebase Authentication:** Handles user authentication.
    * **Cloud Firestore:** NoSQL database for storing all user info, lessons, vocabulary, and progress.
* **Third-party Libraries:**
    * **Glide:** For loading and displaying images from the internet.
    * **Android's built-in Text-to-Speech (TTS)** engine.

## 🚀 Getting Started

1.  Clone this repository to your local machine.
2.  Open the project in Android Studio.
3.  Create a new project on the [Firebase Console](https://console.firebase.google.com/).
4.  Add an Android app to the Firebase project with the package name `com.dex.lingbook`.
5.  Download the `google-services.json` file from Firebase and place it in the project's `app` directory.
6.  In the Firebase Console, enable the following services: **Authentication** (with the Email/Password provider) and **Firestore Database**.
7.  Build and run the application.

## 📱 Install the app on your phone

<img width="500" height="500" alt="LingBook" src="https://github.com/user-attachments/assets/043ede88-0cdb-4eea-bffc-b5e4f4bf1b7a" />



---

# Tiếng Việt

# LingBook - Ứng dụng Học Tiếng Anh Toàn diện 🚀

**LingBook** là một dự án ứng dụng di động dành cho hệ điều hành Android, được xây dựng với mục tiêu cung cấp một công cụ học tiếng Anh toàn diện và cá nhân hóa cho người dùng. Ứng dụng cho phép người dùng học thông qua các bài học có cấu trúc, luyện tập từ vựng bằng flashcard, và theo dõi tiến độ của bản thân một cách chi tiết.

## 🖼️ Ảnh chụp Màn hình

| Màn hình chính | Danh sách Bài học | Chủ đề Từ vựng | Học Flashcard | Tiến độ |Luyện tập |Mạng xã hội |
|---|---|---|---|---|---|---|
| ![image](https://github.com/user-attachments/assets/06bbc305-b62b-433a-8bd3-6ed17f38449b) |![image](https://github.com/user-attachments/assets/97c3a480-d2f3-4a6d-8fb5-677c2339e575) |![image](https://github.com/user-attachments/assets/6cc1e592-1ba9-4ae3-8e3c-637363bda0cb) |![image](https://github.com/user-attachments/assets/b33ff8b5-c9d5-4794-8a34-c5794f87da2c) |![image](https://github.com/user-attachments/assets/f9229106-9e59-4d12-bf8e-f34737f2f078) |![image](https://github.com/user-attachments/assets/5ca8492c-2d74-4b2d-ae85-59bf4e18866c) |![image](https://github.com/user-attachments/assets/5908b6d3-6f48-40fe-ae89-717e3772f199) |

## ✨ Tính năng Nổi bật

Ứng dụng LingBook bao gồm các luồng tính năng chính:

#### 👤 **Xác thực & Quản lý Người dùng**
* Đăng ký, Đăng nhập, và các phiên tự động đăng nhập.
* Quản lý Hồ sơ cá nhân đầy đủ:
    * Xem thông tin người dùng.
    * Cập nhật tên hiển thị.
    * Thay đổi mật khẩu (với yêu cầu xác thực lại để đảm bảo an toàn).
    * Xóa tài khoản vĩnh viễn (bao gồm cả việc dọn dẹp dữ liệu trên Firestore).
    * Chức năng đăng xuất.

#### 📚 **Học theo Bài học có Cấu trúc**
* Các bài học được phân loại theo 3 cấp độ: Beginner, Intermediate, và Advanced.
* **Các dạng Bài tập Đa dạng:**
    * Dịch câu (Việt-Anh & Anh-Việt).
    * Nghe & Điền từ vào chỗ trống.
    * Nghe & Chọn đáp án đúng.
* Tự động lưu điểm số và tiến độ sau khi hoàn thành mỗi bài học.

#### 📇 **Học Từ vựng với Flashcard**
* Học từ vựng được sắp xếp theo chủ đề (Động vật, Thức ăn, Công việc,...).
* Giao diện flashcard hiện đại với **hiệu ứng lật 3D** và **điều hướng bằng thao tác vuốt**.
* Tích hợp **Text-to-Speech** để nghe phát âm chuẩn của từ vựng.
* Lưu lại tiến độ học: Người dùng có thể đánh dấu từ là **"Đã biết"** hoặc **"Chưa biết"**.

#### 📈 **Theo dõi Tiến độ**
* **Bảng điều khiển Tổng quan:** Hiển thị các số liệu thống kê về bài học đã hoàn thành và từ vựng đã thuộc.
* **Lịch sử Bài học Chi tiết:** Hiển thị danh sách các bài học đã hoàn thành kèm theo điểm số và ngày làm bài.
* **Lịch sử Từ vựng Chi tiết:** Hiển thị danh sách các từ đã học.
* **Tương tác:** Cho phép người dùng nhấn vào một bài học trong lịch sử để làm lại.
* (Kế hoạch phát triển trong tương lai) Cho phép người dùng nhấn vào một từ vựng trong lịch sử để xem tổng quan về nó như từ, nghĩa, định nghĩa, phát âm, đánh dấu là đã học hoặc đang học,...

## 🛠️ Công nghệ & Kiến trúc

Dự án được xây dựng dựa trên các công nghệ và kiến trúc hiện đại được Google khuyên dùng.

* **Ngôn ngữ:** **Kotlin** (Ngôn ngữ chính thức để phát triển Android).
* **Kiến trúc:**
    * **Kiến trúc Đơn-Activity (Single-Activity):** Sử dụng một Activity chính để chứa tất cả các Fragment.
    * **MVVM Tinh gọn (Lean MVVM):** Tách biệt giao diện (View) và dữ liệu (Model), sử dụng một `SharedViewModel` để chia sẻ trạng thái người dùng trên toàn ứng dụng.
* **Giao diện (UI):**
    * **Android XML** với ViewBinding.
    * **Material Design 3:** Triển khai các thành phần hiện đại như `MaterialCardView`, `MaterialButton`, `BottomNavigationView`.
* **Thành phần Jetpack:**
    * **Navigation Component:** Quản lý toàn bộ luồng di chuyển giữa các màn hình một cách trực quan và hiệu quả.
    * **ViewModel & LiveData:** Quản lý dữ liệu liên quan đến UI một cách an toàn và nhận biết được vòng đời.
* **Backend & Dữ liệu:**
    * **Firebase Authentication:** Xử lý xác thực người dùng.
    * **Cloud Firestore:** Cơ sở dữ liệu NoSQL để lưu trữ toàn bộ thông tin người dùng, bài học, từ vựng và tiến độ.
* **Các thư viện bên thứ ba:**
    * **Glide:** Để tải và hiển thị hình ảnh từ Internet.
    * **Engine Text-to-Speech (TTS)** có sẵn của Android.

## 🚀 Cài đặt và Chạy dự án

1.  Clone repository này về máy của bạn.
2.  Mở project trong Android Studio.
3.  Tạo một project mới trên [Firebase Console](https://console.firebase.google.com/).
4.  Thêm một ứng dụng Android vào project Firebase với package name là `com.dex.lingbook`.
5.  Tải về file `google-services.json` từ Firebase và đặt nó vào thư mục `app` của dự án.
6.  Trong Firebase Console, bật các dịch vụ sau: **Authentication** (với nhà cung cấp Email/Password) và **Firestore Database**.
7.  Build và chạy ứng dụng.

## 📱 Cài đặt ứng dụng về điện thoại

<img width="500" height="500" alt="LingBook" src="https://github.com/user-attachments/assets/00182052-e8e1-4fa4-ab9d-81765c80884f" />



