package harmonia.ledger.client

import com.daml.ledger.api.v2.ValueOuterClass.Value

/** A prepared choice; request delivery decides when it is safe to submit it. */
final case class LedgerExercise(contract: ActiveContract, choice: String, argument: Value)
