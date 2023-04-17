// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@State(name = "PrettierConfiguration", storages = @Storage("prettier.xml"))
public final class PrettierConfiguration implements JSNpmLinterState<PrettierConfiguration>,
                                                    PersistentStateComponent<PrettierConfiguration.State> {

  @ApiStatus.Internal
  public enum ConfigurationMode {
    DISABLED,
    AUTOMATIC,
    MANUAL
  }

  @ApiStatus.Internal
  public static class State {
    @OptionTag("myConfigurationMode")
    public ConfigurationMode configurationMode = null;
    @OptionTag("myRunOnSave")
    public boolean runOnSave = PRETTIER_ON_SAVE_DEFAULT;
    @OptionTag("myRunOnReformat")
    public boolean runOnReformat = PRETTIER_ON_REFORMAT_DEFAULT;
    @OptionTag("myFilesPattern")
    public @NotNull String filesPattern = PRETTIER_FILES_PATTERN_DEFAULT;
  }

  @NonNls private static final String PACKAGE_PROPERTY = "prettierjs.PrettierConfiguration.Package";

  private static final boolean PRETTIER_ON_SAVE_DEFAULT = false;
  private static final boolean PRETTIER_ON_REFORMAT_DEFAULT = true;
  @NonNls private static final String PRETTIER_FILES_PATTERN_DEFAULT = "{**/*,*}.{js,ts,jsx,tsx,vue,astro}";

  private static final NodePackageDescriptor PKG_DESC = new NodePackageDescriptor(PrettierUtil.PACKAGE_NAME);

  private final @NotNull Project myProject;
  private @NotNull State myState = new State();

  public PrettierConfiguration(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  public static PrettierConfiguration getInstance(@NotNull Project project) {
    return project.getService(PrettierConfiguration.class);
  }

  @Override
  public @NotNull State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    myState = state;
    if (state.configurationMode == null) {
      var pkg = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY);
      state.configurationMode = pkg != null && !pkg.isBlank()
                                ? ConfigurationMode.MANUAL
                                : ConfigurationMode.DISABLED;
    }
  }

  @NotNull
  @Override
  public NodePackageRef getNodePackageRef() {
    return NodePackageRef.create(getPackage(null));
  }

  @Override
  public PrettierConfiguration withLinterPackage(@NotNull NodePackageRef nodePackageRef) {
    NodePackage newPackage = nodePackageRef.getConstantPackage();
    assert newPackage != null : getClass().getSimpleName() + "does not support non-constant package";
    PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, newPackage.getSystemDependentPath());
    return this;
  }

  @NotNull
  public NodePackage getPackage(@Nullable PsiElement context) {
    if (isDisabled()) {
      return PKG_DESC.createPackage("");
    }
    if (myState.configurationMode == ConfigurationMode.MANUAL) {
      String value = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY);
      if (value != null && !value.isBlank()) {
        return PKG_DESC.createPackage(value);
      }
    }
    if (context != null && context.getContainingFile() != null && isAutomatic()) {
      var contextFile = context.getContainingFile().getOriginalFile().getVirtualFile();
      final List<NodePackage> available = new NodePackageDescriptor(PrettierUtil.PACKAGE_NAME)
        .listAvailable(myProject, NodeJsInterpreterManager.getInstance(myProject).getInterpreter(),
                       contextFile, false, true);
      if (!available.isEmpty()) {
        return available.get(0);
      }
    }
    NodePackage pkg = PKG_DESC.findUnambiguousDependencyPackage(myProject);
    if (pkg == null && (context == null || !isAutomatic())) {
      pkg = NodePackage.findDefaultPackage(myProject, PrettierUtil.PACKAGE_NAME,
                                           NodeJsInterpreterManager.getInstance(myProject).getInterpreter());
    }
    if (pkg != null) {
      if (pkg.isValid(myProject)) {
        PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, pkg.getSystemDependentPath());
      }
      return pkg;
    }
    return PKG_DESC.createPackage("");
  }

  public boolean isRunOnSave() {
    return !isDisabled() && myState.runOnSave;
  }

  public boolean isRunOnReformat() {
    return !isDisabled() && (isAutomatic() || myState.runOnReformat);
  }

  @NotNull
  public String getFilesPattern() {
    return myState.filesPattern;
  }

  private boolean isDisabled() {
    return myState.configurationMode == ConfigurationMode.DISABLED;
  }

  private boolean isAutomatic() {
    return myState.configurationMode == ConfigurationMode.AUTOMATIC;
  }
}
