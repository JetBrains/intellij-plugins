package com.intellij.flex.uiDesigner;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class InvalidPropertyException extends Exception {
  private PsiElement psiElement;
  
  public PsiElement getPsiElement() {
    return psiElement;
  }

  public InvalidPropertyException(PsiElement psiElement, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key,
                                  Object... params) {
    super(FlexUIDesignerBundle.message(key, params));
    this.psiElement = psiElement;
  }

  public InvalidPropertyException(String message, PsiElement psiElement) {
    super(message);
    this.psiElement = psiElement;
  }

  public InvalidPropertyException(@PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params));
  }

  public InvalidPropertyException(@NotNull Throwable e, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params), e);
  }
}