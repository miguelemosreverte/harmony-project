# State and authority

The **Dapp** submits work to **Core**. Core holds workflow state on Canton; applications retain their contracts and authority.

At build time, **Builder** generates a **Binding DAR** for an existing application. The DAR declares eligible participation. Applications can also participate directly.

## Read the contracts

Definitions create instances with party bindings and assigned steps. Daml interfaces connect choice-exercise steps to application choices. Outputs can continue to another definition.

Atomic blocks require compatible workflow structure and authority.

## Why draw it?

The enclosure shows **where execution lives**. Grouping shows **who owns what**. Alignment reveals their relationships together.

> “composition state stays on-ledger”
>
> “authority : required from app”

*Original architecture, §1–2.* Imports, interface placement and the binding mechanism remain open.
