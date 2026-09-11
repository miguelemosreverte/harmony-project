# Troubleshooting

| Symptom | Next action |
| --- | --- |
| Java or sbt is missing | Install/select the documented Java 17 and sbt toolchain. On macOS the wrappers select an installed Java 17 when `JAVA_HOME` is unset. |
| Daml assistant or SDK is missing | Install SDK 3.4.11 through the assistant, or set `HARMONIA_DAML` to the installed assistant path. See [setup](setup.md). |
| The source fingerprint changed while compiling | Let the source edit finish and rerun the command. The launcher refused to run a mixture of build inputs. |
| A ledger environment is already running | Stop its owning terminal with Ctrl-C, then retry. Only one sandbox/live network is allowed per workspace. |
| Several Java processes remain | Identify each process's working directory and owner. Stop unused project previews/live sessions through their owning terminals. Keep one book preview; it does not need a ledger. |
| Memory rises during a check | Check that only the intended ledger environment is running. The standard limits are a 2 GiB Canton heap, 512 MiB tool/script heaps, and a 1 GiB compiler heap. Native JVM memory is additional. |
| A DAR digest or package ID differs | Inspect the named source/cache file and the committed pin. Restore the intended bytes or deliberately review a new identity. The resolver never silently selects a newer package. |
| An upstream package is unavailable | Restore access to its pinned URL, or use a verified local copy in a separate manifest with the same identities. |
| The builder says unsupported mapping | Inspection succeeded, but no reviewed executable mapping matches those bytes. Follow the [extension guide](extension-guide.md). Metadata types are not an executable action. |
| A live session is unavailable | Open the provisioned participant link for the currently running local service. Restarting the service creates new credentials and a new ledger. |
| A live request is stale | Refresh its current contract state, inspect the new eligibility, and submit a new request if appropriate. |
| A submission is uncertain or disconnected | Reconnect and reconcile the existing request ID. The UI's retry control reuses it. A timeout does not establish rollback or rejection. |
| A required contract is unavailable | Inspect disclosure, package identity, and current contract state. The UI cannot grant visibility or revive an archived contract. |
| A participant observation exceeds its bound | The bounded evaluation has outgrown its 512-record or 8 MiB query budget. Retain its evidence, stop that evaluation, and start a fresh one. |
| The book fails when opened as a local file | Serve its directory over local HTTP. Use `scripts/harmonia serve-book DIRECTORY` or the evaluation bundle's book launcher. |
| A chapter has no matching recording | This is a partial export. Export the complete evaluation recordings or run the missing check. The chapter will state which evidence is absent. |
| The golden differs | Read `diff.md`, then compare input, expected, actual, and raw observations. Fix the behavior or review an intentional expectation change; ordinary checks never bless a new baseline. |
| The full suite fails | Open the log/evidence path printed for the failing stage. Earlier passing stages are useful evidence, but they do not make the release gate pass. |

The [capability matrix](../docs/capabilities.md) describes tested limits and trust boundaries. The [progress record](../docs/history/progress-through-third.md) distinguishes completed local verification from public release and external acceptance.
