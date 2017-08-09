package com.jetbrains.lang.dart.pubServer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.UrlFilter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.Consumer;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.NetKt;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import icons.DartIcons;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.PlatformDependent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.BuiltInWebServerKt;
import org.jetbrains.builtInWebServer.ConsoleManager;
import org.jetbrains.builtInWebServer.NetService;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.io.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jetbrains.io.NettyUtil.nioClientBootstrap;

final class PubServerService extends NetService {
  private static final Logger LOG = Logger.getInstance(PubServerService.class.getName());

  private static final String PUB_SERVE = "Pub Serve";
  private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.toolWindowGroup(PUB_SERVE, PUB_SERVE, false);

  private volatile VirtualFile firstServedDir;

  private final Bootstrap bootstrap = nioClientBootstrap();

  private final ConcurrentMap<Channel, ClientInfo> serverToClientChannel = ContainerUtil.newConcurrentMap();
  private final ChannelRegistrar serverChannelRegistrar = new ChannelRegistrar();

  private final ConcurrentMap<VirtualFile, ServerInfo> servedDirToSocketAddress = ContainerUtil.newConcurrentMap();

  private static class ServerInfo {
    private final InetSocketAddress address;
    private final Deque<Channel> freeServerChannels = PlatformDependent.newConcurrentDeque();

    private ServerInfo(InetSocketAddress address) {
      this.address = address;
    }
  }

  private static class ClientInfo {
    private final Channel channel;
    private final HttpHeaders extraHeaders;

    private ClientInfo(@NotNull Channel channel, @NotNull HttpHeaders extraHeaders) {
      this.channel = channel;
      this.extraHeaders = extraHeaders;
    }
  }

  private final ChannelFutureListener serverChannelCloseListener = future -> {
    Channel channel = future.channel();
    ServerInfo serverInfo = getServerInfo(channel);
    if (serverInfo != null) {
      serverInfo.freeServerChannels.remove(channel);
    }

    ClientInfo clientInfo = serverToClientChannel.remove(channel);
    if (clientInfo != null) {
      sendBadGateway(clientInfo.channel, clientInfo.extraHeaders);
    }
  };

