package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;

import java.util.Objects;

public class PerforceP4ConfigVirtualFileListener implements VirtualFileListener {
  private final PerforceConnectionManagerI myConnectionManager;
  private final Project myProject;
  private final static Logger LOG = Logger.getInstance(PerforceP4ConfigVirtualFileListener.class);
  private final P4EnvHelper myP4EnvHelper;

  public PerforceP4ConfigVirtualFileListener(PerforceConnectionManagerI connectionManager, Project project) {
    myConnectionManager = connectionManager;
    myProject = project;
    myP4EnvHelper = P4EnvHelper.getConfigHelper(myProject);
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
      for (Pair<VirtualFile, P4ConnectionParameters> parameters : multipleConnections.getAllConnectionParameters()) {
        if (Objects.equals(parameters.second.getConfigFileName(), fileName)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isIgnoredFileName(@NotNull String fileName) {
    if (fileName.equals(myP4EnvHelper.getP4Ignore())) {
      return true;
    }

    PerforceMultipleConnections multipleConnections = myConnectionManager.getMultipleConnectionObject();
    if (multipleConnections != null) {
      for (Pair<VirtualFile, P4ConnectionParameters> parameters : multipleConnections.getAllConnectionParameters()) {
        if (Objects.equals(parameters.second.getIgnoreFileName(), fileName)) {
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
