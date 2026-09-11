# Scala organization for Harmonia

**Status:** implemented with Scala 3.3.6, Cats Effect 3.6.3, JVM tools, and Scala.js 1.22.0. The functional style is a user requirement.  
**Context:** [Product requirements and commit plan](../../PRD.md)

Harmonia's off-ledger application code is Scala. Use Cats Effect with concrete `IO` for effectful operations, immutable data, named functions, and explicit dependencies. Organize the code by capability so a reader can follow one feature without searching across unrelated technical layers.

“Cats Effect” is the working interpretation of the spoken phrase “catch effects.” The on-ledger implementation remains Daml.

## 1. Language and runtime boundaries

| Part | Direction |
| --- | --- |
| Ledger contracts and generated binding contracts | Daml |
| Story runner, golden comparison, builder, package retrieval, API/server, and book generation | Scala on the JVM |
| Interactive book, workflow viewer, and composer | Scala compiled for the browser through Scala.js |
| Story inputs, expectations, and book prose | Human-readable Markdown and YAML |
| Page structure and presentation | HTML/CSS assets produced or used by the Scala application |

The checked toolchain uses Java 17 and Daml/Canton SDK 3.4.11. Browser DOM interoperation is confined to Scala.js presentation and request boundaries. Any dependency that requires JavaScript interoperation should have a small named Scala boundary; an alternative application language would be a change to the user's requirement.

The builder is Scala code that emits Daml code. The browser receives permitted observations and submits authorized requests; it does not become a second implementation of ledger execution rules.

## 2. Functional style

### Data and pure functions

- Represent domain values with immutable case classes, enums, and small types with meaningful names.
- Use `object`s for cohesive pure operations, constructors, and entry points. Objects must not open connections, read configuration, or start background work during initialization.
- Use small `final class` implementations when a capability needs dependencies supplied through its constructor.
- Keep validation, normalization, comparison, and presentation transformations pure wherever practical.
- Use `Either` or an explicit result type for expected invalid input and domain outcomes. An expected ledger rejection is a recorded outcome; inability to observe the ledger is an execution failure.
- Keep functions short enough to reveal their purpose. Split by responsibility rather than imposing a fixed line count.
- Prefer familiar Scala syntax, named intermediate values, and straightforward `for` comprehensions where they improve the reading order.

### Effects and resources

- Return `IO[A]` for filesystem access, network requests, ledger submissions, clocks, logging, and other effects.
- Prefer concrete `IO` in application code. Introduce an abstract effect type only when a real reuse requirement justifies it.
- Use `Resource[IO, A]` for owned clients, file handles, server lifecycles, subscriptions, and other resources requiring cleanup.
- Acquire resources and wire dependencies at the application entry point; release them on completion, failure, or cancellation.
- Keep pure functions pure. Do not wrap an ordinary deterministic comparison in `IO` simply for uniformity.
- Confine conversion from SDK callbacks, futures, or Java APIs to the relevant integration boundary. Defer effects and handle blocking calls appropriately.
- Use scoped concurrency with explicit ownership. Cancellation must stop observation work and release its resources; cancelling a client wait does not prove a submitted ledger transaction was rolled back.
- Run application effects through the managed runtime at the entry point, using `IOApp` on the JVM. Feature code composes effects without calling `unsafeRunSync` or launching unowned background work.

