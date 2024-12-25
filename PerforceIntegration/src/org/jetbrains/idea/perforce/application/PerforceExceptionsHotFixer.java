package org.jetbrains.idea.perforce.application;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
import com.intellij.ide.errorTreeView.HotfixData;
import com.intellij.ide.errorTreeView.HotfixGate;
import com.intellij.ide.errorTreeView.SimpleErrorData;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ActionType;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsExceptionsHotFixer;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import com.intellij.util.Consumer;
import com.intellij.util.ui.MutableErrorTreeView;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;
import java.util.*;

@Service(Service.Level.PROJECT)
public final class PerforceExceptionsHotFixer implements VcsExceptionsHotFixer {

  private final MyListChecker myUpdateChecker;

  public PerforceExceptionsHotFixer(final Project project) {
    List<MyChecker> myCheckers = Collections.singletonList(new MyClobberWriteableChecker(project));
    myUpdateChecker = new MyListChecker(myCheckers);
  }

  @Override
  public Map<HotfixData, List<VcsException>> groupExceptions(final ActionType type, List<VcsException> exceptions) {
    if (ActionType.update.equals(type)) {
      return myUpdateChecker.process(exceptions);
    }
    return null;
  }

  private static List<VcsException> getOrCreate(final Map<HotfixData, List<VcsException>> map, final HotfixData data) {
    return map.computeIfAbsent(data, k -> new ArrayList<>());
  }

  private static final class MyListChecker {
    private final List<MyChecker> myCheckers;

    private MyListChecker(final List<MyChecker> checkers) {
      myCheckers = checkers;
    }

    public Map<HotfixData, List<VcsException>> process(final List<VcsException> list) {
      List<VcsException> aDefault = null;
      final Map<HotfixData, List<VcsException>> result = new HashMap<>();
      for (VcsException exception : list) {
        boolean found = false;
        for (MyChecker checker : myCheckers) {
          if (checker.check(exception)) {
            final List<VcsException> excList = getOrCreate(result, checker.getKey());
            excList.add(checker.convert(exception));
            found = true;
            break;
          }
        }
        if (! found) {
          if (aDefault == null) {
            aDefault = new ArrayList<>();
          }
          aDefault.add(exception);
        }
      }
      if (aDefault != null) {
        result.put(null, aDefault);
      }

      return result;
    }
  }

  private abstract static class MyChecker {
    private final HotfixData myKey;

    MyChecker(HotfixData key) {
      myKey = key;
    }

    protected abstract boolean check(final VcsException exc);

    public VcsException convert(final VcsException e) {
      return e;
    }

    public HotfixData getKey() {
      return myKey;
    }
  }

  private static class MyClobberWriteableHotfix implements Consumer<HotfixGate> {
    private final Project myProject;

    MyClobberWriteableHotfix(Project project) {
      myProject = project;
    }

    @Override
    public void consume(final HotfixGate hotfixGate) {
      ProgressManager.getInstance().run(new Task.Backgroundable(myProject, PerforceBundle.message("activity.opening.file.for.edit"), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          final String name = hotfixGate.getGroupName();
          final List<Object> childData = hotfixGate.getView().getGroupChildrenData(name);

          final List<SimpleErrorData> processed = new ArrayList<>();
          final List<VirtualFile> processedFiles = new ArrayList<>();
          final List<SimpleErrorData> failed = new ArrayList<>();
          try {
            edit(childData, processed, processedFiles, failed);
          }
          catch (ProcessCanceledException e) {
            for (Object child : childData) {
              if (child instanceof VirtualFile vf) {
                failed.add(createErrorData(vf, PerforceBundle.message("hotfix.op.canceled")));
              }
            }
          }

          final MutableErrorTreeView view = hotfixGate.getView();
          view.removeGroup(name);

          if (! processed.isEmpty()) {
            view.addFixedHotfixGroup(PerforceBundle.message("hotfix.cannot.clobber.files"), processed);
            refreshVfs(processedFiles);
          }
          if (! failed.isEmpty()) {
            view.addHotfixGroup(new HotfixData(MyHotfixes.FIX_CLOBBER_WRITEABLES, PerforceBundle.message("hotfix.cannot.clobber.files"),
                                 ' ' + PerforceBundle.message("hotfix.open.for.edit"), MyClobberWriteableHotfix.this), failed);
          }
        }

        @Override
        public void onCancel() {
          onSuccess();
        }

        @Override
        public void onSuccess() {
          hotfixGate.getView().reload();
        }
      });
    }

