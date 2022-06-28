// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.google.dart.server.GetServerPortConsumer;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.RequestError;
import org.jetbrains.annotations.NotNull;

public class AnalysisServerDiagnosticsAction extends DumbAwareAction {
  private static final String GROUP_DISPLAY_ID = "Dart Analysis Server";

  public AnalysisServerDiagnosticsAction() {
    super(DartBundle.messagePointer("analysis.server.show.diagnostics.text"));
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    final DartAnalysisServerService service = project == null ? null : DartAnalysisServerService.getInstance(project);
    e.getPresentation().setEnabledAndVisible(service != null && service.isServerProcessActive());
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    run(project);
  }

  void run(@NotNull final Project project) {
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
        String title = DartBundle.message("analysis.server.show.diagnostics.error");
        @NlsSafe String message = requestError.getMessage();
        Notification notification = new Notification(GROUP_DISPLAY_ID, title, message, NotificationType.ERROR);
        Notifications.Bus.notify(notification);
      }
    });
  }
}
