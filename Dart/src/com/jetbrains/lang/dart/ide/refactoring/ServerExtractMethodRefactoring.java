package com.jetbrains.lang.dart.ide.refactoring;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.util.ArrayUtil.toStringArray;

/**
 * LTK wrapper around Analysis Server 'Extract Method' refactoring.
 */
public class ServerExtractMethodRefactoring extends ServerRefactoring {
  private final ExtractMethodOptions options =
    new ExtractMethodOptions("returnType", false, "name", ImmutableList.of(), false);
  private ExtractMethodFeedback feedback;

  public ServerExtractMethodRefactoring(@NotNull final Project project,
                                        @NotNull final VirtualFile file,
                                        final int offset,
                                        final int length) {
    super(project, "Extract Method", RefactoringKind.EXTRACT_METHOD, file, offset, length);
  }

  public boolean canExtractGetter() {
    return feedback.canCreateGetter();
  }

  @NotNull
  public String[] getNames() {
    return toStringArray(feedback.getNames());
  }

  public int getOccurrences() {
    return feedback.getOffsets().length;
  }

  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  @NotNull
  public List<RefactoringMethodParameter> getParameters() {
    return options.getParameters();
  }

  @NotNull
  public String getSignature() {
    // TODO(scheglov) consider moving to server
    StringBuilder sb = new StringBuilder();
    sb.append(options.getReturnType());
    sb.append(" ");
    boolean createGetter = options.createGetter();
    if (createGetter) {
      sb.append("get ");
    }
    sb.append(options.getName());
    if (!createGetter) {
      sb.append("(");
      boolean firstParameter = true;
      for (RefactoringMethodParameter parameter : options.getParameters()) {
        if (!firstParameter) {
          sb.append(", ");
        }
        firstParameter = false;
        sb.append(parameter.getType());
        sb.append(" ");
        sb.append(parameter.getName());
      }
      sb.append(")");
    }
    return sb.toString();
  }

  public void setCreateGetter(boolean value) {
    options.setCreateGetter(value);
  }

  public void setExtractAll(boolean extractAll) {
    options.setExtractAll(extractAll);
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    boolean firstFeedback = feedback == null;
    feedback = (ExtractMethodFeedback)_feedback;
    if (firstFeedback) {
      options.setExtractAll(true);
      options.setReturnType(feedback.getReturnType());
      options.setCreateGetter(feedback.canCreateGetter());
      options.setParameters(feedback.getParameters());
    }
  }

  public void setName(@NotNull String name) {
    options.setName(name);
    setOptions(true, null);
  }
}
