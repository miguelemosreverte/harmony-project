# Pinned DAR inputs

[inputs.md](inputs.md) is the committed source manifest. `scripts/build` resolves it before compiling the package example. The initial online fetch is a 260 KB DAR from the Splice repository at a full commit ID. Subsequent runs verify and reuse the local content cache.

```sh
scripts/harmonia resolve-packages
scripts/harmonia packages-check
```

The package check imports both pinned inputs, builds the [reference package](../on-ledger/package-example/daml/PackageExample.daml), executes its [input](../on-ledger/package-example/input.md) twice, and compares each observation with the [committed expectation](../on-ledger/package-example/expected.md). It also uploads the upstream DAR separately and downloads it through the participant administrator API, requiring an identical SHA-256 digest.

## What is recorded

The manifest records archive SHA-256, main package ID, source, and Daml-LF version. `.artifacts/packages/resolved.json` adds the complete included package inventory and compiler version. Cached archives live under their digest; `.artifacts/packages/inputs/` contains verified build inputs. Local files are checked again even when their content is cached. Resolving never changes a source archive or edits its manifest.

The validated combinations are local LF 2.2 and upstream LF 2.1 imported with SDK 3.4.11. Package inspection and the compiled example establish compatibility for these inputs; this does not imply that every package emitted by every older SDK is compatible.

## Other inputs and failures

A separate manifest may use a local DAR path or an HTTPS URL under `raw.githubusercontent.com` containing a full commit ID. Other source providers and mutable branch/tag URLs are outside this edition's resolver. Relative local paths are resolved from the project root.

```sh
scripts/harmonia resolve-packages path/to/inputs.md
```

A missing local file reports that its source package must be built. A missing remote file reports the HTTP status and pinned URL. Digest mismatches, unknown fields, unsupported LF versions, and mismatched package IDs fail without accepting a cache entry or selecting a replacement version. An existing corrupted cache fails explicitly; remove only the identified cache directory after inspecting the cause, then resolve again. To work offline, copy the verified pinned DAR to a local path and use a separate manifest retaining its digest and package ID.

Inputs are limited to 8 MiB compressed, 32 MiB expanded, 2,048 archive entries, and sixteen declared packages. The resolver never extracts archive paths into the workspace. These are reference-tool limits, not Canton platform limits.

## A package API is not a DAR download API

The Ledger API package service exposes compiled package payloads. It does not reproduce the original DAR ZIP, manifest, source files, or archive digest. Reconstructing a DAR would create a new archive identity and requires its dependencies; this resolver does not pretend that a single package payload is the original artifact.

The tested Canton 3.4.11 administrator path is `participant.dars.download(mainPackageId, destinationDirectory)`, backed by `PackageService.GetDar`. It can return a DAR that was uploaded to that participant. A package included only as a dependency need not have an independently uploaded DAR. The package check therefore uploads the metadata DAR itself before requesting it.

This path requires participant administration access. Ordinary business party credentials do not grant it. The eventual builder can accept the downloaded file through the same pinned-input checks; it will not expose an unrestricted administration proxy. Package presence also does not grant business authority or disclosure rights. See the [official package management reference](https://archived.docs.digitalasset.com/operate/3.4/howtos/operate/packages/packages.html) and [Daml dependency reference](https://archived.docs.digitalasset.com/build/3.4/reference/daml/packages.html).

## Typed integration

The [financing mapping](mappings/financing.md) and [primitive-argument mapping](mappings/primitive-approval.md) drive the typed adapter generator. Both compare with the same independently authored [approval expectation](mappings/approval-expected.md). Run `scripts/harmonia bindings-check`; see [Chapter 7](../book/07-generated-bindings.md) for the generated project and its limits.
