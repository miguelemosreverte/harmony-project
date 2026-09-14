# State and authority

The **Dapp** submits work to **Core**. On Canton, Core keeps workflow state and step rules. Applications keep their own contracts and authority.

An application participates directly, or **Builder** generates a binding for an existing DAR. **Binding DAR** declares eligible participation. Builder runs at build time.

## Read the contracts

A definition creates an instance, which binds roles to parties and assigns steps. A choice-exercise step reaches an application choice. Outputs can continue to another definition.

Atomic blocks require compatible workflow structure and authority.

## Why draw it?

The enclosure shows **where execution lives**. Grouping shows **who owns what**. The horizontal arrangement lets you see the relationships together, without reconstructing them sentence by sentence.

## From the original

> “composition state stays on-ledger”
>
> “authority : required from app”

*Original architecture, §1–2.* Imports, interface placement and the binding mechanism remain open.
