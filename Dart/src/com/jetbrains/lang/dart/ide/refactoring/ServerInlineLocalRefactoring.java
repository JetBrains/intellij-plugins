package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.dartlang.analysis.server.protocol.InlineLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringOptions;
import org.jetbrains.annotations.NotNull;

/**
 * LTK wrapper around Analysis Server 'Inline Local' refactoring.
 */
public class ServerInlineLocalRefactoring extends ServerRefactoring {
  private String variableName;
  private int occurrences;

  public ServerInlineLocalRefactoring(@NotNull final Project project, @NotNull final VirtualFile file, final int offset, final int length) {
    super(project, "Inline Local Variable", RefactoringKind.INLINE_LOCAL_VARIABLE, file, offset, length);
  }

  public int getOccurrences() {
    return occurrences;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  protected RefactoringOptions getOptions() {
    return null;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    InlineLocalVariableFeedback feedback = (InlineLocalVariableFeedback)_feedback;
    variableName = feedback.getName();
    occurrences = feedback.getOccurrences();
  }
}
