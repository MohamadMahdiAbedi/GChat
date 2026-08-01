# GChat

<div align="center">

**Smart Hybrid Messenger — Real SMS + Online Chat**

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-6.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com)
[![Status](https://img.shields.io/badge/Status-Active%20Development-orange?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-TBD-lightgrey?style=for-the-badge)](#)

A modern Android messaging app that combines **real SMS/MMS** with **real-time online chat**.

</div>

---

## Features

### SMS & MMS
- Can become the default SMS app
- Full support for concatenated (multi-part) SMS
- SIM card selection for sending messages
- Display of receiving SIM under each message
- Beautiful MessagingStyle notifications
- Contact names and profile pictures

### Online Chat
- Real-time messaging via WebSocket
- Registration using SIM ICCID
- User search
- Online status
- Message synchronization

### User Experience
- Built with Jetpack Compose + Material 3
- Light / Dark / System theme
- 19 color palettes
- Full RTL and Persian language support
- Smooth animations

---

## Current Status

GChat is under **active development**.  
There is no stable public release yet.  

We are currently focused on **stability and critical bug fixes**.

### Roadmap Overview

| Stage | Title                        | Status              |
|-------|------------------------------|---------------------|
| **1** | Stability & Critical Bugs    | 🔴 In Progress      |
| **2** | Architecture                 | 🟠 Planned          |
| **3** | Performance                  | 🟡 Planned          |
| **4** | UI / UX                      | 🟢 Planned          |
| **5** | Advanced Features            | 🔵 Planned          |
| **6** | Device Compatibility         | ⚪ Planned          |
| **7** | Documentation                | 🟣 Starting         |
| **8** | Future (Desktop, Themes...)  | ⚫ Later            |

Detailed checklist is available in the [Roadmap](#roadmap) section below.

---

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Minimum SDK: 23 (Android 6.0)
- Target SDK: 37

---

## Build & Run

```bash
git clone https://github.com/MohamadMahdiAbedi/GChat.git
cd GChat
./gradlew assembleDebug
```

Or simply open the project in Android Studio.

> **Note:** To fully test SMS features, you must set GChat as the default SMS app.

---

## Project Structure

```
app/src/main/java/ir/gchat/
├── MainActivity.kt          # Entry point & Navigation
├── SocketViewModel.kt       # WebSocket management
├── SMS.kt                   # Full SMS/MMS logic
├── MainViewModel.kt         # Theme & settings
├── Greeting.kt              # Login & Sign-up screens
├── Settings.kt              # App settings
└── ...
```

---

## Contributing

We welcome all contributions!

### How to contribute

1. Fork the repository
2. Create a new branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes
4. Push to your branch
5. Open a Pull Request

### Current Priorities

- Fix critical SMS bugs (especially on Samsung devices)
- Break down large files (`MainActivity`, `Greeting`, `SMS`)
- Improve state management and reduce recomposition
- Add unit and UI tests

Please open an Issue first if you want to work on a specific item, so we can coordinate.

---

## Roadmap

### 🔴 Stage 1 — Stability & Critical Bugs
**Goal:** The app works reliably on most devices without major bugs.

#### SMS
- [ ] Fix message order in conversations
- [ ] Properly handle concatenated (multi-part) SMS
- [ ] Fix SMS not received on some devices (Galaxy A26, S21 FE, etc.)
- [ ] Fix chat list not updating when app is not default SMS
- [ ] SIM selection for sending messages
- [ ] Show receiving SIM under each message
- [ ] Correct multi-line message display in chat list
- [ ] Proper handling of messages on Persian devices

#### Navigation
- [ ] Fix long Splash Screen
- [ ] Fix auto-login jump between screens
- [ ] Prevent multiple navigations on Back press
- [ ] Fix BackHandler for menus
- [ ] Fix incorrect keyboard open/close behavior

#### Socket
- [ ] Operation queue when connection is lost
- [ ] Sync across different sessions
- [ ] Receive updates without full refresh
- [ ] Update chat only with new messages

---

### 🟠 Stage 2 — Architecture
**Goal:** Make the project maintainable.

- [ ] Break down `MainActivity`
- [ ] Break down `Greeting.kt`
- [ ] Break down `SMS.kt`
- [ ] Break down `SocketViewModel`
- [ ] Separate UI from Business Logic
- [ ] Improve Navigation Architecture
- [ ] Review DataStore usage
- [ ] Remove unnecessary `runBlocking`
- [ ] Better state management
- [ ] Proper queue design

---

### 🟡 Stage 3 — Performance
**Goal:** Improve speed and smoothness.

- [ ] Investigate slow Splash
- [ ] Reduce recomposition
- [ ] Remove heavy Composables
- [ ] Optimize Scaffold usage
- [ ] Better WindowInsets handling
- [ ] Optimize menu animations
- [ ] Limit initial message rendering
- [ ] Expand long messages
- [ ] Optimize conversation list

---

### 🟢 Stage 4 — UI / UX
- Theme & color system improvements
- Better notifications (lock screen, reply, actions)
- Chat improvements (scroll buttons, mark all read, context menu, tabs)

---

### 🔵 Stage 5 — Advanced Features
- Message management (delete, multi-select, archive, saved, spam, blocked, trash)
- Search & link preview
- Per-contact notification settings
- Special messages (CP, configuration)

---

### ⚪ Stage 6 — Compatibility
- Better SMS Role handling with Google Play
- Specific device fixes (Redmi 13, Galaxy A34, etc.)
- Dual SIM behavior
- Older Android support investigation

---

### 🟣 Stage 7 — Documentation
- [x] README (this file)
- [ ] LICENSE
- [ ] CONTRIBUTING
- [ ] Issue Templates
- [ ] Screenshots
- [ ] Release Notes

---

### ⚫ Stage 8 — Future (Do not start yet)
- Desktop versions (Windows, macOS, Linux)
- Alternative themes (One UI, HyperOS, Acrylic, Mica...)
- Possible View-based version for older devices

---

## Screenshots

> Coming soon...

---

## License

Not finalized yet. Will be added soon.

---

## Acknowledgements

Thanks to everyone who reports bugs, suggests ideas, or contributes code.

---

<div align="center">

Made with ❤️ in Iran

**GChat** — Smart, simple, and powerful messaging

</div>

---

<br>

# نسخه فارسی

<div align="center">

**پیام‌رسان هوشمند ترکیبی — پیامک واقعی + چت آنلاین**

</div>

### ویژگی‌ها

**پیامک و MMS**
- قابلیت تبدیل به اپلیکیشن پیش‌فرض SMS
- پشتیبانی کامل از پیامک‌های چندبخشی
- انتخاب سیم‌کارت برای ارسال
- نمایش سیم‌کارت دریافت‌کننده
- نوتیفیکیشن‌های زیبا
- پشتیبانی از مخاطبین و عکس پروفایل

**چت آنلاین**
- ارتباط لحظه‌ای با WebSocket
- ثبت‌نام با ICCID سیم‌کارت
- جستجوی کاربران
- وضعیت آنلاین
- همگام‌سازی پیام‌ها

**تجربه کاربری**
- ساخته‌شده با Jetpack Compose و Material 3
- تم روشن / تاریک / سیستم
- ۱۹ پالت رنگی
- پشتیبانی کامل از راست‌چین و فارسی
- انیمیشن‌های روان

### وضعیت فعلی

پروژه در حال توسعه فعال است و هنوز نسخه پایدار عمومی ندارد.  
تمرکز فعلی ما روی **پایداری و رفع باگ‌های بحرانی** است.

### اولویت‌های فعلی کمک

- رفع باگ‌های پیامک (به‌خصوص روی دستگاه‌های سامسونگ)
- شکستن فایل‌های بزرگ
- بهبود مدیریت State
- افزودن تست

اگر می‌خواهید روی مورد خاصی کار کنید، لطفاً ابتدا یک Issue باز کنید.

---

**ساخته شده با ❤️ در ایران**
