package com.jetbrains.lang.dart.pubServer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.*;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.builtInWebServer.ConsoleManager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PubServerManager implements Disposable {
  private static final Logger LOG = Logger.getInstance(PubServerManager.class);

  private final Project project;
  private final ConsoleManager consoleManager = new ConsoleManager();

  private final LoadingCache<VirtualFile, PubServerService> dartProjectToPubService =
    CacheBuilder.newBuilder().build(new CacheLoader<VirtualFile, PubServerService>() {
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

    VirtualFileManager.getInstance()
      .addVirtualFileListener(new VirtualFileAdapter() {
                                @Override
                                public void beforePropertyChange(@NotNull final VirtualFilePropertyEvent event) {
                                  if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
                                    contentsChanged(event);
                                  }
                                }

                                @Override
                                public void beforeFileMovement(@NotNull final VirtualFileMoveEvent event) {
                                  contentsChanged(event);
                                }

                                @Override
                                public void fileDeleted(@NotNull final VirtualFileEvent event) {
                                  contentsChanged(event);
                                }

                                @Override
                                public void contentsChanged(@NotNull final VirtualFileEvent event) {
                                  final VirtualFile file = event.getFile();
                                  if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName()) &&
                                      file.getFileSystem() == LocalFileSystem.getInstance()) {
                                    pubspecYamlChanged(file);
                                  }
                                }
                              },
                              project);
  }

  private void pubspecYamlChanged(@NotNull final VirtualFile file) {
    final VirtualFile mainDir = file.getParent();
    if (mainDir == null) return;

    // todo remove subfolder iteration when administration is done via admin port
    for (VirtualFile subdir : mainDir.getChildren()) {
      if (!subdir.isDirectory()) continue;

      final PubServerService service = dartProjectToPubService.getIfPresent(subdir);
      if (service != null) {
        Disposer.dispose(service);
      }
    }
  }

  public void send(@NotNull Channel clientChannel,
                   @NotNull FullHttpRequest clientRequest,
                   @NotNull HttpHeaders extraHeaders,
                   @NotNull VirtualFile servedDir,
                   @NotNull String pathForPubServer) {
    try {
      // servedDir - web or test, direct child of directory containing pubspec.yaml
      // "pub serve" process per dart project
      // todo uncomment /*.getParent()*/ below, serve subfolders of the same Dart project using the same pub serve process, manage it via admin port
      dartProjectToPubService.get(servedDir/*.getParent()*/).sendToPubServer(clientChannel, clientRequest, extraHeaders, servedDir, pathForPubServer);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  public boolean hasAlivePubServerProcesses() {
    for (PubServerService service : dartProjectToPubService.asMap().values()) {
      if (service.isPubServerProcessAlive()) return true;
    }
    return false;
  }

  @Override
  public void dispose() {
    stopAllPubServerProcesses();
  }

  public void stopAllPubServerProcesses() {
    for (PubServerService service : dartProjectToPubService.asMap().values()) {
      try {
        Disposer.dispose(service);
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public static int findOneMoreAvailablePort(final int forbiddenPort) throws com.intellij.execution.ExecutionException {
    try {
      while (true) {
        final int port = NetUtils.findAvailableSocketPort();
        if (port != forbiddenPort) {
          return port;
        }
      }
    }
    catch (IOException e) {
      throw new com.intellij.execution.ExecutionException(e);
    }
  }
}