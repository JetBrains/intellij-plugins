// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.JSLinterConfigurable;
import com.intellij.lang.javascript.linter.JSLinterView;
import com.intellij.lang.javascript.linter.NewLinterView;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.service.TslintLanguageServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public class TsLintConfigurable extends JSLinterConfigurable<TsLintState> {
  @NonNls public static final String SETTINGS_JAVA_SCRIPT_LINTERS_TSLINT = "settings.javascript.linters.tslint";

  public TsLintConfigurable(@NotNull Project project) {
    super(project, TsLintConfiguration.class, false);
  }

  public TsLintConfigurable(@NotNull Project project, boolean fullModeDialog) {
    super(project, TsLintConfiguration.class, fullModeDialog);
  }

  @NotNull
  @Override
  protected JSLinterView<TsLintState> createView() {
    return new NewTslintView(myProject, getDisplayName(), new TslintPanel(getProject(), isFullModeDialog(), false));
  }

  @NotNull
  @Override
  public String getId() {
    return SETTINGS_JAVA_SCRIPT_LINTERS_TSLINT;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return TsLintBundle.message("settings.javascript.linters.tslint.configurable.name");
  }

  @Override
  public void apply() throws ConfigurationException {
    super.apply();
    final TsLintState state = getExtendedState(TsLintConfiguration.class).getState();
    NodePackage nodePackage = state.getNodePackageRef().getConstantPackage();
    if (nodePackage != null && !nodePackage.isEmptyPath() && state.isAllowJs()) {
      if (!checkPackageVersionForJs(nodePackage.getVersion())) {
        throw new ConfigurationException(TsLintBundle.message("linting.javascript.is.not.supported.for.this.version.of.tslint"));
      }
    }
    TslintLanguageServiceManager.getInstance(myProject).terminateServices();
  }

  private static boolean checkPackageVersionForJs(@Nullable SemVer semVer) {
    return semVer != null && semVer.getMajor() >= 4;
  }

  private static class NewTslintView extends NewLinterView<TsLintState> {

    private final TslintPanel myPanel;

    NewTslintView(Project project, String displayName, TslintPanel panel) {
      super(project, displayName, panel.createComponent(), "tslint.json");
      myPanel = panel;
    }

    @NotNull
    @Override
    protected TsLintState getStateWithConfiguredAutomatically() {
      return TsLintState.DEFAULT
        .withInterpreterRef(NodeJsInterpreterRef.createProjectRef())
        .withLinterPackage(AutodetectLinterPackage.INSTANCE);
    }

    @Override
    protected void handleEnabledStatusChanged(boolean enabled) {
      myPanel.handleEnableStatusChanged(enabled);
    }

    @Override
    protected void setState(@NotNull TsLintState state) {
      myPanel.setState(state);
    }

    @NotNull
    @Override
    protected TsLintState getState() {
      return myPanel.getState();
    }
  }
}
