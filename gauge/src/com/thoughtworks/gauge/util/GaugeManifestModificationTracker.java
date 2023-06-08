package com.thoughtworks.gauge.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import com.thoughtworks.gauge.GaugeConstants;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class GaugeManifestModificationTracker extends SimpleModificationTracker implements Disposable {
  public static GaugeManifestModificationTracker getInstance(@NotNull Project project) {
    return project.getService(GaugeManifestModificationTracker.class);
  }

  public GaugeManifestModificationTracker(@NotNull Project project) {
    var messageBus = project.getMessageBus().connect(this);
    messageBus.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(new VirtualFileListener() {
      @Override
      public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        handleChange(event.getFile());
      }

      @Override
      public void contentsChanged(@NotNull VirtualFileEvent event) {
        handleChange(event.getFile());
      }

      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        handleChange(event.getFile());
      }

      @Override
      public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
        handleChange(event.getFile());
      }

      @Override
      public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        handleChange(event.getFile());
      }

      @Override
      public void fileCopied(@NotNull VirtualFileCopyEvent event) {
        handleChange(event.getFile());
      }
    }));
  }

  private void handleChange(VirtualFile file) {
    if (GaugeConstants.MANIFEST_FILE.equals(file.getName())) {
      incModificationCount();
    }
  }

  @Override
  public void dispose() {
    // do nothing
  }
}
