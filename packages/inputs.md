# Reproducible application inputs

The local legacy application, primitive binding fixture, and upstream token metadata package are pinned independently. The metadata package is distributed by the Splice project under Apache-2.0; it supplies shared types for the package example, not a token transfer implementation. Its original compiler was a 3.3 snapshot, while its Daml-LF 2.1 format is imported using our pinned 3.4.11 compiler.

The URL includes the full upstream commit. The file digest identifies the archive bytes; the package ID identifies its compiled main package. `scripts/harmonia resolve-packages` verifies both and records all included dependency identities.

## Packages

```yaml
legacy-financing:
  source: on-ledger/applications/legacy-financing/.daml/dist/legacy-financing-0.1.0.dar
  sha256: b77e417dedc16733ef751b79b82df99b063819d514622b8b4e56d16f3ad8cf27
  package_id: b89073854cc6ab806ddc283646f794285684a2402d8301a127fb43270b6afbd2
  lf: "2.2"
metadata:
  source: https://raw.githubusercontent.com/canton-network/splice/4031327bc4fd63362ebd75f47f466c927c81e06e/daml/dars/splice-api-token-metadata-v1-1.0.0.dar
  sha256: 455eb160cb5abd4ae9918a6fbb9dad471f721adda39f0e5c76feef08d05637fc
  package_id: 4ded6b668cb3b64f7a88a30874cd41c75829f5e064b3fbbadf41ec7e8363354f
  lf: "2.1"
primitive-approval:
  source: on-ledger/fixtures/primitive-approval/.daml/dist/harmonia-binding-fixtures-0.1.0.dar
  sha256: 20be5fd4ec0de6fac4ae260c0e89a536c5f15dcc0e350165e6693c93b5132b2f
  package_id: 1c9e5ad40c4cea23c041f8f8b089e20785fa257ddbcedd90fd87b6d0937103f2
  lf: "2.2"
```

Source and license: [Splice metadata types at the pinned revision](https://github.com/canton-network/splice/blob/4031327bc4fd63362ebd75f47f466c927c81e06e/token-standard/splice-api-token-metadata-v1/daml/Splice/Api/Token/MetadataV1.daml), [Apache-2.0 license](https://github.com/canton-network/splice/blob/4031327bc4fd63362ebd75f47f466c927c81e06e/LICENSE).
