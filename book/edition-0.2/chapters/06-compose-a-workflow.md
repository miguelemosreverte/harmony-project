# Do the next task

Nina is reviewing a financing request at the bank. She needs to know what she is looking at, whether the decision belongs to her, and what will happen when she submits it.

## The workspace starts with the current responsibility

The task names the workflow, the person who can act, and the source application that owns the decision. Supporting details stay available without competing with the next action.

When Nina approves the sample financing, the task moves to the buyer. When she refuses it, the sample stops before an offer is created. A waiting participant sees who must act next.

[Try the workspace simulation](../application.html). Start as the bank, make a decision, then switch to the buyer to follow the handoff. Actor switching is a design aid, not authentication.

## What should happen when something goes wrong?

A pending submission disables another command. A lost connection makes the lack of a current observation visible. A refused action leaves progress unchanged. Stale state asks for a new observation before retrying.

The prototype exposes these states under **Explore connection states**, so the ordinary task stays focused.

## How this relates to the product

The underlying core includes more composition behavior than this sequential screen. Branches, joins, and continuation still need their supported live views. The simple task screen is not a claim that every proposed composer feature is implemented.
