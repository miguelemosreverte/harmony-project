# What Harmonia adds

A workflow step identifies **a party and an eligible Daml action**. The Binding DAR declares which application choices can participate.

Core keeps progression on-ledger. **Application authorization still applies.** Assigning a step does not grant permission to execute its choice.

Results update the workflow. A **continuation** carries outputs into another workflow with its own parties.

> “without bespoke pairwise integration”
>
> “authority : required from app”

*Original proposal, abstract; original contract model, §2.*

This illustrates the proposed choice-execution model. Binding mechanics remain open; atomic execution depends on structure and authority.
