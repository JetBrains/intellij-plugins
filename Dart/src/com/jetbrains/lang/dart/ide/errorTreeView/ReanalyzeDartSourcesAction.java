/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReanalyzeDartSourcesAction extends AnAction implements DumbAware {
  public ReanalyzeDartSourcesAction() {
    super(DartBundle.message("dart.reanalyze.action.name"),
          DartBundle.message("dart.reanalyze.action.description"),
          AllIcons.Actions.Restart);
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    if (isApplicable(project)) {
      DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
      if (das.isServerProcessActive()) {
        das.analysis_reanalyze();
      }
      else {
        // Ensure the server is properly stopped.
        das.restartServer();
        // The list of projects was probably lost when the server crashed. Prime it with the current project to get the server restarted.
        das.serverReadyForRequest(project);
      }
    }
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(isApplicable(e.getProject()));
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }
}
