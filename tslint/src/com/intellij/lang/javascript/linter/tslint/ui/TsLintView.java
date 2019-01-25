package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileTexts;
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileView;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public final class TsLintView extends JSLinterBaseView<TsLintState> {
  private static final JSLinterConfigFileTexts CONFIG_TEXTS = getConfigTexts();

  private final Project myProject;
  private final JSLinterConfigFileView myConfigFileView;
  private final NodeJsInterpreterField myNodeInterpreterField;
  private final NodePackageField myNodePackageField;
  private TextFieldWithBrowseButton myRules;

  public TsLintView(@NotNull Project project, boolean fullModeDialog) {
    super(fullModeDialog);
    myProject = project;
    myConfigFileView = new JSLinterConfigFileView(project, CONFIG_TEXTS, null);
    myConfigFileView.setAdditionalConfigFilesProducer(() -> TslintUtil.findAllConfigsInScope(project));
    myNodeInterpreterField = new NodeJsInterpreterField(project, false);
    myNodePackageField = AutodetectLinterPackage.createNodePackageField(ContainerUtil.list(TslintUtil.PACKAGE_NAME),
                                                                        myNodeInterpreterField, myConfigFileView,
                                                                        TslintUtil.isMultiRootEnabled());
  }

  @Nullable
  @Override
  protected Component createTopRightComponent() {
    return null;
  }

  @NotNull
  @Override
  protected Component createCenterComponent() {
    myRules = new TextFieldWithBrowseButton();
    SwingHelper.installFileCompletionAndBrowseDialog(myProject, myRules, "Select additional rules directory",
                                                     FileChooserDescriptorFactory.createSingleFolderDescriptor());
    final FormBuilder nodeFieldsWrapperBuilder = FormBuilder.createFormBuilder()
      .setAlignLabelOnRight(true)
      .setHorizontalGap(UIUtil.DEFAULT_HGAP)
      .setVerticalGap(UIUtil.DEFAULT_VGAP)
      .setFormLeftIndent(UIUtil.DEFAULT_HGAP)
      .addLabeledComponent("&Node interpreter:", myNodeInterpreterField)
      .addLabeledComponent("TSLint package:", myNodePackageField);

    JPanel panel = FormBuilder.createFormBuilder()
      .setAlignLabelOnRight(true)
      .setHorizontalGap(UIUtil.DEFAULT_HGAP)
      .setVerticalGap(UIUtil.DEFAULT_VGAP)
      .setFormLeftIndent(UIUtil.DEFAULT_HGAP)
      .addComponent(nodeFieldsWrapperBuilder.getPanel())
      .addComponent(myConfigFileView.getComponent())
      .addSeparator(4)
      .addVerticalGap(4)
      .addLabeledComponent("Additional rules directory:", myRules)
      .getPanel();
    final JPanel centerPanel = SwingHelper.wrapWithHorizontalStretch(panel);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    return centerPanel;
  }

  @Override
  protected void handleEnableStatusChanged(boolean enabled) {
    NodePackageRef selectedRef = myNodePackageField.getSelectedRef();
    if (selectedRef == AutodetectLinterPackage.INSTANCE) {
      UIUtil.setEnabled(myConfigFileView.getComponent(), false, true);
    }
    myConfigFileView.onEnabledStateChanged(enabled);
  }

  @NotNull
  @Override
  protected TsLintState getState() {
    final TsLintState.Builder builder = new TsLintState.Builder()
      .setNodePath(myNodeInterpreterField.getInterpreterRef())
      .setNodePackageRef(myNodePackageField.getSelectedRef())
      .setCustomConfigFileUsed(myConfigFileView.isCustomConfigFileUsed())
      .setCustomConfigFilePath(myConfigFileView.getCustomConfigFilePath());
    if (!StringUtil.isEmptyOrSpaces(myRules.getText())) {
      builder.setRulesDirectory(myRules.getText().trim());
    }
    return builder.build();
  }

  @Override
  protected void setState(@NotNull TsLintState state) {
    myNodeInterpreterField.setInterpreterRef(state.getInterpreterRef());
    myNodePackageField.setSelectedRef(state.getNodePackageRef());

    myConfigFileView.setCustomConfigFileUsed(state.isCustomConfigFileUsed());
    myConfigFileView.setCustomConfigFilePath(StringUtil.notNullize(state.getCustomConfigFilePath()));
    if (! StringUtil.isEmptyOrSpaces(state.getRulesDirectory())) {
      myRules.setText(state.getRulesDirectory());
    }

    resizeOnSeparateDialog();
  }

  private void resizeOnSeparateDialog() {
    if (isFullModeDialog()) {
      myNodeInterpreterField.setPreferredWidthToFitText();
      myConfigFileView.setPreferredWidthToComponents();
    }
  }

  private static JSLinterConfigFileTexts getConfigTexts() {
    return new JSLinterConfigFileTexts(JSBundle.message("javascript.linter.configurable.config.autoSearch.title"),
                                       "When linting a TypeScript file, TSLint looks for tslint.json or tslint.yaml starting from the file's folder and then moving up to the filesystem root" +
                                       " or in the user's home directory.",
                                       "Select TSLint configuration file (*.json|*.yaml)");
  }
}
