package com.jetbrains.lang.dart.pubServer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import icons.DartIcons;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCounted;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.NetService;
import org.jetbrains.io.*;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PubServerService extends NetService {
  private static final AttributeKey<Channel> SERVER_CHANNEL_KEY = AttributeKey.valueOf(PubServerService.class, "serverChannel");

  private final Bootstrap bootstrap;
  private final ConcurrentMap<Channel, ChannelHandlerContext> serverToClientContext = ContainerUtil.newConcurrentMap();
  private final ConcurrentHashSet<Channel> freeServerChannels = new ConcurrentHashSet<Channel>();
  private final ChannelInboundHandlerAdapter clientChannelStateHandler = new ClientChannelStateHandler();
  private final ChannelRegistrar serverChannelRegistrar = new ChannelRegistrar();

  private volatile SocketAddress serverAddress;

  private final ChannelFutureListener serverChannelCloseListener = new ChannelFutureListener() {
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
      Channel channel = future.channel();
      freeServerChannels.remove(channel);

      ChannelHandlerContext clientContext = serverToClientContext.remove(channel);
      if (clientContext != null) {
        clientContext.attr(SERVER_CHANNEL_KEY).remove();
      }
    }
  };

  protected PubServerService(@NotNull final Project project) {
    super(project);

    bootstrap = NettyUtil.nioClientBootstrap();
    final PubServeChannelHandler pubServeChannelHandler = new PubServeChannelHandler();
    bootstrap.handler(new ChannelInitializer() {
      @Override
      protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(serverChannelRegistrar, new HttpClientCodec());
        channel.pipeline().addLast(pubServeChannelHandler, ChannelExceptionHandler.getInstance());
      }
    });
  }

  public static PubServerService getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, PubServerService.class);
  }

  @Override
  @NotNull
  protected String getConsoleToolWindowId() {
    return "Pub Serve";
  }

  @Override
  @NotNull
  protected Icon getConsoleToolWindowIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  protected void connectToProcess(@NotNull AsyncResult<OSProcessHandler> asyncResult,
                                  int port,
                                  @NotNull OSProcessHandler processHandler,
                                  @NotNull Consumer<String> errorOutputConsumer) {
    serverAddress = new InetSocketAddress(NetUtils.getLoopbackAddress(), port);
    super.connectToProcess(asyncResult, port, processHandler, errorOutputConsumer);
  }

  @Override
  protected OSProcessHandler createProcessHandler(@NotNull final Project project, final int port) throws ExecutionException {
    final DartSdk dartSdk = DartSdk.getGlobalDartSdk();
    if (dartSdk == null) return null;

    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(DartSdkUtil.getPubPath(dartSdk));
    commandLine.addParameter("serve");
    commandLine.addParameter("--hostname=127.0.0.1");
    commandLine.addParameter("--port=" + String.valueOf(port));
    commandLine.addParameter("web");

    commandLine.setWorkDirectory(project.getBasePath());

    return new OSProcessHandler(commandLine);
  }

  @Override
  protected void closeProcessConnections() {
    ChannelHandlerContext[] clientContexts;
    try {
      Collection<ChannelHandlerContext> clients = serverToClientContext.values();
      clientContexts = clients.toArray(new ChannelHandlerContext[clients.size()]);
      freeServerChannels.clear();
      serverToClientContext.clear();
    }
    finally {
      serverChannelRegistrar.close();
    }

    for (ChannelHandlerContext context : clientContexts) {
      try {
        sendBadGateway(context.channel());
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public void sendToPubServe(@NotNull final ChannelHandlerContext clientContext,
                             @NotNull final FullHttpRequest clientRequest,
                             @NotNull final String pathForPubServer) {
    clientRequest.retain();

    if (processHandler.has()) {
      sendToServer(clientContext, clientRequest, pathForPubServer);
    }
    else {
      processHandler.get().doWhenDone(new Runnable() {
        @Override
        public void run() {
          sendToServer(clientContext, clientRequest, pathForPubServer);
        }
      }).doWhenRejected(new Runnable() {
        @Override
        public void run() {
          sendBadGateway(clientContext.channel());
        }
      });
    }
  }

  private static void sendBadGateway(@NotNull Channel channel) {
    if (channel.isActive()) {
      Responses.sendStatus(HttpResponseStatus.BAD_GATEWAY, channel);
    }
  }

  private static void connect(@NotNull final Bootstrap bootstrap, @NotNull final SocketAddress remoteAddress, final @NotNull Consumer<Channel> channelConsumer) {
    final AtomicInteger attemptCounter = new AtomicInteger(1);
    bootstrap.connect(remoteAddress).addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
          channelConsumer.consume(future.channel());
        }
        else {
          int attemptCount = attemptCounter.incrementAndGet();
          if (attemptCount > NettyUtil.DEFAULT_CONNECT_ATTEMPT_COUNT) {
            channelConsumer.consume(null);
          }
          else {
            Thread.sleep(attemptCount * NettyUtil.MIN_START_TIME);
            bootstrap.connect(remoteAddress).addListener(this);
          }
        }
      }
    });
  }

  private void sendToServer(@NotNull final ChannelHandlerContext clientContext, @NotNull final FullHttpRequest clientRequest, @NotNull final String pathToPubServe) {
    final Attribute<Channel> serverChannelAttribute = clientContext.attr(SERVER_CHANNEL_KEY);
    Channel serverChannel = serverChannelAttribute.get();

    if (serverChannel == null) {
      serverChannel = findFreeServerChannel();
      if (serverChannel != null) {
        serverChannelAttribute.set(serverChannel);
      }
    }

    if (serverChannel == null) {
      connect(bootstrap, serverAddress, new Consumer<Channel>() {
        @Override
        public void consume(final Channel serverChannel) {
          if (serverChannel == null) {
            sendBadGateway(clientContext.channel());
          }
          else {
            serverChannelAttribute.set(serverChannel);
            serverChannel.closeFuture().addListener(serverChannelCloseListener);
            ChannelHandlerContext oldClientContext = serverToClientContext.put(serverChannel, clientContext);
            LOG.assertTrue(oldClientContext == null);
            clientContext.channel().pipeline().addLast(clientChannelStateHandler);
            sendToServer(clientRequest, pathToPubServe, serverChannel);
          }
        }
      });
    }
    else {
      sendToServer(clientRequest, pathToPubServe, serverChannel);
    }
  }

  @Nullable
  private Channel findFreeServerChannel() {
    Iterator<Channel> iterator = freeServerChannels.iterator();
    if (iterator.hasNext()) {
      Channel serverChannel = iterator.next();
      iterator.remove();
      return serverChannel;
    }
    return null;
  }

  private static void sendToServer(@NotNull FullHttpRequest clientRequest, @NotNull String pathToPubServe, @NotNull Channel serverChannel) {
    // duplicate - content will be shared (opposite to copy), so, we use duplicate. see ByteBuf javadoc.
    FullHttpRequest request = clientRequest.duplicate().setUri(pathToPubServe);
    // regardless of client, we always keep connection to server
    HttpHeaders.setKeepAlive(request, true);
    serverChannel.writeAndFlush(request);
  }

  @ChannelHandler.Sharable
  private class PubServeChannelHandler extends SimpleChannelInboundHandlerAdapter<HttpObject> {
    public PubServeChannelHandler() {
      super(false);
    }

    @Override
    protected void messageReceived(@NotNull ChannelHandlerContext context, @NotNull HttpObject message) throws Exception {
      ChannelHandlerContext clientContext = serverToClientContext.get(context.channel());
      if (clientContext != null && clientContext.channel().isActive()) {
        clientContext.channel().writeAndFlush(message);
      }
      else if (message instanceof ReferenceCounted) {
        ((ReferenceCounted)message).release();
      }
    }
  }

  @ChannelHandler.Sharable
  private class ClientChannelStateHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
      super.channelInactive(context);

      Channel serverChannel = context.attr(SERVER_CHANNEL_KEY).getAndRemove();
      if (serverChannel != null) {
        serverToClientContext.remove(serverChannel);
        freeServerChannels.add(serverChannel);
      }
    }
  }
}