"""Authored explanations, not inferred runtime graphs or claimed ledger observations."""


def path(caption, *steps):
    nodes = [dict(id=str(i), label=label, actor=actor, detail=detail, state='pending')
             for i, (label, actor, detail) in enumerate(steps)]
    return dict(title='', caption=caption, nodes=nodes,
                edges=[dict(from_=str(i), to=str(i+1), state='pending') for i in range(len(nodes)-1)])


DIAGRAMS = {
    '01-product': path('Authority stays with each application. Only the scoped result crosses the boundary.',
        ('Decide privately', 'Northbank', 'The financing application checks its own rules.'),
        ('Sign a result', 'Daml interface', 'Issuer, consumer, subject and continuation identify the permitted use.'),
        ('Continue the offer', 'Alice', 'The receiving application checks the result before acting.')),
    '02-roles-and-trust': path('The ledger checks authority again when the action executes.',
        ('Request financing', 'Alice', 'A request does not grant the authority to approve it.'),
        ('Assess the case', 'Northbank', 'Only the authorized bank may assess its private case.'),
        ('Use this approval', 'Named consumer', 'Another subject or continuation cannot reuse the result.')),
    '05-bring-an-application': path('A compiled adapter is an integration artifact. It is not automatically installed in the live catalog.',
        ('Inspect the DAR', 'Erik · application owner', 'Read the actual package signatures.'),
        ('Map one choice', 'Explicit binding', 'Name the actor, subject, arguments and result.'),
        ('Compile the adapter', 'Daml compiler', 'Unsupported types produce a refusal.'),
        ('Prove the handoff', 'Golden story', 'Compare the observed result with a committed expectation.')),
    '06-compose-a-workflow': path('Each participant acts under its own authority. The screen advances on observed ledger state.',
        ('Propose a plan', 'Bank', 'Name the steps and the participants who own them.'),
        ('Consent to the plan', 'Buyer', 'The agreed plan exists before execution begins.'),
        ('Perform each action', 'Assigned participant', 'The existing action interfaces enforce the plan.'),
        ('Observe completion', 'Participants', 'A pending request is never shown as success.')),
    '07-evidence-and-boundaries': path('Quotation coverage, execution evidence and adoption are separate claims.',
        ('Preserve the requirement', 'Original quotation', 'Every original unit has a chapter destination.'),
        ('Commit the expectation', 'Golden story', 'Input and expected result form the regression baseline.'),
        ('Record the observation', 'Canton execution', 'Compare the result and preserve its provenance.')),
    '08-release-and-adoption': path('Local behavior is demonstrated. Independent integration and adoption still need their own evidence.',
        ('Local reference', 'Demonstrated', 'Bounded workflows and supported adapter compilation.'),
        ('Independent application', 'Next evidence', 'A separate team integrates without changes to the shared core.'),
        ('External evaluation', 'Not established', 'An evaluator confirms that the integration is useful.'),
        ('Adoption', 'Not established', 'A participating organization confirms real usage.')),
    '09-proposal-context': path('The supplied commitments remain original statements, not revised estimates or accepted delivery claims.',
        ('Original commitment', 'Proposal', 'Scope, effort and commercial context remain preserved.'),
        ('Current evidence', 'Repository', 'Executable observations establish bounded behavior.'),
        ('Acceptance decision', 'Stakeholders', 'Delivery and commercial acceptance need explicit decisions.')),
    '10-context-and-references': path('Our explanations redraw the architecture. The supplied source files remain unchanged.',
        ('Two supplied documents', 'Pinned originals', 'The downloaded Markdown and HTML define quotation coverage.'),
        ('Shared infographic language', 'This field guide', 'Diagrams explain the implementation in the same visual style.'),
        ('External references', 'Outside the denominator', 'Linked publications and four absent attachments are identified.')),
}
# Python reserves `from`; the presentation schema does not.
for diagram in DIAGRAMS.values():
    for edge in diagram['edges']:
        edge['from'] = edge.pop('from_')
