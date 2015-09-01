/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.refactoring;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.dart.server.GetRefactoringConsumer;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatusEntry;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatusSeverity;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The LTK wrapper around an Analysis Server refactoring.
 */
public abstract class ServerRefactoring {
  @NotNull protected final String kind;

  @NotNull private final String file;
  private final int offset;
  private final int length;

  private final Set<Integer> pendingRequestIds = Sets.newHashSet();
  @Nullable private RefactoringStatus serverErrorStatus;
  @Nullable private RefactoringStatus initialStatus;
  @Nullable private RefactoringStatus optionsStatus;
  @Nullable private RefactoringStatus finalStatus;
  @Nullable private SourceChange change;

  private int lastId = 0;
  @Nullable private ServerRefactoringListener listener;

  public ServerRefactoring(@NotNull String kind, @NotNull String file, int offset, int length) {
    this.kind = kind;
    this.file = file;
    this.offset = offset;
    this.length = length;
  }

  @NotNull
  public RefactoringStatus checkFinalConditions() {
    ProgressManager.getInstance().run(new Task.Modal(null, "Checking final conditions", true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        setOptions(false, indicator);
      }
    });
    if (serverErrorStatus != null) {
      return serverErrorStatus;
    }
    RefactoringStatus result = new RefactoringStatus();
    result.merge(optionsStatus);
    result.merge(finalStatus);
    return result;
  }

  @NotNull
  public RefactoringStatus checkInitialConditions() {
    ProgressManager.getInstance().run(new Task.Modal(null, "Checking initial conditions", true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        setOptions(true, indicator);
      }
    });
    if (serverErrorStatus != null) {
      return serverErrorStatus;
    }
    if (initialStatus != null) {
      return  initialStatus;
    }
    return new RefactoringStatus();
  }

  @Nullable
  public SourceChange getChange() {
    return change;
  }

  /**
   * Returns this {@link RefactoringOptions} subclass instance.
   */
  @Nullable
  protected abstract RefactoringOptions getOptions();

  /**
   * Sets the received {@link RefactoringFeedback}.
   */
  protected abstract void setFeedback(@NotNull RefactoringFeedback feedback);

  public void setListener(@Nullable ServerRefactoringListener listener) {
    this.listener = listener;
  }

  protected void setOptions(boolean validateOnly, ProgressIndicator indicator) {
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
    final boolean success = DartAnalysisServerService.getInstance()
      .edit_getRefactoring(kind, file, offset, length, validateOnly, options, new GetRefactoringConsumer() {
        @Override
        public void computedRefactorings(List<RefactoringProblem> initialProblems,
                                         List<RefactoringProblem> optionsProblems,
                                         List<RefactoringProblem> finalProblems,
                                         RefactoringFeedback feedback,
                                         SourceChange _change,
                                         List<String> potentialEdits) {
          if (feedback != null) {
            setFeedback(feedback);
          }
          initialStatus = toRefactoringStatus(initialProblems);
          optionsStatus = toRefactoringStatus(optionsProblems);
          finalStatus = toRefactoringStatus(finalProblems);
          change = _change;
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
        if (indicator.isCanceled()) {
          throw new ProcessCanceledException();
        }
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
