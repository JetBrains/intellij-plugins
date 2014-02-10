package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugSection extends AbstractRunSettingsSection {

  private final ComboBox myPreferredDebugBrowserComboBox;

  public JstdDebugSection() {
    myPreferredDebugBrowserComboBox = new ComboBox(new CollectionComboBoxModel(getDebuggableBrowsers()));
    setupBrowserComboboxRenderer(myPreferredDebugBrowserComboBox);
  }

  private static void setupBrowserComboboxRenderer(@NotNull JComboBox combobox) {
    //noinspection unchecked
    combobox.setRenderer(new ListCellRendererWrapper<WebBrowser>() {
      @Override
      public void customize(JList list, WebBrowser value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
          setText(value.getName());
          setIcon(value.getIcon());
        }
      }
    });
  }

  @NotNull
  private static List<WebBrowser> getDebuggableBrowsers() {
    List<WebBrowser> list = new ArrayList<WebBrowser>();
    JSDebugEngine[] engines = JSDebugEngine.getEngines();
    for (WebBrowser browser : WebBrowserManager.getInstance().getActiveBrowsers()) {
      for (JSDebugEngine engine : engines) {
        if (engine.isBrowserSupported(browser)) {
          list.add(browser);
          break;
        }
      }
    }
    return list;
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
