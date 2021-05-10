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

package com.thoughtworks.gauge.markdownPreview;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.NotificationGroups;
import com.thoughtworks.gauge.core.GaugeVersion;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.vcs.VcsNotifier.STANDARD_NOTIFICATION;

final class Spectacle {
  private static final Logger LOG = Logger.getInstance(Spectacle.class);

  public static final String NAME = "spectacle";

  private final Project project;
  private final GaugeSettingsModel settings;
  private static boolean installing = false;

  Spectacle(Project project, GaugeSettingsModel settings) {
    this.project = project;
    this.settings = settings;
  }

  private void install() {
    if (installing) {
      Notifications.Bus.notify(
        new Notification(NotificationGroups.GAUGE_GROUP, GaugeBundle.message("notification.title.installation.in.progress"),
                         GaugeBundle.message("notification.content.installing.plugin.spectacle"), NotificationType.INFORMATION));
      return;
    }
    installing = true;
    ProgressManager.getInstance().run(new Task.Backgroundable(this.project,
                                                              GaugeBundle.message("progress.title.installing.plugin.spectacle"), false) {
      @Override
      public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true);
        progressIndicator.setText(GaugeBundle.message("progress.text.installing.plugin.spectacle"));
        try {
          ProcessBuilder processBuilder = new ProcessBuilder(settings.getGaugePath(), GaugeConstants.INSTALL, NAME);
          GaugeUtil.setGaugeEnvironmentsTo(processBuilder, settings);
          Process process = processBuilder.start();
          int exitCode = process.waitFor();
          installing = false;
          if (exitCode != 0) {
            throw new RuntimeException(GaugeUtil.getOutput(process.getInputStream(), "\n"));
          }
          Notifications.Bus.notify(new Notification(
            NotificationGroups.GAUGE_GROUP,
            GaugeBundle.message("notification.title.installation.completed"),
            GaugeBundle.message("notification.content.installation.plugin.spectacle.completed"),
            NotificationType.INFORMATION)
          );
        }
        catch (Exception e) {
          LOG.debug(e);
          Notification notification = new Notification(
            NotificationGroups.GAUGE_ERROR_GROUP, GaugeBundle.message("notification.title.installation.failed"), e.getMessage(), NotificationType.ERROR);
          Notifications.Bus.notify(notification, project);
        }
        progressIndicator.cancel();
      }
    });
  }

  boolean isInstalled() {
    return GaugeVersion.getVersion(true).isPluginInstalled(NAME);
  }

  void notifyToInstall() {
    Notification notification = STANDARD_NOTIFICATION.createNotification(
      GaugeBundle.message("notification.title.error.specification.preview"),
      GaugeBundle.message("notification.content.missing.plugin.spectacle.to.install.do"),
      NotificationType.ERROR);
    notification.addAction(new NotificationAction(GaugeBundle.message("notification.content.install.spectacle")) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        install();
        notification.expire();
      }
    });
    Notifications.Bus.notify(notification);
  }
}
