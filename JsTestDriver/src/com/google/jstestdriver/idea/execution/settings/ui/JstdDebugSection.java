package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.execution.JavaScriptDebugSettingsEditor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugSection extends AbstractRunSettingsSection {

  private final ComboBox myPreferredDebugBrowserComboBox;

  public JstdDebugSection() {
    myPreferredDebugBrowserComboBox = new ComboBox(new CollectionComboBoxModel(JavaScriptDebugSettingsEditor.getDebuggableBrowsers()));
    JavaScriptDebugSettingsEditor.setupBrowserComboboxRenderer(myPreferredDebugBrowserComboBox);
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
    myPreferredDebugBrowserComboBox.setSelectedItem(runSettings.getPreferredDebugBrowser());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    WebBrowser selectedBrowser = (WebBrowser)myPreferredDebugBrowserComboBox.getSelectedItem();
    if (selectedBrowser != null) {
      runSettingsBuilder.setPreferredDebugBrowser(selectedBrowser);
    }
  }

}
