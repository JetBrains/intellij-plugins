package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.JSLinterConfigurable;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.JSLinterView;
import com.intellij.lang.javascript.linter.eslint.NewLinterView;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.service.TslintLanguageServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

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
    return TslintUtil.isMultiRootEnabled() && JSLinterUtil.newAutodetectUiEnabled()
           ? new NewTslintView(myProject, getDisplayName(), new TslintPanel(getProject(), isFullModeDialog(), false))
           : new OldTslintView(new TslintPanel(getProject(), isFullModeDialog(), true));
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
    TslintLanguageServiceManager.getInstance(myProject).terminateServices();
  }

  private static class OldTslintView extends JSLinterBaseView<TsLintState> {
    private final TslintPanel myPanel;

    OldTslintView(TslintPanel panel) {
      myPanel = panel;
    }

    @NotNull
    @Override
    protected Component createCenterComponent() {
      return myPanel.createComponent();
    }

    @NotNull
    @Override
    protected TsLintState getState() {
      return myPanel.getState();
    }

    @Override
    protected void setState(@NotNull TsLintState state) {
      myPanel.setState(state);
    }
  }

  private static class NewTslintView extends NewLinterView<TsLintState> {

    private final TslintPanel myPanel;

    NewTslintView(Project project, String displayName, TslintPanel panel) {
      super(project, displayName, panel.createComponent());
      myPanel = panel;
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
