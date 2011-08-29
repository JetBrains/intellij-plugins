package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTestFileStructure {

  private final JSFile myJsFile;

  public AbstractTestFileStructure(@NotNull JSFile jsFile) {
    myJsFile = jsFile;
  }

  @NotNull
  public JSFile getJsFile() {
    return myJsFile;
  }

}
