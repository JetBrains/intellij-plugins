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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.NotificationGroups;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;

import java.io.IOException;

import static com.thoughtworks.gauge.util.GaugeUtil.getGaugeSettings;

public final class GaugeVersion {
  private static final Logger LOG = Logger.getInstance(GaugeVersion.class);

  private static volatile GaugeVersionInfo versionInfo = null;

  public static GaugeVersionInfo getVersion(boolean update) {
    if (versionInfo == null) {
      try {
        versionInfo = getVersionInternal(update);
      } catch (Exception ignored) {
      }
      if (versionInfo == null) {
        versionInfo = new GaugeVersionInfo();
      }
      return versionInfo;
    }
    return getVersionInternal(update);
  }

  static GaugeVersionInfo getVersionInternal(boolean update) {
    if (!update) return versionInfo;
    GaugeVersionInfo gaugeVersionInfo = new GaugeVersionInfo();
    try {
      GaugeSettingsModel settings = getGaugeSettings();
      ProcessBuilder processBuilder = new ProcessBuilder(settings.getGaugePath(), GaugeConstants.VERSION, GaugeConstants.MACHINE_READABLE);
      GaugeUtil.setGaugeEnvironmentsTo(processBuilder, settings);
      Process process = processBuilder.start();
      int exitCode = process.waitFor();
      if (exitCode == 0) {
        String output = GaugeUtil.getOutput(process.getInputStream(), "\n");
        try {
          GsonBuilder builder = new GsonBuilder();
          Gson gson = builder.create();
          gaugeVersionInfo = gson.fromJson(output, GaugeVersionInfo.class);
        }
        catch (Exception e) {
          LOG.error(String.format("Unable to parse <%s %s %s> command's output.\n%s", settings.getGaugePath(), GaugeConstants.VERSION,
                                  GaugeConstants.MACHINE_READABLE, output));
          Notification notification = new Notification(
            NotificationGroups.GAUGE_ERROR_GROUP,
            GaugeBundle.message("notification.title.unable.to.parse.command.output",
                                settings.getGaugePath(), GaugeConstants.VERSION, GaugeConstants.MACHINE_READABLE),
            e.getMessage(),
            NotificationType.ERROR
          );
          Notifications.Bus.notify(notification);
        }
      }
    }
    catch (InterruptedException | IOException | GaugeNotFoundException e) {
      LOG.warn("Unable to start Gauge " + e.getMessage());

      Notification notification = new Notification(NotificationGroups.GAUGE_ERROR_GROUP,
                                                   GaugeBundle.message("notification.title.unable.to.start.gauge.intellij.plugin"),
                                                   e.getMessage(), NotificationType.ERROR);
      Notifications.Bus.notify(notification);
    }
    return gaugeVersionInfo;
  }

  public static Boolean isGreaterOrEqual(String v1, boolean update) {
    getVersion(update);
    return versionInfo.isGreaterOrEqual(new GaugeVersionInfo(v1));
  }
}

