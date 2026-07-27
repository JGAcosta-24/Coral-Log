# Task Management - Sprint 1: Room Database & Persistence

## Sprint 1 Goals
- Implement Room Database for offline-first storage of cycle and symptom data.
- Align with `CoralLog-rules.md` (Feature-based structure, naming conventions, Koin DI).
- Implement HU-02 (Registering bleeding, flow, and cramps).

## Todos
- [x] Research and Planning
	- [x] Analyze `CoralLog-rules.md`, `Coral Log Brief Agil v.1.1.md`, and `Coral Log ERS Agil v1.1.md`
	- [x] Design Database Schema (Entities & DAOs)
- [x] Core Data Layer Implementation
	- [x] Create `CycleEntity` and `SymptomEntity`
	- [x] Create `CycleDao` and `SymptomDao`
	- [x] Implement `AppDatabase` (Room)
	- [x] Set up Koin module for Database and Repositories
- [/] Feature: Calendar Persistence
	- [x] Implement `CalendarRepository`
	- [ ] Update `CalendarViewModel` to interact with Repository
- [ ] UI Integration (HU-02)
	- [ ] Implement `ModalBottomSheet` for logging
	- [ ] Connect UI to ViewModel for persistent data entry
- [ ] Verification
	- [ ] Verify data persistence across app restarts
	- [ ] Run automated tests for DAOs
