package org.angularjs.codeInsight.router;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 3/8/2016.
 */
public class UiView {
  @NotNull
  private final String myName;
  @Nullable
  private final String myTemplateUrl;
  @Nullable private final SmartPsiElementPointer<PsiElement> myPointer;

  public UiView(@NotNull String name, @Nullable String template, @Nullable SmartPsiElementPointer<PsiElement> pointer) {
    myName = name;
    myTemplateUrl = template;
    myPointer = pointer;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public String getTemplate() {
    return myTemplateUrl;
  }

  @Nullable
  public SmartPsiElementPointer<PsiElement> getPointer() {
    return myPointer;
  }
}
