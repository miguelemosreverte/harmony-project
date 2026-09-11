package harmonia.app

import harmonia.ledger.client.{ParticipantLedger, TemplateCatalog}
import java.nio.file.Path

/** Supplied connections; their owner decides how participants are provisioned. */
final case class Connections(
    ledgers: Map[String, ParticipantLedger],
    packageExports: Path,
    catalog: TemplateCatalog
):
  val parties: Map[String, String] = ledgers.map((name, ledger) => name -> ledger.party)
