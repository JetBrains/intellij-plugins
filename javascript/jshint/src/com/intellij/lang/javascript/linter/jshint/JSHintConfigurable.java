package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.JSLinterConfigurable;
import com.intellij.openapi.options.Configurable.NoScroll;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JSHintConfigurable extends JSLinterConfigurable<JSHintState> implements NoScroll {

  public static final String ID = "Settings.JavaScript.Linters.JSHint";

  public JSHintConfigurable(@NotNull Project project) {
    super(project, JSHintConfiguration.class, false);
  }

  public JSHintConfigurable(@NotNull Project project, boolean fullModeDialog) {
    super(project, JSHintConfiguration.class, fullModeDialog);
  }

  @Override
  public @Nls String getDisplayName() {
    return JSHintBundle.message("settings.javascript.linters.jshint.configurable.name");
  }

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  protected @NotNull JSLinterBaseView<JSHintState> createView() {
    return new JSHintView(getProject(), isFullModeDialog());
  }
}
