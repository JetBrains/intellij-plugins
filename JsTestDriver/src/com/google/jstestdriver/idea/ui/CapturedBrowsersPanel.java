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
package com.google.jstestdriver.idea.ui;

import com.google.common.collect.Maps;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.JsTestDriverServer;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.browser.BrowserCaptureEvent;
import com.google.jstestdriver.idea.PluginResources.BrowserIcon;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@SuppressWarnings("serial")
public class CapturedBrowsersPanel extends JPanel implements Observer {
  private static final Logger log = Logger.getInstance(CapturedBrowsersPanel.class);

  private final Map<String, BrowserLabel> myBrowserLabelByNameMap;

  public CapturedBrowsersPanel() {
    try {
      myBrowserLabelByNameMap = Maps.newLinkedHashMap(); // order is important
      addBrowser("Chrome", "Chrome.png");
      addBrowser("Microsoft Internet Explorer", "IE.png");
      addBrowser("Firefox", "Firefox.png");
      addBrowser("Opera", "Opera.png");
      addBrowser("Safari", "Safari.png");
    } catch (IOException e) {
      throw new RuntimeException();
    }

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    Collection<BrowserLabel> browserLabels = myBrowserLabelByNameMap.values();
    for (BrowserLabel browserLabel : browserLabels) {
      add(browserLabel);
    }

    Icon icon = browserLabels.iterator().next().getBrowserIcon().getColorIcon();
    Dimension minimumSize = new Dimension(icon.getIconWidth() * browserLabels.size(), icon.getIconHeight());
    setMinimumSize(minimumSize);
    setPreferredSize(minimumSize);
  }

  private void addBrowser(String browserName, String iconPath) throws IOException {
    String lowerCasedName = browserName.toLowerCase();
    if (myBrowserLabelByNameMap.containsKey(lowerCasedName)) {
      throw new RuntimeException("Attempt to duplicate browser '" + browserName + "'!");
    }
    BrowserLabel browserLabel = new BrowserLabel(BrowserIcon.buildFromResource(iconPath));
    myBrowserLabelByNameMap.put(lowerCasedName, browserLabel);
  }

  @Nullable
  private BrowserLabel getBrowserLabelByName(@NotNull String browserName) {
    return myBrowserLabelByNameMap.get(browserName.toLowerCase());
  }

  public void update(final Observable observable, final Object event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (observable instanceof JsTestDriverServer) {
          JsTestDriverServer.Event serverEvent = (JsTestDriverServer.Event) event;
          if (serverEvent == JsTestDriverServer.Event.STOPPED) {
            for (BrowserLabel browserLabel : myBrowserLabelByNameMap.values()) {
              browserLabel.setIcon(browserLabel.getBrowserIcon().getIconForEvent(BrowserCaptureEvent.Event.DISCONNECTED));
            }
          }
        } else if (event instanceof BrowserCaptureEvent) {
          BrowserCaptureEvent browserCaptureEvent = (BrowserCaptureEvent) event;
          SlaveBrowser slaveBrowser = browserCaptureEvent.getBrowser();
          BrowserInfo browserInfo = slaveBrowser.getBrowserInfo();
          BrowserLabel browserLabel = getBrowserLabelByName(browserInfo.getName());
          if (browserLabel != null) {
            browserLabel.setIcon(browserLabel.getBrowserIcon().getIconForEvent(browserCaptureEvent.event));
          } else {
            log.warn("Unregistered browser '" + browserInfo.getName()
                + "' BrowserCaptureEvent has been received. " + slaveBrowser + ", type: " + browserCaptureEvent.event);
          }
        }
      }
    });
  }

  private static class BrowserLabel extends JLabel {
    private final BrowserIcon myBrowserIcon;

    BrowserLabel(BrowserIcon browserIcon) {
      super(browserIcon.getGreyscaleIcon());
      myBrowserIcon = browserIcon;
    }

    public BrowserIcon getBrowserIcon() {
      return myBrowserIcon;
    }
  }
}
