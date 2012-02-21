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
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class StatusBar extends JPanel implements ServerListener {

  private static final long serialVersionUID = 8729866568493622493L;

  private final JLabel myLabel;
  private final ResourceBundle myMessageBundle;

  public StatusBar(ResourceBundle messageBundle) {
    myLabel = new JLabel();
    add(myLabel);
    myMessageBundle = messageBundle;
    setStatusDeferred(Status.NOT_RUNNING);
  }

  private void setStatusDeferred(final Status status) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        StatusBar.this.setBackground(status.getColor());
        myLabel.setText(myMessageBundle.getString(status.name()));
      }
    });
  }

  @Override
  public void serverStarted() {
    setStatusDeferred(Status.NO_BROWSERS);
  }

  @Override
  public void serverStopped() {
    setStatusDeferred(Status.NOT_RUNNING);
  }

  @Override
  public void browserCaptured(BrowserInfo info) {
    setStatusDeferred(Status.READY);
  }

  @Override
  public void browserPanicked(BrowserInfo info) {
  }

  public enum Status {
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
