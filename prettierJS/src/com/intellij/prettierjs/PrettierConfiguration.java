// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
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

@Service(Service.Level.PROJECT)
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
    @OptionTag("myConfigurationMode") public @Nullable ConfigurationMode configurationMode = null;
    @OptionTag("myRunOnSave")
    public boolean runOnSave = PRETTIER_ON_SAVE_DEFAULT;
    @OptionTag("myRunOnReformat")
    public boolean runOnReformat = PRETTIER_ON_REFORMAT_DEFAULT;
    @OptionTag("myFilesPattern")
    public @NotNull String filesPattern = PRETTIER_FILES_PATTERN_DEFAULT;
    public @NotNull String customIgnorePath = "";
    public boolean formatFilesOutsideDependencyScope = PRETTIER_FORMAT_FILES_OUTSIDE_DEPENDENCY_SCOPE_DEFAULT;
    public boolean codeStyleSettingsModifierEnabled = true;
  }

  private static final @NonNls String PACKAGE_PROPERTY = "prettierjs.PrettierConfiguration.Package";

  private static final boolean PRETTIER_ON_SAVE_DEFAULT = false;
  private static final boolean PRETTIER_FORMAT_FILES_OUTSIDE_DEPENDENCY_SCOPE_DEFAULT = true;
  private static final boolean PRETTIER_ON_REFORMAT_DEFAULT = false;
  private static final @NonNls String PRETTIER_FILES_PATTERN_DEFAULT = "**/*.{js,ts,jsx,tsx,cjs,cts,mjs,mts,vue,astro}";

  private static final NodePackageDescriptor PKG_DESC = new NodePackageDescriptor(PrettierUtil.PACKAGE_NAME);
  private static final @NotNull NodePackage EMPTY_PACKAGE = PKG_DESC.createPackage("");

  private final @NotNull Project myProject;
  private @NotNull State myState = new State();

  public PrettierConfiguration(@NotNull Project project) {
    myProject = project;
  }

  public static @NotNull PrettierConfiguration getInstance(@NotNull Project project) {
    return project.getService(PrettierConfiguration.class);
  }

  @Override
  public @NotNull State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    myState = state;
  }

  @Override
  public @NotNull NodePackageRef getNodePackageRef() {
    return NodePackageRef.create(getPackage(null));
  }

  @Override
  public PrettierConfiguration withLinterPackage(@NotNull NodePackageRef nodePackageRef) {
    NodePackage newPackage = nodePackageRef.getConstantPackage();
    assert newPackage != null : getClass().getSimpleName() + "does not support non-constant package";
    PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, newPackage.getSystemDependentPath());
    return this;
  }

  /**
   * The only allowed usage of this method is in {@link PrettierConfigurable#createPanel()} for the `packageField` binding.
   * This method is necessary for the correct 'Apply' button state on the Prettier page in Settings:
   * it must be exactly the opposite of {@code PrettierConfiguration#withLinterPackage()}.
   */
  NodePackageRef getPackageRefForPackageFieldBindingInConfigurable() {
    String value = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY);
    if (value != null && !value.isBlank()) {
      return NodePackageRef.create(PKG_DESC.createPackage(value));
    }
    return NodePackageRef.create(EMPTY_PACKAGE);
  }

  public @NotNull NodePackage getPackage(@Nullable PsiElement context) {
    if (isDisabled()) {
      return EMPTY_PACKAGE;
    }
    if (getConfigurationMode() == ConfigurationMode.MANUAL) {
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
    return EMPTY_PACKAGE;
  }

  public boolean isRunOnSave() {
    return !isDisabled() && myState.runOnSave;
  }

  public boolean isRunOnReformat() {
    return !isDisabled() && (isAutomatic() || myState.runOnReformat);
  }

  public @NotNull String getFilesPattern() {
    return myState.filesPattern;
  }

  public @NotNull String getCustomIgnorePath() {
    return myState.customIgnorePath;
  }

  public boolean getFormatFilesOutsideDependencyScope() {
    return isManual() && myState.formatFilesOutsideDependencyScope;
  }

  public boolean getCodeStyleSettingsModifierEnabled() {
    return !isDisabled() && myState.codeStyleSettingsModifierEnabled;
  }

  public ConfigurationMode getConfigurationMode() {
    ConfigurationMode mode = myState.configurationMode;
    if (mode == null) {
      var pkg = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY);
      return pkg != null && !pkg.isBlank()
             ? ConfigurationMode.MANUAL
             : ConfigurationMode.DISABLED;
    }
    return mode;
  }

  public boolean isDisabled() {
    return getConfigurationMode() == ConfigurationMode.DISABLED;
  }

  public boolean isDefaultConfigurationMode() {
    State defaultState = new State();
    return this.myState.configurationMode == defaultState.configurationMode;
  }

  public boolean isAutomatic() {
    return getConfigurationMode() == ConfigurationMode.AUTOMATIC;
  }

  private boolean isManual() {
    return getConfigurationMode() == ConfigurationMode.MANUAL;
  }
}
