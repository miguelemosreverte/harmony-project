# One system, three views

## 1 · Location

The Dapp submits work and reads state. **Core and application contracts live on Canton.**

## 2 · Responsibility

Core holds workflow state and step rules. **Each application retains its contracts and authority.** Coordination must respect that authority.

## 3 · Participation

Applications supply their own integration, or **Builder generates bindings** for an existing DAR. Both join the same Core model.

The views establish the boundary, open it, then explain how applications join.

> “composition state stays on-ledger”
>
> “own authorization + ownership”

*Original architecture, §1. Proposed logical structure; detailed binding mechanics remain open.*
