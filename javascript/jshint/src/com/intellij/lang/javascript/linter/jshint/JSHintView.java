package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.JSLinterBaseView;
import com.intellij.lang.javascript.linter.jshint.version.JSHintVersionView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;

/**
 * @author Sergey Simonchik
 */
public class JSHintView extends JSLinterBaseView<JSHintState> {

  private final JCheckBox myConfigFileUsedCheckBox;
  private final JSHintOptionsTreeView myOptionsTreeView;
  private final JSHintVersionView myVersionView;
  private final JPanel myCardPanel;
  private final JSHintConfigFileView myConfigFileView;

  public JSHintView(@NotNull Project project, boolean fullModeDialog) {
    myConfigFileUsedCheckBox = new JCheckBox(JSHintBundle.message("jshint.use.config.files"));
    myVersionView = new JSHintVersionView(project);

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
  protected @Nullable Component createTopRightComponent() {
    return SwingHelper.newHorizontalPanel(
      Component.CENTER_ALIGNMENT,
      myConfigFileUsedCheckBox,
      Box.createHorizontalStrut(20),
      myVersionView.getComponent()
    );
  }

  @Override
  protected @NotNull Component createCenterComponent() {
    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.add(myCardPanel, BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    return panel;
  }

  @Override
  protected void handleEnableStatusChanged(boolean enabled) {
    myOptionsTreeView.setEnabled(enabled);
    myConfigFileView.onEnabledStateChange(enabled);
  }

  @Override
  protected @NotNull JSHintState getState() {
    JSHintState.Builder builder = new JSHintState.Builder();
    builder
      .setVersion(myVersionView.getVersion())
      .setOptionsState(myOptionsTreeView.getOptionsState())
      .setConfigFileUsed(myConfigFileUsedCheckBox.isSelected())
      .setCustomConfigFileUsed(myConfigFileView.isCustomConfigFileUsed())
      .setCustomConfigFilePath(myConfigFileView.getCustomConfigFilePath());
    return builder.build();
  }

  @Override
  protected void setState(@NotNull JSHintState state) {
    myVersionView.setVersion(state.getVersion());
    myOptionsTreeView.setOptionsState(state.getOptionsState());
    myConfigFileUsedCheckBox.setSelected(state.isConfigFileUsed());
    myConfigFileView.setCustomConfigFileUsed(state.isCustomConfigFileUsed());
    myConfigFileView.setCustomConfigFilePath(state.getCustomConfigFilePath());
    showCard(state.isConfigFileUsed());
  }

  @Override
  public void disposeResources() {
    myOptionsTreeView.disposeUI();
    Disposer.dispose(myVersionView);
  }

  private enum ConfigFileUsed { ON, OFF }

}
