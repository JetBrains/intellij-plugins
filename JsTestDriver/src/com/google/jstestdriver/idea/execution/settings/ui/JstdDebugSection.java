package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.JavaScriptDebugSettingsEditorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugSection extends AbstractRunSettingsSection {

  private final ComboBox myPreferredDebugBrowserComboBox;

  public JstdDebugSection(@NotNull Project project) {
    myPreferredDebugBrowserComboBox = new ComboBox(JavaScriptDebugSettingsEditorBase.getEngines(project).toArray());
    JavaScriptDebugSettingsEditorBase.setupBrowserComboboxRenderer(myPreferredDebugBrowserComboBox);
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel linePanel = SwingHelper.newHorizontalPanel(
      Component.CENTER_ALIGNMENT,
      new JLabel("Debug in"),
      myPreferredDebugBrowserComboBox,
      new JLabel(" if both browsers are captured.")
    );
    JPanel result = SwingHelper.newLeftAlignedVerticalPanel(
      Box.createVerticalStrut(5),
      new JLabel("Debugging is available in a local browser (Chrome or Firefox)"),
      Box.createVerticalStrut(4),
      new JLabel("captured by a local JsTestDriver server running in IDE."),
      Box.createVerticalStrut(15),
      SwingHelper.wrapWithoutStretch(linePanel)
    );
    return SwingHelper.wrapWithoutStretch(result);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myPreferredDebugBrowserComboBox.setSelectedItem(JSDebugEngine.findByBrowserName(runSettings.getPreferredDebugBrowser().getName()));
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    JSDebugEngine selectedBrowser = (JSDebugEngine)myPreferredDebugBrowserComboBox.getSelectedItem();
    if (selectedBrowser != null) {
      runSettingsBuilder.setPreferredDebugBrowser(selectedBrowser.getWebBrowser());
    }
  }

}
