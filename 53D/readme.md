# [Moodle](https://2627moodle.isel.pt/course/view.php?id=11145)

# Outline

## Week 1 - 07/09/2026
### Subject: Course introduction and Android Development 101

#### Topic breakdown:
* Syllabus, teaching methodology and bibliography.
  * Evaluation
  * Resources
* The Activity component as the UI host
  * Basic lifecycle: onCreate, onStart, onStop and onDestroy
* Jetpack Compose (revisions from TDS course)
  * @Composable functions: the building blocks of the UI
  * Mental model: the UI as a function of state (state -> @Composable -> UI)
  * Stateless @Composable functions
    * Elementary UI elements: Text, Button, Image
    * Layouts: Column, Row, Box

#### For reference:
* [Download Android Studio & App Tools](https://developer.android.com/studio)
* [Android API Levels](https://apilevels.com/)
* [SDK Platform release notes | Android Developers](https://developer.android.com/studio/releases/platforms)
* [Coding Conventions | Kotlin Documentation](https://kotlinlang.org/docs/coding-conventions.html)
* [Thinking in Compose | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/mental-model)
* [Material components in Compose](https://developer.android.com/develop/ui/compose/components)
* [Compose Layout Basics](https://developer.android.com/develop/ui/compose/layouts/basics)
* [Preview your UI with composable previews | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/tooling/previews)


## Week 2 - 14/09/2026
### Subject: Building a UI with Jetpack Compose - Introduction

#### Topic breakdown:
* Architecting the UI: introduction
* Jetpack Compose
  * Stateless vs. stateful @Composables, `remember` and `mutableStateOf` 
  * State hoisting: lifting the state to the parent composable
* Architecting the UI: introduction

#### For reference:
* [Thinking in Compose | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/mental-model)
* [Material components in Compose](https://developer.android.com/develop/ui/compose/components)
* [Compose Layout Basics](https://developer.android.com/develop/ui/compose/layouts/basics)
* [Preview your UI with composable previews | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/tooling/previews)
* [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state#state-and-composition)
* [Architecting your Compose UI](https://developer.android.com/develop/ui/compose/architecture)
* [Guide to app architecture](https://developer.android.com/topic/architecture)


## Week 3 - 21/09/2026
### Subject: Building a UI with Jetpack Compose - Continuation

#### Topic breakdown:
* Architecting the UI: continuation
  * Immutability
  * View States

#### For reference:
* [State and Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/state#managing-state) 
* [Architecting your Compose UI | Jetpack Compose | Android Developers](https://developer.android.com/jetpack/compose/architecture)
* [Guide to application architecture](https://developer.android.com/jetpack/guide)
* [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing) 
* [Test apps on Android](https://developer.android.com/training/testing)


## Week 4 - 28/09/2026
### Subject: Building a UI with Jetpack Compose - Continuation

#### Topic breakdown:
* Automatic testing in Android
  * Unit tests with JUnit
  * Instrumented UI tests
  * Testing @Composable functions with Compose Testing
* The Activity component: continued
  * Lifecycle revisited: behavior on configuration changes (e.g. screen rotation)
  * Implications of configuration changes for state management
* Jetpack Compose: continued
  * Preservation of presentation state
    * `rememberSaveable`
    * `Parcelable` contract and `Parcelize` annotation
	* `Saver`
* Architectural considerations
  * Designing the UI as a state machine
  * Introduction to MVVM (Model-View-ViewModel) in Android
  * ViewModel component
    * Lifecycle: behavior on configuration changes
    * ViewModel and presentation state: host for the UI state machine

#### For reference:
* [State and Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/state#managing-state) 
* [Architecting your Compose UI | Jetpack Compose | Android Developers](https://developer.android.com/jetpack/compose/architecture)
* [Guide to application architecture](https://developer.android.com/jetpack/guide)
* [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing) 
* [Test apps on Android](https://developer.android.com/training/testing)
* [Save UI state in Compose | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/state-saving)
* [State and Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/state#restore-ui-state)
* [Parcelable implementation generator | Kotlin | Android Developers](https://developer.android.com/kotlin/parcelize) 
* [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
  * [View Model lifecycle](https://developer.android.com/topic/libraries/architecture/viewmodel#lifecycle)
  * [Use Kotlin Coroutines with lifecycle-aware components](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope)
* [ViewModelStore](https://developer.android.com/reference/androidx/lifecycle/ViewModelStore)
* [ViewModelStoreOwner](https://developer.android.com/reference/androidx/lifecycle/ViewModelStoreOwner)