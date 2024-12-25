package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Listen P4 config files changes located in the project content roots and thus available in VFS.
 * <p/>
 * For listening config file changes located outside project roots consider to use {@link PerforceExternalConfigTracker}
 */
public class PerforceP4ConfigVirtualFileListener implements VirtualFileListener {
  private final PerforceConnectionManagerI myConnectionManager;
  private final Project myProject;
  private static final Logger LOG = Logger.getInstance(PerforceP4ConfigVirtualFileListener.class);
  private final P4EnvHelper myP4EnvHelper;

  public PerforceP4ConfigVirtualFileListener(@NotNull PerforceConnectionManagerI connectionManager,
                                             @NotNull Project project,
                                             @NotNull Disposable parentDisposable) {
    myConnectionManager = connectionManager;
    myProject = project;
    myP4EnvHelper = P4EnvHelper.getConfigHelper(myProject);
    project.getMessageBus().connect(parentDisposable).subscribe(P4ConfigListener.TOPIC, new MyPerforceConfigListener());
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
    processFileChanged(fileName);
  }

  private void processFileChanged(@NotNull String fileName) {
    if (isIgnoredFileName(fileName)) {
      LOG.debug("p4ignore file changed");
      PerforceVcs.getInstance(myProject).getOnlineChangeProvider().discardCache();
    }

    if (isConfigFileName(fileName)) {
      LOG.debug("p4config file changed");
      myConnectionManager.updateConnections();
    }
  }

   private class MyPerforceConfigListener implements P4ConfigListener {

     @Override
     public void notifyConfigChanged(@NotNull String configPath) {
       try {
         Path path = Paths.get(configPath);
         Path fileNamePath = path.getFileName();

         if (fileNamePath != null) {
           processFileChanged(fileNamePath.toString());
         }
         else {
           LOG.warn("Invalid config path: " + configPath);
         }
       }
       catch (InvalidPathException e) {
         LOG.warn("Invalid config path: " + configPath, e);
       }
     }
   }
}
