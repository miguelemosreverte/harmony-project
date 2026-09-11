package harmonia.live

import com.google.protobuf.StringValue
import harmonia.live.ledger.BoundedRecords
import munit.FunSuite
import scala.jdk.CollectionConverters.*

class ObservationBoundsSuite extends FunSuite:
  test(
    "the 512th record is retained and a 513th record fails instead of returning truncated state"
  ) {
    val records = Vector.fill(512)(StringValue.of("observed"))
    assertEquals(BoundedRecords.read(records.iterator.asJava).size, 512)
    intercept[IllegalArgumentException](
      BoundedRecords.read((records :+ StringValue.of("extra")).iterator.asJava)
    )
  }
  test("protobuf payload bytes bound a small number of individually large records") {
    val record = StringValue.of("x" * (3 * 1024 * 1024))
    assertEquals(BoundedRecords.read(Vector(record, record).iterator.asJava).size, 2)
    intercept[IllegalArgumentException](
      BoundedRecords.read(Vector(record, record, record).iterator.asJava)
    )
  }
