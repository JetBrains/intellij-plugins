// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public final class DartExecutionHelper {
  private DartExecutionHelper() {}

  public static void displayIssues(final @NotNull Project project,
                                   @NotNull VirtualFile launchFile,
                                   @NotNull @Nls String message,
                                   @Nullable Icon icon) {
    clearIssueNotifications(project);

    GlobalSearchScope scope = getScopeOfFilesThatMayAffectExecution(project, launchFile);
    if (scope == null) return;

    DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
    List<DartServerData.DartError> errors = ContainerUtil.filter(das.getErrors(scope), DartServerData.DartError::isError);
    if (errors.isEmpty()) return;

    // Show a notification on the dart analysis tool window.
    final DartProblemsView problemsView = DartProblemsView.getInstance(project);
    problemsView.showErrorNotification("", message, icon);
  }

  public static void clearIssueNotifications(final @NotNull Project project) {
    final DartProblemsView problemsView = DartProblemsView.getInstance(project);
    problemsView.clearNotifications();
  }

  @VisibleForTesting
  public static @Nullable GlobalSearchScope getScopeOfFilesThatMayAffectExecution(@NotNull Project project, @NotNull VirtualFile file) {
    if (!FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE)) return null;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    if (!fileIndex.isInContent(file)) return null;

    final Module module = fileIndex.getModuleForFile(file);
    if (module == null) return null;

    VirtualFile contextSubdir = null;
    VirtualFile dir = file.getParent();

    while (dir != null && fileIndex.isInContent(dir)) {
      final VirtualFile pubspecFile = dir.findChild(PubspecYamlUtil.PUBSPEC_YAML);
      if (pubspecFile != null) {
        return getScopeForContextSubdir(module, pubspecFile, contextSubdir);
      }
      contextSubdir = dir;
      dir = dir.getParent();
    }

    // no pubspec.yaml => return module content scope
    return module.getModuleContentScope();
  }

  private static @NotNull GlobalSearchScope getScopeForContextSubdir(@NotNull Module module,
                                                                     @NotNull VirtualFile pubspecFile,
                                                                     @Nullable VirtualFile contextSubdir) {
    final Project project = module.getProject();
    final VirtualFile dartRoot = pubspecFile.getParent();

    if (contextSubdir == null) {
      return GlobalSearchScopesCore.directoryScope(project, dartRoot, true);
    }

    GlobalSearchScope subdirScope = GlobalSearchScopesCore.directoryScope(project, contextSubdir, true);
    VirtualFile libDir = dartRoot.findChild("lib");
    if (libDir == null || libDir.equals(contextSubdir)) {
      return subdirScope;
    }
    return subdirScope.union(GlobalSearchScopesCore.directoryScope(project, libDir, true));
  }
}
