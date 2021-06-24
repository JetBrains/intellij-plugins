package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LastUnchangedContentTracker;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.AppUIUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.*;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.*;

public final class PerforceVFSListener extends VcsVFSListener {
  private static final Logger LOG = Logger.getInstance(PerforceVFSListener.class);

  private PerforceVFSListener(@NotNull Project project) {
    super(PerforceVcs.getInstance(project));
  }

  @NotNull
  public static PerforceVFSListener createInstance(@NotNull Project project) {
    PerforceVFSListener listener = new PerforceVFSListener(project);
    listener.installListeners();
    return listener;
  }

  @Override
  protected void installListeners() {
    super.installListeners();
    myProject.getMessageBus().connect(myProject).subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override
      public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (VFileEvent event : events) {
          if (event instanceof VFileCreateEvent) {
            if (!(event.getFileSystem() instanceof LocalFileSystem)) continue;
            FilePath filePath = VcsUtil.getFilePath(event.getPath());
            if (!isUnderMyVcs(filePath)) continue; //call event.getFile() only for the file path under Perforce VCS

            VirtualFile file = event.getFile();
            if (file != null) {
              LastUnchangedContentTracker.markTouched(file);
            }
          }
        }
      }
    });
  }

  @Override
  protected boolean filterOutUnknownFiles() {
    return false;
  }

  @Override
  protected boolean isEventAccepted(@NotNull VFileEvent event) {
    return !(event.getRequestor() instanceof PerforceOfflineRollbackEnvironment) && super.isEventAccepted(event);
  }

  @Override
  protected void executeAdd(@NotNull final List<VirtualFile> addedFiles, @NotNull final Map<VirtualFile, VirtualFile> copiedFiles) {
    executeAddWithoutIgnores(addedFiles, copiedFiles,
                             (notIgnoredAddedFiles, copiedFilesMap) -> super.executeAdd(notIgnoredAddedFiles, copiedFilesMap));
  }

  @Override
  protected void executeAddWithoutIgnores(@NotNull List<VirtualFile> addedFiles,
                                          @NotNull Map<VirtualFile, VirtualFile> copiedFiles,
                                          @NotNull ExecuteAddCallback executeAddCallback) {
    saveUnsavedVcsIgnoreFiles();

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      super.executeAdd(addedFiles, copiedFiles);
      return;
    }
    if (!PerforceSettings.getSettings(myProject).ENABLED) {
      AppUIUtil.invokeLaterIfProjectAlive(myProject, () -> super.executeAdd(addedFiles, copiedFiles));
      return;
    }

    ProgressManager.getInstance().run(new Task.Backgroundable(myProject,
                                                              PerforceBundle.message("progress.title.checking.for.ignored.files"),
                                                              false) {
      @Override
      public void run(@NotNull ProgressIndicator pi) {
        MultiMap<P4Connection, VirtualFile> map = FileGrouper.distributeFilesByConnection(addedFiles, myProject);
        for (P4Connection connection : map.keySet()) {
          try {
            addedFiles.removeAll(PerforceUnversionedTracker.getFilesOutsideClientSpec(myProject, connection, map.get(connection)));
          }
          catch (VcsException e) {
            AbstractVcsHelper.getInstance(myProject).showError(e, PerforceBundle.message("perforce.error"));
          }
        }
        AppUIUtil.invokeLaterIfProjectAlive(myProject, () -> executeAddCallback.executeAdd(addedFiles, copiedFiles));
      }
    });
  }

  @Override
  protected void performAdding(@NotNull final Collection<VirtualFile> addedFiles, @NotNull final Map<VirtualFile, VirtualFile> copyFromMap) {

    final String title = PerforceBundle.message("progress.title.running.perforce.commands");
    List<VcsOperation> operations = createOperations(addedFiles, copyFromMap);
    VcsOperationLog.getInstance(myProject).queueOperations(operations, title, PerformInBackgroundOption.ALWAYS_BACKGROUND);
  }

  private List<VcsOperation> createOperations(Collection<VirtualFile> addedFiles, final Map<VirtualFile, VirtualFile> copyFromMap) {
    final String chList = myChangeListManager.getDefaultChangeList().getName();

    final ArrayList<VcsOperation> operations = new ArrayList<>();
    final Processor<VirtualFile> fileProcessor = file -> {
      if (!file.isDirectory()) {
        final VirtualFile copyFrom = copyFromMap.get(file);
        operations.add(copyFrom != null ? new P4CopyOperation(chList, file, copyFrom) : new P4AddOperation(chList, file));
      }
      return true;
    };
    for (VirtualFile file : addedFiles) {
      VfsUtil.processFileRecursivelyWithoutIgnored(file, fileProcessor);
    }
    return operations;
  }

  @NotNull
  @Override
  @SuppressWarnings("UnresolvedPropertyKey")
  protected String getSingleFileAddPromptTemplate() {
    return PerforceBundle.message("confirmation.text.add.files");
  }

  @NotNull
  @Override
  protected String getSingleFileAddTitle() {
    return PerforceBundle.message("confirmation.title.add.files");
  }

  @NotNull
  @Override
  protected String getAddTitle() {
    return PerforceBundle.message("add.select.files");
  }

  @NotNull
  @Override
  protected VcsDeleteType needConfirmDeletion(@NotNull final VirtualFile file) {
    return ChangeListManager.getInstance(myProject).isUnversioned(file) ? VcsDeleteType.IGNORE : VcsDeleteType.CONFIRM;
  }

  @Override
  protected void performDeletion(@NotNull final List<FilePath> filesToDelete) {
    PerforceVcs.getInstance(myProject).getCheckinEnvironment().scheduleMissingFileForDeletion(filesToDelete);
  }

  @Override
  @SuppressWarnings("UnresolvedPropertyKey")
  protected String getSingleFileDeletePromptTemplate() {
    return PerforceBundle.message("confirmation.text.remove.files");
  }

  @Override
  protected String getSingleFileDeleteTitle() {
    return PerforceBundle.message("confirmation.title.remove.files");
  }

  @NotNull
  @Override
  protected String getDeleteTitle() {
    return PerforceBundle.message("delete.select.files");
  }

  @Override
  protected void processMovedFile(@NotNull final VirtualFile file, @NotNull final String newParentPath, @NotNull final String newName) {
    LOG.debug("processMovedFile " + file + " newParentPath=" + newParentPath + " newName=" + newName);
    updateLastUnchangedContent(file, myChangeListManager);
    PerforceCachingContentRevision.removeCachedContent(file);
    P4File.invalidateFstat(file);
    super.processMovedFile(file, newParentPath, newName);
  }

  @Override
  protected void performMoveRename(@NotNull final List<MovedFileInfo> movedFiles) {
    List<VcsOperation> operations = new ArrayList<>();
    for (MovedFileInfo movedFile : movedFiles) {
      operations.add(new P4MoveRenameOperation(ChangeListManager.getInstance(myProject).getDefaultChangeList().getName(),
                                               movedFile.myOldPath, movedFile.myNewPath));
    }

    VcsOperationLog.getInstance(myProject).queueOperations(operations,
                                                           PerforceBundle.message("progress.title.running.perforce.commands"),
                                                           PerformInBackgroundOption.ALWAYS_BACKGROUND);
  }

  @Override
  protected boolean isDirectoryVersioningSupported() {
    return false;
  }

  @Override
  protected void beforeContentsChange(@NotNull VFileContentChangeEvent event) {
    VirtualFile file = event.getFile();
    updateLastUnchangedContent(file, myChangeListManager);

    if (!event.isFromRefresh() && myChangeListManager.getStatus(file) == FileStatus.NOT_CHANGED) {
      final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(file);
      if (connection != null && PerforceChangeProvider.isAllWriteWorkspace(connection, myProject)) {
        asyncEdit(file);
      }
    }
  }

  private void asyncEdit(VirtualFile file) {
    final PerforceVcs vcs = PerforceVcs.getInstance(myProject);
    if (vcs.getAsyncEditedFiles().contains(file)) {
      return;
    }

    vcs.startAsyncEdit(file);

    P4EditOperation op = new P4EditOperation(myChangeListManager.getDefaultListName(), file);
    op.setSuppressErrors(true);
    VcsOperationLog.getInstance(myProject).queueOperations(
      Collections.singletonList(op),
      PerforceBundle.message("progress.title.running.perforce.commands"),
      PerformInBackgroundOption.ALWAYS_BACKGROUND);
  }

  public static void updateLastUnchangedContent(VirtualFile file, final ChangeListManager changeListManager) {
    FileStatus status = changeListManager.getStatus(file);
    if (status == FileStatus.NOT_CHANGED) {
      LastUnchangedContentTracker.updateLastUnchangedContent(file);
    }
  }
}
