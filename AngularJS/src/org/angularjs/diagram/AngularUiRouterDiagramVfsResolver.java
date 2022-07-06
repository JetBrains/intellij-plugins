// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.diagram;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.angularjs.codeInsight.router.AngularUiRouterGraphBuilder;
import org.angularjs.codeInsight.router.DiagramObject;
import org.angularjs.codeInsight.router.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.APP)
public final class AngularUiRouterDiagramVfsResolver implements DiagramVfsResolver<DiagramObject> {
  @Override
  public String getQualifiedName(DiagramObject element) {
    if ((Type.template.equals(element.getType()) || Type.topLevelTemplate.equals(element.getType())) &&
        element.getNavigationTarget() != null) {
      final PsiFile psiFile = element.getNavigationTarget().getContainingFile();
      return psiFile == null ? "" : psiFile.getVirtualFile().getPath();
    }
    else {
      return "";
    }
  }

  @Override
  public @Nullable DiagramObject resolveElementByFQN(@NotNull String fqn, @NotNull Project project) {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fqn);
    if (file == null) {
      return null;
    }
    else {
      AngularUiRouterGraphBuilder.GraphNodesBuilder builder =
        AngularUiRouterProviderContext.getInstance(project).getBuilder(file);
      return builder == null ? null : builder.getRootNode().getIdentifyingElement();
    }
  }
}
