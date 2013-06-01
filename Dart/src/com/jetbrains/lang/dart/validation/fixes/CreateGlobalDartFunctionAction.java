package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartExecutionScope;
import org.jetbrains.annotations.NotNull;

public class CreateGlobalDartFunctionAction extends CreateDartFunctionActionBase {
  public CreateGlobalDartFunctionAction(@NotNull String name) {
    super(name);
  }

  @NotNull
  @Override
  public String getName() {
    return DartBundle.message("dart.create.global.function.fix.name", myFunctionName);
  }

  @Override
  protected PsiElement getScopeBody(PsiElement element) {
    return PsiTreeUtil.getTopmostParentOfType(element, DartExecutionScope.class);
  }
}
