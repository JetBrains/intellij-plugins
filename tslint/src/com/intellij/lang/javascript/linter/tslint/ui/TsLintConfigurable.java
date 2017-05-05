package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.JSLinterConfigurable;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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
    if (!StringUtil.isEmptyOrSpaces(state.getPackagePath()) && state.isAllowJs()) {
      if (!checkPackageVersionForJs(state.getPackagePath())) throw new ConfigurationException("Linting JavaScript is not supported for this version of TSLint.");
    }
    final TsLintLanguageService service = TsLintLanguageService.getService(myProject);
    service.terminateStartedProcess(false);
  }

  private static boolean checkPackageVersionForJs(final String packagePath) {
    final VirtualFile packageVf = LocalFileSystem.getInstance().findFileByPath(packagePath);
    if (packageVf != null) {
      final VirtualFile packageJson = packageVf.findChild(PackageJsonUtil.FILE_NAME);
      if (packageJson != null) {
        final PackageJsonData data = PackageJsonUtil.getOrCreateData(packageJson);
        return data.getVersion() != null && data.getVersion().getMajor() >= 4;
      }
    }
    return false;
  }
}