These conventions build on the [Cats Effect application model](https://typelevel.org/cats-effect/docs/getting-started) and its [resource lifecycle abstraction](https://typelevel.org/cats-effect/docs/std/resource).

## 3. Vertical slices

A slice is a user or developer capability: read a story, run it, compare its result, inspect a package, generate a binding, or execute an eligible workflow step.

Each slice owns its local input/error types, pure operations, effectful program, and command/HTTP entry adapter when needed. Tests mirror that package under `src/test/scala`. A change to one capability should normally lead the reader to one feature folder and its corresponding tests.

The implemented source ownership is:

```text
off-ledger/
  build.sbt, .jvmopts, project/    Pinned build and compiler resource budget
  shared/src/main/scala/harmonia/
    book/                        Recorded evidence model
    stories/compare/             Pure structural comparison shared by both targets
  jvm/src/main/scala/harmonia/
    app/                         CLI wiring
    stories/                     Typed story slices, reading, execution, observation
    bindings/                    Mapping model, LF shape inspection, typed generation
    packages/                    Source pins, acquisition, DAR inspection
    live/                        Restricted participant sessions, state, actions, HTTP
    composer/                    Plan model, commands, projections, golden verifier
    builder/                     Package input, compilation, portable project archive
    ledger/                      Runtime topology, authentication, events, lifecycle lease
    book/                        Chapter/evidence export and static playback server
    verification/                Cross-feature bounds and portable reference checks
    files/, processes/           Explicit I/O and owned child lifecycles
  browser/src/main/scala/harmonia/
    book/                        Chapters, recorded playback, feature-specific diagrams
    live/                        Session requests, uncertain-command recovery, rendering
    composer/                    Editor and actual consent/source-state presentation
    builder/                     Bounded package operation panel
```

The build defines the shared code for JVM and Scala.js consumption. It shares only the models and pure operations actually needed by both targets. JVM ledger clients and filesystem code remain in the JVM target. Scala.js supplies the browser compilation target; see its [official documentation](https://www.scala-js.org/doc/).

The JVM and browser remain two targets in one build. Add a folder when it owns a working feature; add a build module only when it enforces a useful dependency or platform boundary. Local mutation is confined to integration loops and DOM state where it bounds memory or preserves user input; domain values and transformations remain immutable.

### Dependency rules

1. Shared models and pure functions have no dependency on JVM services, browser APIs, credentials, or application entry points.
2. A feature depends on its own model and the small external capabilities it uses.
3. Integration code adapts an external API to a named capability; SDK-specific types stay at that boundary where practical.
4. Application entry points load configuration, allocate resources, construct feature programs, and connect commands/routes.
5. Feature-to-feature dependencies use an explicit public function or result type and remain acyclic. Repeated dependencies may justify a small shared concept once its meaning is clear.

Create narrowly named shared packages when justified. Avoid general-purpose `Utils`, `Helpers`, `Common`, or `Manager` collections. Keep models, handlers, and validation near their feature rather than spreading them across repository-wide folders named after technical layers.

Interfaces are useful at actual boundaries, such as ledger submission or artifact storage. A function or concrete class is enough when there is only one small implementation and no meaningful boundary to express.

## 4. What a slice should read like

These actual API shapes make the separation visible:

```scala
object CompareResults:
  def compare(expected: Json, actual: Json): Vector[Difference]

object LiveSnapshot:
  def read(ledger: ParticipantLedger, catalog: TemplateCatalog): IO[LiveSnapshot]
```

`CompareResults` calculates a difference from ordinary values. `LiveSnapshot.read` observes a restricted participant, filters supported package identities, and returns the current view. `LiveActions` owns submission/reconciliation; a read failure cannot become a business rejection. The story checkers read independent expectations, execute the input, then compare actual results without using the expectation to drive business actions.

The resource wiring remains visible in the application entry point. A reader should be able to identify which ledger identity is used, which artifacts are written, and who owns each resource without navigating a dependency-injection framework.

## 5. Review criteria

- [ ] The folder and primary function names tell the reader which capability they implement.
- [ ] Related input types, validation, program, and command/route adapters have clear feature ownership.
- [ ] Tests mirror the feature packages, and readable golden stories remain in the top-level story collection.
- [ ] Pure operations use ordinary values; effectful operations return `IO`.
- [ ] Dependencies enter explicitly and resource ownership is visible.
- [ ] SDK effects, blocking work, and concurrency are handled at defined boundaries.
- [ ] Invalid input, expected business rejection, observation failure, and cancellation retain distinct meanings.
- [ ] Shared code has a demonstrated consumer and an understandable name.
- [ ] A reader can follow the story from input through observed output to comparison without finding a second copy of its business rules.
- [ ] New abstractions make a concrete feature easier to read or maintain.

New slices should follow these working precedents and preserve the explicit pure/effect and resource boundaries.
