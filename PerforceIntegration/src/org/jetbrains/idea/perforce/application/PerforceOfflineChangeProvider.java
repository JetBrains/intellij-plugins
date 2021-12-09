package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PerforceOfflineChangeProvider implements ChangeProvider {
  private final Project myProject;

  public PerforceOfflineChangeProvider(final Project project) {
    myProject = project;
  }

  @Override
  public void getChanges(@NotNull VcsDirtyScope dirtyScope, @NotNull ChangelistBuilder builder, @NotNull ProgressIndicator progress,
                         @NotNull final ChangeListManagerGate addGate) throws VcsException {
    builder.reportAdditionalInfo(() -> {
      HyperlinkLabel label = new HyperlinkLabel();
      label.setForeground(JBColor.RED);
      label.setHyperlinkText(PerforceBundle.message("connection.offline") + ' ', PerforceBundle.message("connection.go.online"), "");
      label.addHyperlinkListener(new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            final PerforceSettings settings = PerforceSettings.getSettings(myProject);
            if (!settings.ENABLED) {
              settings.enable();
            }
          }
        }
      });
      return label;
    });

    ChangeListManager clm = ChangeListManager.getInstance(dirtyScope.getProject());

    Set<VirtualFile> writable = PerforceChangeProvider.collectWritableFiles(dirtyScope, true);

    Iterator<VirtualFile> iterator = writable.iterator();
    while (iterator.hasNext()) {
      VirtualFile file = iterator.next();
      if (clm.isIgnoredFile(file)) {
        builder.processIgnoredFile(file);
        iterator.remove();
      }
    }

    final VcsOperationLog opLog = VcsOperationLog.getInstance(myProject);
    Map<String, String> reopenedPaths = opLog.getReopenedPaths();
    List<LastSuccessfulUpdateTracker.PersistentChangeList> changeLists =
      LastSuccessfulUpdateTracker.getInstance(myProject).getChangeLists();
    if (changeLists != null) {
      for (LastSuccessfulUpdateTracker.PersistentChangeList changeList : changeLists) {
        for (LastSuccessfulUpdateTracker.ChangedFile file : changeList.files) {
          String changeListName = changeList.name;
          if (file.beforePath != null && reopenedPaths.containsKey(file.beforePath)) {
            changeListName = reopenedPaths.get(file.beforePath);
          }
          else if (file.afterPath != null && reopenedPaths.containsKey(file.afterPath)) {
            changeListName = reopenedPaths.get(file.afterPath);
          }
          if (changeListName == null) {
            continue;
          }

          FilePath beforePath = createFilePath(file.beforePath);
          FilePath afterPath = createFilePath(file.afterPath);
          if (isInScope(dirtyScope, beforePath, afterPath)) {
            ContentRevision beforeRevision = null;
            ContentRevision afterRevision = null;
            if (beforePath != null) {
              beforeRevision = PerforceCachingContentRevision.createOffline(myProject, beforePath,
                                                                            afterPath != null ? afterPath : beforePath);
            }
            if (afterPath != null) {
              afterRevision = CurrentContentRevision.create(afterPath);
              writable.remove(afterPath.getVirtualFile());
            }
            builder.processChangeInList(new Change(beforeRevision, afterRevision), changeListName, PerforceVcs.getKey());
          }
        }
      }
    }

    List<VcsOperation> list = opLog.getPendingOperations();
    for (VcsOperation op : list) {
      Change c = op.getChange(myProject, addGate);
      if (c != null) {
        FilePath afterPath = ChangesUtil.getAfterPath(c);
        if (isInScope(dirtyScope, ChangesUtil.getBeforePath(c), afterPath)) {
          if (afterPath != null) {
            writable.remove(afterPath.getVirtualFile());
          }
          builder.processChangeInList(c, op.getChangeList(), PerforceVcs.getKey());
        }
      }
    }

    for (VirtualFile file : writable) {
      if (LastUnchangedContentTracker.hasSavedContent(file)) {
        builder.processModifiedWithoutCheckout(file);
      }
      else {
        builder.processUnversionedFile(file);
      }
    }
  }

  private static boolean isInScope(final VcsDirtyScope dirtyScope, final FilePath beforePath, final FilePath afterPath) {
    return (beforePath != null && dirtyScope.belongsTo(beforePath)) || (afterPath != null && dirtyScope.belongsTo(afterPath));
  }

  @Nullable
  private static FilePath createFilePath(final String beforePath) {
    if (beforePath == null) return null;
    return VcsContextFactory.SERVICE.getInstance().createFilePathOn(new File(beforePath), false);
  }

  @Override
  public boolean isModifiedDocumentTrackingRequired() {
    return false;
  }
}