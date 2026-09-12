"""Redraw the two supplied architectural proposals; these are not execution evidence."""


def diagram(title, caption, nodes, edges):
    return dict(title=title, caption=caption,
                nodes=[dict(id=id, label=label, actor=actor, detail=detail, state='pending')
                       for id, label, actor, detail in nodes],
                edges=[{'from':a, 'to':b, 'state':'pending'} for a,b in edges])


DIAGRAMS = {
    'architecture-88': diagram('Proposed component boundaries',
        'Runtime: dapp → core → application. Build time: package source → builder → binding → application. The external package manager is illustrative.',
        [
            ('dapp', 'harmonia-dapp', 'Off ledger · runtime', 'Viewer, composer, execution views and transaction submission.'),
            ('packages', 'Package source', 'External · illustrative', 'Provides DARs and package metadata; no external integration is implied.'),
            ('core', 'harmonia-core', 'On ledger · orchestration', 'Daml owns workflow state and the shared action interfaces.'),
            ('builder', 'harmonia-builder', 'Off ledger · build time', 'Inspects packages and generates a binding project.'),
            ('binding', 'Binding DAR', 'Generated integration', 'Connects a supported choice in an unchanged source application.'),
            ('application', 'Source application DAR', 'On ledger · business authority', 'The application retains its choices and authorization.')
        ],
        [('dapp','core'),('core','application'),('packages','builder'),('builder','binding'),('binding','application')]),
    'architecture-318': diagram('Proposed contract relationships',
        'The supplied model names these templates, interfaces and data. It does not establish that every proposed construct exists in the current implementation.',
        [
            ('definition', 'WorkflowDefinition', 'Template · proposed core', 'Defines steps, branches, joins and bounded atomic blocks.'),
            ('roles', 'RoleBinding', 'Data · role and party', 'Associates a party with its permitted steps.'),
            ('instance', 'WorkflowInstance', 'Template · workflow state', 'Instantiates a definition with roles and observed progress.'),
            ('step', 'StepAssignment', 'Template · assigned actor', 'The actor executes an application choice with the required authority.'),
            ('continuation', 'Continuation', 'Template · domain handoff', 'Carries outputs from a producer instance to a consumer definition.'),
            ('interface', 'HarmoniaStepAction', 'Interface · executeStep', 'Describes the shared action view and operation.'),
            ('application', 'AppTemplate', 'Source application', 'Implements the interface directly or is connected by a generated binding.'),
            ('applies', 'AppliesTo', 'Data · binding declaration', 'Identifies the template, choice, step kind and role.')
        ],
        [('definition','instance'),('roles','instance'),('instance','step'),('instance','continuation'),('step','interface'),('interface','application'),('applies','application')])
}
