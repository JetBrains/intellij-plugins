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
  private final @NotNull String myName;
  private final @Nullable String myTemplateUrl;
  private final @Nullable VirtualFile myTemplateFile;
  private final @Nullable SmartPsiElementPointer<PsiElement> myPointer;
  private @Nullable SmartPsiElementPointer<PsiElement> myTemplatePointer;

  public UiView(@NotNull String name, @Nullable String template, final @Nullable VirtualFile templateFile,
                @Nullable SmartPsiElementPointer<PsiElement> pointer) {
    myName = name;
    myTemplateUrl = template;
    myTemplateFile = templateFile;
    myPointer = pointer;
  }

  public @NotNull String getName() {
    return myName;
  }

  public @Nullable String getTemplate() {
    return myTemplateUrl;
  }

  public @Nullable SmartPsiElementPointer<PsiElement> getPointer() {
    return myPointer;
  }

  public @Nullable VirtualFile getTemplateFile() {
    return myTemplateFile;
  }

  public @Nullable SmartPsiElementPointer<PsiElement> getTemplatePointer() {
    return myTemplatePointer;
  }

  public void setTemplatePointer(@Nullable SmartPsiElementPointer<PsiElement> templatePointer) {
    myTemplatePointer = templatePointer;
  }
}
