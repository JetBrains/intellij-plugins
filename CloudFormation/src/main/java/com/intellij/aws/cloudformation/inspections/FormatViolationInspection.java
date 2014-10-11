package com.intellij.aws.cloudformation.inspections;

import com.intellij.aws.cloudformation.CloudFormationFormatChecker;
import com.intellij.aws.cloudformation.CloudFormationPsiUtils;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormatViolationInspection extends LocalInspectionTool {
  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (!CloudFormationPsiUtils.isCloudFormationFile(file)) {
      return null;
    }

    final CloudFormationFormatChecker checker = new CloudFormationFormatChecker(manager, isOnTheFly);
    checker.file(file);
    return ArrayUtil.toObjectArray(checker.getProblems(), ProblemDescriptor.class);
  }
}
