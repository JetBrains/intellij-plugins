// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.pubServer;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
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
import java.util.Map;

@Service(Service.Level.PROJECT)
public final class PubServerManager implements Disposable {
  private static final Logger LOG = Logger.getInstance(PubServerManager.class);

  private final Project project;
  private final ConsoleManager consoleManager = new ConsoleManager();

  private String myServedSdkVersion;

  private final LoadingCache<VirtualFile, PubServerService> myServedDirToPubService;

  public static @NotNull PubServerManager getInstance(@NotNull Project project) {
    return project.getService(PubServerManager.class);
  }

  public PubServerManager(@NotNull Project project) {
    this.project = project;

    VirtualFileManager.getInstance()
      .addVirtualFileListener(new VirtualFileListener() {
                                @Override
                                public void beforePropertyChange(final @NotNull VirtualFilePropertyEvent event) {
                                  if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
                                    contentsChanged(event);
                                  }
                                }

                                @Override
                                public void beforeFileMovement(final @NotNull VirtualFileMoveEvent event) {
                                  contentsChanged(event);
                                }

                                @Override
                                public void fileDeleted(final @NotNull VirtualFileEvent event) {
                                  contentsChanged(event);
                                }

                                @Override
                                public void contentsChanged(final @NotNull VirtualFileEvent event) {
                                  final VirtualFile file = event.getFile();
                                  if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName()) &&
                                      file.getFileSystem() == LocalFileSystem.getInstance()) {
                                    pubspecYamlChanged(file);
                                  }
                                }
                              },
                              this);
    myServedDirToPubService = Caffeine.newBuilder().build(key -> new PubServerService(this.project, consoleManager));
  }

  private void pubspecYamlChanged(final @NotNull VirtualFile file) {
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
    catch (Exception e) {
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

  public @NotNull Collection<String> getAllAlivePubServerAuthorities() {
    final Collection<String> result = new SmartList<>();
    for (PubServerService service : myServedDirToPubService.asMap().values()) {
      result.addAll(service.getAllPubServeAuthorities());
    }
    return result;
  }

  public @NotNull Collection<String> getAlivePubServerAuthoritiesForDartRoot(final @NotNull VirtualFile dartProjectRoot) {
    final Collection<String> result = new SmartList<>();
    for (VirtualFile subdir : dartProjectRoot.getChildren()) {
      if (!subdir.isDirectory()) continue;
      final PubServerService service = myServedDirToPubService.getIfPresent(subdir);
      if (service == null) continue;
      ContainerUtil.addIfNotNull(result, service.getPubServeAuthority(subdir));
    }
    return result;
  }

  public @Nullable VirtualFile getDartRootByAuthority(final @NotNull String authority) {
    for (Map.Entry<VirtualFile, PubServerService> entry : myServedDirToPubService.asMap().entrySet()) {
      if (entry.getValue().getAllPubServeAuthorities().contains(authority)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public @Nullable String getPubServerAuthorityForServedDir(final @NotNull VirtualFile servedDir) {
    LOG.assertTrue(servedDir.isDirectory() && servedDir.getParent().findChild(PubspecYamlUtil.PUBSPEC_YAML) != null,
                   "Bad argument: " + servedDir.getPath());
    final PubServerService service = myServedDirToPubService.getIfPresent(servedDir);
    return service != null ? service.getPubServeAuthority(servedDir) : null;
  }
}