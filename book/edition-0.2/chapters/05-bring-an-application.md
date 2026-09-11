# Connect your application

Erik maintains an existing financing application. He wants one eligible approval action to participate in a shared workflow while keeping that application's authority and rules.

## Choose how the application participates

| Path | What changes | What remains the application's responsibility |
| --- | --- | --- |
| Direct interface | The application implements the shared action interface | Its choices, authorization, and business rules |
| Generated adapter | A compiled binding connects a supported choice in an unchanged archive | The actual source choice and its authority |

## Make the mapping explicit

The integration needs an actor, a subject, an eligible choice, and a meaningful observation of its result. Package types can inform this mapping. They cannot decide its business meaning for Erik.

The adapter must compile against the actual package signatures and pass an independently committed input and expected result before anyone relies on it.

[Explore the integration screen](../application-builder.html). It walks through a sample inspection and mapping. It does not upload, compile, or install an archive.

## Where the current boundary lies

The existing generator supports specific consuming choices and primitive fields. An unsupported nested argument must produce a clear refusal. Downloading a package and installing an action into a live catalog are separate operations.

The next product proof is an independently developed application integrated without changing the shared core. See Evidence & limits for the existing implementation guide.
