package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractJsTestFileStructureBuilder {

  @NotNull
  public abstract JsTestFileStructure buildJsTestFileStructure(@NotNull JSFile jsFile);

}
