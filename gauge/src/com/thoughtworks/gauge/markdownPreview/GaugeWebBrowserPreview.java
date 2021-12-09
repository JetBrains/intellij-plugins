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

import com.intellij.ide.browsers.OpenInBrowserRequest;
import com.intellij.ide.browsers.WebBrowserUrlProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.NotificationGroups;
import com.thoughtworks.gauge.language.ConceptFileType;
import com.thoughtworks.gauge.language.SpecFileType;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

import static com.thoughtworks.gauge.util.GaugeUtil.getGaugeSettings;

final class GaugeWebBrowserPreview extends WebBrowserUrlProvider {
  private static File tempDirectory;

  private static File createOrGetTempDirectory(String projectName) throws IOException {
    if (tempDirectory == null) {
      tempDirectory = FileUtil.createTempDirectory(projectName, null, true);
    }
    return tempDirectory;
  }

  @Override
  public boolean canHandleElement(OpenInBrowserRequest request) {
    FileType fileType = request.getFile().getFileType();
    return fileType instanceof SpecFileType || fileType instanceof ConceptFileType;
  }

  @Nullable
  @Override
  protected Url getUrl(@NotNull OpenInBrowserRequest request, @NotNull VirtualFile virtualFile) {
    try {
      if (!request.isAppendAccessToken()) return null;
      GaugeSettingsModel settings = getGaugeSettings();
      Spectacle spectacle = new Spectacle(request.getProject(), settings);
      if (spectacle.isInstalled()) {
        return previewUrl(request, virtualFile, settings);
      }
      spectacle.notifyToInstall();
    }
    catch (Exception e) {
      Messages.showWarningDialog(GaugeBundle.message("dialog.message.unable.to.create.html.file.for", virtualFile.getName()),
                                 GaugeBundle.message("gauge.error"));
    }
    return null;
  }

  @Nullable
  private static Url previewUrl(OpenInBrowserRequest request, VirtualFile virtualFile, GaugeSettingsModel settings)
    throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(settings.getGaugePath(), GaugeConstants.DOCS, Spectacle.NAME, virtualFile.getPath());
    String projectName = request.getProject().getName();
    builder.environment().put("spectacle_out_dir", FileUtil.join(createOrGetTempDirectory(projectName).getPath(), "docs"));
    File gaugeModuleDir = GaugeUtil.moduleDir(GaugeUtil.moduleForPsiElement(request.getFile()));
    builder.directory(gaugeModuleDir);
    GaugeUtil.setGaugeEnvironmentsTo(builder, settings);
    Process docsProcess = builder.start();
    int exitCode = docsProcess.waitFor();
    if (exitCode != 0) {
      String docsOutput =
        String.format("<pre>%s</pre>", GaugeUtil.getOutput(docsProcess.getInputStream(), " ").replace("<", "&lt;").replace(">", "&gt;"));
      Notifications.Bus
        .notify(new Notification(NotificationGroups.GAUGE_ERROR_GROUP,
                                 GaugeBundle.message("notification.title.error.specification.preview"), docsOutput,
                                 NotificationType.ERROR));
      return null;
    }
    String relativePath = FileUtil.getRelativePath(gaugeModuleDir, new File(virtualFile.getParent().getPath()));
    return Urls.newUnparsable(FileUtil.join(createOrGetTempDirectory(projectName).getPath(), "docs", "html", relativePath,
                                     virtualFile.getNameWithoutExtension() + ".html"));
  }
}
