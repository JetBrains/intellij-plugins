package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.ArrayUtil.toStringArray;

/**
 * LTK wrapper around Analysis Server 'Extract Local Variable' refactoring.
 */
public class ServerExtractLocalVariableRefactoring extends ServerRefactoring {
  private final ExtractLocalVariableOptions options = new ExtractLocalVariableOptions("name", true);
  private ExtractLocalVariableFeedback feedback;

  public ServerExtractLocalVariableRefactoring(@NotNull final Project project,
                                               @NotNull final VirtualFile file,
                                               final int offset,
                                               final int length) {
    super(project, "Extract Local Variable", RefactoringKind.EXTRACT_LOCAL_VARIABLE, file, offset, length);
  }

  @NotNull
  public int[] getCoveringExpressionOffsets() {
    return DartAnalysisServerService.getInstance(getProject()).getConvertedOffsets(getFile(), feedback.getCoveringExpressionOffsets());
  }

  @NotNull
  public int[] getCoveringExpressionLengths() {
    return DartAnalysisServerService.getInstance(getProject())
      .getConvertedLengths(getFile(), feedback.getCoveringExpressionOffsets(), feedback.getCoveringExpressionLengths());
  }

  @NotNull
  public String[] getNames() {
    return toStringArray(feedback.getNames());
  }

  @NotNull
  public int[] getOccurrencesOffsets() {
    return DartAnalysisServerService.getInstance(getProject()).getConvertedOffsets(getFile(), feedback.getOffsets());
  }

  @NotNull
  public int[] getOccurrencesLengths() {
    return DartAnalysisServerService.getInstance(getProject()).getConvertedLengths(getFile(), feedback.getOffsets(), feedback.getLengths());
  }

  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  public void setExtractAll(boolean extractAll) {
    options.setExtractAll(extractAll);
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    feedback = (ExtractLocalVariableFeedback)_feedback;
  }

  public void setName(@NotNull String name) {
    options.setName(name);
    setOptions(true, null);
  }
}
