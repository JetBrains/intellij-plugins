package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartStatements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateLocalVariableAction extends CreateVariableActionBase {
  public CreateLocalVariableAction(String name) {
    super(name, false);
  }

  @NotNull
  @Override
  public String getName() {
    return DartBundle.message("dart.create.local.variable", myName);
  }

  @Nullable
  @Override
  protected PsiElement getScopeBody(PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, DartStatements.class);
  }
}
