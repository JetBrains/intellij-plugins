// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.dart.server.GetRefactoringConsumer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatusEntry;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatusSeverity;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The LTK wrapper around an Analysis Server refactoring.
 */
public abstract class ServerRefactoring {
  private final @NotNull Project myProject;
  private final @NotNull @NlsContexts.DialogTitle String myRefactoringProgressTitle;
  private final @NotNull @NonNls String kind;

  private final @NotNull VirtualFile file;
  private final int offset;
  private final int length;

  private final Set<Integer> pendingRequestIds = new HashSet<>();
  private @Nullable RefactoringStatus serverErrorStatus;
  private @Nullable RefactoringStatus initialStatus;
  private @Nullable RefactoringStatus optionsStatus;
  private @Nullable RefactoringStatus finalStatus;
  private @Nullable SourceChange change;
  private final @NotNull Set<String> potentialEdits = new HashSet<>();

  private int lastId = 0;
  private @Nullable ServerRefactoringListener listener;

  public ServerRefactoring(@NotNull Project project,
                           @NotNull @NlsContexts.DialogTitle String refactoringProgressTitle,
                           @NotNull @NonNls String kind,
                           @NotNull VirtualFile file,
                           int offset,
                           int length) {
    myProject = project;
    myRefactoringProgressTitle = refactoringProgressTitle;
    this.kind = kind;
    this.file = file;
    this.offset = offset;
    this.length = length;
  }

  protected @NotNull Project getProject() {
    return myProject;
  }

  protected @NotNull VirtualFile getFile() {
    return file;
  }

  public @Nullable RefactoringStatus checkFinalConditions() {
    ProgressManager.getInstance().run(new Task.Modal(null, myRefactoringProgressTitle, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText(DartBundle.message("progress.text.validating.the.specified.parameters"));
        indicator.setIndeterminate(true);
        setOptions(false, indicator);
      }
    });
    if (serverErrorStatus != null) {
      return serverErrorStatus;
    }
    if (finalStatus == null) {
      return null;
    }
    RefactoringStatus result = new RefactoringStatus();
    result.merge(optionsStatus);
    result.merge(finalStatus);
    return result;
  }

  public @Nullable RefactoringStatus checkInitialConditions() {
    ProgressManager.getInstance().run(new Task.Modal(null, myRefactoringProgressTitle, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText(DartBundle.message("progress.text.checking.availability.at.the.selection"));
        indicator.setIndeterminate(true);
        setOptions(true, indicator);
      }
    });
    if (serverErrorStatus != null) {
      return serverErrorStatus;
    }
    return initialStatus;
  }

  public @Nullable SourceChange getChange() {
    return change;
  }

  /**
   * Returns this {@link RefactoringOptions} subclass instance.
   */
  protected abstract @Nullable RefactoringOptions getOptions();

  public @NotNull Set<String> getPotentialEdits() {
    return potentialEdits;
  }

  /**
   * Sets the received {@link RefactoringFeedback}.
   */
  protected abstract void setFeedback(@NotNull RefactoringFeedback feedback);

  public void setListener(@Nullable ServerRefactoringListener listener) {
    this.listener = listener;
  }

  protected void setOptions(boolean validateOnly, @Nullable ProgressIndicator indicator) {
    // add a new pending request ID
    final int id;
    synchronized (pendingRequestIds) {
      id = ++lastId;
      pendingRequestIds.add(id);
    }
    // do request
    serverErrorStatus = null;
    final CountDownLatch latch = new CountDownLatch(1);
    RefactoringOptions options = getOptions();
    DartAnalysisServerService.getInstance(myProject).updateFilesContent();
    final boolean success = DartAnalysisServerService.getInstance(myProject)
      .edit_getRefactoring(kind, file, offset, length, validateOnly, options, new GetRefactoringConsumer() {
        @Override
        public void computedRefactorings(List<RefactoringProblem> initialProblems,
                                         List<RefactoringProblem> optionsProblems,
                                         List<RefactoringProblem> finalProblems,
                                         RefactoringFeedback feedback,
                                         SourceChange _change,
                                         List<String> _potentialEdits) {
          if (feedback != null) {
            setFeedback(feedback);
          }
          initialStatus = toRefactoringStatus(initialProblems);
          optionsStatus = toRefactoringStatus(optionsProblems);
          finalStatus = toRefactoringStatus(finalProblems);
          change = _change;
          potentialEdits.clear();
          if (_potentialEdits != null) {
            potentialEdits.addAll(_potentialEdits);
          }
          latch.countDown();
          requestDone(id);
        }

        @Override
        public void onError(RequestError requestError) {
          String message = "Server error: " + requestError.getMessage();
          serverErrorStatus = RefactoringStatus.createFatalErrorStatus(message);
          latch.countDown();
          requestDone(id);
        }

        private void requestDone(final int id) {
          synchronized (pendingRequestIds) {
            pendingRequestIds.remove(id);
            notifyListener();
          }
        }
      });
    if (!success) return;
    // wait for completion
    if (indicator != null) {
      while (true) {
        ProgressIndicatorUtils.checkCancelledEvenWithPCEDisabled(indicator);
        boolean done = Uninterruptibles.awaitUninterruptibly(latch, 10, TimeUnit.MILLISECONDS);
        if (done) {
          return;
        }
      }
    }
    else {
      // Wait a very short time, just in case if it can be done fast,
      // so that we don't have to disable UI and re-enable it 2 milliseconds later.
      Uninterruptibles.awaitUninterruptibly(latch, 10, TimeUnit.MILLISECONDS);
      notifyListener();
    }
  }

  private void notifyListener() {
    if (listener != null) {
      boolean hasPendingRequests = !pendingRequestIds.isEmpty();
      RefactoringStatus status = optionsStatus != null ? optionsStatus : new RefactoringStatus();
      listener.requestStateChanged(hasPendingRequests, status);
    }
  }

  private static RefactoringStatusSeverity toProblemSeverity(@NotNull String severity) {
    if (RefactoringProblemSeverity.FATAL.equals(severity)) {
      return RefactoringStatusSeverity.FATAL;
    }
    if (RefactoringProblemSeverity.ERROR.equals(severity)) {
      return RefactoringStatusSeverity.ERROR;
    }
    if (RefactoringProblemSeverity.WARNING.equals(severity)) {
      return RefactoringStatusSeverity.WARNING;
    }
    return RefactoringStatusSeverity.OK;
  }

  private static RefactoringStatus toRefactoringStatus(@NotNull List<RefactoringProblem> problems) {
    RefactoringStatus status = new RefactoringStatus();
    for (RefactoringProblem problem : problems) {
      final String serverSeverity = problem.getSeverity();
      final RefactoringStatusSeverity problemSeverity = toProblemSeverity(serverSeverity);
      final String message = problem.getMessage();
      status.addEntry(new RefactoringStatusEntry(problemSeverity, message));
    }
    return status;
  }

  public interface ServerRefactoringListener {
    void requestStateChanged(boolean hasPendingRequests, @NotNull RefactoringStatus optionsStatus);
  }
}
