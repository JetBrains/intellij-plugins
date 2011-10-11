package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTestFileStructureBuilder {

  @NotNull
  public abstract AbstractTestFileStructure buildTestFileStructure(@NotNull JSFile jsFile);

}
