package harmonia.ledger.events

import cats.effect.{IO, Resource}
import com.daml.ledger.api.v2.{
  StateServiceOuterClass as State,
  UpdateServiceOuterClass as Updates,
  TransactionFilterOuterClass as Filter
}
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import io.grpc.{CallOptions, ManagedChannel, ManagedChannelBuilder, MethodDescriptor}
import io.grpc.protobuf.ProtoUtils
import io.grpc.stub.ClientCalls
import io.circe.Json
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

object LedgerEvents:
  def read(port: Int, party: String): IO[Vector[Json]] = channel(port).use { connection =>
    IO.interruptible {
      val options = CallOptions.DEFAULT.withDeadlineAfter(30, TimeUnit.SECONDS)
      val end = ClientCalls
        .blockingUnaryCall(
          connection,
          method(
            "StateService/GetLedgerEnd",
            MethodDescriptor.MethodType.UNARY,
            State.GetLedgerEndRequest.getDefaultInstance,
            State.GetLedgerEndResponse.getDefaultInstance
          ),
          options,
          State.GetLedgerEndRequest.getDefaultInstance
        )
        .getOffset
      val filter = Filter.EventFormat
        .newBuilder()
        .setVerbose(true)
        .putFiltersByParty(
          party,
          Filter.Filters
            .newBuilder()
            .addCumulative(
              Filter.CumulativeFilter
                .newBuilder()
                .setWildcardFilter(Filter.WildcardFilter.newBuilder())
            )
            .build()
        )
      val transaction = Filter.TransactionFormat
        .newBuilder()
        .setEventFormat(filter)
        .setTransactionShape(Filter.TransactionShape.TRANSACTION_SHAPE_LEDGER_EFFECTS)
      val request = Updates.GetUpdatesRequest
        .newBuilder()
        .setBeginExclusive(0L)
        .setEndInclusive(end)
        .setUpdateFormat(Filter.UpdateFormat.newBuilder().setIncludeTransactions(transaction))
        .build()
      val responses = ClientCalls.blockingServerStreamingCall(
        connection,
        method(
          "UpdateService/GetUpdates",
          MethodDescriptor.MethodType.SERVER_STREAMING,
          Updates.GetUpdatesRequest.getDefaultInstance,
          Updates.GetUpdatesResponse.getDefaultInstance
        ),
        options,
        request
      )
      responses.asScala.toVector.map(response =>
        io.circe.parser.parse(JsonFormat.printer().print(response)).fold(throw _, identity)
      )
    }
  }

  private def channel(port: Int): Resource[IO, ManagedChannel] = Resource.make(
    IO.blocking(ManagedChannelBuilder.forAddress("127.0.0.1", port).usePlaintext().build())
  )(connection =>
    IO.blocking { connection.shutdownNow(); connection.awaitTermination(5, TimeUnit.SECONDS); () }
  )

  private def method[A <: Message, B <: Message](
      name: String,
      kind: MethodDescriptor.MethodType,
      input: A,
      output: B
  ): MethodDescriptor[A, B] =
    MethodDescriptor
      .newBuilder[A, B]()
      .setType(kind)
      .setFullMethodName("com.daml.ledger.api.v2." + name)
      .setRequestMarshaller(ProtoUtils.marshaller(input))
      .setResponseMarshaller(ProtoUtils.marshaller(output))
      .build()
