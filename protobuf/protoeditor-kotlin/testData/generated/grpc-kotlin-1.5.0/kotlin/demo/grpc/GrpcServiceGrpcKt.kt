package demo.grpc

import demo.grpc.GreeterGrpc.getServiceDescriptor
import io.grpc.CallOptions
import io.grpc.CallOptions.DEFAULT
import io.grpc.Channel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServerServiceDefinition.builder
import io.grpc.ServiceDescriptor
import io.grpc.Status.UNIMPLEMENTED
import io.grpc.StatusException
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.kotlin.AbstractCoroutineStub
import io.grpc.kotlin.ClientCalls.bidiStreamingRpc
import io.grpc.kotlin.ClientCalls.clientStreamingRpc
import io.grpc.kotlin.ClientCalls.serverStreamingRpc
import io.grpc.kotlin.ClientCalls.unaryRpc
import io.grpc.kotlin.ServerCalls.bidiStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.clientStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.serverStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.unaryServerMethodDefinition
import io.grpc.kotlin.StubFor
import kotlin.String
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

/**
 * Holder for Kotlin coroutine-based client and server APIs for grpcdemo.Greeter.
 */
public object GreeterGrpcKt {
  public const val SERVICE_NAME: String = GreeterGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = getServiceDescriptor()

  public val sayHelloMethod: MethodDescriptor<HelloRequest, HelloReply>
    @JvmStatic
    get() = GreeterGrpc.getSayHelloMethod()

  public val listRepliesMethod: MethodDescriptor<HelloRequest, HelloReply>
    @JvmStatic
    get() = GreeterGrpc.getListRepliesMethod()

  public val collectRepliesMethod: MethodDescriptor<HelloRequest, HelloReply>
    @JvmStatic
    get() = GreeterGrpc.getCollectRepliesMethod()

  public val chatMethod: MethodDescriptor<HelloRequest, HelloReply>
    @JvmStatic
    get() = GreeterGrpc.getChatMethod()

  /**
   * A stub for issuing RPCs to a(n) grpcdemo.Greeter service as suspending coroutines.
   */
  @StubFor(GreeterGrpc::class)
  public class GreeterCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<GreeterCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): GreeterCoroutineStub = GreeterCoroutineStub(channel, callOptions)

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun sayHello(request: HelloRequest, headers: Metadata = Metadata()): HelloReply = unaryRpc(
      channel,
      GreeterGrpc.getSayHelloMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][io.grpc.Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    public fun listReplies(request: HelloRequest, headers: Metadata = Metadata()): Flow<HelloReply> = serverStreamingRpc(
      channel,
      GreeterGrpc.getListRepliesMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * This function collects the [Flow] of requests.  If the server terminates the RPC
     * for any reason before collection of requests is complete, the collection of requests
     * will be cancelled.  If the collection of requests completes exceptionally for any other
     * reason, the RPC will be cancelled for that reason and this method will throw that
     * exception.
     *
     * @param requests A [Flow] of request messages.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun collectReplies(requests: Flow<HelloRequest>, headers: Metadata = Metadata()): HelloReply = clientStreamingRpc(
      channel,
      GreeterGrpc.getCollectRepliesMethod(),
      requests,
      callOptions,
      headers
    )

    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][io.grpc.Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    public fun chat(requests: Flow<HelloRequest>, headers: Metadata = Metadata()): Flow<HelloReply> = bidiStreamingRpc(
      channel,
      GreeterGrpc.getChatMethod(),
      requests,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the grpcdemo.Greeter service based on Kotlin coroutines.
   */
  public abstract class GreeterCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for grpcdemo.Greeter.SayHello.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun sayHello(request: HelloRequest): HelloReply = throw StatusException(UNIMPLEMENTED.withDescription("Method grpcdemo.Greeter.SayHello is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for grpcdemo.Greeter.ListReplies.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [io.grpc.Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open fun listReplies(request: HelloRequest): Flow<HelloReply> = throw StatusException(UNIMPLEMENTED.withDescription("Method grpcdemo.Greeter.ListReplies is unimplemented"))

    /**
     * Returns the response to an RPC for grpcdemo.Greeter.CollectReplies.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to collect
     *        it more than once.
     */
    public open suspend fun collectReplies(requests: Flow<HelloRequest>): HelloReply = throw StatusException(UNIMPLEMENTED.withDescription("Method grpcdemo.Greeter.CollectReplies is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for grpcdemo.Greeter.Chat.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [io.grpc.Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to collect
     *        it more than once.
     */
    public open fun chat(requests: Flow<HelloRequest>): Flow<HelloReply> = throw StatusException(UNIMPLEMENTED.withDescription("Method grpcdemo.Greeter.Chat is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = GreeterGrpc.getSayHelloMethod(),
      implementation = ::sayHello
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = GreeterGrpc.getListRepliesMethod(),
      implementation = ::listReplies
    ))
      .addMethod(clientStreamingServerMethodDefinition(
      context = this.context,
      descriptor = GreeterGrpc.getCollectRepliesMethod(),
      implementation = ::collectReplies
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = GreeterGrpc.getChatMethod(),
      implementation = ::chat
    )).build()
  }
}
