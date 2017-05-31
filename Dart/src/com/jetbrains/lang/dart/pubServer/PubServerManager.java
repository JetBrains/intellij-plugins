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
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.ConsoleManager;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class PubServerManager implements Disposable {
  private static final Logger LOG = Logger.getInstance(PubServerManager.class);

  private final Project project;
  private final ConsoleManager consoleManager = new ConsoleManager();

  private String myServedSdkVersion;

  private final LoadingCache<VirtualFile, PubServerService> myServedDirToPubService =
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
      .addVirtualFileListener(new VirtualFileListener() {
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

    for (VirtualFile subdir : mainDir.getChildren()) {
      if (!subdir.isDirectory()) continue;

      final PubServerService service = myServedDirToPubService.getIfPresent(subdir);
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
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk != null && !sdk.getVersion().equals(myServedSdkVersion)) {
      stopAllPubServerProcesses();
      myServedSdkVersion = sdk.getVersion();
    }

    try {
      // servedDir - web or test, direct child of directory containing pubspec.yaml
      myServedDirToPubService.get(servedDir).sendToPubServer(clientChannel, clientRequest, extraHeaders, servedDir, pathForPubServer);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  public boolean hasAlivePubServerProcesses() {
    for (PubServerService service : myServedDirToPubService.asMap().values()) {
      if (service.isPubServerProcessAlive()) return true;
    }
    return false;
  }

  @Override
  public void dispose() {
    stopAllPubServerProcesses();
  }

  public void stopAllPubServerProcesses() {
    for (PubServerService service : myServedDirToPubService.asMap().values()) {
      try {
        Disposer.dispose(service);
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  @NotNull
  public Collection<String> getAllAlivePubServerAuthorities() {
    final Collection<String> result = new SmartList<>();
    for (PubServerService service : myServedDirToPubService.asMap().values()) {
      result.addAll(service.getAllPubServeAuthorities());
    }
    return result;
  }

  @NotNull
  public Collection<String> getAlivePubServerAuthoritiesForDartRoot(@NotNull final VirtualFile dartProjectRoot) {
    final Collection<String> result = new SmartList<>();
    for (VirtualFile subdir : dartProjectRoot.getChildren()) {
      if (!subdir.isDirectory()) continue;
      final PubServerService service = myServedDirToPubService.getIfPresent(subdir);
      if (service == null) continue;
      ContainerUtil.addIfNotNull(result, service.getPubServeAuthority(subdir));
    }
    return result;
  }

  @Nullable
  public String getPubServerAuthorityForServedDir(@NotNull final VirtualFile servedDir) {
    LOG.assertTrue(servedDir.isDirectory() && servedDir.getParent().findChild(PubspecYamlUtil.PUBSPEC_YAML) != null,
                   "Bad argument: " + servedDir.getPath());
    final PubServerService service = myServedDirToPubService.getIfPresent(servedDir);
    return service != null ? service.getPubServeAuthority(servedDir) : null;
  }
}