package org.angularjs.codeInsight.router;

import com.intellij.openapi.vfs.VirtualFile;
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
  @Nullable private final VirtualFile myTemplateFile;
  @Nullable private final SmartPsiElementPointer<PsiElement> myPointer;
  @Nullable private SmartPsiElementPointer<PsiElement> myTemplatePointer;

  public UiView(@NotNull String name, @Nullable String template, @Nullable final VirtualFile templateFile,
                @Nullable SmartPsiElementPointer<PsiElement> pointer) {
    myName = name;
    myTemplateUrl = template;
    myTemplateFile = templateFile;
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

  @Nullable
  public VirtualFile getTemplateFile() {
    return myTemplateFile;
  }

  @Nullable
  public SmartPsiElementPointer<PsiElement> getTemplatePointer() {
    return myTemplatePointer;
  }

  public void setTemplatePointer(@Nullable SmartPsiElementPointer<PsiElement> templatePointer) {
    myTemplatePointer = templatePointer;
  }
}
