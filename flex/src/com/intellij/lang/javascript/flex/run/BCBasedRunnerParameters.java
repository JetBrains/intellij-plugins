package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BCBasedRunnerParameters implements Cloneable {
  @NotNull private String myModuleName = "";
  @NotNull private String myBCName = "";

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

  public Pair<Module, FlexIdeBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    if (myModuleName.isEmpty() || myBCName.isEmpty()) {
      throw new RuntimeConfigurationError(FlexBundle.message("bc.not.specified"));
    }

    final Module module = ModuleManager.getInstance(project).findModuleByName(myModuleName);
    if (module == null || !(ModuleType.get(module) instanceof FlexModuleType)) {
      throw new RuntimeConfigurationError(FlexBundle.message("bc.not.specified"));
    }

    final FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(myBCName);
    if (bc == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("module.does.not.contain.bc", myModuleName, myBCName));
    }

    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), module.getName()));
    }

    return Pair.create(module, bc);
  }

  protected BCBasedRunnerParameters clone() {
    try {
      return (BCBasedRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final BCBasedRunnerParameters that = (BCBasedRunnerParameters)o;

    if (!myBCName.equals(that.myBCName)) return false;
    if (!myModuleName.equals(that.myModuleName)) return false;

    return true;
  }

  public int hashCode() {
    assert false;
    return super.hashCode();
  }

  public void handleBuildConfigurationsRename(final Map<Pair<String, String>, String> renamedConfigs) {
    for (Pair<String, String> oldModuleAndBc : renamedConfigs.keySet()) {
      if (oldModuleAndBc.first.equals(myModuleName) && oldModuleAndBc.second.equals(myBCName)) {
        myBCName = renamedConfigs.get(oldModuleAndBc);
        break;
      }
    }
  }

  public void handleModulesRename(final Map<String, String> renamedModules) {
    for (String oldName : renamedModules.keySet()) {
      if (oldName.equals(myModuleName)) {
        myModuleName = renamedModules.get(oldName);
        break;
      }
    }
  }
}
