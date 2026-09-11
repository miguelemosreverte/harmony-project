package harmonia.live.ledger

import cats.effect.IO
import com.daml.ledger.api.v2.ValueOuterClass.Value
import io.circe.Json

trait ParticipantLedger:
  def party: String
  def user: String
  def active(readParty: String = party): IO[Vector[ActiveContract]]
  def events: IO[Vector[Json]]
  def exercise(
      contract: ActiveContract,
      choice: String,
      argument: Value,
      commandId: String,
      actAs: String = party
  ): IO[Json]
