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

import com.google.inject.Inject;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.JsTestDriverServer;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class StatusBar extends JPanel implements Observer {

  private static final long serialVersionUID = 8729866568493622493L;

  public enum Status {
    NOT_RUNNING("#FF6666"),
    NO_BROWSERS("#FFFF66"),
    READY("#66CC66");

    private final Color color;

    Status(String hexColorStr) {
      color = Color.decode(hexColorStr);
    }

    public Color getColor() {
      return color;
    }
  }

  private JLabel label;
  private final ResourceBundle messageBundle;

  @Inject
  public StatusBar(Status status, ResourceBundle messageBundle) {
    label = new JLabel();
    add(label);
    this.messageBundle = messageBundle;
    this.setStatus(status);
  }

  private void setStatus(Status status) {
    this.setBackground(status.getColor());
    label.setText(messageBundle.getString(status.name()));
  }

  public void update(final Observable observable, final Object event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (observable instanceof JsTestDriverServer) {
          switch ((JsTestDriverServer.Event) event) {
            case STARTED:
              setStatus(Status.NO_BROWSERS);
              break;
            case STOPPED:
              setStatus(Status.NOT_RUNNING);
              break;
          }
        } else if (observable instanceof CapturedBrowsers) {
          if (((CapturedBrowsers)observable).getBrowsers().isEmpty()) {
            setStatus(Status.NO_BROWSERS);
          } else {
            setStatus(Status.READY);
          }
        }
      }
    });
  }
}
