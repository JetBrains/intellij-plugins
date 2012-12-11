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

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.idea.MessageBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.ConcurrentHashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class StatusBar implements ServerListener {

  private final JComponent myComponent;
  private final JLabel myLabel;
  private final Set<BrowserInfo> myCapturedBrowsers = new ConcurrentHashSet<BrowserInfo>();

  public StatusBar() {
    myComponent = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    myComponent.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
    myLabel = new JLabel();
    myComponent.add(myLabel);
    setStatusDeferred(Status.NOT_RUNNING);
  }

  @NotNull
  public JComponent getComponent() {
    return myComponent;
  }

  private void setStatusDeferred(final Status status) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        myComponent.setBackground(status.getColor());
        myLabel.setText(MessageBundle.message(status.name()));
      }
    });
  }

  @Override
  public void serverStarted() {
    setStatusDeferred(Status.NO_BROWSERS);
    myCapturedBrowsers.clear();
  }

  @Override
  public void serverStopped() {
    setStatusDeferred(Status.NOT_RUNNING);
    myCapturedBrowsers.clear();
  }

  @Override
  public void browserCaptured(BrowserInfo info) {
    setStatusDeferred(Status.READY);
    myCapturedBrowsers.add(info);
  }

  @Override
  public void browserPanicked(BrowserInfo info) {
    myCapturedBrowsers.remove(info);
    if (myCapturedBrowsers.isEmpty()) {
      setStatusDeferred(Status.NO_BROWSERS);
    }
  }

  private enum Status {
    NOT_RUNNING("#FF6666"),
    NO_BROWSERS("#FFFF66"),
    READY("#66CC66");

    private final Color myColor;

    Status(String hexColorStr) {
      myColor = Color.decode(hexColorStr);
    }

    public Color getColor() {
      return myColor;
    }
  }

}
