// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.Consumer;
import com.intellij.util.SmartList;
import com.intellij.util.net.NetKt;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import icons.DartIcons;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCounted;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.BuiltInWebServerKt;
import org.jetbrains.builtInWebServer.ConsoleManager;
import org.jetbrains.builtInWebServer.NetService;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.ide.BuiltInServerManager;
import org.jetbrains.io.ChannelExceptionHandler;
import org.jetbrains.io.ChannelRegistrar;
import org.jetbrains.io.Responses;
import org.jetbrains.io.SimpleChannelInboundHandlerAdapter;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class PubServerService extends NetService {
  private static final Logger LOG = Logger.getInstance(PubServerService.class.getName());

  private static final String DART_WEBDEV = "Dart Webdev";
  private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.toolWindowGroup(DART_WEBDEV, DART_WEBDEV, false);

  private volatile VirtualFile firstServedDir;

  private final ConcurrentMap<Channel, ClientInfo> serverToClientChannel = new ConcurrentHashMap<>();
  private final ChannelRegistrar serverChannelRegistrar = new ChannelRegistrar();

  private final ConcurrentMap<VirtualFile, ServerInfo> servedDirToSocketAddress = new ConcurrentHashMap<>();

  private final Object myServerReadyLock = new Object();

  private static final class ServerInfo {
    private final InetSocketAddress address;

    private ServerInfo(InetSocketAddress address) {
      this.address = address;
    }
  }

  private static final class ClientInfo {
    private final Channel channel;
    private final HttpHeaders extraHeaders;

    private ClientInfo(@NotNull Channel channel, @NotNull HttpHeaders extraHeaders) {
      this.channel = channel;
      this.extraHeaders = extraHeaders;
    }
  }

  PubServerService(@NotNull Project project, @NotNull ConsoleManager consoleManager) {
    super(project, consoleManager);

    BuiltInServerManager.getInstance().createClientBootstrap().handler(new ChannelInitializer() {
      @Override
      protected void initChannel(Channel channel) {
        channel.pipeline().addLast(serverChannelRegistrar, new HttpClientCodec());
        channel.pipeline().addLast(new PubServeChannelHandler(), ChannelExceptionHandler.getInstance());
      }
    });
  }

  @Override
  protected int getAvailableSocketPort() {
    int initialPort = DartConfigurable.getWebdevPort(getProject());
    int maxAttempts = 10;
    int currentPort = initialPort;

    while (true) {
      try (ServerSocket serverSocket = new ServerSocket(currentPort)) {
        return serverSocket.getLocalPort();
      }
      catch (IOException e) {/* try next port */}

      if (++currentPort - initialPort > maxAttempts) {
        return super.getAvailableSocketPort();
      }
    }
  }

  private @Nullable ServerInfo getServerInfo(@NotNull Channel channel) {
    for (ServerInfo serverInstanceInfo : servedDirToSocketAddress.values()) {
      if (channel.remoteAddress().equals(serverInstanceInfo.address)) {
        return serverInstanceInfo;
      }
    }
    return null;
  }

  @Override
  protected @NotNull String getConsoleToolWindowId() {
    return DART_WEBDEV;
  }

  @Override
  protected @NotNull Icon getConsoleToolWindowIcon() {
    return DartIcons.PubServeToolWindow;
  }

  @Override
  public @NotNull ActionGroup getConsoleToolWindowActions() {
    return new DefaultActionGroup(ActionManager.getInstance().getAction("Dart.stop.dart.webdev.server"));
  }

  @Override
  protected void configureConsole(final @NotNull TextConsoleBuilder consoleBuilder) {
    consoleBuilder.addFilter(new DartConsoleFilter(getProject(), firstServedDir));
    consoleBuilder.addFilter(new DartRelativePathsConsoleFilter(getProject(), firstServedDir.getParent().getPath()));
    consoleBuilder.addFilter(new UrlFilter());
  }

  public boolean isPubServerProcessAlive() {
    OSProcessHandler processHandler = getProcessHandler().getResultIfFullFilled();
    return processHandler != null && !processHandler.isProcessTerminated();
  }

  public void sendToPubServer(final @NotNull Channel clientChannel,
                              final @NotNull FullHttpRequest clientRequest,
                              @NotNull HttpHeaders extraHeaders,
                              final @NotNull VirtualFile servedDir,
                              final @NotNull String pathForPubServer) {
    clientRequest.retain();

    if (getProcessHandler().getResultIfFullFilled() != null) {
      sendToServer(servedDir, clientChannel, clientRequest, extraHeaders, pathForPubServer);
    }
    else {
      firstServedDir = servedDir;

      getProcessHandler()
        .get()
        .onSuccess(osProcessHandler -> sendToServer(servedDir, clientChannel, clientRequest, extraHeaders, pathForPubServer))
        .onError(throwable -> sendBadGateway(clientChannel, extraHeaders));
    }
  }

  @Override
  protected @Nullable OSProcessHandler createProcessHandler(final @NotNull Project project, final int port) throws ExecutionException {
    final DartSdk dartSdk = DartSdk.getDartSdk(project);
    if (dartSdk == null) return null;

    if (DartWebdev.INSTANCE.useWebdev(DartSdk.getDartSdk(getProject()))) {
      if (!DartWebdev.INSTANCE.getActivated()) {
        ApplicationManager.getApplication()
          .invokeAndWait(() -> DartWebdev.INSTANCE.ensureWebdevActivated(getProject()), ModalityState.any());
      }
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine().withWorkDirectory(firstServedDir.getParent().getPath());
    DartPubActionBase.setupPubExePath(commandLine, dartSdk);
    commandLine.withEnvironment(DartPubActionBase.PUB_ENV_VAR_NAME, DartPubActionBase.getPubEnvValue());

    if (DartWebdev.INSTANCE.useWebdev(dartSdk)) {
      commandLine.addParameters("global", "run", "webdev", "serve");
      commandLine.addParameter(firstServedDir.getName() + ":" + port);
      commandLine.withEnvironment(DartPubActionBase.PUB_ENV_VAR_NAME, DartPubActionBase.getPubEnvValue() + ".webdev");
    }
    else {
      commandLine.addParameter("serve");
      commandLine.addParameter(firstServedDir.getName());
      commandLine.addParameter("--port=" + port);
    }

    final OSProcessHandler processHandler = new OSProcessHandler(commandLine);
    processHandler.addProcessListener(new PubServeOutputListener(project, myServerReadyLock));

    return processHandler;
  }

  @Override
  protected void connectToProcess(final @NotNull AsyncPromise<OSProcessHandler> promise,
                                  final int port,
                                  final @NotNull OSProcessHandler processHandler,
                                  final @NotNull Consumer<String> errorOutputConsumer) {
    if (DartWebdev.INSTANCE.useWebdev(DartSdk.getDartSdk(getProject()))) {
      synchronized (myServerReadyLock) {
        try {
          // wait for the Webdev server to start before redirecting, so that Chrome doesn't show error.
          //noinspection WaitNotInLoop
          myServerReadyLock.wait(15000);
        }
        catch (InterruptedException e) {/**/}
      }
    }

    if (processHandler.isProcessTerminated()) {
      promise.cancel();
      return;
    }

    InetSocketAddress firstPubServerAddress = NetKt.loopbackSocketAddress(port);
    ServerInfo old = servedDirToSocketAddress.put(firstServedDir, new ServerInfo(firstPubServerAddress));
    LOG.assertTrue(old == null);

    super.connectToProcess(promise, port, processHandler, errorOutputConsumer);
  }

  static void sendBadGateway(final @NotNull Channel channel, @NotNull HttpHeaders extraHeaders) {
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
      list = clientInfos.toArray(new ClientInfo[0]);
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

  void sendToServer(final @NotNull VirtualFile servedDir,
                    final @NotNull Channel clientChannel,
                    final @NotNull FullHttpRequest clientRequest,
                    @NotNull HttpHeaders extraHeaders,
                    final @NotNull String pathToPubServe) {
    ServerInfo serverInstanceInfo = servedDirToSocketAddress.get(servedDir);
    final InetSocketAddress address = serverInstanceInfo.address;

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
  String getPubServeAuthority(final @NotNull VirtualFile dir) {
    final ServerInfo serverInfo = servedDirToSocketAddress.get(dir);
    final InetSocketAddress address = serverInfo == null ? null : serverInfo.address;
    return address != null ? address.getHostString() + ":" + address.getPort() : null;
  }

  @ChannelHandler.Sharable
  private class PubServeChannelHandler extends SimpleChannelInboundHandlerAdapter<HttpObject> {
    PubServeChannelHandler() {
      super(false);
    }

    @Override
    protected void messageReceived(@NotNull ChannelHandlerContext context, @NotNull HttpObject message) {
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
    private final Object myServerReadyLock;
    private boolean myNotificationAboutErrors;
    private Notification myNotification;

    PubServeOutputListener(@NotNull Project project, @NotNull Object serverReadyLock) {
      myProject = project;
      myServerReadyLock = serverReadyLock;
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
      synchronized (myServerReadyLock) {
        myServerReadyLock.notifyAll();
      }
    }

    @Override
    public void onTextAvailable(final @NotNull ProcessEvent event, final @NotNull Key outputType) {
      final String text = StringUtil.toLowerCase(event.getText());

      // Serving `web` on http://localhost:53322
      // or [INFO] Serving `web` on http://localhost:53322
      if (text.contains("serving ")) {
        synchronized (myServerReadyLock) {
          myServerReadyLock.notifyAll();
        }
      }

      if (outputType == ProcessOutputTypes.STDERR || text.startsWith("[error] ")) {
        final boolean error = text.contains("error");

        ApplicationManager.getApplication().invokeLater(() -> showNotificationIfNeeded(error));
      }
      else if (text.contains("could not run in the current directory.") ||
               text.contains("webdev could not run for this project.")) {
        ApplicationManager.getApplication().invokeLater(() -> showNotificationIfNeeded(true));
      }
    }

    private void showNotificationIfNeeded(final boolean isError) {
      if (ToolWindowManager.getInstance(myProject).getToolWindow(DART_WEBDEV).isVisible()) {
        return;
      }

      if (myNotification != null && !myNotification.isExpired()) {
        final Balloon balloon1 = myNotification.getBalloon();
        final Balloon balloon2 = ToolWindowManager.getInstance(myProject).getToolWindowBalloon(DART_WEBDEV);
        if ((balloon1 != null || balloon2 != null) && (myNotificationAboutErrors || !isError)) {
          return; // already showing correct balloon
        }
        myNotification.expire();
      }

      myNotificationAboutErrors = isError; // previous errors are already reported, so reset our flag

      final String message = DartBundle.message(myNotificationAboutErrors ? "dart.webdev.server.output.contains.errors"
                                                                          : "dart.webdev.server.output.contains.warnings");

      myNotification = NOTIFICATION_GROUP.createNotification(message, NotificationType.WARNING).setListener(new NotificationListener.Adapter() {
        @Override
        protected void hyperlinkActivated(final @NotNull Notification notification, final @NotNull HyperlinkEvent e) {
          notification.expire();
          ToolWindowManager.getInstance(myProject).getToolWindow(DART_WEBDEV).activate(null);
        }
      });

      myNotification.notify(myProject);
    }
  }
}
