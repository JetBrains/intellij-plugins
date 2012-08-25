/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.server.ui;

import com.google.common.collect.Maps;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.browser.BrowserCaptureEvent;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.idea.icons.JstdIcons;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.ide.BrowserSettings;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.ide.browsers.WebBrowserSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class CapturedBrowsersController implements ServerListener {

  private static final Logger LOG = Logger.getInstance(CapturedBrowsers.class);

  // browsers' order is important
  private final LinkedHashMap<Browser, BrowserButton> myBrowserButtonByNameMap;
  private final JComponent myComponent;

  public CapturedBrowsersController(@NotNull JTextField captureUrlTextField) {
    myBrowserButtonByNameMap = createBrowserLabelByNameMap(captureUrlTextField);
    myComponent = createComponent(myBrowserButtonByNameMap);
  }

  @NotNull
  public JComponent getComponent() {
    return myComponent;
  }

  @NotNull
  private static JComponent createComponent(@NotNull LinkedHashMap<Browser, BrowserButton> browserButtonByNameMap) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    Collection<BrowserButton> browserButtons = browserButtonByNameMap.values();
    boolean addSpacing = false;
    final int spacing = 15;
    for (BrowserButton browserButton : browserButtons) {
      if (addSpacing) {
        panel.add(Box.createHorizontalStrut(spacing));
      }
      addSpacing = true;
      panel.add(browserButton);
      browserButton.init();
    }

    int buttonCount = browserButtons.size();
    Dimension buttonDimension = browserButtons.iterator().next().getPreferredSize();
    Dimension minimumSize = new Dimension(
        (int) ((buttonDimension.getWidth() + spacing) * buttonCount) - spacing,
        (int) buttonDimension.getHeight()
    );
    panel.setMinimumSize(minimumSize);
    panel.setPreferredSize(minimumSize);
    panel.setMaximumSize(minimumSize);

    JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
    wrap.add(panel);
    wrap.setBorder(IdeBorderFactory.createTitledBorder("Click the icons to capture local browsers"));

    JPanel wrap2 = new JPanel(new BorderLayout());
    wrap2.add(wrap, BorderLayout.CENTER);
    wrap2.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 10));

    return wrap2;
  }

  @NotNull
  private static LinkedHashMap<Browser, BrowserButton> createBrowserLabelByNameMap(
      @NotNull JTextField captureUrlTextField
  ) {
    LinkedHashMap<Browser, BrowserButton> map = Maps.newLinkedHashMap();
    for (Browser browser : Browser.values()) {
      BrowserButton button = new BrowserButton(browser, captureUrlTextField);
      map.put(browser, button);
    }
    return map;
  }

  @Override
  public void serverStarted() {
    batchChangeBrowserState(BrowserCaptureEvent.Event.DISCONNECTED);
  }

  @Override
  public void serverStopped() {
    batchChangeBrowserState(BrowserCaptureEvent.Event.DISCONNECTED);
  }

  private void batchChangeBrowserState(@NotNull final BrowserCaptureEvent.Event event) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        for (BrowserButton browserButton : myBrowserButtonByNameMap.values()) {
          browserButton.stateChanged(event);
        }
      }
    });
  }

  @Override
  public void browserCaptured(BrowserInfo info) {
    browserStateChanged(info, BrowserCaptureEvent.Event.CONNECTED);
  }

  @Override
  public void browserPanicked(BrowserInfo info) {
    browserStateChanged(info, BrowserCaptureEvent.Event.DISCONNECTED);
  }

  private void browserStateChanged(@NotNull final BrowserInfo info, final BrowserCaptureEvent.Event event) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        Browser browser = Browser.findByBrowserInfo(info);
        BrowserButton browserButton = myBrowserButtonByNameMap.get(browser);
        if (browserButton == null) {
          LOG.warn("Can't find " + BrowserButton.class.getSimpleName() + " instance by " + browser + ", requested browser name: '" + info.getName()
              + "', browser: " + info + ", event: " + event);
          return;
        }
        browserButton.stateChanged(event);
      }
    });
  }

  private static class BrowserButton extends ActionButton {

    private BrowserButton(@NotNull BrowserAction action) {
      super(action,
            action.getTemplatePresentation(),
            JstdToolWindowPanel.PLACE,
            calcDimension(action.getTemplatePresentation().getIcon())
      );
    }

    public BrowserButton(@NotNull Browser browser, @NotNull JTextField captureUrlTextField) {
      this(new BrowserAction(browser, captureUrlTextField));
    }

    private static Dimension calcDimension(Icon icon) {
      return new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10);
    }

    @Override
    public BrowserAction getAction() {
      return (BrowserAction) super.getAction();
    }

    public void stateChanged(BrowserCaptureEvent.Event event) {
      getAction().stateChanged(event);
    }

    public void init() {
      addNotify();
    }
  }

  private static class BrowserAction extends AnAction {

    private final Browser myBrowser;
    private final JTextField myCaptureUrlTextField;
    private final Icon myCapturedIcon;
    private final Icon myNotCapturedIcon;
    private boolean myCaptured = false;
    //private BrowserButton myButton;

    public BrowserAction(@NotNull Browser browser, @NotNull JTextField captureUrlTextField) {
      super(browser.getName());

      final Presentation presentation = getTemplatePresentation();
      Icon capturedIcon = JstdIcons.getIcon(browser.getIconPath());
      Icon notCapturedIcon = SwingUtils.getGreyIcon(capturedIcon);
      presentation.setIcon(notCapturedIcon);
      presentation.setDisabledIcon(notCapturedIcon);
      presentation.setText("Capture " + browser.getName());

      myBrowser = browser;
      myCaptureUrlTextField = captureUrlTextField;
      myCapturedIcon = capturedIcon;
      myNotCapturedIcon = notCapturedIcon;
    }

    private void update(boolean enabled, boolean captured) {
      myCaptured = captured;
      Presentation presentation = getTemplatePresentation();
      presentation.setText((captured ? "Captured " : "Capture ") + myBrowser.getName());
      presentation.setIcon(myNotCapturedIcon);
      presentation.setDisabledIcon(captured ? myCapturedIcon : myNotCapturedIcon);
      presentation.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      update(false, false);
      boolean success;
      try {
        success = tryCapture();
      }
      catch (Exception ex) {
        LOG.debug(ex);
        success = false;
      }
      if (success) {
        Timer timer = new Timer(5000, new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (!myCaptured) {
              update(true, false);
            }
          }
        });
        timer.setRepeats(false);
        timer.start();
      } else {
        update(true, false);
      }
    }

    private boolean tryCapture() {
      String captureUrl = myCaptureUrlTextField.getText();
      if (StringUtil.isEmptyOrSpaces(captureUrl)) {
        Messages.showErrorDialog("Please start a local JsTestDriver server before capturing a browser.", "JsTestDriver Browser Capturing Failed");
        return false;
      }
      BrowsersConfiguration browsersConfiguration = BrowsersConfiguration.getInstance();
      if (browsersConfiguration == null) {
        return false;
      }
      WebBrowserSettings browserSettings = browsersConfiguration.getBrowserSettings(myBrowser.getBrowserFamily());
      String browserPath = browserSettings.getPath();
      if (StringUtil.isEmptyOrSpaces(browserPath)) {
        String message = "Path to " + myBrowser.getName() + " is not specified.";
        Project project = null;
        int exitCode = Messages.showOkCancelDialog(project, message, "JsTestDriver Browser Capturing Failed",
            "Specify path", "Cancel", Messages.getWarningIcon());
        if (exitCode == DialogWrapper.OK_EXIT_CODE) {
          ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
          settingsUtil.editConfigurable(project, new BrowserSettings());
        }
        return false;
      }

      BrowsersConfiguration.launchBrowser(myBrowser.getBrowserFamily(), captureUrl);
      return true;
    }

    public void stateChanged(BrowserCaptureEvent.Event event) {
      ApplicationManager.getApplication().assertIsDispatchThread();
      boolean captured = event == BrowserCaptureEvent.Event.CONNECTED;
      update(!captured, captured);
    }

  }

  private enum Browser {
    CHROME("Chrome", "browsers/Chrome.png", BrowsersConfiguration.BrowserFamily.CHROME),
    IE("Microsoft Internet Explorer", "browsers/IE.png", BrowsersConfiguration.BrowserFamily.EXPLORER),
    FIREFOX("Firefox", "browsers/Firefox.png", BrowsersConfiguration.BrowserFamily.FIREFOX),
    OPERA("Opera", "browsers/Opera.png", BrowsersConfiguration.BrowserFamily.OPERA),
    SAFARI("Safari", "browsers/Safari.png", BrowsersConfiguration.BrowserFamily.SAFARI);

    private final String myName;
    private final String myIconPath;
    private final BrowsersConfiguration.BrowserFamily myBrowserFamily;

    Browser(@NotNull String name, @NotNull String iconPath, @NotNull BrowsersConfiguration.BrowserFamily browserFamily) {
      myName = name;
      myIconPath = iconPath;
      myBrowserFamily = browserFamily;
    }

    public String getName() {
      return myName;
    }

    public String getIconPath() {
      return myIconPath;
    }

    @NotNull
    public BrowsersConfiguration.BrowserFamily getBrowserFamily() {
      return myBrowserFamily;
    }

    @Nullable
    public static Browser findByBrowserInfo(@NotNull BrowserInfo browserInfo) {
      String browserName = browserInfo.getName();
      for (Browser browser : values()) {
        if (browser.getName().equalsIgnoreCase(browserName)) {
          return browser;
        }
      }
      return null;
    }
  }

}
