// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.SendFeedbackAction;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AnalysisServerFeedbackAction extends DumbAwareAction {
  public AnalysisServerFeedbackAction() {
    super(DartBundle.messagePointer("analysis.server.status.good.text"), Presentation.NULL_STRING, AllIcons.General.Balloon);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    sendFeedback(project);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Presentation presentation = e.getPresentation();
    final Project project = e.getProject();
    if (isApplicable(project)) {
      presentation.setEnabledAndVisible(true);
    }
    else {
      presentation.setEnabledAndVisible(false);
    }
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }

  private void sendFeedback(@NotNull final Project project) {
    final ApplicationInfo appInfo = ApplicationInfo.getInstance();
    final boolean isEAP = appInfo.isEAP();
    final String intellijBuild = isEAP ? appInfo.getBuild().asStringWithoutProductCode() : appInfo.getBuild().asString();
    final String sdkVersion = getSdkVersion(project);
    ProgressManager.getInstance().run(new Task.Backgroundable(project, IdeBundle.message("reportProblemAction.progress.title.submitting")) {

      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        final String platformDescription = StringUtil.replace(SendFeedbackAction.getDescription(project), ";", " ").trim();

        final String url = DartBundle.message("dart.feedback.url", urlEncode("Analyzer Feedback from IntelliJ"));
        String body = DartBundle.message("dart.feedback.template", intellijBuild, sdkVersion, platformDescription);

        BrowserUtil.browse(url + urlEncode(body + "\n"), project);
      }
    });
  }

  protected String getSdkVersion(@NotNull Project project) {
    DartSdk sdk = DartSdk.getDartSdk(project);
    return sdk == null ? "<NO SDK>" : sdk.getVersion();
  }

  private static String urlEncode(String input) {
    return URLEncoder.encode(input, StandardCharsets.UTF_8);
  }
}
