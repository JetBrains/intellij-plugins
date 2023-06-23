package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public final class PerforceReadOnlyFileStateManager {
  private static final Logger LOG = Logger.getInstance(PerforceReadOnlyFileStateManager.class);

  private final Project myProject;
  private final PerforceDirtyFilesHandler myDirtyFilesHandler;
  private final ProjectLevelVcsManager myVcsManager;
  private final Object myLock = new Object();
  private final ApplicationActivationListener myFrameStateListener = new ApplicationActivationListener() {
    @Override
    public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
      processFocusLost();
    }
  };
  private final Set<VirtualFile> myPreviousAddedSnapshot = new HashSet<>();

  private volatile boolean myHasLostFocus;

  public PerforceReadOnlyFileStateManager(Project project, PerforceDirtyFilesHandler dirtyFilesHandler) {
    myProject = project;
    myDirtyFilesHandler = dirtyFilesHandler;
    myVcsManager = ProjectLevelVcsManager.getInstance(myProject);
  }

  public void activate(@NotNull Disposable parentDisposable) {
    Disposer.register(parentDisposable, this::deactivate);

    VirtualFileManager.getInstance().addVirtualFileListener(new MyVfsListener(), parentDisposable);
    MessageBusConnection appConnection = ApplicationManager.getApplication().getMessageBus().connect(parentDisposable);
    appConnection.subscribe(ApplicationActivationListener.TOPIC, myFrameStateListener);
  }

  private void deactivate() {
    myHasLostFocus = true;
  }

  public void getChanges(final VcsDirtyScope dirtyScope, final ChangelistBuilder builder, final ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    final Set<VirtualFile> newAdded = getAddedFilesInCurrentChangesView(addGate);
    synchronized (myLock) {
      progress.checkCanceled();
      recheckPreviouslyAddedFiles(newAdded);
      recheckWhatUnversionedRefreshNeeded(dirtyScope);
    }

    Set<String> missingFiles = myDirtyFilesHandler.scanAndGetMissingFiles(progress);

    Set<String> locallyDeleted = findLocallyDeletedMissingFiles(addGate, missingFiles);
    for (String path : locallyDeleted) {
      builder.processLocallyDeletedFile(VcsUtil.getFilePath(path, false));
    }
  }

  private Set<VirtualFile> getAddedFilesInCurrentChangesView(ChangeListManagerGate addGate) {
    final Set<VirtualFile> set = new HashSet<>();
    for (LocalChangeList list : addGate.getListsCopy()) {
      for (Change change : list.getChanges()) {
        ContentRevision afterRevision = change.getAfterRevision();
        if (FileStatus.ADDED.equals(change.getFileStatus()) && afterRevision != null) {
          final VirtualFile file = afterRevision.getFile().getVirtualFile();
          if (file != null && fileIsUnderP4Root(file)) {
            set.add(file);
          }
        }
      }
    }
    return set;
  }

  private void recheckWhatUnversionedRefreshNeeded(VcsDirtyScope dirtyScope) {
    if (myHasLostFocus && dirtyScope.wasEveryThingDirty()) {
      LOG.info("--- recheck missing");
      myHasLostFocus = false;
      myDirtyFilesHandler.rescanIfProblems();
    }
  }

  private void recheckPreviouslyAddedFiles(Set<VirtualFile> newAdded) {
    final HashSet<VirtualFile> copy = new HashSet<>(myPreviousAddedSnapshot);
    copy.removeAll(newAdded);
    myPreviousAddedSnapshot.clear();
    myPreviousAddedSnapshot.addAll(newAdded);
    if (!copy.isEmpty()) {
      myDirtyFilesHandler.reportRecheck(copy);
    }
  }

  private static Set<String> findLocallyDeletedMissingFiles(ChangeListManagerGate addGate, Set<String> missingFiles) {
    Set<String> locallyDeleted = new HashSet<>();
    for (String path : missingFiles) {
      if (!FileStatus.DELETED.equals(addGate.getStatus(VcsUtil.getFilePath(new File(path))))) {
        locallyDeleted.add(path);
      }
    }
    return locallyDeleted;
  }

  public void processFocusLost() {
    myHasLostFocus = true;
  }

  public void discardUnversioned() {
    myDirtyFilesHandler.scheduleTotalRescan();
  }

  private class MyVfsListener implements VirtualFileListener {

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
      VirtualFile file = event.getFile();
      if (fileIsUnderP4Root(file) && VirtualFile.PROP_WRITABLE.equals(event.getPropertyName())) {
        myDirtyFilesHandler.reportRecheck(file);
      }
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
      VirtualFile file = event.getFile();
      if (fileIsUnderP4Root(file) && !file.isWritable()) {
        myDirtyFilesHandler.reportRecheck(file);
      }
    }

    @Override
    public void fileCreated(@NotNull final VirtualFileEvent event) {
      if (!fileIsUnderP4Root(event.getFile())) return;
      myDirtyFilesHandler.reportRecheck(event.getFile());
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
      if (!fileIsUnderP4Root(event.getFile())) return;
      myDirtyFilesHandler.reportRecheck(event.getFile());
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
      if (!fileIsUnderP4Root(event.getFile())) return;
      myDirtyFilesHandler.reportRecheck(event.getFile());
    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
      if (!fileIsUnderP4Root(event.getFile())) return;
      myDirtyFilesHandler.reportDelete(event.getFile());
    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
      if (!fileIsUnderP4Root(event.getFile())) return;
      myDirtyFilesHandler.reportDelete(event.getFile());
    }
  }

  private boolean fileIsUnderP4Root(final VirtualFile file) {
    if (myProject.isDisposed()) {
      return false;
    }

    if (ChangeListManager.getInstance(myProject).isIgnoredFile(file)) {
      return false;
    }

    AbstractVcs vcs = myVcsManager.getVcsFor(file);
    return vcs != null && PerforceVcs.getKey().equals(vcs.getKeyInstanceMethod());
  }
}
