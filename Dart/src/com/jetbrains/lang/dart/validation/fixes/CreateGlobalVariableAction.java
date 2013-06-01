package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartExecutionScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateGlobalVariableAction extends CreateVariableActionBase {
  public CreateGlobalVariableAction(String name) {
    super(name, false);
  }

  @NotNull
  @Override
  public String getName() {
    return DartBundle.message("dart.create.global.variable", myName);
  }

  @Nullable
  @Override
  protected PsiElement getScopeBody(PsiElement element) {
    return PsiTreeUtil.getTopmostParentOfType(element, DartExecutionScope.class);
  }
}
