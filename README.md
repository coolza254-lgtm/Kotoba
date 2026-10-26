# Kotoba (ことば) 🇯🇵
### Native Android Japanese Vocabulary & Grammar Learning App

[![Build and Release Kotoba APK](https://github.com/coolza254-lgtm/Kotoba/actions/workflows/build-release.yml/badge.svg)](https://github.com/coolza254-lgtm/Kotoba/actions/workflows/build-release.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Arch-arm64--v8a%20(Samsung%20Galaxy%20A54%205G)-orange.svg)](https://www.samsung.com)

---

## 📱 จุดเด่นของแอปพลิเคชัน (Key Highlights)

Kotoba ถูกออกแบบและพัฒนาขึ้นด้วย **Modern Native Android (Kotlin + Jetpack Compose + Room SQLite)** สำหรับผู้เรียนภาษาญี่ปุ่นตั้งแต่เริ่มต้นจนถึงระดับ **JLPT N4 (N5 & N4)** โดยมุ่งเน้นความสวยงามระดับพรีเมียม สบายตา ใช้งานได้จริงทุกวัน และทำงานแบบ **Offline-First 100%**:

1. **คลังคำศัพท์และไวยากรณ์ครอบคลุม N5 – N4**
   - **คำศัพท์ (Vocabulary)**: รวมคำศัพท์พื้นฐานจนถึง N4 พร้อมคัดแยกส่วนประกอบโมรา (Mora), คำอ่านฟุริงานะ, คำแปลภาษาไทย-อังกฤษ และตัวอย่างประโยค
   - **ไวยากรณ์ (Grammar Mode)**: รวมพื้นฐานการผันรูปกริยา (て-form, ない-form, た-form, 辞書形, 条件形) และไวยากรณ์ N5 & N4 พร้อมแถบแยกโครงสร้าง (接続 · Setsuzoku), จุดสังเกตความแตกต่าง (Nuance), ประโยคตัวอย่างพร้อมเสียงอ่าน และแบบฝึกหัดทบทวนในตัว

2. **ระบบ Furigana แก้ปัญหาตัวคานะลอยผิดตำแหน่ง (Baseline-Aligned)**
   - พัฒนา Custom Layout บน Jetpack Compose ที่ตัดปัญหาจุดบกพร่องของ `<ruby>` ในเว็บบราวเซอร์ โดยจัดวางฟุริงานะกึ่งกลางเหนือคันจิที่มีระยะห่าง 3.dp คงที่ และคงตัวอักษรคานะ/โอคูริงะนะลงบน Baseline เดียวกันอย่างไร้รอยต่อ

3. **แผนภาพระดับเสียงวรรณยุกต์โตเกียว (Tokyo Pitch Accent Diagram)**
   - แสดงเส้นเสียง Waveform และจุดเมโลดี้ตามระบบสัทศาสตร์โตเกียว (0: 平板, 1: 頭高, 2+: 中高/尾高) พร้อมไฮไลท์จุดเปลี่ยนระดับเสียง (Accent Kernel) สีแดงเด่นชัด

4. **อัลกอริทึมทบทวนความจำระยะยาว FSRS-5**
   - คำนวณช่วงเวลาทบทวนคำศัพท์ (Spaced Repetition) ด้วย FSRS-5 (Free Spaced Repetition Scheduler) แม่นยำกว่า SM-2 ดั้งเดิมของ Anki พร้อมพรีวิวระยะเวลาปุ่มกด (ลืม / ยาก / จำได้ / ง่าย)

5. **ระบบ In-App Updater (อัปเดตอัตโนมัติจากในแอป)**
   - ตรวจสอบเวอร์ชันล่าสุดผ่าน GitHub Releases API ของ Repository `coolza254-lgtm/Kotoba`
   - ดาวน์โหลดและเปิดตัวติดตั้ง APK ผ่าน `Android PackageInstaller` ได้ทันทีโดยไม่ต้องต่อคอมพิวเตอร์

6. **ปรับแต่งเฉพาะ Samsung Galaxy A54 5G**
   - คอมไพล์เจาะจงสถาปัตยกรรม 64-bit (`arm64-v8a`) เพื่อรีดประสิทธิภาพชิปประมวลผล **Exynos 1380**
   - แอนิเมชันลื่นไหลเต็มพิกัดบนหน้าจอ **120Hz Super AMOLED** และรองรับ Dark Mode ถนอมสายตา

---

## 🏗️ โครงสร้างสถาปัตยกรรมระบบ (Architecture)

```
com.kotoba.app
├── audio/
│   └── NativeAudioPlayer.kt         # ระบบสังเคราะห์เสียงอ่านภาษาญี่ปุ่น (Android TTS Engine)
├── data/
│   ├── db/
│   │   ├── KotobaDatabase.kt        # Room SQLite Database & ตัวโหลดฐานข้อมูลตั้งต้นจาก Assets
│   │   ├── WordDao.kt               # Data Access Object สำหรับคำศัพท์และ FSRS Scheduler
│   │   └── GrammarDao.kt            # Data Access Object สำหรับไวยากรณ์
│   └── model/
│       ├── Word.kt                  # Entity โครงสร้างคำศัพท์, Morae, Pitch Accent
│       └── Grammar.kt               # Entity โครงสร้างไวยากรณ์, สูตรเชื่อม, ข้อควรระวัง, ควิซ
├── fsrs/
│   └── FsrsEngine.kt                # อัลกอริทึม FSRS-5 คำนวณ Stability (S) และ Difficulty (D)
├── ui/
│   ├── components/
│   │   ├── FuriganaText.kt          # Custom Baseline-Aligned Furigana
│   │   └── PitchAccentView.kt       # แผนภาพวาดระดับเสียงพิทช์แอคเซนต์
│   ├── screens/
│   │   ├── TodayScreen.kt           # หน้าสรุปเป้าหมายการทบทวนประจำวัน
│   │   ├── WordsScreen.kt           # หน้ารายการค้นหาและกรองคำศัพท์
│   │   ├── WordDetailScreen.kt      # หน้ารายละเอียดคำศัพท์แบบเจาะลึก
│   │   ├── GrammarScreen.kt         # หน้ารวมบทเรียนไวยากรณ์
│   │   ├── GrammarDetailScreen.kt   # หน้ารายละเอียดไวยากรณ์ โครงสร้าง และมินิควิซ
│   │   ├── FlashcardSessionScreen.kt# หน้าแฟลชการ์ดทบทวนพร้อมปุ่ม FSRS Rating
│   │   ├── StatsScreen.kt           # หน้าสถิติอัตราการจดจำและสถานะความจำ
│   │   ├── SettingsScreen.kt        # หน้าตั้งค่าแอปและตัวตรวจสอบอัปเดต
│   │   ├── InAppUpdaterDialog.kt    # ไดอะล็อกแสดงสถานะอัปเดตและดาวน์โหลด APK
│   │   └── MainScreen.kt            # Scaffold 5 แท็บหลักและการจัดการเนวิเกชัน
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── updater/
│   └── GitHubUpdater.kt             # ระบบเช็คเวอร์ชัน GitHub Releases & ดาวน์โหลด OTA
├── KotobaApplication.kt
└── MainActivity.kt
```

---

## 🚀 การคอมไพล์และติดตั้ง (Build & Installation)

คุณสามารถสร้างไฟล์ติดตั้ง `.apk` ได้อย่างง่ายดายผ่าน **GitHub Cloud** โดยไม่ต้องติดตั้ง Android Studio หรือกินพื้นที่ในคอมพิวเตอร์:

### วิธีที่ 1: สั่ง Build ผ่าน GitHub Actions (แนะนำ)
1. ไปที่แท็บ **Actions** ใน GitHub Repository: [coolza254-lgtm/Kotoba/actions](https://github.com/coolza254-lgtm/Kotoba/actions)
2. เลือกเวิร์กโฟลว์ **"Build and Release Kotoba APK"** ทางซ้ายมือ
3. คลิกปุ่ม **"Run workflow"** แล้วระบุเลขเวอร์ชัน เช่น `v1.0.0`
4. เมื่อกระบวนการเสร็จสิ้น (ประมาณ 2-3 นาที):
   - สามารถดาวน์โหลดไฟล์ `Kotoba-v1.0.0-arm64.apk` จากแท็บ **Releases** บน GitHub ไปเปิดติดตั้งบนมือถือได้ทันที

### วิธีที่ 2: คอมไพล์ในเครื่อง Local (หากมี Android SDK & Java 21)
```bash
./gradlew assembleRelease
```
ไฟล์ APK จะถูกสร้างขึ้นที่ `app/build/outputs/apk/release/app-release.apk`
