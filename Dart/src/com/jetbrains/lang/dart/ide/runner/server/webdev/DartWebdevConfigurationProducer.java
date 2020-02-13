// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlLikeFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartWebdevConfigurationProducer extends LazyRunConfigurationProducer<DartWebdevConfiguration> {
  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return DartWebdevConfigurationType.getInstance().getConfigurationFactories()[0];
  }

  @Override
  protected boolean setupConfigurationFromContext(final @NotNull DartWebdevConfiguration configuration,
                                                  final @NotNull ConfigurationContext context,
                                                  final @NotNull Ref<PsiElement> sourceElement) {
    final VirtualFile htmlFile = getRunnableHtmlFileFromContext(context);
    if (htmlFile == null) {
      return false;
    }

    configuration.getParameters().setHtmlFilePath(htmlFile.getPath());
    configuration.setGeneratedName();
    sourceElement.set(sourceElement.isNull() ? null : sourceElement.get().getContainingFile());
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(final @NotNull DartWebdevConfiguration configuration,
                                            final @NotNull ConfigurationContext context) {
    final VirtualFile htmlFile = getHtmlFileFromContext(context);
    return htmlFile != null && htmlFile.getPath().equals(configuration.getParameters().getHtmlFilePath());
  }

  @Nullable
  private static VirtualFile getRunnableHtmlFileFromContext(@NotNull final ConfigurationContext context) {
    // return null if this is not a Dart project
    final Project project = context.getProject();
    if (project == null || !DartAnalysisServerService.getInstance(project).serverReadyForRequest()) {
      return null;
    }

    // Check the version of the Dart SDK
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk != null && !DartAnalysisServerService.isDartSdkVersionSufficientForWebdev(sdk)) {
      return null;
    }

    final VirtualFile htmlVirtualFile = getHtmlFileFromContext(context);
    if (htmlVirtualFile == null) {
      return null;
    }

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    return projectFileIndex.isInContent(htmlVirtualFile) ? htmlVirtualFile : null;
  }

  @Nullable
  private static VirtualFile getHtmlFileFromContext(@NotNull final ConfigurationContext context) {
    final PsiElement psiLocation = context.getPsiLocation();
    final PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
    final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();
    return psiFile instanceof HtmlLikeFile && virtualFile != null ? virtualFile : null;
  }
}
