package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartExecutionScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateGlobalDartGetterSetterAction extends CreateDartGetterSetterAction {
  public CreateGlobalDartGetterSetterAction(@NotNull String name, boolean isGetter) {
    super(name, isGetter, false);
  }

  @NotNull
  @Override
  public String getName() {
    return myGetter ? DartBundle.message("dart.create.global.getter.fix.name", myFunctionName)
                    : DartBundle.message("dart.create.global.setter.fix.name", myFunctionName);
  }

  @Nullable
  @Override
  protected PsiElement findAnchor(PsiElement element) {
    PsiElement scopeBody = PsiTreeUtil.getTopmostParentOfType(element, DartExecutionScope.class);
    PsiElement result = element;
    while (result.getParent() != null && result.getParent() != scopeBody) {
      result = result.getParent();
    }
    return result;
  }
}
