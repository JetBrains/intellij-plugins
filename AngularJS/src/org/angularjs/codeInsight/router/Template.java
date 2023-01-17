package org.angularjs.codeInsight.router;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Template {
  private final @NotNull String myPath;
  private final SmartPsiElementPointer<PsiElement> myPointer;
  private Map<String, SmartPsiElementPointer<PsiElement>> myViewPlaceholders;
  private Map<String, UiRouterState> myStateLinks;

  public Template(@NotNull String path, @Nullable SmartPsiElementPointer<PsiElement> file) {
    myPath = path;
    myPointer = file;
  }

  public @NotNull String getPath() {
    return myPath;
  }

  public SmartPsiElementPointer<PsiElement> getPointer() {
    return myPointer;
  }

  public Map<String, SmartPsiElementPointer<PsiElement>> getViewPlaceholders() {
    return myViewPlaceholders;
  }

  public Map<String, UiRouterState> getStateLinks() {
    return myStateLinks;
  }

  public void setViewPlaceholders(Map<String, SmartPsiElementPointer<PsiElement>> viewPlaceholders) {
    myViewPlaceholders = viewPlaceholders;
  }

  public void setStateLinks(Map<String, UiRouterState> stateLinks) {
    myStateLinks = stateLinks;
  }
}
