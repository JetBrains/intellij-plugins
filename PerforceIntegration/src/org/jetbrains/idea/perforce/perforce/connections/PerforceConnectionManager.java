package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.impl.ContentRootChangeListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.platform.backend.workspace.WorkspaceModelTopics;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PerforceConnectionManager implements PerforceConnectionManagerI {
  private static final Logger LOG = Logger.getInstance(PerforceConnectionManager.class);

  private final PerforceSettings mySettings;
  private final Project myProject;

  private PerforceConnectionMapper myConnectionMapper;

  private final Object myLock = new Object();
  private final PerforceConnectionProblemsNotifier myNotifier;
  private boolean isInitializing;

  public PerforceConnectionManager(Project project) {
    myNotifier = PerforceConnectionProblemsNotifier.getInstance(project);
    mySettings = PerforceSettings.getSettings(project);
    myProject = project;
  }

  @Override
  public boolean isInitialized() {
    synchronized (myLock) {
      return myConnectionMapper != null;
    }
  }

  private @NotNull PerforceConnectionMapper getConnectionMapper() {
    synchronized (myLock) {
      if (myConnectionMapper != null) {
        return myConnectionMapper;
      }
    }
    return initialize();
  }

  private @NotNull PerforceConnectionMapper initialize() {
    PerforceConnectionMapper mapper;
    if (isSingletonConnectionUsed()) {
      mapper = SingletonConnection.getInstance(myProject);
    }
    else {
      P4ConnectionCalculator calculator = new P4ConnectionCalculator(myProject);
      calculator.execute();
      mapper = calculator.getMultipleConnections();
    }
    synchronized (myLock) {
      if (myConnectionMapper == null) {
        myConnectionMapper = mapper;
        if (mapper instanceof PerforceMultipleConnections) {
          myNotifier.setProblems(((PerforceMultipleConnections)mapper).hasAnyErrors(), false);
        }
      }
      return mapper;
    }
  }

  @Override
  public @Nullable PerforceMultipleConnections getMultipleConnectionObject() {
    final PerforceConnectionMapper mapper = getConnectionMapper();
    return mapper instanceof PerforceMultipleConnections ? (PerforceMultipleConnections)mapper : null;
  }

  public static PerforceConnectionManagerI getInstance(Project project) {
    return project.getService(PerforceConnectionManagerI.class);
  }

  @Override
  public @NotNull Map<VirtualFile, P4Connection> getAllConnections() {
    return getConnectionMapper().getAllConnections();
  }

  public void scheduleInitialization() {
    synchronized (myLock) {
      if (myConnectionMapper != null || isInitializing) {
        return;
      }

      isInitializing = true;
    }

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (myProject.isDisposed()) {
        isInitializing = false;
        return;
      }

      initialize();
      synchronized (this) {
        isInitializing = false;
      }
    });
  }

  private Project getProject() {
    return myProject;
  }

  public static @Nullable VirtualFile findNearestLiveParentFor(File ioFile) {
    do {
      VirtualFile parent = LocalFileSystem.getInstance().findFileByIoFile(ioFile);
      if (parent != null) return parent;
      ioFile = ioFile.getParentFile();
      if (ioFile == null) return null;
    }
    while (true);
  }

  @Override
  public @Nullable P4Connection getConnectionForFile(@NotNull File file) {
    PerforceConnectionMapper mapper = getConnectionMapper();
    if (mapper instanceof SingletonConnection) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("getConnectionForFile(" + file + ") returning singleton");
      }
      return (P4Connection)mapper;
    }
    final VirtualFile vFile = findNearestLiveParentFor(file);
    if (vFile == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("getConnectionForFile(" + file + ") found no live parent");
      }
      return null;
    }
    return mapper.getConnection(vFile);
  }

  @Override
  public P4Connection getConnectionForFile(@NotNull P4File file) {
    return getConnectionForFile(file.getLocalFile());
  }

  @Override
  public P4Connection getConnectionForFile(final @NotNull VirtualFile file) {
    // todo : also use client roots information
    return getConnectionMapper().getConnection(file);
  }

  @Override
  public boolean isSingletonConnectionUsed() {
    return !mySettings.useP4CONFIG;
  }

  public void startListening(@NotNull Disposable parentDisposable) {
    MessageBusConnection busConnection = getProject().getMessageBus().connect(parentDisposable);
    busConnection.subscribe(WorkspaceModelTopics.CHANGED, new MyContentRootChangeListener());
    busConnection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, this::updateConnections);

    VirtualFileManager.getInstance()
      .addVirtualFileListener(new PerforceP4ConfigVirtualFileListener(this, myProject, parentDisposable), parentDisposable);
    updateConnections();
  }

  @Override
  public void updateConnections() {
    synchronized (myLock) {
      myConnectionMapper = null;
    }
    final PerforceManager manager = PerforceManager.getInstance(getProject());
    manager.configurationChanged();
  }

  @Override
  public boolean isUnderProjectConnections(final @NotNull File file) {
    Set<VirtualFile> allRoots = getConnectionMapper().getAllConnections().keySet();
    return ContainerUtil.or(allRoots, root -> FileUtil.isAncestor(VfsUtilCore.virtualToIoFile(root), file, false));
  }

  private class MyContentRootChangeListener extends ContentRootChangeListener {
    private MyContentRootChangeListener() {
      super(/* skipFileChanges */ false);
    }

    @Override
    public void contentRootsChanged(@NotNull List<? extends VirtualFile> removed, @NotNull List<? extends VirtualFile> added) {
      updateConnections();
    }
  }
}
