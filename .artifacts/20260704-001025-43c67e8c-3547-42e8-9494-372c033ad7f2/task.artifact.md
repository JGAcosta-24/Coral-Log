# Task Management - Sprint 1: Room Database & Persistence

## Sprint 1 Goals
- Implement Room Database for offline-first storage of cycle and symptom data.
- Align with `CoralLog-rules.md` (Feature-based structure, naming conventions, Koin DI).
- Implement HU-02 (Registering bleeding, flow, and cramps).

## Todos
- [/] Research and Planning
	- [x] Analyze `CoralLog-rules.md`, `Coral Log Brief Agil v.1.1.md`, and `Coral Log ERS Agil v1.1.md`
	- [/] Design Database Schema (Entities & DAOs)
- [ ] Core Data Layer Implementation
	- [ ] Create `CycleEntity` and `SymptomEntity`
	- [ ] Create `CycleDao` and `SymptomDao`
	- [ ] Implement `AppDatabase` (Room)
	- [ ] Set up Koin module for Database and Repositories
- [ ] Feature: Calendar Persistence
	- [ ] Implement `CalendarRepository`
	- [ ] Update `CalendarViewModel` to interact with Repository
- [ ] UI Integration (HU-02)
	- [ ] Implement `ModalBottomSheet` for logging
	- [ ] Connect UI to ViewModel for persistent data entry
- [ ] Verification
	- [ ] Verify data persistence across app restarts
	- [ ] Run automated tests for DAOs
