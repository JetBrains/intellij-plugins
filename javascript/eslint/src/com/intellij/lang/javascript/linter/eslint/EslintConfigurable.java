package com.intellij.lang.javascript.linter.eslint;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actionsOnSave.ActionOnSaveBackedByOwnConfigurable;
import com.intellij.ide.actionsOnSave.ActionOnSaveComment;
import com.intellij.ide.actionsOnSave.ActionOnSaveContext;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.lang.javascript.linter.NewLinterView;
import com.intellij.lang.javascript.linter.UntypedJSLinterConfigurable;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfiguration;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSLanguageServiceManager;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSState;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EslintConfigurable extends UntypedJSLinterConfigurable {

  public static final String ID = "settings.javascript.linters.eslint";

  @SuppressWarnings("unused")
  public EslintConfigurable(@NotNull Project project) {
    this(project, false);
  }

  public EslintConfigurable(@NotNull Project project, boolean fullModeDialog) {
    super(project, fullModeDialog);
  }

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public @Nls String getDisplayName() {
    return EslintBundle.message("settings.javascript.linters.eslint.configurable.name");
  }

  @Override
  protected @NotNull NewEslintView createView() {
    return new NewEslintView(getProject(), getDisplayName(), new EslintPanel(getProject(), isFullModeDialog()));
  }

  @Override
  public void reset() {
    NewEslintView view = getEslintView();
    if (view == null) {
      return;
    }

    view.setExtendedState(loadUiState());
    view.reset();
    resizeDialogToFitPreferredSize(view);
  }

  @Override
  public void apply() {
    NewEslintView view = getEslintView();
    if (view == null) {
      return;
    }
    ExtendedLinterState<EslintState> extendedStateFromUi = view.getExtendedState();
    EslintState eslintStateFromUi = extendedStateFromUi.getState();
    boolean isStandardJS = EslintPanel.isStandardJs(eslintStateFromUi.getNodePackageRef());
    if (isStandardJS) {
      NodePackage nodePackage = Objects.requireNonNull(eslintStateFromUi.getNodePackageRef().getConstantPackage());
      StandardJSState standardJsState = new StandardJSState(nodePackage);
      setExtendedState(ExtendedLinterState.create(extendedStateFromUi.isEnabled(), standardJsState), StandardJSConfiguration.class);

      EslintState eslintState = new EslintState.Builder()
        .setFilesPattern(eslintStateFromUi.getFilesPattern())
        .setRunOnSave(eslintStateFromUi.isRunOnSave())
        .build();
      setExtendedState(ExtendedLinterState.create(false, eslintState), EslintConfiguration.class);
    }
    else {
      setExtendedState(ExtendedLinterState.create(false, StandardJSState.DEFAULT), StandardJSConfiguration.class);
      setExtendedState(ExtendedLinterState.create(extendedStateFromUi.isEnabled(), eslintStateFromUi), EslintConfiguration.class);
    }

    view.apply();

    EslintLanguageServiceManager.getInstance(myProject).terminateServices();
    StandardJSLanguageServiceManager.getInstance(myProject).terminateServices();
  }

  @Override
  public boolean isModified() {
    NewEslintView view = getEslintView();
    if (view == null) {
      return false;
    }
    return !loadUiState().equals(view.getExtendedState()) || view.isModified();
  }

  private NewEslintView getEslintView() {
    return (NewEslintView)myView;
  }

  private @NotNull ExtendedLinterState<EslintState> loadUiState() {
    ExtendedLinterState<EslintState> eslintExtendedState = getExtendedState(EslintConfiguration.class);
    ExtendedLinterState<StandardJSState> standardJsExtendedState = getExtendedState(StandardJSConfiguration.class);
    EslintState eslintState = eslintExtendedState.getState();
    StandardJSState standardJSState = standardJsExtendedState.getState();
    boolean isStandardJS = standardJsExtendedState.isEnabled();
    EslintState uiState = isStandardJS
                          ? (new EslintState.Builder()
                               .setEslintPackage(NodePackageRef.create(standardJSState.getNodePackage()))
                               .setFilesPattern(eslintState.getFilesPattern())
                               .setRunOnSave(eslintState.isRunOnSave())
                               .build())
                          : eslintState;
    return ExtendedLinterState.create(eslintExtendedState.isEnabled() || standardJsExtendedState.isEnabled(), uiState);
  }

  private static class NewEslintView extends NewLinterView<EslintState> {
    private final EslintPanel myEslintPanel;
    private EslintBottomContent bottomContent;
    private boolean myRunOnSaveCheckBoxWasSelectedBeforeItBecameDisabled;

    NewEslintView(Project project, String displayName, EslintPanel eslintPanel) {
      super(project, displayName, eslintPanel.panel, ".eslintrc.*");
      myEslintPanel = eslintPanel;
    }

    @Override
    protected void addBottomComponents(@NotNull FormBuilder builder) {
      bottomContent = new EslintBottomContent();
      builder.addComponent(bottomContent.panel);
    }

    @Override
    protected void setState(@NotNull EslintState state) {
      myEslintPanel.setState(state);
      myRunOnSaveCheckBoxWasSelectedBeforeItBecameDisabled = state.isRunOnSave();
      bottomContent.runForFilesField.setText(state.getFilesPattern());
      bottomContent.runOnSaveCheckBox.setSelected(state.isRunOnSave());
    }

    @Override
    protected @NotNull EslintState getStateWithConfiguredAutomatically() {
      return new EslintState.Builder()
        .setFilesPattern(bottomContent.runForFilesField.getText().trim())
        .setRunOnSave(bottomContent.runOnSaveCheckBox.isSelected())
        .build()
        .withLinterPackage(AutodetectLinterPackage.INSTANCE);
    }

    @Override
    protected void handleEnabledStatusChanged(boolean enabled) {
      myEslintPanel.handleEnableStatusChanged(enabled);

      bottomContent.setRunForFilesRowEnabled(enabled);

      boolean checkBoxWasEnabled = bottomContent.runOnSaveCheckBox.isEnabled();
      bottomContent.runOnSaveCheckBox.setEnabled(enabled);

      // when the 'run on save' check box becomes disabled we want to show it as not selected; but if it becomes enabled again, we want to restore its previous state
      if (checkBoxWasEnabled && !enabled) {
        myRunOnSaveCheckBoxWasSelectedBeforeItBecameDisabled = bottomContent.runOnSaveCheckBox.isSelected();
        bottomContent.runOnSaveCheckBox.setSelected(false);
      }

      if (!checkBoxWasEnabled && enabled) {
        bottomContent.runOnSaveCheckBox.setSelected(myRunOnSaveCheckBoxWasSelectedBeforeItBecameDisabled);
      }
    }

    @Override
    protected @NotNull EslintState getState() {
      EslintState.Builder builder = myEslintPanel.buildEslintState();
      builder.setFilesPattern(bottomContent.runForFilesField.getText().trim());
      builder.setRunOnSave(bottomContent.runOnSaveCheckBox.isEnabled() && bottomContent.runOnSaveCheckBox.isSelected());
      return builder.build();
    }

    private JCheckBox getRunOnSaveCheckBox() {
      return bottomContent.runOnSaveCheckBox;
    }

    private boolean isDisabledRadioButtonSelected() {
      return myDisabledRb.isSelected();
    }

    boolean isAutomaticRadioButtonSelected() {
      return myConfigureAutomaticallyRadioRb.isSelected();
    }
  }

  static class EsLintOnSaveActionInfo extends ActionOnSaveBackedByOwnConfigurable<EslintConfigurable> {

    EsLintOnSaveActionInfo(@NotNull ActionOnSaveContext context) {
      super(context, ID, EslintConfigurable.class);
    }

    @Override
    public @NotNull String getActionOnSaveName() {
      return EslintBundle.message("eslint.run.on.save.checkbox.on.actions.on.save.page");
    }

    @Override
    protected boolean isApplicableAccordingToStoredState() {
      return (EslintConfiguration.getInstance(getProject()).isEnabled() || StandardJSConfiguration.getInstance(getProject()).isEnabled());
    }

    @Override
    protected boolean isApplicableAccordingToUiState(@NotNull EslintConfigurable configurable) {
      JCheckBox runOnSaveCheckBox = configurable.getEslintView().getRunOnSaveCheckBox();
      return runOnSaveCheckBox.isVisible() && runOnSaveCheckBox.isEnabled();
    }

    @Override
    protected @Nullable ActionOnSaveComment getCommentAccordingToStoredState() {
      ExtendedLinterState<EslintState> state = EslintConfiguration.getInstance(getProject()).getExtendedState();
      if (!state.isEnabled()) {
        return ActionOnSaveComment.info(EslintBundle.message("eslint.run.on.save.disabled.comment"));
      }
      // similar to the radio button initialization in com.intellij.lang.javascript.linter.NewLinterView.setExtendedState()
      @SuppressWarnings("SpellCheckingInspection") String propertyName = "js.linters.configure.manually.selectedeslint";
      if (NewLinterView.isValidAutomaticState(state.getState()) &&
          !PropertiesComponent.getInstance(getProject()).getBoolean(propertyName, false)) {
        return ActionOnSaveComment.info(EslintBundle.message("eslint.run.on.save.auto.configuration.comment"));
      }
      return ActionOnSaveComment.info(EslintBundle.message("eslint.run.on.save.manual.configuration.comment"));
    }

    @Override
    protected @Nullable ActionOnSaveComment getCommentAccordingToUiState(@NotNull EslintConfigurable configurable) {
      NewEslintView view = configurable.getEslintView();
      if (view.isDisabledRadioButtonSelected()) {
        return ActionOnSaveComment.info(EslintBundle.message("eslint.run.on.save.disabled.comment"));
      }
      if (view.isAutomaticRadioButtonSelected()) {
        return ActionOnSaveComment.info(EslintBundle.message("eslint.run.on.save.auto.configuration.comment"));
      }
      return ActionOnSaveComment.info(EslintBundle.message("eslint.run.on.save.manual.configuration.comment"));
    }

    @Override
    protected boolean isActionOnSaveEnabledAccordingToStoredState() {
      return EslintConfiguration.getInstance(getProject()).isFixOnSaveEnabled() ||
             StandardJSConfiguration.getInstance(getProject()).isFixOnSaveEnabled();
    }

    @Override
    protected boolean isActionOnSaveEnabledAccordingToUiState(@NotNull EslintConfigurable configurable) {
      JCheckBox runOnSaveCheckBox = configurable.getEslintView().getRunOnSaveCheckBox();
      return runOnSaveCheckBox.isVisible() && runOnSaveCheckBox.isEnabled() && runOnSaveCheckBox.isSelected();
    }

    @Override
    protected void setActionOnSaveEnabled(@NotNull EslintConfigurable configurable, boolean enabled) {
      JCheckBox runOnSaveCheckBox = configurable.getEslintView().getRunOnSaveCheckBox();
      runOnSaveCheckBox.setSelected(enabled);
    }

    @Override
    public @NotNull List<? extends ActionLink> getActionLinks() {
      String linkText = getValueFromSavedStateOrFromUiState(this::getOpenEsLintPageTextAccordingToStoredState,
                                                            EsLintOnSaveActionInfo::getOpenEsLintPageTextAccordingToUiState);
      return Collections.singletonList(createGoToPageInSettingsLink(linkText, ID));
    }

    private @NotNull String getOpenEsLintPageTextAccordingToStoredState() {
      ExtendedLinterState<EslintState> state = EslintConfiguration.getInstance(getProject()).getExtendedState();
      return state.isEnabled()
             ? IdeBundle.message("actions.on.save.link.configure")
             : EslintBundle.message("eslint.run.on.save.link.enable.eslint");
    }

    private static @NotNull String getOpenEsLintPageTextAccordingToUiState(@NotNull EslintConfigurable configurable) {
      ExtendedLinterState<EslintState> state = configurable.getEslintView().getExtendedState();
      return state.isEnabled()
             ? IdeBundle.message("actions.on.save.link.configure")
             : EslintBundle.message("eslint.run.on.save.link.enable.eslint");
    }
  }
}