package harmonia.packages

import harmonia.packages.read.PackageManifest
import munit.FunSuite

class PackageManifestSuite extends FunSuite:
  private val commit = "1" * 40
  private val digest = "a" * 64
  private val url = s"https://raw.githubusercontent.com/owner/project/$commit/example.dar"
  private val manifest = s"""# A pinned package
                            |## Packages
                            |```yaml
                            |application:
                            |  source: $url
                            |  sha256: $digest
                            |  package_id: $digest
                            |  lf: "2.1"
                            |```
                            |""".stripMargin

  test("accept an immutable URL and the two proven LF versions") {
    assert(PackageManifest.read(manifest).isRight)
    assert(PackageManifest.read(manifest.replace("2.1", "2.2")).isRight)
  }
  test("reject mutable source references and unsupported versions") {
    assert(PackageManifest.read(manifest.replace(commit, "main")).isLeft)
    assert(PackageManifest.read(manifest.replace("2.1", "99.0")).isLeft)
  }
  test("reject credentials, redirects to arbitrary hosts, and incomplete identities") {
    assert(PackageManifest.validateSource(url.replace("https://", "https://secret@")).isLeft)
    assert(
      PackageManifest.validateSource(url.replace("raw.githubusercontent.com", "example.org")).isLeft
    )
    assert(PackageManifest.read(manifest.replace(digest, "short")).isLeft)
  }
  test("reject unknown package fields and non-DAR local inputs") {
    assert(PackageManifest.read(manifest.replace("  lf:", "  extra: true\n  lf:")).isLeft)
    assert(PackageManifest.validateSource("source.zip").isLeft)
    assert(PackageManifest.validateSource("on-ledger/app.dar").isRight)
  }
