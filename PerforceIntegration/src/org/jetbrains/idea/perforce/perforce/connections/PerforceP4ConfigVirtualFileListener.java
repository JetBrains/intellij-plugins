package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;

/**
 * @author irengrig
 */
public class PerforceP4ConfigVirtualFileListener implements VirtualFileListener {
  private final PerforceConnectionManagerI myConnectionManager;
  private final Project myProject;
  private final static Logger LOG = Logger.getInstance(PerforceP4ConfigVirtualFileListener.class);

  public PerforceP4ConfigVirtualFileListener(PerforceConnectionManagerI connectionManager, Project project) {
    myConnectionManager = connectionManager;
    myProject = project;
  }

  @Override
  public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
    processFileEvent(event);
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    processFileEvent(event);
  }

  @Override
  public void fileCreated(@NotNull VirtualFileEvent event) {
    processFileEvent(event);
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    processFileEvent(event);
  }

  @Override
  public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    processFileEvent(event);
  }

  @Override
  public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
    processFileEvent(event);
  }

  @Override
  public void beforeContentsChange(@NotNull VirtualFileEvent event) {
    processFileEvent(event);
  }

  @Override
  public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
    processFileEvent(event);
  }

  @Override
  public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
    processFileEvent(event);
  }

  private boolean isConfigFileName(@NotNull String fileName) {
    PerforceMultipleConnections multipleConnections = myConnectionManager.getMultipleConnectionObject();
    if (multipleConnections != null) {
      for (P4ConnectionParameters parameters : multipleConnections.getParametersMap().values()) {
        if (Objects.equals(parameters.getConfigFileName(), fileName)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isIgnoredFileName(@NotNull String fileName) {
    if (fileName.equals(P4ConfigHelper.getP4IgnoreFileName())) {
      return true;
    }

    PerforceMultipleConnections multipleConnections = myConnectionManager.getMultipleConnectionObject();
    if (multipleConnections != null) {
      for (P4ConnectionParameters parameters : multipleConnections.getParametersMap().values()) {
        if (Objects.equals(parameters.getIgnoreFileName(), fileName)) {
          return true;
        }
      }
    }

    return false;
  }

  private void processFileEvent(final VirtualFileEvent event) {
    String fileName = event.getFileName();
    if (isIgnoredFileName(fileName)) {
      LOG.debug("received virtual file event on p4ignore file");
      PerforceVcs.getInstance(myProject).getOnlineChangeProvider().discardCache();
    }

    if (isConfigFileName(fileName)) {
      LOG.debug("received virtual file event on p4config file");
      myConnectionManager.updateConnections();
    }
  }
}
