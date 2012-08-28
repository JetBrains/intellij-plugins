package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugSection extends AbstractRunSettingsSection {

  private final JComboBox myPreferredDebugBrowserComboBox;

  public JstdDebugSection() {
    BrowsersConfiguration.BrowserFamily[] supportedBrowsers = new BrowsersConfiguration.BrowserFamily[] {
      BrowsersConfiguration.BrowserFamily.CHROME,
      BrowsersConfiguration.BrowserFamily.FIREFOX
    };
    myPreferredDebugBrowserComboBox = new JComboBox(supportedBrowsers);
    myPreferredDebugBrowserComboBox.setRenderer(new ListCellRendererWrapper<BrowsersConfiguration.BrowserFamily>(myPreferredDebugBrowserComboBox.getRenderer()) {
      @Override
      public void customize(JList list,
                            BrowsersConfiguration.BrowserFamily value,
                            int index,
                            boolean selected,
                            boolean hasFocus) {
        setIcon(value.getIcon());
        setText(value.getName());
      }
    });
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
      SwingHelper.wrapWithNoFilling(linePanel)
    );
    return SwingHelper.wrapWithNoFilling(result);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myPreferredDebugBrowserComboBox.setSelectedItem(runSettings.getPreferredDebugBrowser());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    BrowsersConfiguration.BrowserFamily selectedBrowser = ObjectUtils.tryCast(
      myPreferredDebugBrowserComboBox.getSelectedItem(),
      BrowsersConfiguration.BrowserFamily.class
    );
    if (selectedBrowser != null) {
      runSettingsBuilder.setPreferredDebugBrowser(selectedBrowser);
    }
  }

}
