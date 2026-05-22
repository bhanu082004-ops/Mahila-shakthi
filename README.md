# 🌸 Mahila Shakti Unnati
### ಮಹಿಳಾ ಶಕ್ತಿ ಉನ್ನತಿ — SHG Digital Ledger

An offline-first Android app that acts as a **Digital Accountant for Women's Self-Help Groups (SHGs)**.

---

## 📱 Features

| Feature | Description |
|---|---|
| 🏠 Group Setup Wizard | One-time setup with group name, meeting day, PIN |
| 👥 Member Directory | Add members with photo, search, credit score badges |
| 📅 Weekly Meeting | Mark members as Paid ✅ / Pending ⚠️ per week |
| 💰 Group Capital | Real-time total of all savings (LiveData → ViewModel) |
| 🏦 Loan Management | Issue loans with auto SI calculation, repayment tracking |
| 📊 Credit Score | 0–100 score based on savings consistency + repayments |
| 🤖 AI Financial Coach | Gemini-powered Q&A in local language |
| 📝 Monthly Reports | WhatsApp-shareable plain-text summary |
| 🔒 Secretary PIN | Admin actions protected by 4-digit PIN |

---

## 🚀 Setup Instructions

### 1. Open in Android Studio
- Open **Android Studio Hedgehog** (or newer)
- Choose **Open** → Select the `MahilaShaktiUnnati` folder
- Wait for Gradle sync to complete

### 2. Set your Android SDK path
Edit `local.properties`:
```
sdk.dir=/Users/yourname/Library/Android/sdk       # Mac
sdk.dir=C:\Users\yourname\AppData\Local\Android\Sdk  # Windows
sdk.dir=/home/yourname/Android/Sdk                 # Linux
```

### 3. Add Gemini API Key *(for AI Coach)*
1. Go to [https://aistudio.google.com](https://aistudio.google.com) → Get API Key (free)
2. Open `app/build.gradle.kts`
3. Replace `"YOUR_GEMINI_API_KEY_HERE"` with your actual key:
```kotlin
buildConfigField("String", "GEMINI_API_KEY", "\"AIza...your...key\"")
```

### 4. Run the app
- Connect an Android device or start an emulator (API 21+)
- Click the ▶️ Run button

---

## 🏗️ Architecture

```
MVVM Architecture
├── UI Layer         → Jetpack Compose screens
├── ViewModel        → MainViewModel (business logic, LiveData)
├── Repository       → SHGRepository (single source of truth)
└── Data Layer       → Room DB (SQLite, offline-first)

Entities: SHGGroup · Member · SavingsEntry · Loan · LoanRepayment
```

## 🧑‍💻 Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Repository
- **Database:** Room DB (offline-first)
- **DI:** Hilt
- **AI:** Google Gemini API (generativeai)
- **Images:** Coil + CameraX
- **Navigation:** Jetpack Navigation Compose
- **Min SDK:** API 21 (Android 5.0)

---

## 📁 Project Structure

```
app/src/main/
├── java/com/mahilashakti/unnati/
│   ├── data/
│   │   ├── entity/        → Room entities + relations
│   │   ├── dao/           → DAO interfaces
│   │   ├── AppDatabase.kt → Room database
│   │   └── repository/    → SHGRepository
│   ├── ui/
│   │   ├── screens/       → All Compose screens
│   │   ├── components/    → Shared UI components
│   │   └── theme/         → Colors, typography, theme
│   ├── viewmodel/         → MainViewModel
│   ├── AppModule.kt       → Hilt DI
│   ├── MahilaShaktiApp.kt → Application class
│   └── MainActivity.kt    → Entry point + NavGraph
└── res/
    ├── values/strings.xml → English + Kannada strings
    └── xml/file_paths.xml → FileProvider paths
```

---

## 🎨 Design Language
- **Primary color:** Saffron Gold `#E8A020` (Women's empowerment)
- **Secondary:** Deep Magenta `#9C3587`
- **Tertiary:** Teal `#006B5D` (Trust / Finance)
- **Paid status:** Green `#2E7D32`
- **Pending status:** Amber `#F57F17`

---

## 💡 Key Code Highlights

### Real-Time Group Capital
```kotlin
@Query("SELECT SUM(amount) FROM savings_entries WHERE status = 'PAID'")
fun getTotalGroupCapital(): Flow<Double?>
```

### Simple Interest Calculation
```kotlin
val si = (principal * rate * (tenureMonths / 12.0)) / 100.0
val totalRepayable = principal + si
val monthlyInstalment = totalRepayable / tenureMonths
```

### Loan Block Enforcement
```kotlin
val active = repo.getActiveLoanForMember(memberId)
if (active != null) {
    onResult(false, "Member has an existing active loan. Please clear it first.")
    return@launch
}
```

---

*Built for the Android App Development using GenAI internship course | MindMatrix 2026*
