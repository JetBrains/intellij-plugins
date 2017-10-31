package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.*;
import com.intellij.util.Alarm;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkdownCodeFencePluginCache implements ProjectComponent {
  public static final String MARKDOWN_FILE_PATH_KEY = "markdown-md5-file-path";

  @NotNull private static final Logger LOG = Logger.getInstance(MarkdownCodeFencePluginCache.class);
  @NotNull private Alarm myAlarm = new Alarm();

  @NotNull private final Collection<MarkdownCodeFencePluginCacheProvider> myCodeFencePluginCaches = ContainerUtil.newConcurrentSet();
  @NotNull private final Collection<VirtualFile> myAdditionalCacheToDelete = ContainerUtil.newConcurrentSet();
  @NotNull private Collection<VirtualFile> myCodeFencePluginSystemCache = ContainerUtil.emptyList();

  public static MarkdownCodeFencePluginCache getInstance(@NotNull Project project) {
    return project.getComponent(MarkdownCodeFencePluginCache.class);
  }

  public MarkdownCodeFencePluginCache(@NotNull Project project) {
    initCodeFencePluginCache(project);

    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void fileDeleted(@NotNull VirtualFileEvent event) {
        if (MarkdownFileType.INSTANCE == event.getFile().getFileType()) {
          myAdditionalCacheToDelete.addAll(processSourceFileToDelete(event.getFile(), ContainerUtil.emptyList()));
        }
      }
    });
  }

  private static void initCodeFencePluginCache(@NotNull Project project) {
    StartupManager.getInstance(project).registerStartupActivity(
      () -> ApplicationManager.getApplication().invokeLater(
        () -> WriteAction.run(
          () -> Arrays.stream(MarkdownCodeFencePluginGeneratingProvider.Companion.getEP_NAME().getExtensions())
            .forEach(provider -> createDirectories(provider)))));
  }

  private static void createDirectories(@NotNull MarkdownCodeFencePluginGeneratingProvider provider) {
    try {
      VfsUtil.createDirectories(provider.getCacheRootPath());
    }
    catch (IOException e) {
      LOG.error("Cannot init code fence plugin cache", e);
    }
  }

  public Collection<VirtualFile> collectFilesToRemove() {
    return myCodeFencePluginCaches.stream()
      .flatMap(cacheProvider -> processSourceFileToDelete(cacheProvider.getFile(), cacheProvider.getAliveCachedFiles()).stream())
      .collect(Collectors.toList());
  }

  private Collection<VirtualFile> processSourceFileToDelete(@NotNull VirtualFile sourceFile,
                                                            @NotNull Collection<VirtualFile> aliveCachedFiles) {
    Collection<VirtualFile> filesToDelete = ContainerUtil.newHashSet();
    for (VirtualFile codeFencePluginSystemPath : myCodeFencePluginSystemCache) {
      for (VirtualFile sourceFileCacheDirectory : codeFencePluginSystemPath.getChildren()) {
        if (isCachedSourceFile(sourceFileCacheDirectory, sourceFile) && aliveCachedFiles.isEmpty()) {
          filesToDelete.add(sourceFileCacheDirectory);
          continue;
        }

        for (VirtualFile imgFile : sourceFileCacheDirectory.getChildren()) {
          if (!isCachedSourceFile(sourceFileCacheDirectory, sourceFile) || aliveCachedFiles.contains(imgFile)) {
            continue;
          }

          filesToDelete.add(imgFile);
        }
      }
    }

    return filesToDelete;
  }

  boolean isCachedSourceFile(@NotNull VirtualFile sourceFileDir, @NotNull VirtualFile sourceFile) {
    return sourceFileDir.getName().equals(MarkdownUtil.md5(sourceFile.getPath(), MARKDOWN_FILE_PATH_KEY));
  }

  public void registerCacheProvider(@NotNull MarkdownCodeFencePluginCacheProvider pluginCacheProvider) {
    myCodeFencePluginCaches.add(pluginCacheProvider);
  }

  @Override
  public void projectOpened() {
    scheduleClearCache();
  }

  void scheduleClearCache() {
    myAlarm.addRequest(() -> {
      Stream.concat(myAdditionalCacheToDelete.stream(), collectFilesToRemove().stream())
        .forEach(file -> ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(() -> {
          try {
            file.delete(this);
          }
          catch (IOException e) {
            LOG.error("Cannot delete file: " + file.getPath(), e);
          }
        })));

      clear();

      scheduleClearCache();
    }, Registry.intValue("markdown.clear.cache.interval"));
  }

  private void clear() {
    myAdditionalCacheToDelete.clear();
    myCodeFencePluginCaches.clear();
  }

  @Override
  public void projectClosed() {
    myAlarm.cancelAllRequests();
  }
}