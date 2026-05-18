package com.intellij.lang.javascript.linter.jshint;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.jshint.config.JSHintDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Component;

/**
 * @author Sergey Simonchik
 */
public class JSHintView extends JSLinterBaseView<JSHintState> {

  private final JCheckBox myConfigFileUsedCheckBox;
  private final JSHintOptionsTreeView myOptionsTreeView;
  private final JPanel myCardPanel;
  private final JSHintConfigFileView myConfigFileView;
  private final NodePackageField myJSHintPackageField;

  public JSHintView(@NotNull Project project, boolean fullModeDialog) {
    myConfigFileUsedCheckBox = new JCheckBox(JSHintBundle.message("jshint.use.config.files"));
    myJSHintPackageField = new NodePackageField(project, JSHintDescriptor.PACKAGE_NAME, () -> {
      return NodeJsInterpreterManager.getInstance(project).getInterpreter();
    });

    myOptionsTreeView = new JSHintOptionsTreeView(fullModeDialog);

    myCardPanel = new JPanel(new CardLayout(0, 0));
    myCardPanel.add(myOptionsTreeView.getComponent(), ConfigFileUsed.OFF.name());
    myConfigFileView = new JSHintConfigFileView(project);
    myCardPanel.add(myConfigFileView.getComponent(), ConfigFileUsed.ON.name());

    myConfigFileUsedCheckBox.addActionListener(e -> showCard(myConfigFileUsedCheckBox.isSelected()));
  }

  private void showCard(boolean configFileUsed) {
    CardLayout layout = (CardLayout) myCardPanel.getLayout();
    ConfigFileUsed type = configFileUsed ? ConfigFileUsed.ON : ConfigFileUsed.OFF;
    layout.show(myCardPanel, type.name());
  }

  @Override
  protected @NotNull Component createCenterComponent() {
    return new JSHintViewContent(myJSHintPackageField, myConfigFileUsedCheckBox, myCardPanel).getPanel();
  }

  @Override
  protected void handleEnableStatusChanged(boolean enabled) {
    myJSHintPackageField.setEnabled(enabled);
    myOptionsTreeView.setEnabled(enabled);
    myConfigFileView.onEnabledStateChange(enabled);
  }

  @Override
  protected @NotNull JSHintState getState() {
    JSHintState.Builder builder = new JSHintState.Builder();
    builder
      .setPackageRef(myJSHintPackageField.getSelectedRef())
      .setOptionsState(myOptionsTreeView.getOptionsState())
      .setConfigFileUsed(myConfigFileUsedCheckBox.isSelected())
      .setCustomConfigFileUsed(myConfigFileView.isCustomConfigFileUsed())
      .setCustomConfigFilePath(myConfigFileView.getCustomConfigFilePath());
    return builder.build();
  }

  @Override
  protected void setState(@NotNull JSHintState state) {
    myJSHintPackageField.setSelectedRef(state.getNodePackageRef());
    myOptionsTreeView.setOptionsState(state.getOptionsState());
    myConfigFileUsedCheckBox.setSelected(state.isConfigFileUsed());
    myConfigFileView.setCustomConfigFileUsed(state.isCustomConfigFileUsed());
    myConfigFileView.setCustomConfigFilePath(state.getCustomConfigFilePath());
    showCard(state.isConfigFileUsed());
  }

  @Override
  public void disposeResources() {
    myOptionsTreeView.disposeUI();
  }

  private enum ConfigFileUsed { ON, OFF }

}
