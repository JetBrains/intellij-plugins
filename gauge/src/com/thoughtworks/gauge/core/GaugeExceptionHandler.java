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
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.NotificationGroups;
import com.thoughtworks.gauge.util.GaugeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public final class GaugeExceptionHandler extends Thread {
  private static final Logger LOG = Logger.getInstance(GaugeExceptionHandler.class);

  private static final String LINE_BREAK = "\n";

  private static final @NlsSafe String ISSUE_HEADER = "#### Gauge process exited with code ";
  private static final @NlsSafe String ISSUE_IDE_VERSION = "* IDE version: ";
  private static final @NlsSafe String ISSUE_API_VERSION = "* API version: ";
  private static final @NlsSafe String ISSUE_PLUGIN_VERSION = "* Plugin version: ";
  private static final @NlsSafe String ISSUE_GAUGE_VERSION = "* Gauge version: ";

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
      if (Thread.currentThread().isInterrupted()) {
        return;
      }

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
    catch (InterruptedIOException ignored) {
    }
    catch (Exception ex) {
      LOG.debug(ex);
    }
  }

  private static String getOutput(String output, InputStream stream) throws IOException {
    String lines = GaugeUtil.getOutput(stream, LINE_BREAK);
    return lines.trim().isEmpty() ? "" : String.format("%s%s%s", output, LINE_BREAK, lines);
  }

  private static Notification createNotification(@NlsSafe String stacktrace, int exitValue) {
    IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.findId(GaugeConstants.PLUGIN_ID));
    @NlsSafe String apiVersion = ApplicationInfo.getInstance().getApiVersion();
    @NlsSafe String gaugeVersion = GaugeVersion.getVersion(false).version;
    String pluginVersion = plugin == null ? "" : plugin.getVersion();
    String ideaVersion = ApplicationInfo.getInstance().getFullVersion();

    HtmlChunk.Element issueChunk = new HtmlBuilder()
      .append(ISSUE_HEADER)
      .appendRaw("```")
      .append(Integer.toString(exitValue))
      .appendRaw("```")
      .append(stacktrace)
      .appendRaw(ISSUE_IDE_VERSION)
      .append(ideaVersion).append("\n")
      .appendRaw(ISSUE_API_VERSION)
      .append(apiVersion).append("\n")
      .appendRaw(ISSUE_PLUGIN_VERSION)
      .append(pluginVersion).append("\n")
      .appendRaw(ISSUE_GAUGE_VERSION)
      .append(gaugeVersion).append("\n")
      .wrapWith("pre");

    HtmlBuilder builder = new HtmlBuilder();
    builder.append(GaugeBundle.message("notification.more.details"))
      .br().br()
      .append(GaugeBundle.message("notification.please.report.an.issue"))
      .br().br()
      .append(issueChunk);

    return new Notification(NotificationGroups.GAUGE_ERROR_GROUP,
                            GaugeBundle.message("notification.title.exception.occurred.in.gauge.plugin"), builder.toString(),
                            NotificationType.ERROR)
      .setListener(NotificationListener.URL_OPENING_LISTENER);
  }
}
