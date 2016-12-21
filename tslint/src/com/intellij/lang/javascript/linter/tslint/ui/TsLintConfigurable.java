package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.JSLinterConfigurable;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.project.Project;
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
    return "TSLint";
  }
}
