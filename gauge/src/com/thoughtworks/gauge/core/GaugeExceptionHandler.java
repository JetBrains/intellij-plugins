/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.core;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.thoughtworks.gauge.util.GaugeUtil;

import java.io.IOException;
import java.io.InputStream;

public final class GaugeExceptionHandler extends Thread {
  private static final Logger LOG = Logger.getInstance(GaugeExceptionHandler.class);

  private static final String LINE_BREAK = "\n";
  private static final String NOTIFICATION_TEMPLATE = "More details...<br><br>%s%s";
  private static final String NOTIFICATION_TITLE = "Exception occurred in Gauge plugin";
  private static final String ISSUE_TEMPLATE =
    "\n\nPlease log an issue in https://github.com/getgauge/intellij-plugin with following details:<br><br>" +
    "#### gauge process exited with code %d" +
    "<pre>```%s```" +
    "\n* Idea version: %s\n* API version: %s\n* Plugin version: %s\n* Gauge version: %s</pre>";
  private final Process process;
  private final Project project;

  public GaugeExceptionHandler(Process process, Project project) {
    this.process = process;
    this.project = project;
  }

  @Override
  public void run() {
    String output = "";
    try {
      do {
        output = getOutput(output, process.getErrorStream());
        output = getOutput(output, process.getInputStream());
      }
      while (process.isAlive());
      if (process.exitValue() != 0 && !output.trim().isEmpty() && project.isOpen()) {
        LOG.debug(output);
        Notifications.Bus.notify(createNotification(output, process.exitValue()), project);
      }
    }
    catch (Exception ex) {
      LOG.debug(ex);
    }
  }

  private static String getOutput(String output, InputStream stream) throws IOException {
    String lines = GaugeUtil.getOutput(stream, LINE_BREAK);
    return lines.trim().isEmpty() ? "" : String.format("%s%s%s", output, LINE_BREAK, lines);
  }

  private static Notification createNotification(String stacktrace, int exitValue) {
    IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.findId("com.thoughtworks.gauge"));
    String pluginVersion = plugin == null ? "" : plugin.getVersion();
    String apiVersion = ApplicationInfo.getInstance().getApiVersion();
    String ideaVersion = ApplicationInfo.getInstance().getFullVersion();
    String gaugeVersion = GaugeVersion.getVersion(false).version;
    String body = String.format(ISSUE_TEMPLATE, exitValue, stacktrace, ideaVersion, apiVersion, pluginVersion, gaugeVersion);
    String content = String.format(NOTIFICATION_TEMPLATE, LINE_BREAK, body);
    return new Notification("Gauge Exception", NOTIFICATION_TITLE, content, NotificationType.ERROR,
                            NotificationListener.URL_OPENING_LISTENER);
  }
}
