package org.jetbrains.idea.perforce.application;

import com.intellij.ide.FrameStateListener;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.*;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Irina.Chernushina
 */
public class PerforceReadOnlyFileStateManager {
  private static final Logger LOG = Logger.getInstance(PerforceReadOnlyFileStateManager.class);

  private final Project myProject;
  private final ProjectLevelVcsManager myVcsManager;
  private final PerforceUnversionedTracker myUnversionedTracker;
  private final Object myLock = new Object();
  private final FrameStateListener myFrameStateListener = new FrameStateListener() {
    @Override
    public void onFrameDeactivated() {
      processFocusLost();
    }
  };
  private final Set<VirtualFile> myPreviousAddedSnapshot = new HashSet<>();

  private MessageBusConnection myConnection;

  private volatile boolean myPreviousRescanProblem;
  private volatile boolean myHasLostFocus;

  public PerforceReadOnlyFileStateManager(Project project) {
    myProject = project;
    myVcsManager = ProjectLevelVcsManager.getInstance(myProject);
    myUnversionedTracker = new PerforceUnversionedTracker(project);
  }

  PerforceUnversionedTracker getUnversionedTracker() {
    return myUnversionedTracker;
  }

  public void activate(@NotNull Disposable parentDisposable) {
    final Runnable scheduleTotalRescan = () -> {
      myUnversionedTracker.isActive = false;
      myUnversionedTracker.totalRescan();
    };

    myConnection = myProject.getMessageBus().connect();
    myConnection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, () -> scheduleTotalRescan.run());
    VirtualFileManager.getInstance().addVirtualFileListener(new MyVfsListener(), parentDisposable);
    FrameStateManager.getInstance().addListener(myFrameStateListener);
    myConnection.subscribe(PerforceSettings.OFFLINE_MODE_EXITED, scheduleTotalRescan);
  }

  public void deactivate() {
    myUnversionedTracker.isActive = false;
    myConnection.disconnect();
    FrameStateManager.getInstance().removeListener(myFrameStateListener);
    myHasLostFocus = true;
  }

  public void getChanges(final VcsDirtyScope dirtyScope, final ChangelistBuilder builder, final ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    final Set<VirtualFile> newAdded = getAddedFilesInCurrentChangesView(addGate);
    ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException> scanner;
    synchronized (myLock) {
      progress.checkCanceled();
      recheckPreviouslyAddedFiles(newAdded);
      recheckWhatUnversionedRefreshNeeded(dirtyScope);

      scanner = myUnversionedTracker.createScanner();
    }

    progress.checkCanceled();
    UnversionedScopeScanner.ScanResult result = rescan(scanner);
    progress.checkCanceled();

    for (VirtualFile file : result.allLocalFiles) {
      myUnversionedTracker.markUnknown(file);
    }
    myUnversionedTracker.markUnversioned(result.localOnly);

    dirtyScope.iterateExistingInsideScope(vf -> {
      progress.checkCanceled();
      if (!isKnownToPerforce(addGate, vf)) {
        if (myUnversionedTracker.isUnversioned(vf)) {
          builder.processUnversionedFile(vf);
        } else if (myUnversionedTracker.isIgnored(vf)) {
          builder.processIgnoredFile(vf);
        }
      }
      return true;
    });

    Set<String> locallyDeleted = findLocallyDeletedMissingFiles(addGate, result.missingFiles);
    for (String path : locallyDeleted) {
      builder.processLocallyDeletedFile(VcsUtil.getFilePath(path, false));
    }
  }

  private static boolean isKnownToPerforce(ChangeListManagerGate addGate, VirtualFile file) {
    FileStatus status = addGate.getStatus(file);
    if (LOG.isDebugEnabled()) {
      LOG.debug("status " + status + " for " + file);
    }
    return status != null;
  }

  private UnversionedScopeScanner.ScanResult rescan(final ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException> scanner) throws VcsException {
    myUnversionedTracker.isActive = true;
    myPreviousRescanProblem = false;
    try {
      return scanner.compute();
    }
    catch (VcsException e) {
      myPreviousRescanProblem = true;
      throw e;
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
      if (myPreviousRescanProblem) {
        myUnversionedTracker.totalRescan();
      }
    }
  }

  private void recheckPreviouslyAddedFiles(Set<VirtualFile> newAdded) {
    final HashSet<VirtualFile> copy = new HashSet<>(myPreviousAddedSnapshot);
    copy.removeAll(newAdded);
    myPreviousAddedSnapshot.clear();
    myPreviousAddedSnapshot.addAll(newAdded);
    if (!copy.isEmpty()) {
      myUnversionedTracker.reportRecheck(copy);
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
    myUnversionedTracker.totalRescan();
  }

  private class MyVfsListener implements VirtualFileListener {

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
      VirtualFile file = event.getFile();
      if (fileIsUnderP4Root(file) && VirtualFile.PROP_WRITABLE.equals(event.getPropertyName())) {
        myUnversionedTracker.reportRecheck(file);
      }
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
      VirtualFile file = event.getFile();
      if (fileIsUnderP4Root(file) && !file.isWritable()) {
        myUnversionedTracker.reportRecheck(file);
      }
    }

    @Override
    public void fileCreated(@NotNull final VirtualFileEvent event) {
      if (! fileIsUnderP4Root(event.getFile())) return;
      processCreated(event.getFile());
    }

    private void processCreated(VirtualFile root) {
      myUnversionedTracker.reportRecheck(root);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
      if (! fileIsUnderP4Root(event.getFile())) return;
      processCreated(event.getFile());
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
      if (! fileIsUnderP4Root(event.getFile())) return;
      processCreated(event.getFile());
    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
      if (! fileIsUnderP4Root(event.getFile())) return;
      myUnversionedTracker.reportDelete(event.getFile());
    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
      if (! fileIsUnderP4Root(event.getFile())) return;
      myUnversionedTracker.reportDelete(event.getFile());
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
