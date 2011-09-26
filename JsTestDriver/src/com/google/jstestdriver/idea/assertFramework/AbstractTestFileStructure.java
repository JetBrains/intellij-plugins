package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTestFileStructure {

  private final JSFile myJsFile;

  public AbstractTestFileStructure(@NotNull JSFile jsFile) {
    myJsFile = jsFile;
  }

  @NotNull
  public JSFile getJsFile() {
    return myJsFile;
  }

  @Nullable
  public abstract JstdRunElement findJstdRunElement(@NotNull TextRange textRange);
}
