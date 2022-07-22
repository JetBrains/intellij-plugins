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

package com.thoughtworks.gauge.formatter;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.NotificationGroups;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.thoughtworks.gauge.util.GaugeUtil.getGaugeSettings;

public final class SpecFormatter extends AnAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    boolean available = false;

    Project project = e.getData(CommonDataKeys.PROJECT);
    if (project != null) {
      FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
      VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
      if (selectedFiles.length > 0) {
        VirtualFile selectedFile = selectedFiles[0];
        Document doc = FileDocumentManager.getInstance().getDocument(selectedFile);
        if (doc != null) {
          available = true;
        }
      }
    }

    e.getPresentation().setEnabledAndVisible(available);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      return;
    }
    String projectDir = project.getBasePath();
    if (projectDir == null) {
      return;
    }

    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
    VirtualFile selectedFile = fileEditorManager.getSelectedFiles()[0];
    String fileName = selectedFile.getCanonicalPath();
    Document doc = FileDocumentManager.getInstance().getDocument(selectedFile);
    if (doc != null) {
      FileDocumentManager.getInstance().saveDocument(doc);
    }
    try {
      GaugeSettingsModel settings = getGaugeSettings();
      ProcessBuilder processBuilder = new ProcessBuilder(settings.getGaugePath(), GaugeConstants.FORMAT, fileName);
      GaugeUtil.setGaugeEnvironmentsTo(processBuilder, settings);
      processBuilder.directory(new File(projectDir));
      Process process = processBuilder.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        String output =
          String.format("<pre>%s</pre>", GaugeUtil.getOutput(process.getInputStream(), "\n").replace("<", "&lt;").replace(">", "&gt;"));
        Notifications.Bus.notify(
          new Notification(NotificationGroups.GAUGE_ERROR_GROUP,
                           GaugeBundle.message("notification.title.error.spec.formatting"), output,
                           NotificationType.ERROR));
        return;
      }
      VirtualFileManager.getInstance().syncRefresh();
      selectedFile.refresh(false, false);
    }
    catch (Exception e) {
      Logger.getInstance(SpecFormatter.class).debug(e);
      Messages.showErrorDialog(GaugeBundle.message("dialog.message.error.on.formatting.spec"),
                               GaugeBundle.message("dialog.title.format.error"));
    }
  }
}
