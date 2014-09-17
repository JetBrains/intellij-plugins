package com.jetbrains.lang.dart.pubServer;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.util.containers.ContainerUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCounted;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.*;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

class PubServerProxy {
  private static final Logger LOG = Logger.getInstance(PubServerProxy.class.getName());
  private static final AttributeKey<Channel> SERVER_CHANNEL_KEY = AttributeKey.valueOf(PubServerService.class, "serverChannel");

  private final SocketAddress myServerAddress;

  private final Bootstrap bootstrap = NettyUtil.nioClientBootstrap();
  private final ConcurrentMap<Channel, ChannelHandlerContext> serverToClientContext = ContainerUtil.newConcurrentMap();
  private final ConcurrentHashSet<Channel> freeServerChannels = new ConcurrentHashSet<Channel>();
  private final ChannelInboundHandlerAdapter clientChannelStateHandler = new ClientChannelStateHandler();
  private final ChannelRegistrar serverChannelRegistrar = new ChannelRegistrar();

  private final ChannelFutureListener serverChannelCloseListener = new ChannelFutureListener() {
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
      Channel channel = future.channel();
      freeServerChannels.remove(channel);

      ChannelHandlerContext clientContext = serverToClientContext.remove(channel);
      if (clientContext != null) {
        clientContext.attr(SERVER_CHANNEL_KEY).remove();
        PubServerService.sendBadGateway(clientContext.channel());
      }
    }
  };

  PubServerProxy(@NotNull final SocketAddress serverAddress) {
    myServerAddress = serverAddress;
    bootstrap.handler(new ChannelInitializer() {
      protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(serverChannelRegistrar, new HttpClientCodec());
        channel.pipeline().addLast(new PubServeChannelHandler(), ChannelExceptionHandler.getInstance());
      }
    });
  }

  void closeProcessConnections() {
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
        PubServerService.sendBadGateway(context.channel());
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }


  private static void connect(@NotNull final Bootstrap bootstrap,
                              @NotNull final SocketAddress remoteAddress,
                              final @NotNull Consumer<Channel> channelConsumer) {
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

  void sendToServer(@NotNull final ChannelHandlerContext clientContext,
                    @NotNull final FullHttpRequest clientRequest,
                    @NotNull final String pathToPubServe) {
    final Attribute<Channel> serverChannelAttribute = clientContext.attr(SERVER_CHANNEL_KEY);
    Channel serverChannel = serverChannelAttribute.get();

    if (serverChannel == null) {
      serverChannel = findFreeServerChannel();
      if (serverChannel != null) {
        serverChannelAttribute.set(serverChannel);
      }
    }

    if (serverChannel == null) {
      connect(bootstrap, myServerAddress, new Consumer<Channel>() {
        @Override
        public void consume(final Channel serverChannel) {
          if (serverChannel == null) {
            PubServerService.sendBadGateway(clientContext.channel());
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
        if (serverToClientContext.remove(serverChannel, context)) {
          freeServerChannels.add(serverChannel);
        }
      }
    }
  }
}