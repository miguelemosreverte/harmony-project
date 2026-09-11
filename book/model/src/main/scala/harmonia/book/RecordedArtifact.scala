package harmonia.book

/** The finite, public evidence files belonging to a recording. */
enum RecordedArtifact(val filename: String, val label: String):
  case Input extends RecordedArtifact("input.md", "Input")
  case Expected extends RecordedArtifact("expected.md", "Expected")
  case Actual extends RecordedArtifact("actual.md", "Actual")
  case Diff extends RecordedArtifact("diff.md", "Diff")
  case Observations extends RecordedArtifact("observation.json", "Raw observations")
  case Provenance extends RecordedArtifact("run.json", "Execution provenance")

object RecordedArtifact:
  def read(value: String): Option[RecordedArtifact] = values.find(_.toString.toLowerCase == value)
