package com.intellij.flex.uiDesigner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

class TestDocumentProblemManager extends DocumentProblemManager {
  private static final Set<ProblemDescriptor> EMPTY_EXPECTED_PROBLEMS = new THashSet<ProblemDescriptor>();

  private static Set<ProblemDescriptor> expectedProblems = EMPTY_EXPECTED_PROBLEMS;

  public static void setExpectedProblems(@Nullable Set<ProblemDescriptor> value) {
    expectedProblems = value == null ? EMPTY_EXPECTED_PROBLEMS : value;
  }

  @Override
  public void report(Project project, ProblemsHolder problems) {
    for (ProblemDescriptor problem : problems.getProblems()) {
      if (!expectedProblems.remove(problem)) {
        StringBuilder builder = new StringBuilder("Unexpected problem: ");
        throw new AssertionError(toString(problem, builder));
      }
    }
  }

  @Override
  public void report(Project project, String message, MessageType messageType) {
    throw new AssertionError(message);
  }
}
