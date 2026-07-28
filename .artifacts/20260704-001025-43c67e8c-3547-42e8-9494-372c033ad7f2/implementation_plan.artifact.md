# Implementation Plan - Sprint 1: Room Database & Persistence

Initialize the data layer for Coral Log to support 100% offline-first storage as per ERS and Project Rules.

## User Review Required

- **Schema Design**: I have confirmed that `date` (String or Long) will be the **Primary Key** for `SymptomEntity` as requested, ensuring only one log per day.
- **Package Structure**: I will reorganize the current `com.example.corallog` to `com.corallog.feature.calendar` for consistency with `CoralLog-rules.md`.

## Proposed Changes

### [Dependencies] - build.gradle.kts & libs.versions.toml

- Add Room (Database)
- Add Koin (DI)
- Add KSP (Annotation processing for Room)

### [Data Layer] - com.corallog.data

#### [NEW] [CycleEntity.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/data/CycleEntity.kt)
- Defines the `CycleEntity` with `startDate`, `endDate`, and `id`.

#### [NEW] [SymptomEntity.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/data/SymptomEntity.kt)
- Defines `SymptomEntity` with `date` (Primary Key), `isBleeding`, `flowLevel` (Int), and `crampIntensity` (Int).

#### [NEW] [AppDatabase.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/data/AppDatabase.kt)
- Room Database configuration and TypeConverters for Date/LocalDate.

---

### [Feature: Calendar] - com.corallog.feature.calendar

#### [NEW] [CalendarRepository.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/feature/calendar/CalendarRepository.kt)
- Coordinates data between Room and the ViewModel.

#### [NEW] [CalendarViewModel.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/feature/calendar/CalendarViewModel.kt)
- Manages UI State using `Sealed Interfaces` and `StateFlow`.
- Handles user interactions (logging symptoms).

#### [REFACTOR] [CalendarScreen.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/feature/calendar/CalendarScreen.kt)
- Porting current `MainActivity` UI code to a dedicated feature package.
- Implementing `ModalBottomSheet` for symptom logging.

---

### [DI] - com.corallog.di

#### [NEW] [KoinModules.kt](file:///C:/Users/17865/OneDrive/Documentos/Proyectos/Coral-Log/app/src/main/java/com/corallog/di/KoinModules.kt)
- Koin modules for Database, Repository, and ViewModels.

## Verification Plan

### Automated Tests
- `.\gradlew.bat :app:test`
- I will add a `SymptomDaoTest` to verify that symptoms are correctly saved and retrieved.

### Manual Verification
1. Open the app.
2. Select a day in the calendar.
3. Open the logging sheet and save flow/cramp data.
4. Kill the app and restart.
5. Verify that the selected day still shows the saved symptoms.
