# 51-52D course section's materials (by Prof. Paulo Pereira)

## Lectures
* [Live on Twitch](https://www.twitch.tv/paulo_pereira)
* [Playlist on YouTube](https://www.youtube.com/@ProfPauloPereira)

## Outline

### Week 1 (07/09/2026) - Course introduction

* Syllabus, teaching methodology and bibliography
  * Evaluation
  * Resources

* Conversation about the course's methodology and the challenges in the Generative AI era
  * Software Qualities (internal and external)
    * Why they matter and to whom they matter
  * The increasing relevance of Software Design and Software Engineering practices 

* Preparation of the [development environment (Android Studio)](https://developer.android.com/studio/install)
  * Android Studio installation and configuration
  * Android Emulator installation and configuration
  * Using a physical device for development and testing

##### Some recommended books on software design and software engineering:
* [Seriously Good Software, by Marco Faella](https://www.manning.com/books/seriously-good-software) (Introductory)
* [Code that Fits in Your Head, by Mark Seemann](https://www.oreilly.com/library/view/code-that-fits/9780137464302/) (Intermediate)
* [A Philosophy of Software Design, by John Ousterhout](https://www.amazon.com/dp/173210221X) (Intermediate)
* [Release It!, by Michael T. Nygard](https://pragprog.com/titles/mnee2/release-it-second-edition/) (Intermediate to Professional)
* [Domain-Driven Design, by Eric Evans](https://books.google.pt/books/about/Domain_driven_Design.html?id=7dlaMs0SECsC&redir_esc=y) (Professional)

### Week 2 (14/09/2026) - Introduction to Android development
* Android application development: introduction
  * [Inversion of Control](https://martinfowler.com/bliki/InversionOfControl.html)
  * Anatomy of an Android application
* Activity
  * Android component for hosting the UI
  * Lifecycle (introduction)
* Building a UI with Jetpack Compose (revision)
  * Composable functions (State → @Composable → UI)
  * Elementary composables (e.g., `Text`, `Button`, etc.)
  * Layouts (e.g., `Row`, `Column`, `Box`, etc.)
  * Stateless and Stateful composables
  * Primitives for _view state_ management (`remember` and `mutableStateOf`)
* Software Design considerations
  * The value of immutability
  * Separation of concerns (domain vs UI)

##### Resources:
* On Android and Compose:
  * [Thinking in Compose](https://developer.android.com/develop/ui/compose/mental-model)
  * [Compose layout basics](https://developer.android.com/jetpack/compose/layouts/basics)
  * [Testing your Compose layout](https://developer.android.com/jetpack/compose/testing)
  * [Preview your UI with composable previews](https://developer.android.com/develop/ui/compose/tooling/previews)
* On Software Design and Engineering:
  * [The Value of Values, by Rich Hickey](https://www.youtube.com/watch?v=-I-VpPMzG7c) (video)
  * [Immutability we can afford. A look at modern programming languages, by Roman Elizarov](https://elizarov.medium.com/immutability-we-can-afford-10c0dcb8351d)
  * [From Primitive Obsession to Domain Modelling](https://blog.ploeh.dk/2015/01/19/from-primitive-obsession-to-domain-modelling/)
* Videos from the Prodigi course on Mobile Device Programming (in Portuguese)
  * [Prodigi - Curso de PDM - M05 - Compose (Introdução)](https://www.youtube.com/watch?v=qAo7WkwewZ0&list=PL8XxoCaL3dBhS-THuPazx0BSAswof6wqh&index=6)
  * [Prodigi - Curso de PDM - M06 - Compose Layouts](https://www.youtube.com/watch?v=IWumBm4olEs&list=PL8XxoCaL3dBhS-THuPazx0BSAswof6wqh&index=7)
  * [Prodigi - Curso de PDM - M07 - Compose (Estado)](https://www.youtube.com/watch?v=fR_o9v40Co4)
* Lectures videos from Prof. Paulo Pereira (in Portuguese)
  * [Aula 01 (14/09/2026)](https://www.youtube.com/watch?v=IkWP-G6ptZ0&list=PLX4G3J1kqg4U&index=1)
  * [Aula 02 (18/09/2026)](https://www.youtube.com/watch?v=yYw6U-0EArc&list=PLX4G3J1kqg4U&index=2)

### Week 3 (21/09/2026) - Buiding a UI in Android: state management
* State management in Android applications
  * Presentation state (a.k.a. view state) vs application state
* Building a UI with Jetpack Compose: state management
  * Stateless and Stateful composables
  * Primitives for _view state_ management (`remember` and `mutableStateOf`), revisited
* Design considerations for state management
  * State hoisting
  * Designing the UI as a state machine: 
    * The UI as a function of the state
    * The UI as a state machine
* The MVVM pattern (Model-View-ViewModel) in Android applications
  * `ViewModel` class
    * Purpose and applicability
    * Life cycle and relationship with the Activity
      * ViewModelStore and ViewModelStoreOwner
  * Design considerations for state management, continued
    * ViewModel while 
      * Gateway to the domain model
      * Local application state container to the screen
      * Host of the execution of use case
    * Screen as a visual materialization of the state contained in the view model
* Automated testing on Android
  * Automated tests for the domain model
  * Automated UI Testing (Introduction)

##### Resources:
* On Android and Compose:
  * [Architecting your Compose UI](https://developer.android.com/develop/ui/compose/architecture)
  * [State in Compose](https://developer.android.com/jetpack/compose/state)
  * [State Hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting)
  * [ViewModel overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
  * [ViewModel lifecycle](https://developer.android.com/topic/libraries/architecture/viewmodel#lifecycle)
  * [Testing your Compose layout](https://developer.android.com/jetpack/compose/testing)
* Videos from the Prodigi course on Mobile Device Programming (in Portuguese)
  * [Prodigi - Curso de PDM - M04 - Activity](https://www.youtube.com/watch?v=2DGGvxAqNj4&list=PL8XxoCaL3dBhS-THuPazx0BSAswof6wqh&index=5)
  * [Prodigi - Curso de PDM - M09 - ViewModel](https://www.youtube.com/watch?v=9vYmkeg72vg&list=PL8XxoCaL3dBhS-THuPazx0BSAswof6wqh&index=10)
* Lectures videos from Prof. Paulo Pereira (in Portuguese)
  * [Aula 03 (21/09/2026)](https://www.youtube.com/watch?v=L8VyU_gUitE&list=PLX4G3J1kqg4U&index=3)
  * [Aula 04 parte 1](https://www.youtube.com/watch?v=vi3Op-yXoso&list=PLX4G3J1kqg4U&index=4) e [Aula 04 parte 2](https://www.youtube.com/watch?v=Poaa_8qwg3k&list=PLX4G3J1kqg4U&index=5) (25/09/2026)

### Week 4 (28/09/2026) - Building a UI in Android: concurrency model (introduction)
* Android concurrency model
  * Implementation of change notification methods in the lifecycle
  * Execution in handlers of events
* Kotlin Concurrency Model (revisions)
  * Thread vs coroutine
  * Suspending functions, coroutines, scopes and dispatchers
* ViewModel, revisited
  * ViewModel as the host of the execution: viewModelScope
* Architecting the UI
  * Support for multiple orientations: LocalConfiguration.current.orientation
* Application resources, introduction
  * Multilingual text

#### Resources
* [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
* [ViewModel overview | Android Developers](https://developer.android.com/topic/libraries/architecture/viewmodel)
  * [Use Kotlin coroutines with lifecycle-aware components | Android Developers](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope) 
* [Prodigi - Curso de PDM - M08 - Modelo de Concorrência](https://www.youtube.com/watch?v=QShOCxQc6oI&list=PL8XxoCaL3dBhS-THuPazx0BSAswof6wqh&index=9)
* [Application resources | Android Developers](https://developer.android.com/guide/topics/resources/providing-resources)
* Lectures videos from Prof. Paulo Pereira (in Portuguese)
  * Aula 05 (28/09/2026)
  * Aula 06 (02/10/2026)

### Week 5 (05/10/2026) - Monitoring of the course's assignment
* Students will need to record a video of up to 7 minutes presenting their application.
* The presentation includes the indication
  * of the features already implemented
  * of the main decisions taken
  * of the strategy for verifying correctness
  * planning the execution of the missing tasks
  
