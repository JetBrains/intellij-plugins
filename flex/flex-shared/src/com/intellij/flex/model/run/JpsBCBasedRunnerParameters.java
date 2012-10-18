package com.intellij.flex.model.run;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsElementBase;

public abstract class JpsBCBasedRunnerParameters<Self extends JpsBCBasedRunnerParameters<Self>> extends JpsElementBase<Self> {

  @NotNull protected String myModuleName = "";
  @NotNull protected String myBCName = "";

  protected JpsBCBasedRunnerParameters() {
  }

  protected JpsBCBasedRunnerParameters(final Self original) {
    myModuleName = original.myModuleName;
    myBCName = original.myBCName;
  }

  @NotNull
  public String getModuleName() {
    return myModuleName;
  }

  public void setModuleName(@NotNull final String moduleName) {
    myModuleName = moduleName;
  }

  @NotNull
  public String getBCName() {
    return myBCName;
  }

  public void setBCName(@NotNull final String BCName) {
    myBCName = BCName;
  }

  public void applyChanges(@NotNull final Self modified) {
    myModuleName = modified.myModuleName;
    myBCName = modified.myBCName;
  }
}
