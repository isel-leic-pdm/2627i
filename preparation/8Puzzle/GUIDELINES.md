# Project Guidelines: 8-Puzzle Demo

This document defines the coding standards and architectural boundaries for the project. **AI agents must strictly adhere to these rules.**

## 1. Testing-First Mandate
* **Verification is non-negotiable.** No feature or logic change should be submitted without accompanying tests.
* **Unit Tests**: Required for all domain logic, math, and state transformations.
* **UI Tests**: Required for user interactions and critical UI states.
* **Naming Convention**:
    * **Unit Tests**: Use backticks with spaces to describe the scenario and outcome (e.g., `` `action when precondition expected outcome` ``).
    * **Instrumented Tests**: Use underscores (e.g., `action_whenPrecondition_expectedOutcome`) because Android's DEX format (prior to version 040) does not support spaces in method names.
* **Structure**: Tests follow the Arrange-Act-Assert pattern.  

## 2. Current Architectural Phase: Iteration 1 (ViewModels)
To preserve the pedagogical value of this project, **do not introduce advanced patterns** until explicitly moved to the next iteration.

### Allowed Patterns:
* Standard Kotlin classes and basic functions.
* Simple Jetpack Compose @Composables.
* State management using `androidx.lifecycle.ViewModel` for **Application State** (Screen UI State).
* The ViewModel hosts the screen's state machine and exposes the current state as a `mutableStateOf` property.
* State management using plain classes or `remember` for **Presentation State** (UI Element State).
* The application contains a single Activity.
* Clear separation of concerns between UI and business logic.

### Prohibited Patterns (Do NOT use yet):
* **No Dependency Injection (Hilt/Koin)**: Use manual instantiation or basic factories.
* **No Data Persistence (Room)**: Use in-memory collections.
* **No Advanced Navigation**: Use simple conditional rendering for screen switching.

## 3. Coding Style
* Prefer functional transformations (`map`, `filter`) over imperative loops.
* Prefer immutable data structures over mutable ones.
* Compose: Use state hoisting to keep UI components simple and testable.
* All public-facing functions must have KDoc explaining their purpose.
* Use Kotlin coding conventions. 

## 4. Core Design Principles
* **Make Invalid States Unrepresentable**: Leverage the type system and visibility modifiers to ensure the system cannot enter an inconsistent state.
    * Use `internal` or `private` constructors for domain objects that require complex validation.
    * Use nested classes and scoping to tie related concepts together (e.g., `Board.Coordinate`).
    * Use `init` blocks to enforce invariants at construction time.
    * Prefer returning existing valid states over throwing exceptions when a transition is invalid (e.g., `Board.move`).
