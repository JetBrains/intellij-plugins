package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.execution.JavaScriptDebugSettingsEditor;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugSection extends AbstractRunSettingsSection {
  private final BrowserSelector myBrowserSelector;

  public JstdDebugSection() {
    myBrowserSelector = new BrowserSelector(JavaScriptDebugSettingsEditor.BROWSER_CONDITION);
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel linePanel = SwingHelper.newHorizontalPanel(
      Component.CENTER_ALIGNMENT,
      new JLabel("Debug in"),
      myBrowserSelector.getMainComponent(),
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
    myBrowserSelector.setSelected(runSettings.getPreferredDebugBrowser());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    WebBrowser selectedBrowser = myBrowserSelector.getSelected();
    if (selectedBrowser != null) {
      runSettingsBuilder.setPreferredDebugBrowser(selectedBrowser);
    }
  }

}
