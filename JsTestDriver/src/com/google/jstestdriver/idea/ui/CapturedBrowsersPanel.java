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
import com.google.jstestdriver.browser.BrowserCaptureEvent;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.idea.PluginResources.BrowserIcon;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@SuppressWarnings("serial")
public class CapturedBrowsersPanel extends JPanel implements ServerListener {
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
        for (BrowserLabel browserLabel : myBrowserLabelByNameMap.values()) {
          Icon icon = browserLabel.getBrowserIcon().getIconForEvent(event);
          browserLabel.setIcon(icon);
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
        BrowserLabel browserLabel = getBrowserLabelByName(info.getName());
        if (browserLabel != null) {
          browserLabel.setIcon(browserLabel.getBrowserIcon().getIconForEvent(event));
        } else {
          log.warn("Unregistered browser '" + info.getName()
              + "' BrowserCaptureEvent has been received. " + info + ", type: " + event);
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
