// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(name = "PrettierConfiguration", storages = @Storage("prettier.xml"))
public final class PrettierConfiguration implements JSNpmLinterState<PrettierConfiguration>,
                                                    PersistentStateComponent<PrettierConfiguration.State> {

  static class State {
    public boolean myRunOnSave = PRETTIER_ON_SAVE_DEFAULT;
    public boolean myRunOnReformat = PRETTIER_ON_REFORMAT_DEFAULT;
    public @NotNull String myFilesPattern = PRETTIER_FILES_PATTERN_DEFAULT;
  }

  @NonNls private static final String PACKAGE_PROPERTY = "prettierjs.PrettierConfiguration.Package";

  @NonNls private static final String OLD_PRETTIER_ON_SAVE_PROPERTY = "run.prettier.on.save";
  @NonNls private static final String OLD_PRETTIER_FILES_PATTERN_PROPERTY = "prettier.files.pattern";

  private static final boolean PRETTIER_ON_SAVE_DEFAULT = false;
  private static final boolean PRETTIER_ON_REFORMAT_DEFAULT = false;
  @NonNls private static final String PRETTIER_FILES_PATTERN_DEFAULT = "{**/*,*}.{js,ts,jsx,tsx}";

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

    PropertiesComponent.getInstance(myProject).setValue(OLD_PRETTIER_ON_SAVE_PROPERTY, null);
    PropertiesComponent.getInstance(myProject).setValue(OLD_PRETTIER_FILES_PATTERN_PROPERTY, null);
  }

  @Override
  public void noStateLoaded() {
    // Previously, 'run on save' and 'pattern' values were stored in workspace.xml. Need to load old values if any.
    PropertiesComponent properties = PropertiesComponent.getInstance(myProject);
    boolean oldRunOnSave = properties.getBoolean(OLD_PRETTIER_ON_SAVE_PROPERTY, PRETTIER_ON_SAVE_DEFAULT);
    String oldPattern = properties.getValue(OLD_PRETTIER_FILES_PATTERN_PROPERTY, PRETTIER_FILES_PATTERN_DEFAULT);

    properties.setValue(OLD_PRETTIER_ON_SAVE_PROPERTY, null);
    properties.setValue(OLD_PRETTIER_FILES_PATTERN_PROPERTY, null);

    if (oldRunOnSave != PRETTIER_ON_SAVE_DEFAULT) {
      setRunOnSave(oldRunOnSave);
    }
    if (!PRETTIER_FILES_PATTERN_DEFAULT.equals(oldPattern)) {
      setFilesPattern(oldPattern);
    }
  }

  @NotNull
  @Override
  public NodePackageRef getNodePackageRef() {
    return NodePackageRef.create(getPackage());
  }

  @Override
  public PrettierConfiguration withLinterPackage(@NotNull NodePackageRef nodePackageRef) {
    NodePackage newPackage = nodePackageRef.getConstantPackage();
    assert newPackage != null : getClass().getSimpleName() + "does not support non-constant package";
    PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, newPackage.getSystemDependentPath());
    return this;
  }

  @NotNull
  public NodePackage getPackage() {
    String value = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY);
    if (value != null) {
      return PKG_DESC.createPackage(value);
    }
    NodePackage pkg = PKG_DESC.findUnambiguousDependencyPackage(myProject);
    if (pkg != null) {
      if (pkg.isValid(myProject)) {
        PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, pkg.getSystemDependentPath());
      }
      return pkg;
    }
    return PKG_DESC.createPackage("");
  }

  public boolean isRunOnSave() {
    return myState.myRunOnSave;
  }

  public void setRunOnSave(boolean runOnSave) {
    myState.myRunOnSave = runOnSave;
  }

  public boolean isRunOnReformat() {
    return myState.myRunOnReformat;
  }

  public void setRunOnReformat(boolean runOnReformat) {
    myState.myRunOnReformat = runOnReformat;
  }

  @NotNull
  public String getFilesPattern() {
    return myState.myFilesPattern;
  }

  public void setFilesPattern(@NotNull String filesPattern) {
    myState.myFilesPattern = filesPattern;
  }
}
