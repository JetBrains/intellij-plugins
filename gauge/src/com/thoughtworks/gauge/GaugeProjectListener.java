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

package com.thoughtworks.gauge;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.thoughtworks.gauge.core.GaugeVersion;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.thoughtworks.gauge.GaugeConstants.MIN_GAUGE_VERSION;
import static com.thoughtworks.gauge.util.GaugeUtil.isGaugeProjectDir;

public final class GaugeProjectListener implements ProjectManagerListener {
  private static final Logger LOG = Logger.getInstance(GaugeProjectListener.class);

  @Override
  public void projectOpened(@NotNull Project project) {
    if (isGaugeProjectDir(new File(project.getBasePath()))) {
      ProgressManager.getInstance().run(new Task.Backgroundable(project, GaugeBundle.message("gauge.check.supported.version"), false) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          if (!GaugeVersion.isGreaterOrEqual(MIN_GAUGE_VERSION, true)) {
            String version = GaugeVersion.getVersion(false).version;
            if (version == null) return;

            String notificationTitle = GaugeBundle.message("notification.title.unsupported.gauge.version", version);
            String errorMessage = GaugeBundle.message("dialog.message.gauge.intellij.plugin.only.works.with.version", MIN_GAUGE_VERSION);

            LOG.warn(String.format("Unsupported Gauge version %s\n%s", version, errorMessage));

            Notification notification =
              new Notification(NotificationGroups.GAUGE_ERROR_GROUP, notificationTitle, errorMessage, NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
          }
        }
      });
    }
  }

  @Override
  public void projectClosing(@NotNull Project project) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(project);
      for (Module module : ModuleManager.getInstance(project).getModules()) {
        bootstrapService.disposeComponent(module);
      }
    }, GaugeBundle.message("gauge.dispose.progress"), true, project);
  }
}