  public PubServerService(@NotNull Project project, @NotNull ConsoleManager consoleManager) {
    super(project, consoleManager);

    bootstrap.handler(new ChannelInitializer() {
      @Override
      protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(serverChannelRegistrar, new HttpClientCodec());
        channel.pipeline().addLast(new PubServeChannelHandler(), ChannelExceptionHandler.getInstance());
      }
    });
  }

  @Nullable
  private ServerInfo getServerInfo(@NotNull Channel channel) {
    for (ServerInfo serverInstanceInfo : servedDirToSocketAddress.values()) {
      if (channel.remoteAddress().equals(serverInstanceInfo.address)) {
        return serverInstanceInfo;
      }
    }
    return null;
  }

  @Override
  @NotNull
  protected String getConsoleToolWindowId() {
    return PUB_SERVE;
  }

  @Override
  @NotNull
  protected Icon getConsoleToolWindowIcon() {
    return DartIcons.Dart_13;
  }

  @NotNull
  @Override
  public ActionGroup getConsoleToolWindowActions() {
    return new DefaultActionGroup(ActionManager.getInstance().getAction("Dart.stop.pub.server"));
  }

  @Override
  protected void configureConsole(@NotNull final TextConsoleBuilder consoleBuilder) {
    consoleBuilder.addFilter(new DartConsoleFilter(getProject(), firstServedDir));
    consoleBuilder.addFilter(new DartRelativePathsConsoleFilter(getProject(), firstServedDir.getParent().getPath()));
    consoleBuilder.addFilter(new UrlFilter());
  }

  public boolean isPubServerProcessAlive() {
    return getProcessHandler().has() && !getProcessHandler().getResult().isProcessTerminated();
  }

  public void sendToPubServer(@NotNull final Channel clientChannel,
                              @NotNull final FullHttpRequest clientRequest,
                              @NotNull HttpHeaders extraHeaders,
                              @NotNull final VirtualFile servedDir,
                              @NotNull final String pathForPubServer) {
    clientRequest.retain();

    if (getProcessHandler().has()) {
      sendToServer(servedDir, clientChannel, clientRequest, extraHeaders, pathForPubServer);
    }
    else {
      firstServedDir = servedDir;

      getProcessHandler().get()
        .done(osProcessHandler -> sendToServer(servedDir, clientChannel, clientRequest, extraHeaders, pathForPubServer))
        .rejected(throwable -> sendBadGateway(clientChannel, extraHeaders));
    }
  }

  @Override
  @Nullable
  protected OSProcessHandler createProcessHandler(@NotNull final Project project, final int port) throws ExecutionException {
    final DartSdk dartSdk = DartSdk.getDartSdk(project);
    if (dartSdk == null) return null;

    final GeneralCommandLine commandLine = new GeneralCommandLine().withWorkDirectory(firstServedDir.getParent().getPath());
    commandLine.setExePath(FileUtil.toSystemDependentName(DartSdkUtil.getPubPath(dartSdk)));
    commandLine.addParameter("serve");
    commandLine.addParameter(firstServedDir.getName());
    commandLine.addParameter("--port=" + String.valueOf(port));
    commandLine.withEnvironment(DartPubActionBase.PUB_ENV_VAR_NAME, DartPubActionBase.getPubEnvValue());

    final OSProcessHandler processHandler = new OSProcessHandler(commandLine);
    processHandler.addProcessListener(new PubServeOutputListener(project));

    return processHandler;
  }

  @Override
  protected void connectToProcess(@NotNull final AsyncPromise<OSProcessHandler> promise,
                                  final int port,
                                  @NotNull final OSProcessHandler processHandler,
                                  @NotNull final Consumer<String> errorOutputConsumer) {
    InetSocketAddress firstPubServerAddress = NetKt.loopbackSocketAddress(port);
    ServerInfo old = servedDirToSocketAddress.put(firstServedDir, new ServerInfo(firstPubServerAddress));
    LOG.assertTrue(old == null);

    super.connectToProcess(promise, port, processHandler, errorOutputConsumer);
  }

  static void sendBadGateway(@NotNull final Channel channel, @NotNull HttpHeaders extraHeaders) {
    if (channel.isActive()) {
      Responses.send(HttpResponseStatus.BAD_GATEWAY, channel, null, null, extraHeaders);
    }
  }

  @Override
  protected void closeProcessConnections() {
    servedDirToSocketAddress.clear();

    ClientInfo[] list;
    try {
      Collection<ClientInfo> clientInfos = serverToClientChannel.values();
      list = clientInfos.toArray(new ClientInfo[clientInfos.size()]);
      for (ServerInfo serverInstanceInfo : servedDirToSocketAddress.values()) {
        serverInstanceInfo.freeServerChannels.clear();
      }
      serverToClientChannel.clear();
    }
    finally {
      serverChannelRegistrar.close();
    }

    for (ClientInfo info : list) {
      try {
        sendBadGateway(info.channel, info.extraHeaders);
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

  void sendToServer(@NotNull final VirtualFile servedDir,
                    @NotNull final Channel clientChannel,
                    @NotNull final FullHttpRequest clientRequest,
                    @NotNull HttpHeaders extraHeaders,
                    @NotNull final String pathToPubServe) {
    ServerInfo serverInstanceInfo = servedDirToSocketAddress.get(servedDir);
    final InetSocketAddress address = serverInstanceInfo.address;

    if (Registry.is("dart.redirect.to.pub.server", true)) {
      // We can't use 301 (MOVED_PERMANENTLY) response status because Pub Serve port will change after restart, but browser will remember outdated redirection URL
      final HttpResponse response = Responses.response(HttpResponseStatus.FOUND, clientRequest, null);
      //assert serverInstanceInfo != null;

      final Map<String, List<String>> parameters = new QueryStringDecoder(clientRequest.uri()).parameters();
      parameters.remove(BuiltInWebServerKt.TOKEN_PARAM_NAME);

      final QueryStringEncoder encoder =
        new QueryStringEncoder("http://" + address.getHostString() + ":" + address.getPort() + pathToPubServe);
      for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
        for (String value : entry.getValue()) {
          encoder.addParam(entry.getKey(), value);
        }
      }

      response.headers().add(HttpHeaderNames.LOCATION, encoder.toString());
      Responses.send(response, clientChannel, clientRequest, extraHeaders);
      return;
    }

    Channel serverChannel = findFreeServerChannel(serverInstanceInfo.freeServerChannels);
    if (serverChannel == null) {
      connect(bootstrap, address, serverChannel1 -> {
        if (serverChannel1 == null) {
          if (clientChannel.isActive()) {
            Responses.send(HttpResponseStatus.BAD_GATEWAY, clientChannel, clientRequest, null, extraHeaders);
          }
        }
        else {
          serverChannel1.closeFuture().addListener(serverChannelCloseListener);
          sendToServer(clientChannel, clientRequest, extraHeaders, pathToPubServe, serverChannel1);
        }
      });
    }
    else {
      sendToServer(clientChannel, clientRequest, extraHeaders, pathToPubServe, serverChannel);
    }
  }

  @Nullable
  private static Channel findFreeServerChannel(@NotNull Deque<Channel> freeServerChannels) {
    while (true) {
      Channel channel = freeServerChannels.pollLast();
      if (channel == null) {
        break;
      }

      if (channel.isActive()) {
        return channel;
      }
    }
    return null;
  }

  private void sendToServer(@NotNull final Channel clientChannel,
                            @NotNull FullHttpRequest clientRequest,
                            @NotNull HttpHeaders extraHeaders,
                            @NotNull String pathToPubServe,
                            @NotNull Channel serverChannel) {
    ClientInfo oldClientInfo = serverToClientChannel.put(serverChannel, new ClientInfo(clientChannel, extraHeaders));
    LOG.assertTrue(oldClientInfo == null);

    // duplicate - content will be shared (opposite to copy), so, we use duplicate. see ByteBuf javadoc.
    FullHttpRequest request = clientRequest.duplicate().setUri(pathToPubServe);

    // regardless of client, we always keep connection to server
    request.setProtocolVersion(HttpVersion.HTTP_1_1);
    HttpUtil.setKeepAlive(request, true);

    InetSocketAddress serverAddress = (InetSocketAddress)serverChannel.remoteAddress();
    request.headers().set(HttpHeaderNames.HOST, serverAddress.getAddress().getHostAddress() + ':' + serverAddress.getPort());
    serverChannel.writeAndFlush(request);
  }

  @NotNull
  Collection<String> getAllPubServeAuthorities() {
    final Collection<String> result = new SmartList<>();
    for (ServerInfo serverInfo : servedDirToSocketAddress.values()) {
      if (serverInfo.address != null) {
        result.add(serverInfo.address.getHostString() + ":" + serverInfo.address.getPort());
      }
    }
    return result;
  }


  @Nullable
  String getPubServeAuthority(@NotNull final VirtualFile dir) {
    final ServerInfo serverInfo = servedDirToSocketAddress.get(dir);
    final InetSocketAddress address = serverInfo == null ? null : serverInfo.address;
    return address != null ? address.getHostString() + ":" + address.getPort() : null;
  }

  @ChannelHandler.Sharable
  private class PubServeChannelHandler extends SimpleChannelInboundHandlerAdapter<HttpObject> {
    public PubServeChannelHandler() {
      super(false);
    }

    @Override
    protected void messageReceived(@NotNull ChannelHandlerContext context, @NotNull HttpObject message) throws Exception {
      Channel serverChannel = context.channel();
      ClientInfo clientInfo = serverToClientChannel.get(serverChannel);
      if (clientInfo == null || !clientInfo.channel.isActive()) {
        // client abort request, so, just close server channel as well and don't try to reuse it
        serverToClientChannel.remove(serverChannel);
        serverChannel.close();

        if (message instanceof ReferenceCounted) {
          ((ReferenceCounted)message).release();
        }
      }
      else {
        if (message instanceof HttpResponse) {
          HttpResponse response = (HttpResponse)message;
          HttpUtil.setKeepAlive(response, true);
          response.headers().add(clientInfo.extraHeaders);
        }
        if (message instanceof LastHttpContent) {
          serverToClientChannel.remove(serverChannel);
          ServerInfo serverInfo = getServerInfo(serverChannel);
          if (serverInfo != null) {
            // todo sometimes dart pub server stops to respond, so, we don't reuse it for now
            //serverInfo.freeServerChannels.add(serverChannel);
            serverChannel.close();
          }
        }

        clientInfo.channel.writeAndFlush(message);
      }
    }
  }

  private static class PubServeOutputListener extends ProcessAdapter {
    private final Project myProject;
    private boolean myNotificationAboutErrors;
    private Notification myNotification;

    public PubServeOutputListener(final Project project) {
      myProject = project;
    }

    @Override
    public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
      if (outputType == ProcessOutputTypes.STDERR) {
        final boolean error = event.getText().toLowerCase(Locale.US).contains("error");

        ApplicationManager.getApplication().invokeLater(() -> showNotificationIfNeeded(error));
      }
    }

    private void showNotificationIfNeeded(final boolean isError) {
      if (ToolWindowManager.getInstance(myProject).getToolWindow(PUB_SERVE).isVisible()) {
        return;
      }

      if (myNotification != null && !myNotification.isExpired()) {
        final Balloon balloon1 = myNotification.getBalloon();
        final Balloon balloon2 = ToolWindowManager.getInstance(myProject).getToolWindowBalloon(PUB_SERVE);
        if ((balloon1 != null || balloon2 != null) && (myNotificationAboutErrors || !isError)) {
          return; // already showing correct balloon
        }
        myNotification.expire();
      }

      myNotificationAboutErrors = isError; // previous errors are already reported, so reset our flag

      final String message =
        DartBundle.message(myNotificationAboutErrors ? "pub.serve.output.contains.errors" : "pub.serve.output.contains.warnings");

      myNotification = NOTIFICATION_GROUP.createNotification("", message, NotificationType.WARNING, new NotificationListener.Adapter() {
        @Override
        protected void hyperlinkActivated(@NotNull final Notification notification, @NotNull final HyperlinkEvent e) {
          notification.expire();
          ToolWindowManager.getInstance(myProject).getToolWindow(PUB_SERVE).activate(null);
        }
      });

      myNotification.notify(myProject);
    }
  }
}
