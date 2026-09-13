"""Short authored exchanges. These explain diagrams; they are not execution evidence."""


def exchange(first, second, people=('developer', 'reviewer'), names=('Developer', 'Reviewer')):
    return dict(first=dict(portrait=people[0], name=names[0], text=first),
                second=dict(portrait=people[1], name=names[1], text=second))


CHAPTERS = {
    '01-product': exchange('My application makes its own decision.', 'I use the scoped result in the next application.', ('bank', 'alice'), ('Northbank', 'Alice')),
    '02-roles-and-trust': exchange('A request does not grant approval authority.', 'Each action still checks who may perform it.', ('bank', 'alice'), ('Northbank', 'Alice')),
    '05-bring-an-application': exchange('We connect a supported application choice.', 'The compiler checks the generated adapter.'),
    '06-compose-a-workflow': exchange('I propose the actions and their participants.', 'I review the exact plan before consenting.', ('bank', 'alice'), ('Bank', 'Buyer')),
    '07-evidence-and-boundaries': exchange('We commit the input and expected result.', 'Then compare them with the recorded execution.'),
    '08-release-and-adoption': exchange('The local reference has execution evidence.', 'Independent adoption still needs its own proof.'),
    '09-proposal-context': exchange('The original commitments remain unchanged.', 'Acceptance needs an explicit stakeholder decision.'),
    '10-context-and-references': exchange('The two supplied originals stay intact.', 'Their quotations lead to the relevant chapters.'),
}
SLICES = {
    'financing': exchange('The bank keeps the private application.', 'The buyer shares only the scoped result.', ('bank', 'alice'), ('Bank', 'Buyer')),
    'process': exchange('The core coordinates the next action.', 'Daml still enforces the application’s authority.'),
    'transfer': exchange('Preparation reserves the position first.', 'The final withdrawal and receipt are atomic.', ('bank', 'bank'), ('Source', 'Destination')),
    'composition': exchange('The plan names each action and participant.', 'Buyer consent comes before execution.', ('bank', 'alice'), ('Bank', 'Buyer')),
    'packages': exchange('An adapter connects a reviewed choice.', 'Compilation and live registration are separate.'),
    'book': exchange('The guide reads source and recorded evidence.', 'It gives the product no execution authority.'),
}
SOURCES = {
    'architecture-88': exchange('This is the original component proposal.', 'Build-time integration and runtime are separate.'),
    'architecture-318': exchange('These are the proposed contract relationships.', 'A proposal alone does not prove implementation.'),
}
RELATIONSHIPS = {
    'dependencies': exchange('These arrows come from package manifests.', 'An import is a dependency, not a runtime call.'),
    'execution': exchange('These handoffs were reviewed against the source.', 'The linked files show where each boundary lives.'),
}
