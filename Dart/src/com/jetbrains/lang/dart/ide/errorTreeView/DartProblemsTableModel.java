// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.analysis.problemsView.AnalysisProblem;
import com.intellij.analysis.problemsView.AnalysisProblemsPresentationHelper;
import com.intellij.analysis.problemsView.AnalysisProblemsTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class DartProblemsTableModel extends AnalysisProblemsTableModel {
  public DartProblemsTableModel(@NotNull AnalysisProblemsPresentationHelper presentationHelper) {
    super(presentationHelper);
  }

  /**
   * If {@code selectedProblem} was removed and similar one added again then this method returns the added one,
   * so that the caller could update selected row in the table
   */
  @Nullable
  public AnalysisProblem setErrorsAndReturnReplacementForSelection(@NotNull Set<String> filePaths,
                                                                   @NotNull List<? extends AnalysisProblem> problems,
                                                                   @Nullable AnalysisProblem selectedProblem) {
    final boolean selectedProblemRemoved = removeRowsForFilesInSet(filePaths, selectedProblem);
    return addProblemsAndReturnReplacementForSelection(problems, selectedProblemRemoved ? selectedProblem : null);
  }

  private boolean removeRowsForFilesInSet(@NotNull Set<String> filePaths, @Nullable final AnalysisProblem selectedProblem) {
    // Looks for regions in table items that should be removed and removes them.
    // For performance reasons we try to call removeRows() as rare as possible, that means with regions as big as possible.
    // Logic is based on the fact that all errors for each particular file are stored continuously in the myItems model

    boolean selectedProblemRemoved = false;

    int matchedFilesCount = 0;

    for (int i = getRowCount() - 1; i >= 0; i--) {
      final AnalysisProblem problem = getItem(i);
      if (filePaths.contains(problem.getSystemIndependentPath())) {
        matchedFilesCount++;
        final int lastRowToDelete = i;

        if (problem == selectedProblem) {
          selectedProblemRemoved = true;
        }

        AnalysisProblem lastProblemForCurrentFile = problem;

        int j = i - 1;
        while (j >= 0) {
          final AnalysisProblem previousProblem = getItem(j);

          if (previousProblem.getSystemIndependentPath().equals(lastProblemForCurrentFile.getSystemIndependentPath())) {
            // previousProblem should be removed from the table as well
            j--;

            if (previousProblem == selectedProblem) {
              selectedProblemRemoved = true;
            }

            continue;
          }

          if (filePaths.contains(previousProblem.getSystemIndependentPath())) {
            matchedFilesCount++;
            // continue iterating the table because we met a range of problems for another file that also should be removed
            lastProblemForCurrentFile = previousProblem;
            j--;

            if (previousProblem == selectedProblem) {
              selectedProblemRemoved = true;
            }

            continue;
          }

          break;
        }

        final int firstRowToDelete = j + 1;
        removeRows(firstRowToDelete, lastRowToDelete);

        if (matchedFilesCount == filePaths.size()) {
          break;
        }

        //noinspection AssignmentToForLoopParameter
        i = j + 1; // rewind according to the amount of removed rows
      }
    }

    return selectedProblemRemoved;
  }
}
