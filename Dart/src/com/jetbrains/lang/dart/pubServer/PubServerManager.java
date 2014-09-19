package com.jetbrains.lang.dart.pubServer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.builtInWebServer.ConsoleManager;

import java.util.concurrent.ExecutionException;

public class PubServerManager implements Disposable {
  private static final Logger LOG = Logger.getInstance(PubServerManager.class);

  private final Project project;
  private final ConsoleManager consoleManager = new ConsoleManager();

  private final LoadingCache<VirtualFile, PubServerService> dartProjectToPubService = CacheBuilder.newBuilder().build(new CacheLoader<VirtualFile, PubServerService>() {
    @Override
    public PubServerService load(@NotNull VirtualFile key) throws Exception {
      return new PubServerService(project, consoleManager);
    }
  });

  @NotNull
  public static PubServerManager getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, PubServerManager.class);
  }

  public PubServerManager(@NotNull Project project) {
    this.project = project;
  }

  public void send(@NotNull ChannelHandlerContext clientContext,
                   @NotNull FullHttpRequest clientRequest,
                   @NotNull VirtualFile servedDir,
                   @NotNull String pathForPubServer) {
    try {
      // servedDir - web or test, direct child of directory containing pubspec.yaml
      // "pub serve" process per dart project
      dartProjectToPubService.get(servedDir.getParent()).sendToPubServer(clientContext, clientRequest, servedDir, pathForPubServer);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  @Override
  public void dispose() {
    for (PubServerService service : dartProjectToPubService.asMap().values()) {
      try {
        Disposer.dispose(service);
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }
}