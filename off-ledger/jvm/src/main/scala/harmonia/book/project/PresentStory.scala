package harmonia.book.project

import harmonia.examples.ExampleKind
import harmonia.examples.ExampleKind.*
import io.circe.Json
import io.circe.syntax.*

/** Selects explanatory labels from actual observations. Expected values never choose an outcome. */
object PresentStory:
  def apply(kind: ExampleKind, input: Json, expected: Json, actual: Json): Json =
    val (subtitle, start, detail) = kind match
      case Boundaries =>
        (
          "Independent verification phases",
          "Empty local networks",
          "Core limits, then concurrent requests"
        )
      case Packages =>
        ("Inspect → verify → generate", "No package inputs", "Eight available input slots")
      case Composition =>
        ("Propose → consent → core execution", "Empty workspace", "Sources await partner consent")
      case Transfer =>
        ("Four-party atomic transfer", "proposed", "Trade and source position created")
      case other =>
        val subtitle = other match
          case Financing   => "Application action"
          case Workflow    => "Direct interface"
          case Adapter     => "Typed adapter"
          case Generated   => "Generated typed adapter"
          case Private     => "Signed result handoff"
          case Progression => "Versioned progression, branches, and joins"
          case Purchase    => "Financing → offer → agents"
          case _           => throw IllegalArgumentException("Unexpected ordinary presentation")
        (
          subtitle,
          required(input.hcursor.downField("setup").downField("application").focus.get, "status"),
          "Application created"
        )
    val operation = kind match
      case Financing | Workflow => "on-ledger/applications/financing/daml/Financing.daml"
      case Private => "off-ledger/jvm/src/main/scala/harmonia/financing/Financing.scala"
      case Adapter => "on-ledger/bindings/daml/FinancingBinding.daml"
      case Generated =>
        "off-ledger/jvm/src/main/scala/harmonia/bindings/generate/GenerateSources.scala"
      case Progression => "on-ledger/core/daml/Harmonia/Process/Engine.daml"
      case Purchase    => "on-ledger/applications/property-offer/daml/PropertyOffer.daml"
      case Transfer    => "on-ledger/applications/transfer/daml/AtomicTransfer.daml"
      case Composition => "on-ledger/composition/daml/Composer.daml"
      case Packages =>
        "off-ledger/jvm/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala"
      case Boundaries => "on-ledger/tests/daml/Boundaries.daml"
    val units =
      if kind == Boundaries then BoundaryPhases(expected, actual)
      else
        input.hcursor.get[Vector[Json]]("actions").fold(throw _, identity).map { action =>
          val id = required(action, "id")
          def observed(result: Json): Json = result.hcursor
            .get[Vector[Json]]("actions")
            .fold(throw _, identity)
            .find(_.hcursor.get[String]("id").contains(id))
            .getOrElse(Json.Null)
          val left = observed(expected)
          val right = observed(actual)
          unit(
            id,
            required(action, "actor"),
            required(action, "action"),
            left,
            right,
            observedState(kind, right),
            if kind == Packages then
              right.hcursor
                .get[Int]("http")
                .fold(_ => "Missing HTTP observation", code => s"HTTP $code")
            else right.hcursor.get[String]("outcome").getOrElse("Missing outcome")
          )
        }
    Json.obj(
      "kind" -> kind.asJson,
      "subtitle" -> Json.fromString(subtitle),
      "start" -> Json.fromString(start),
      "start_detail" -> Json.fromString(detail),
      "operation" -> Json.fromString(operation),
      "units" -> Json.arr(units*)
    )

  private def observedState(kind: ExampleKind, actual: Json): String = kind match
    case Purchase =>
      Vector("proposal", "offer", "application")
        .flatMap(k => actual.hcursor.get[String](k).toOption)
        .find(v => v != "none" && v != "not-opened")
        .getOrElse("Missing state")
    case Transfer    => actual.hcursor.get[String]("trade").getOrElse("Missing trade")
    case Composition => actual.hcursor.get[String]("workflow").getOrElse("Missing workflow")
    case Packages => actual.hcursor.get[Int]("inputs").fold(_ => "Missing count", n => s"$n inputs")
    case _        => actual.hcursor.get[String]("application").getOrElse("Missing application")

  private[project] def unit(
      id: String,
      actor: String,
      action: String,
      expected: Json,
      actual: Json,
      observed: String,
      outcome: String
  ): Json = Json.obj(
    "id" -> Json.fromString(id),
    "actor" -> Json.fromString(actor),
    "action" -> Json.fromString(action),
    "expected" -> expected,
    "actual" -> actual,
    "observed_state" -> Json.fromString(observed),
    "outcome_label" -> Json.fromString(outcome)
  )
  private def required(json: Json, name: String): String =
    json.hcursor.get[String](name).fold(throw _, identity)
