# Scala organization for Harmonia

**Status:** Scala and the functional style are user requirements. The layout below is a proposal to implement incrementally.  
**Context:** [Product requirements and commit plan](../../PRD.md)

Harmonia's off-ledger application code will be Scala. Use Cats Effect with concrete `IO` for effectful operations, immutable data, named functions, and explicit dependencies. Organize the code by capability so a reader can follow one feature without searching across unrelated technical layers.

“Cats Effect” is the working interpretation of the spoken phrase “catch effects.” The on-ledger implementation remains Daml.

## 1. Language and runtime boundaries

| Part | Direction |
| --- | --- |
| Ledger contracts and generated binding contracts | Daml |
| Story runner, golden comparison, builder, package retrieval, API/server, and book generation | Scala on the JVM |
| Interactive book, workflow viewer, and composer | Scala compiled for the browser through Scala.js, subject to the early UI dependency check |
| Story inputs, expectations, and book prose | Human-readable Markdown and YAML |
| Page structure and presentation | HTML/CSS assets produced or used by the Scala application |

Scala 3 is the proposed default. Pin a compatible Scala, JDK, Cats Effect, and Canton client combination during the build baseline. Select the Scala.js/UI dependencies when the first interactive edition is built. Any dependency that requires JavaScript interoperation should have a small named Scala boundary; an alternative application language would be a change to the user's requirement.

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

This is a proposed source layout as the capabilities arrive:

```text
off-ledger/
  build.sbt
  project/

  shared/src/main/scala/harmonia/
    stories/
      Story.scala
      StoryResult.scala
      StoryDiff.scala
    workflows/
      WorkflowView.scala

  jvm/src/main/scala/harmonia/
    stories/
      read/
        ReadStory.scala
        StoryFormat.scala
      run/
        RunStory.scala
        RunStoryCommand.scala
        RunStoryError.scala
      compare/
        CompareResults.scala
    bindings/
      inspect/
        InspectPackage.scala
      generate/
        GenerateBinding.scala
        BindingMapping.scala
    workflows/
      inspect/
        InspectWorkflow.scala
      execute/
        ExecuteStep.scala
    ledger/
      LedgerSubmission.scala
      CantonSubmission.scala
    files/
      ArtifactFiles.scala
    app/
      Main.scala
      Resources.scala

  browser/src/main/scala/harmonia/
    stories/
      playback/
        StoryPlayback.scala
    workflows/
      view/
        WorkflowPage.scala
      compose/
        WorkflowComposer.scala
    app/
      Main.scala
```

The build defines the shared code for JVM and Scala.js consumption. It shares only the models and pure operations actually needed by both targets. JVM ledger clients and filesystem code remain in the JVM target. Scala.js supplies the browser compilation target; see its [official documentation](https://www.scala-js.org/doc/).

The tree illustrates ownership, not a requirement to create every folder at initialization. Start with the first runnable story and add slices when implementing their capabilities. Use one build initially; create additional build modules only when they enforce a useful dependency or platform boundary.

### Dependency rules

1. Shared models and pure functions have no dependency on JVM services, browser APIs, credentials, or application entry points.
2. A feature depends on its own model and the small external capabilities it uses.
3. Integration code adapts an external API to a named capability; SDK-specific types stay at that boundary where practical.
4. Application entry points load configuration, allocate resources, construct feature programs, and connect commands/routes.
5. Feature-to-feature dependencies use an explicit public function or result type and remain acyclic. Repeated dependencies may justify a small shared concept once its meaning is clear.

Create narrowly named shared packages when justified. Avoid general-purpose `Utils`, `Helpers`, `Common`, or `Manager` collections. Keep models, handlers, and validation near their feature rather than spreading them across repository-wide folders named after technical layers.

Interfaces are useful at actual boundaries, such as ledger submission or artifact storage. A function or concrete class is enough when there is only one small implementation and no meaningful boundary to express.

## 4. What a slice should read like

The following signatures illustrate the separation; they are design examples, not compiled implementation:

```scala
object CompareResults:
  def compare(expected: StoryResult, actual: StoryResult): StoryDiff

final class RunStory(ledger: StoryLedger, artifacts: StoryArtifacts):
  def run(story: Story): IO[ObservedRun]
```

`CompareResults` calculates a difference. `RunStory` performs the effects required to execute a story and retain its observed output. Its execution does not receive the expected result. A command connects those capabilities, passes the actual output to the comparator, writes the diff, and chooses the exit status.

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

The first implementation establishes the style through one small, complete example. Later slices should follow that working precedent rather than starting with a large scaffold.
