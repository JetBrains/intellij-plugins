package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractTestFileStructure {

  private final JSFile myJsFile;

  public AbstractTestFileStructure(@NotNull JSFile jsFile) {
    myJsFile = jsFile;
  }

  @NotNull
  public JSFile getJsFile() {
    return myJsFile;
  }

  public abstract boolean isEmpty();

  @Nullable
  public abstract JstdRunElement findJstdRunElement(@NotNull TextRange textRange);

  @Nullable
  public abstract PsiElement findPsiElement(@NotNull String testCaseName, @Nullable String testMethodName);

  @NotNull
  public abstract List<String> getTopLevelElements();

  @NotNull
  public abstract List<String> getChildrenOf(@NotNull String topLevelElementName);
}
