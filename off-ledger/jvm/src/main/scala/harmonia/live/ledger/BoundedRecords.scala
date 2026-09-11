package harmonia.live.ledger

import com.google.protobuf.MessageLite

private[harmonia] object BoundedRecords:
  def read[A <: MessageLite](stream: java.util.Iterator[A]): Vector[A] =
    val values = Vector.newBuilder[A]
    var count = 0
    var bytes = 0L
    while stream.hasNext do
      val item = stream.next()
      count += 1; bytes += item.getSerializedSize
      require(
        count <= 512 && bytes <= 8L * 1024 * 1024,
        "Participant observation exceeds 512 records or 8 MiB; restart the bounded evaluation"
      )
      values += item
    values.result()
