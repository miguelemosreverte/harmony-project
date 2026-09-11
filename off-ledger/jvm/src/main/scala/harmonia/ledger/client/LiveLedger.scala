package harmonia.ledger.client

import cats.effect.{IO, Resource}
import com.daml.ledger.api.v2.{
  StateServiceOuterClass as State,
  UpdateServiceOuterClass as Updates,
  TransactionFilterOuterClass as Filter,
  CommandServiceOuterClass as Submit,
  CommandsOuterClass as Commands,
  ValueOuterClass as Value
}
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import io.grpc.{
  CallOptions,
  Channel,
  ClientInterceptors,
  ManagedChannel,
  ManagedChannelBuilder,
  Metadata,
  MethodDescriptor
}
import io.grpc.protobuf.ProtoUtils
import io.grpc.stub.{ClientCalls, MetadataUtils}
import io.circe.Json
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

final class LiveLedger private (
    connection: ManagedChannel,
    val party: String,
    val user: String,
    token: IO[String]
) extends ParticipantLedger:
  private def authenticated[A](f: Channel => A): IO[A] = token.flatMap { bearer =>
    IO.interruptible {
      val headers = new Metadata()
      headers.put(
        Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
        "Bearer " + bearer
      )
      val context = io.grpc.Context.current().withCancellation()
      try
        context.call(new java.util.concurrent.Callable[A] {
          def call(): A = f(
            ClientInterceptors
              .intercept(connection, MetadataUtils.newAttachHeadersInterceptor(headers))
          )
        })
      finally context.cancel(null)
    }
  }
  private def options = CallOptions.DEFAULT.withDeadlineAfter(20, TimeUnit.SECONDS)
  private def filter(readParty: String) = Filter.EventFormat
    .newBuilder()
    .setVerbose(true)
    .putFiltersByParty(
      readParty,
      Filter.Filters
        .newBuilder()
        .addCumulative(
          Filter.CumulativeFilter.newBuilder().setWildcardFilter(Filter.WildcardFilter.newBuilder())
        )
        .build()
    )
  private def format = Filter.TransactionFormat
    .newBuilder()
    .setEventFormat(filter(party))
    .setTransactionShape(Filter.TransactionShape.TRANSACTION_SHAPE_LEDGER_EFFECTS)
  private def end(channel: Channel): Long = ClientCalls
    .blockingUnaryCall(
      channel,
      LiveLedger.method(
        "StateService/GetLedgerEnd",
        false,
        State.GetLedgerEndRequest.getDefaultInstance,
        State.GetLedgerEndResponse.getDefaultInstance
      ),
      options,
      State.GetLedgerEndRequest.getDefaultInstance
    )
    .getOffset

  def active(readParty: String = party): IO[Vector[ActiveContract]] = authenticated { channel =>
    val request = State.GetActiveContractsRequest
      .newBuilder()
      .setActiveAtOffset(end(channel))
      .setEventFormat(filter(readParty))
      .build()
    BoundedRecords
      .read(
        ClientCalls
          .blockingServerStreamingCall(
            channel,
            LiveLedger.method(
              "StateService/GetActiveContracts",
              true,
              State.GetActiveContractsRequest.getDefaultInstance,
              State.GetActiveContractsResponse.getDefaultInstance
            ),
            options,
            request
          )
      )
      .filter(_.hasActiveContract)
      .map { response =>
        val event = response.getActiveContract.getCreatedEvent
        val fields = event.getCreateArguments.getFieldsList.asScala.map { field =>
          field.getLabel -> LiveLedger.json(field.getValue)
        }.toMap
        ActiveContract(event.getContractId, event.getTemplateId, fields)
      }
  }

  def events: IO[Vector[Json]] = authenticated { channel =>
    val request = Updates.GetUpdatesRequest
      .newBuilder()
      .setBeginExclusive(0L)
      .setEndInclusive(end(channel))
      .setUpdateFormat(Filter.UpdateFormat.newBuilder().setIncludeTransactions(format))
      .build()
    BoundedRecords
      .read(
        ClientCalls
          .blockingServerStreamingCall(
            channel,
            LiveLedger.method(
              "UpdateService/GetUpdates",
              true,
              Updates.GetUpdatesRequest.getDefaultInstance,
              Updates.GetUpdatesResponse.getDefaultInstance
            ),
            options,
            request
          )
      )
      .map(LiveLedger.json)
  }

  def exercise(
      contract: ActiveContract,
      choice: String,
      argument: Value.Value,
      commandId: String,
      actAs: String = party
  ): IO[Json] = authenticated { channel =>
    val command = Commands.Command
      .newBuilder()
      .setExercise(
        Commands.ExerciseCommand
          .newBuilder()
          .setTemplateId(contract.template)
          .setContractId(contract.id)
          .setChoice(choice)
          .setChoiceArgument(argument)
      )
    val request = Submit.SubmitAndWaitForTransactionRequest
      .newBuilder()
      .setCommands(
        Commands.Commands
          .newBuilder()
          .setUserId(user)
          .setCommandId(commandId)
          .addActAs(actAs)
          .setDeduplicationDuration(com.google.protobuf.Duration.newBuilder().setSeconds(120))
          .addCommands(command)
      )
      .setTransactionFormat(format)
      .build()
    LiveLedger.json(
      ClientCalls.blockingUnaryCall(
        channel,
        LiveLedger.method(
          "CommandService/SubmitAndWaitForTransaction",
          false,
          Submit.SubmitAndWaitForTransactionRequest.getDefaultInstance,
          Submit.SubmitAndWaitForTransactionResponse.getDefaultInstance
        ),
        options,
        request
      )
    )
  }

object LiveLedger:
  def resource(
      port: Int,
      party: String,
      user: String,
      token: IO[String]
  ): Resource[IO, LiveLedger] = Resource
    .make(
      IO.blocking(ManagedChannelBuilder.forAddress("127.0.0.1", port).usePlaintext().build())
    )(c => IO.blocking { c.shutdownNow(); c.awaitTermination(5, TimeUnit.SECONDS); () })
    .map(new LiveLedger(_, party, user, token))
  val emptyArgument: Value.Value =
    Value.Value.newBuilder().setRecord(Value.Record.newBuilder()).build()
  def continuation(proof: Option[ActiveContract]): Value.Value =
    val optional = Value.Optional.newBuilder()
    proof.foreach(p => optional.setValue(Value.Value.newBuilder().setContractId(p.id)))
    Value.Value
      .newBuilder()
      .setRecord(
        Value.Record
          .newBuilder()
          .addFields(
            Value.RecordField
              .newBuilder()
              .setLabel("result")
              .setValue(Value.Value.newBuilder().setOptional(optional))
          )
      )
      .build()
  private def json(value: Message): Json =
    io.circe.parser.parse(JsonFormat.printer().print(value)).fold(throw _, identity)
  private def method[A <: Message, B <: Message](
      name: String,
      stream: Boolean,
      input: A,
      output: B
  ): MethodDescriptor[A, B] =
    MethodDescriptor
      .newBuilder[A, B]()
      .setType(
        if stream then MethodDescriptor.MethodType.SERVER_STREAMING
        else MethodDescriptor.MethodType.UNARY
      )
      .setFullMethodName("com.daml.ledger.api.v2." + name)
      .setRequestMarshaller(ProtoUtils.marshaller(input))
      .setResponseMarshaller(ProtoUtils.marshaller(output))
      .build()
