# ADR 009: authenticated live sessions

Status: implemented for the local private-approval evaluation.

The live viewer uses the existing private application, signed result, and shared
progress contracts. It does not decide whether a business transition is valid.
Every active-contract query, history query, and command carries a JWT for one
participant user with `CanActAs` rights for exactly one party. The bank, buyer,
and observer run on separate participant nodes and a common synchronizer.

## Credentials and lifecycle

A disposable setup script allocates parties, creates the initially waiting
handoff, and provisions restricted users. Its bootstrap credentials use a
separate signing secret. The HTTP service receives only participant clients
bound to restricted users; it cannot choose an arbitrary user or `actAs` party.

The operator provisions a random capability link per session. The fragment is
removed from the address bar and retained in that tab's session storage. Requests
send the capability in an Authorization header. The server maps it to a fixed
participant client. It offers no identity-switch or credential-minting endpoint.
Recorded-book perspective switching remains a display operation in a separate
mode.

The [pinned SDK's authentication documentation](https://archived.docs.digitalasset.com/operate/3.4/howtos/secure/apis/jwt.html)
defines the test HMAC service and distinguishes it from production signing and
TLS configuration. This evaluation uses random per-node HMAC secrets, five-minute
JWTs refreshed for the same fixed user, loopback-only endpoints, and an in-memory
network. It is not a production identity provider. Generated credential files
are owner-readable only, under ignored local artifacts, outside the book export.
All participants, channels, the HTTP server, executor, and command supervisor
have explicit Cats Effect resource lifecycles.

## Commands and observations

The service accepts only a request ID, supported action, and observed contract
version. It selects the relevant contract from an authenticated current query.
The request cannot supply a party, contract identifier, or success flag. A version
is a hash of visible active contract identities. A changed version produces a
stale-view result before submission.

A command first receives a pending record. A ledger confirmation produces a
committed record. Definite permission, validation, and business failures produce
a rejected record. An unavailable transport or ambiguous completion produces a
disconnected record. Reconnecting queries real state and searches the party's
history for the stable command ID; absence of a matching event does not prove
that an uncertain command failed. Reusing an identical request ID returns its
existing result, and reusing it for different input is rejected. The browser
retains an unconfirmed request in session storage, scoped to its capability,
across reloads. It disables new actions until the request is observed or retried
with the same identifier and input. Pending feedback is drawn before the HTTP
request begins; a failed response cannot silently erase an uncertain submission.

The first release bounds the live interaction to private approval and
continuation, with at most 100 requests per running evaluation. State, history,
and duplicate-request records last for the running local service. Browser
reconnection is supported; durable process restart recovery is outside this
in-memory evaluation.

## Evidence

`evaluations/live-handoff/` contains readable setup/actions, independent business
expectations, and independent identity/reconnect expectations. The checker uses
separate HTTP capabilities and actual restricted Ledger API calls. Its business
observations use the same Markdown result format, comparison, and provenance as
the recorded stories. A direct request using the buyer's credentials to read or
act as the bank must fail at the Ledger API itself.
