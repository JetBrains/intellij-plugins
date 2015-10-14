package com.intellij.flex.uiDesigner;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class InvalidPropertyException extends Exception {
  private PsiElement psiElement;
  
  public PsiElement getPsiElement() {
    return psiElement;
  }

  public InvalidPropertyException(PsiElement psiElement, @PropertyKey(resourceBundle = FlashUIDesignerBundle.BUNDLE) String key,
                                  Object... params) {
    super(FlashUIDesignerBundle.message(key, params));
    this.psiElement = psiElement;
  }

  public InvalidPropertyException(String message, PsiElement psiElement) {
    super(message);
    this.psiElement = psiElement;
  }

  public InvalidPropertyException(@PropertyKey(resourceBundle = FlashUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlashUIDesignerBundle.message(key, params));
  }

  public InvalidPropertyException(@NotNull Throwable e, @PropertyKey(resourceBundle = FlashUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlashUIDesignerBundle.message(key, params), e);
  }
}