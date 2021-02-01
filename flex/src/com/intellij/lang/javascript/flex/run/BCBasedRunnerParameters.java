// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.actions.airpackage.DeviceInfo;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BCBasedRunnerParameters implements Cloneable {
  private @NotNull @NlsSafe String myModuleName = "";
  private @NotNull @NlsSafe String myBCName = "";

  @Nullable private DeviceInfo myDeviceInfo;

  @NotNull
  public @NlsSafe String getModuleName() {
    return myModuleName;
  }

  public void setModuleName(@NotNull @NlsSafe final String moduleName) {
    myModuleName = moduleName;
  }

  @NotNull
  public @NlsSafe String getBCName() {
    return myBCName;
  }

  public void setBCName(final @NotNull @NlsSafe String BCName) {
    myBCName = BCName;
  }

  @Transient
  @Nullable
  public DeviceInfo getDeviceInfo() {
    return myDeviceInfo;
  }

  public void setDeviceInfo(@Nullable final DeviceInfo deviceInfo) {
    myDeviceInfo = deviceInfo;
  }

  public Pair<Module, FlexBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    if (myModuleName.isEmpty() || myBCName.isEmpty()) {
      throw new RuntimeConfigurationError(FlexBundle.message("bc.not.specified"));
    }

    final Module module = ModuleManager.getInstance(project).findModuleByName(myModuleName);
    if (module == null || !(ModuleType.get(module) instanceof FlexModuleType)) {
      throw new RuntimeConfigurationError(FlexBundle.message("bc.not.specified"));
    }

    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(myBCName);
    if (bc == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("module.does.not.contain.bc", myModuleName, myBCName));
    }

    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new RuntimeConfigurationError(FlexCommonBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), module.getName()));
    }

    return Pair.create(module, bc);
  }

  @Override
  protected BCBasedRunnerParameters clone() {
    try {
      return (BCBasedRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BCBasedRunnerParameters that = (BCBasedRunnerParameters)o;
    return Objects.equals(myModuleName, that.myModuleName) &&
           Objects.equals(myBCName, that.myBCName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myModuleName, myBCName);
  }

  public void handleBuildConfigurationsRename(final Map<Pair<String, String>, String> renamedConfigs) {
    for (Pair<String, String> oldModuleAndBc : renamedConfigs.keySet()) {
      if (oldModuleAndBc.first.equals(myModuleName) && oldModuleAndBc.second.equals(myBCName)) {
        myBCName = renamedConfigs.get(oldModuleAndBc);
        break;
      }
    }
  }

  public void handleModulesRename(List<? extends Module> modules, Function<? super Module, String> oldNameProvider) {
    for (Module module : modules) {
      String oldName = oldNameProvider.fun(module);
      if (oldName.equals(myModuleName)) {
        myModuleName = module.getName();
        break;
      }
    }
  }
}
