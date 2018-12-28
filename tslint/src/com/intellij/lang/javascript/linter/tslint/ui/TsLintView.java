package com.intellij.lang.javascript.linter.tslint.ui;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.NodeModuleConfigurationView;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileTexts;
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileView;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBCheckBox;
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
  private final NodeModuleConfigurationView myNodeModuleConfigurationView;
  private final JSLinterConfigFileView myConfigFileView;
  private TextFieldWithBrowseButton myRules;
  private JBCheckBox myAllowJs;

  public TsLintView(@NotNull Project project, boolean fullModeDialog) {
    super(fullModeDialog);
    myProject = project;
    myConfigFileView = new JSLinterConfigFileView(project, CONFIG_TEXTS, null);
    myConfigFileView.setAdditionalConfigFilesProducer(() -> TslintUtil.findAllConfigsInScope(project));
    myNodeModuleConfigurationView = new NodeModuleConfigurationView(project, "tslint", null);
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
    myAllowJs = new JBCheckBox();
    SwingHelper.installFileCompletionAndBrowseDialog(myProject, myRules, "Select additional rules directory",
                                                     FileChooserDescriptorFactory.createSingleFolderDescriptor());
    final FormBuilder nodeFieldsWrapperBuilder = FormBuilder.createFormBuilder()
      .setAlignLabelOnRight(true)
      .setHorizontalGap(UIUtil.DEFAULT_HGAP)
      .setVerticalGap(UIUtil.DEFAULT_VGAP)
      .setFormLeftIndent(UIUtil.DEFAULT_HGAP)
      .addLabeledComponent("&Node interpreter:", myNodeModuleConfigurationView.getNodeInterpreterField())
      .addLabeledComponent("TSLint package:", myNodeModuleConfigurationView.getPackageField());

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
      .addLabeledComponent("Lint JavaScript files:", myAllowJs)
      .getPanel();
    final JPanel centerPanel = SwingHelper.wrapWithHorizontalStretch(panel);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    return centerPanel;
  }

  @NotNull
  @Override
  protected TsLintState getState() {
    final TsLintState.Builder builder = new TsLintState.Builder()
      .setNodePath(myNodeModuleConfigurationView.getNodeInterpreterField().getInterpreterRef())
      .setNodePackage(myNodeModuleConfigurationView.getPackageField().getSelected())
      .setCustomConfigFileUsed(myConfigFileView.isCustomConfigFileUsed())
      .setCustomConfigFilePath(myConfigFileView.getCustomConfigFilePath())
      .setAllowJs(myAllowJs.isSelected());
    if (!StringUtil.isEmptyOrSpaces(myRules.getText())) {
      builder.setRulesDirectory(myRules.getText().trim());
    }
    return builder.build();
  }

  @Override
  protected void setState(@NotNull TsLintState state) {
    myNodeModuleConfigurationView.getNodeInterpreterField().setInterpreterRef(state.getInterpreterRef());
    myNodeModuleConfigurationView.getPackageField().setSelected(state.getNodePackage());

    myConfigFileView.setCustomConfigFileUsed(state.isCustomConfigFileUsed());
    myConfigFileView.setCustomConfigFilePath(StringUtil.notNullize(state.getCustomConfigFilePath()));
    if (! StringUtil.isEmptyOrSpaces(state.getRulesDirectory())) {
      myRules.setText(state.getRulesDirectory());
    }
    myAllowJs.setSelected(state.isAllowJs());

    resizeOnSeparateDialog();
  }

  private void resizeOnSeparateDialog() {
    if (isFullModeDialog()) {
      myNodeModuleConfigurationView.setPreferredWidthToComponents();
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
