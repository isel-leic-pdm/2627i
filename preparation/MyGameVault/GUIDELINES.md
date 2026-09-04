# Project Guidelines: My Game Vault

This document defines the coding standards and architectural boundaries for the project. **AI agents must strictly adhere to these rules.**

## 1. Testing-First Mandate
* **Verification is non-negotiable.** No feature or logic change should be submitted without accompanying tests.
* **Unit Tests**: Required for all domain logic, math, and state transformations.
* **UI Tests**: Required for user interactions and critical UI states.
* **Naming Convention**:
    * **Unit Tests**: Use backticks with spaces to describe the scenario and outcome (e.g., `` `action when precondition expected outcome` ``).
    * **Instrumented Tests**: Use underscores (e.g., `action_whenPrecondition_expectedOutcome`) because Android's DEX format (prior to version 040) does not support spaces in method names.
* **Structure**: Tests follow the Arrange-Act-Assert pattern.
* **Resource Management**: Use JUnit Rules (e.g., `TestWatcher`) to manage setup and teardown of external resources (files, databases, dispatchers) to ensure test isolation and reliability.

## 2. Current Architectural Phase: Iteration 3 (Single Activity with Navigation 3)
To preserve the pedagogical value of this project, **do not introduce advanced patterns** until explicitly moved to the next iteration.

### Allowed Patterns:
* Standard Kotlin classes and basic functions.
* Simple Jetpack Compose @Composables.
* State management using `androidx.lifecycle.ViewModel` for **Application State** (Screen UI State).
* The ViewModel hosts the screen's state machine and exposes the current state as a `StateFlow` property.
* The UI observes the ViewModel's state using `collectAsStateWithLifecycle()`.
* State management using plain classes or `remember` for **Presentation State** (UI Element State).
* **Single Activity architecture**: The application uses one Activity as a host for all screens.
* **Navigation 3**: Screen management and transitions are handled using `androidx.navigation3`.
  * Navigation keys are defined as `sealed class` hierarchies annotated with `@Serializable`.
  * Screens are provided via a `NavDisplay` and an `entryProvider`.
  * Screen-specific ViewModels are scoped to the `NavEntry`.
* **Screen/View Pattern**: Every logical screen must be divided into two distinct layers to ensure a clean separation between interaction logic and UI layout.
    * **Screen Layer (`XScreen.kt`)**: A high-level @Composable that manages interactions with the `ViewModel`. It is responsible for:
        * Collecting state using `collectAsStateWithLifecycle()`.
        * Managing side effects (e.g., `LaunchedEffect` for initial data fetch).
        * Wiring UI events to ViewModel functions.
        * Passing the immutable state and simplified callbacks to the View layer.
    * **View Layer (`XScreenView.kt`)**: A pure, stateless @Composable that defines the UI layout. It must:
        * Receive its data as an immutable state object.
        * Expose simple lambda callbacks for all user interactions.
        * Have no knowledge of ViewModels or repositories.
        * Contain all internal helper composables (e.g., section headers, specialized list items) unless they are designed for project-wide reuse.
* Clear separation of concerns between UI and business logic.

### Prohibited Patterns (Do NOT use yet):
* **No Dependency Injection (Hilt/Koin)**: Use manual instantiation or basic factories.
* **No Legacy Navigation**: Do not add new Activities for screens or use manual Activity switching.

## 3. Coding Style
* Prefer functional transformations (`map`, `filter`) over imperative loops.
* Prefer immutable data structures over mutable ones.
* Compose: Use state hoisting to keep UI components simple and testable.
* All public-facing functions must have KDoc explaining their purpose.
* Use Kotlin coding conventions. 

## 4. Core Design Principles
* **Make Invalid States Unrepresentable**: Leverage the type system and visibility modifiers to ensure the system cannot enter an inconsistent state.
    * Use `internal` or `private` constructors for domain objects that require complex validation.
    * Use nested classes and scoping to tie related concepts together.
    * Use `init` blocks to enforce invariants at construction time.
    * Prefer returning existing valid states over throwing exceptions when a transition is invalid.
* **Minimize Visibility**: Every declaration (classes, functions, properties, composables) must use the most restrictive modifier that still satisfies its actual callers.
    * Default to `private` for anything used only within its own file.
    * Use `internal` for anything shared across files/packages that is never consumed outside the current Gradle module.
    * Reserve `public` for a module's real external API. This has two distinct meanings, and both count as "external":
        * The `core` module's domain API consumed by `app` — designed as an intentional contract, not narrowed to whatever the UI happens to call today.
        * Android framework entry points declared in the manifest (Activities, Services, BroadcastReceivers, ContentProviders) — the OS instantiates these via Intents, so they must stay public regardless of whether another in-module class also references them.

## 5. Logging and Observability
* **Tagging Convention**: Use a consistent tag for all log messages.
    * The tag must follow the pattern: `AppName.ComponentName` (e.g., `MyGameVault.IgdbSearchService`).
    * Use the `MyGameVaultApplication.buildTag()` function to generate these tags.
* **Message Format**: Every log message should start with the name of the function being executed, followed by a colon and the message (e.g., `Log.d(TAG, "search: started with query = \"$query\"")`).
* **Process Boundaries**: Log all interactions that cross process boundaries (e.g., network calls, database access once implemented).