    private void refreshVfs(final List<VirtualFile> processedFiles) {
      RefreshQueue.getInstance().refresh(true, false, () -> {
        VcsDirtyScopeManager.getInstance(myProject).filesDirty(processedFiles, null);
      }, processedFiles);
    }

    private void edit(final List<Object> childData, final List<SimpleErrorData> processed, final List<VirtualFile> processedFiles,
                      final List<SimpleErrorData> failed) {
      final PerforceRunner runner = PerforceRunner.getInstance(myProject);
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();

      for (Object child : childData) {
        if (child instanceof VirtualFile vf) {
          if (indicator != null) {
            indicator.checkCanceled();
          }
          final P4File p4File = P4File.create(VcsUtil.getFilePath(vf));
          try {
            final FStat p4FStat = p4File.getFstat(myProject, true);

            if (FStat.STATUS_ON_SERVER_AND_LOCAL != p4FStat.status) {
              failed.add(createErrorData(vf, PerforceBundle.message("hotfix.skipped.status.unknown")));
            }
            else if (FStat.LOCAL_CHECKED_IN != p4FStat.local) {
              failed.add(createErrorData(vf, PerforceBundle.message("hotfix.skipped.not.checked")));
            } else {
              final String complaint = PerforceVcs.getFileNameComplaint(p4File);
              if (complaint != null) {
                failed.add(createErrorData(vf, PerforceBundle.message("message.text.filename.non.acceptable", complaint)));
              } else {
                runner.edit(p4File);
                processed.add(createFixedData(vf));
                processedFiles.add(vf);
              }
            }
          }
          catch (VcsException e) {
            failed.add(createErrorData(vf, e.getMessage()));
          }
        }
      }
    }

    private static SimpleErrorData createFixedData(final VirtualFile vf) {
      return new SimpleErrorData(ErrorTreeElementKind.ERROR, new String[] {vf.getPath()}, vf);
    }

    private static SimpleErrorData createErrorData(final VirtualFile vf, final String comment) {
      // todo message format
      return new SimpleErrorData(ErrorTreeElementKind.ERROR, new String[] {vf.getPath() + " (fix failed: " + comment + ")"}, vf);
    }
  }

    private static final class MyClobberWriteableChecker extends MyChecker {
      private static final String ourClobberWriteable = "Can't clobber writable file";
      private final Project myProject;

      private MyClobberWriteableChecker(final Project project) {
      super(new HotfixData(MyHotfixes.FIX_CLOBBER_WRITEABLES, PerforceBundle.message("hotfix.cannot.clobber.files"),
                           ' ' + PerforceBundle.message("hotfix.open.for.edit"), new MyClobberWriteableHotfix(project)));
        myProject = project;
      }

      @Override
      public VcsException convert(final VcsException e) {
        final VirtualFile vf = e.getVirtualFile();
        if (vf == null) {
          return e;
        }
        final VcsException newE = new VcsException(vf.getPresentableUrl());
        newE.setVirtualFile(vf);
        return newE;
      }

      @Override
      protected boolean check(final VcsException exc) {
      final String[] messages = exc.getMessages();
      if (messages.length > 0 && messages[0].startsWith(ourClobberWriteable)) {

        String filePathCandidate = messages[0].substring(ourClobberWriteable.length());
        filePathCandidate = PerforceManager.getInstance(myProject).convertP4ParsedPath(null, filePathCandidate);
        filePathCandidate = FileUtil.toSystemDependentName(filePathCandidate);
        LocalFileSystem lfs = LocalFileSystem.getInstance();
        File ioFile = new File(filePathCandidate);
        VirtualFile vf = lfs.findFileByIoFile(ioFile);
        if (vf == null) {
          vf = lfs.refreshAndFindFileByIoFile(ioFile);
        }
        if (vf != null) {
          exc.setVirtualFile(vf);
          return true;
        }
      }
        return false;
      }
  }

  private interface MyHotfixes {
    String FIX_CLOBBER_WRITEABLES = "FIX_CLOBBER_WRITEABLES";
  }
}
