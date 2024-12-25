package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.LastUnchangedContentTracker;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.rollback.DefaultRollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4RevertOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PerforceOfflineRollbackEnvironment extends DefaultRollbackEnvironment {
  private final Logger LOG = Logger.getInstance(PerforceOfflineRollbackEnvironment.class);

  private final Project myProject;

  public PerforceOfflineRollbackEnvironment(final Project project) {
    myProject = project;
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getRollbackOperationName() {
    return PerforceBundle.message("operation.name.revert");
  }

  @Override
  public void rollbackChanges(List<? extends Change> changes, final List<VcsException> _exceptions, final @NotNull RollbackProgressListener listener) {
    final List<? super VcsException> exceptions = Collections.synchronizedList(_exceptions);

    List<Runnable> actions = new ArrayList<>();

    for (final Change c : changes) {
      final ContentRevision beforeRevision = c.getBeforeRevision();
      if (beforeRevision != null) {
        try {
          final ContentRevision afterRevision = c.getAfterRevision();
          final boolean isRenameOrMove;
          final VirtualFile file;
          if (afterRevision != null && !afterRevision.getFile().equals(beforeRevision.getFile())) {
            file = afterRevision.getFile().getVirtualFile();
            isRenameOrMove = true;
          }
          else {
            file = beforeRevision.getFile().getVirtualFile();
            isRenameOrMove = false;
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug("before rollback " + c + "; " + beforeRevision);
            if (file != null) {
              LOG.debug("has last content " + (LastUnchangedContentTracker.getLastUnchangedContent(file) != null));
            }
          }
          final String content = beforeRevision.getContent();
          if (content != null && file != null) {
            actions.add(() -> {
              if (myProject.isDisposed()) return;
              listener.accept(c);
              VcsOperationLog.getInstance(myProject).addToLog(new P4RevertOperation(c));
              try {
                file.setWritable(true);
                VfsUtil.saveText(file, content);
                if (isRenameOrMove) {
                  handleMoveRename(beforeRevision, file, afterRevision);
                }
                file.setWritable(false);
              }
              catch (IOException e) {
                exceptions.add(new VcsException(e));
              }
            });
          }
          else {
            LOG.debug("contentUnavailable: content " + (content != null) + ", file " + file);
            exceptions.add(contentUnavailable(beforeRevision.getFile().getPath()));
          }
        }
        catch (VcsException ex) {
          exceptions.add(ex);
        }
      }
      else {
        VcsOperationLog.getInstance(myProject).addToLog(new P4RevertOperation(c));
      }
    }

    executeActions(listener, exceptions, actions);
  }

  private static void executeActions(RollbackProgressListener listener, List<? super VcsException> exceptions, List<? extends Runnable> actions) {
    if (!exceptions.isEmpty()) {
      return;
    }

    listener.determinate();
    for (Runnable action : actions) {
      WriteAction.runAndWait(action::run);
    }
  }

  private static VcsException contentUnavailable(final String file) {
    return new VcsException(PerforceBundle.message("error.cannot.revert.file.original.content.is.not.available.offline", file));
  }

  private void handleMoveRename(ContentRevision beforeRevision, VirtualFile file, ContentRevision afterRevision) throws IOException {
    final String oldName = beforeRevision.getFile().getName();
    final FilePath oldParentPath = beforeRevision.getFile().getParentPath();
    final VirtualFile newParent = file.getParent();
    if (oldParentPath != null && newParent != null) {
      if (!FileUtil.toSystemIndependentName(oldParentPath.getPath()).equals(newParent.getPath())) {
        final File oldParentFile = new File(oldParentPath.getPath());
        //noinspection ResultOfMethodCallIgnored
        oldParentFile.mkdirs();
        final VirtualFile oldParentVFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(oldParentFile);
        if (oldParentVFile != null) {
          file.move(this, oldParentVFile);
        }
      }
    }
    if (!file.getName().equals(oldName)) {
      file.rename(this, oldName);
    }
    VcsDirtyScopeManager.getInstance(myProject).fileDirty(beforeRevision.getFile());
    VcsDirtyScopeManager.getInstance(myProject).fileDirty(afterRevision.getFile());
  }

  @Override
  public void rollbackMissingFileDeletion(List<? extends FilePath> files, final List<? super VcsException> exceptions,
                                          final RollbackProgressListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void rollbackModifiedWithoutCheckout(List<? extends VirtualFile> files,
                                              List<? super VcsException> _exceptions,
                                              final RollbackProgressListener listener) {
    final List<? super VcsException> exceptions = Collections.synchronizedList(_exceptions);

    List<Runnable> actions = new ArrayList<>();

    for (final VirtualFile file : files) {
      final byte[] content = LastUnchangedContentTracker.getLastUnchangedContent(file);
      if (content == null) {
        exceptions.add(contentUnavailable(file.getPath()));
      } else {
        actions.add(() ->{
            if (myProject.isDisposed()) return;
            LOG.debug("rollbackModifiedWithoutCheckout file = " + file);
            listener.accept(file);
            try {
              file.setWritable(true);
              file.setBinaryContent(content);
              file.setWritable(false);
              VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
            }
            catch (IOException e) {
              exceptions.add(new VcsException(e));
            }
        });
      }
    }

    executeActions(listener, exceptions, actions);
  }
}