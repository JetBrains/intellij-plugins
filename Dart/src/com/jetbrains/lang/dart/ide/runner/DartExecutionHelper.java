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
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.resolve.DartResolveScopeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DartExecutionHelper {

  private DartExecutionHelper() {
  }

  @SuppressWarnings("unused")
  public static boolean hasIssues(@NotNull final Project project, @NotNull VirtualFile launchFile) {
    return !getIssues(project, launchFile).isEmpty();
  }

  public static List<DartServerData.DartError> getIssues(@NotNull final Project project, @NotNull VirtualFile launchFile) {
    return getIssues(project, launchFile, true);
  }

  public static List<DartServerData.DartError> getIssues(@NotNull final Project project,
                                                         @NotNull VirtualFile launchFile,
                                                         boolean onlyErrors) {
    GlobalSearchScope scope = DartResolveScopeProvider.getDartScope(project, launchFile, true);
    if (scope == null) {
      return Collections.emptyList();
    }

    // Collect errors.
    final DartAnalysisServerService analysisServerService = DartAnalysisServerService.getInstance(project);
    List<DartServerData.DartError> errors = analysisServerService.getErrors(scope);
    if (onlyErrors) {
      errors = errors.stream().filter(DartServerData.DartError::isError).collect(Collectors.toList());
    }

    return errors;
  }

  @SuppressWarnings("unused")
  public static void displayIssues(@NotNull final Project project, @NotNull VirtualFile launchFile) {
    displayIssues(project, launchFile, "Analysis issues with launch", null);
  }

  @SuppressWarnings("SameParameterValue")
  public static void displayIssues(@NotNull final Project project,
                                   @NotNull VirtualFile launchFile,
                                   @NotNull String launchTitle,
                                   @Nullable Icon icon) {
    clearIssueNotifications(project);

    List<DartServerData.DartError> errors = getIssues(project, launchFile);

    if (errors.isEmpty()) {
      return;
    }

    final DartProblemsView problemsView = DartProblemsView.getInstance(project);

    final String content;
    if (errors.size() == 1) {
      content = errors.get(0).getMessage() + " (<a href=\"issues\">show</a>)";
    }
    else {
      content = errors.size() + " " + StringUtil.pluralize("issue", errors.size()) + " found. (<a href=\"issues\">show</a>)";
      problemsView.showErrorNotification(project, launchTitle, content, icon);
    }

    // Show a notification on the dart analysis tool window.
    problemsView.showErrorNotification(project, launchTitle, content, icon);

    // Jump to and highlight the first error (order unspecified) in the editor.
    DartServerData.DartError error = errors.get(0);
    final VirtualFile errorFile = LocalFileSystem.getInstance().findFileByPath(error.getAnalysisErrorFileSD());
    if (errorFile != null) {
      final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, errorFile, error.getOffset());
      descriptor.setScrollType(ScrollType.MAKE_VISIBLE);
      descriptor.navigate(true);
    }
  }

  public static void clearIssueNotifications(@NotNull final Project project) {
    final DartProblemsView problemsView = DartProblemsView.getInstance(project);
    problemsView.clearNotifications();
  }
}
