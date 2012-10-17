package com.intellij.flex.model.run;

import org.jetbrains.annotations.NotNull;

public class JpsFlashRunnerParameters extends JpsBCBasedRunnerParameters<JpsFlashRunnerParameters> {

  private boolean myOverrideMainClass = false;
  private @NotNull String myOverriddenMainClass = "";
  private @NotNull String myOverriddenOutputFileName = "";

  public JpsFlashRunnerParameters() {
  }

  private JpsFlashRunnerParameters(final JpsFlashRunnerParameters original) {
    myOverrideMainClass = original.myOverrideMainClass;
    myOverriddenMainClass = original.myOverriddenMainClass;
    myOverriddenOutputFileName = original.myOverriddenOutputFileName;
  }

  @NotNull
  public JpsFlashRunnerParameters createCopy() {
    return new JpsFlashRunnerParameters(this);
  }

  public void applyChanges(@NotNull final JpsFlashRunnerParameters modified) {
    super.applyChanges(modified);

    myOverrideMainClass = modified.myOverrideMainClass;
    myOverriddenMainClass = modified.myOverriddenMainClass;
    myOverriddenOutputFileName = modified.myOverriddenOutputFileName;
  }

  // -----------------------

  public boolean isOverrideMainClass() {
    return myOverrideMainClass;
  }

  public void setOverrideMainClass(final boolean overrideMainClass) {
    myOverrideMainClass = overrideMainClass;
  }

  @NotNull
  public String getOverriddenMainClass() {
    return myOverriddenMainClass;
  }

  public void setOverriddenMainClass(@NotNull final String overriddenMainClass) {
    myOverriddenMainClass = overriddenMainClass;
  }

  @NotNull
  public String getOverriddenOutputFileName() {
    return myOverriddenOutputFileName;
  }

  public void setOverriddenOutputFileName(@NotNull final String overriddenOutputFileName) {
    myOverriddenOutputFileName = overriddenOutputFileName;
  }
}
