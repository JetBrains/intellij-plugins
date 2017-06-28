/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.google.dart.server.GetServerPortConsumer;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.RequestError;

public class AnalysisServerDiagnosticsAction extends DumbAwareAction {
  private static final String GROUP_DISPLAY_ID = "Dart Analysis Server";

  public AnalysisServerDiagnosticsAction() {
    super(DartBundle.message("analysis.server.show.diagnostics.text"));
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    // Get the current analysis server.
    DartAnalysisServerService server = DartAnalysisServerService.getInstance(project);

    // Ask it for the diagnostics port.
    server.diagnostic_getServerPort(new GetServerPortConsumer() {
      @Override
      public void computedServerPort(int port) {
        // Open a new browser page.
        BrowserUtil.browse("http://localhost:" + port + "/status");
      }

      @Override
      public void onError(RequestError requestError) {
        Notification notification = new Notification(
          GROUP_DISPLAY_ID,
          DartBundle.message("analysis.server.show.diagnostics.error"),
          requestError.getMessage(),
          NotificationType.ERROR);
        Notifications.Bus.notify(notification);
      }
    });
  }
}
