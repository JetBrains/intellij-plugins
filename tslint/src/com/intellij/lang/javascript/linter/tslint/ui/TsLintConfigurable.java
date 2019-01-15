package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.JSLinterConfigurable;
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
  protected JSLinterBaseView<TsLintState> createView() {
    return new TsLintView(getProject(), isFullModeDialog());
  }

  @NotNull
  @Override
  public String getId() {
    return SETTINGS_JAVA_SCRIPT_LINTERS_TSLINT;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return JSBundle.message("settings.javascript.linters.tslint.configurable.name");
  }

  @Override
  public void apply() throws ConfigurationException {
    super.apply();
    final TsLintState state = getExtendedState(TsLintConfiguration.class).getState();
    NodePackage nodePackage = state.getNodePackageRef().getConstantPackage();
    if (nodePackage != null && !nodePackage.isEmptyPath() && state.isAllowJs()) {
      if (!checkPackageVersionForJs(nodePackage.getVersion())) {
        throw new ConfigurationException("Linting JavaScript is not supported for this version of TSLint.");
      }
    }
    TslintLanguageServiceManager.getInstance(myProject).terminateServices();
  }

  private static boolean checkPackageVersionForJs(@Nullable SemVer semVer) {
    return semVer != null && semVer.getMajor() >= 4;
  }
}
