// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.NotNull;

final class DartNodeDecorator implements ProjectViewNodeDecorator {
  @Override
  public void decorate(final @NotNull ProjectViewNode node, final @NotNull PresentationData presentation) {
    if (node instanceof PsiFileNode || node instanceof PsiDirectoryNode) {
      final VirtualFile nodeFile = node.getVirtualFile();
      final Project project = node.getProject();
      if (nodeFile != null && project != null && DartAnalysisServerService.getInstance(project).isFileWithErrors(nodeFile)) {
        presentation.setAttributesKey(CodeInsightColors.ERRORS_ATTRIBUTES);
      }
    }
  }
}
