package com.jetbrains.lang.dart.pubServer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import gnu.trove.THashMap;
import icons.DartIcons;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.NetService;
import org.jetbrains.io.Responses;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class PubServerService extends NetService {
  private static final Logger LOG = Logger.getInstance(PubServerService.class.getName());

  private final Map<VirtualFile, PubServerProxy> myServedDirToProxyMap = new THashMap<VirtualFile, PubServerProxy>();
  private VirtualFile myFirstServedDir;

  protected PubServerService(@NotNull Project project) {
    super(project);
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
    return DartIcons.Dart_13;
  }

  public void sendToPubServer(@NotNull final ChannelHandlerContext clientContext,
                              @NotNull final FullHttpRequest clientRequest,
                              @NotNull final VirtualFile servedDir,
                              @NotNull final String pathForPubServer) {
    clientRequest.retain();

    // stop pub serve if it serves a different Dart project
    if (processHandler.has() && !servedDir.getParent().equals(myFirstServedDir.getParent())) {
      final OSProcessHandler osProcessHandler = processHandler.get().getResult();
      osProcessHandler.destroyProcess();
      // need to wait for the process end so that processHandler is reset in org.jetbrains.builtInWebServer.NetService.MyProcessAdapter.processTerminated
      osProcessHandler.waitFor();
    }

    if (processHandler.has()) {
      final PubServerProxy pubServerProxy = myServedDirToProxyMap.get(servedDir);
      if (pubServerProxy != null) {
        pubServerProxy.sendToServer(clientContext, clientRequest, pathForPubServer);
      }
      else {
        serveDirAndSendRequest(clientContext, clientRequest, servedDir, pathForPubServer);
      }
    }
    else {
      startPubServerAndSendRequest(clientContext, clientRequest, servedDir, pathForPubServer);
    }
  }

  @Nullable
  protected OSProcessHandler createProcessHandler(@NotNull final Project project, final int port) throws ExecutionException {
    final DartSdk dartSdk = DartSdk.getGlobalDartSdk();
    if (dartSdk == null) return null;

    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(DartSdkUtil.getPubPath(dartSdk));
    commandLine.addParameter("serve");
    commandLine.addParameter(myFirstServedDir.getName());
    commandLine.addParameter("--port=" + String.valueOf(port));
    commandLine.addParameter("--admin-port=" + String.valueOf(findAvailablePort(port)));

    commandLine.setWorkDirectory(myFirstServedDir.getParent().getPath());

    return new OSProcessHandler(commandLine);
  }

  @Override
  protected void connectToProcess(@NotNull final AsyncResult<OSProcessHandler> asyncResult,
                                  final int port,
                                  @NotNull final OSProcessHandler processHandler,
                                  @NotNull final Consumer<String> errorOutputConsumer) {
    final InetSocketAddress firstPubServerAddress = new InetSocketAddress(NetUtils.getLoopbackAddress(), port);
    myServedDirToProxyMap.put(myFirstServedDir, new PubServerProxy(firstPubServerAddress));

    super.connectToProcess(asyncResult, port, processHandler, errorOutputConsumer);
  }

  private void startPubServerAndSendRequest(final ChannelHandlerContext clientContext,
                                            final FullHttpRequest clientRequest,
                                            final VirtualFile servedDir,
                                            final String pathForPubServer) {
    LOG.assertTrue(!processHandler.has());

    myFirstServedDir = servedDir;

    processHandler.get().doWhenDone(new Runnable() {
      @Override
      public void run() {
        final PubServerProxy pubServerProxy = myServedDirToProxyMap.get(servedDir);
        LOG.assertTrue(myServedDirToProxyMap.size() == 1 && pubServerProxy != null, myServedDirToProxyMap.size());
        pubServerProxy.sendToServer(clientContext, clientRequest, pathForPubServer);
      }
    }).doWhenRejected(new Runnable() {
      @Override
      public void run() {
        myServedDirToProxyMap.clear();
        sendBadGateway(clientContext.channel());
      }
    });
  }

  private void serveDirAndSendRequest(@NotNull final ChannelHandlerContext clientContext,
                                      @NotNull final FullHttpRequest clientRequest,
                                      @NotNull final VirtualFile servedDir,
                                      @NotNull final String pathForPubServer) {
    // todo implement
  }

  @Override
  protected void closeProcessConnections() {
    for (PubServerProxy pubServerProxy : myServedDirToProxyMap.values()) {
      pubServerProxy.closeProcessConnections();
    }
    myServedDirToProxyMap.clear();
  }

  static void sendBadGateway(@NotNull final Channel channel) {
    if (channel.isActive()) {
      Responses.sendStatus(HttpResponseStatus.BAD_GATEWAY, channel);
    }
  }

  private static int findAvailablePort(int forbiddenPort) throws ExecutionException {
    try {
      while (true) {
        final int adminPort = NetUtils.findAvailableSocketPort();
        if (adminPort != forbiddenPort) {
          return adminPort;
        }
      }
    }
    catch (IOException e) {
      throw new ExecutionException(e);
    }
  }
}