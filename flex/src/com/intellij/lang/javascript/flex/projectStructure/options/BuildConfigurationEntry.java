package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModulePointer;
import com.intellij.openapi.module.ModulePointerManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildConfigurationEntry extends DependencyEntry {

  private final ModulePointer myModulePointer;

  private final String myBcName;

  public BuildConfigurationEntry(Module module, @NotNull String bcName) {
    this(ModulePointerManager.getInstance(module.getProject()).create(module), bcName);
  }

  public BuildConfigurationEntry(Project project, String moduleName, @NotNull String bcName) {
    this(ModulePointerManager.getInstance(project).create(moduleName), bcName);
  }

  public BuildConfigurationEntry(ModulePointer modulePointer, @NotNull String bcName) {
    myModulePointer = modulePointer;
    myBcName = bcName;
  }

  public String getModuleName() {
    return myModulePointer.getModuleName();
  }

  @Nullable
  public Module getModule() {
    return myModulePointer.getModule();
  }

  public String getBcName() {
    return myBcName;
  }

  @Override
  public BuildConfigurationEntry getCopy() {
    BuildConfigurationEntry copy = new BuildConfigurationEntry(myModulePointer, myBcName);
    super.applyTo(copy);
    return copy;
  }
}
