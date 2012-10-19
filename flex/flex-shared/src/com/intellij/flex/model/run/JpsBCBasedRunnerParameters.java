package com.intellij.flex.model.run;

import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.module.JpsTypedModule;

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

  @Nullable
  public JpsFlexBuildConfiguration getBC(final JpsProject project) {
    if (!myModuleName.isEmpty() && !myBCName.isEmpty()) {
      for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : project.getModules(JpsFlexModuleType.INSTANCE)) {
        if (module.getName().equals(myModuleName)) {
          return module.getProperties().findConfigurationByName(myBCName);
        }
      }
    }

    return null;
  }
}
