// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.diagram;

import com.intellij.CommonBundle;
import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.angularjs.codeInsight.router.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class AngularUiRouterDiagramModel extends DiagramDataModel<DiagramObject> {
  private final VirtualFile myRootFile;
  private final @NotNull List<AngularUiRouterNode> myNodes;
  private final @NotNull List<AngularUiRouterEdge> myEdges;

  public AngularUiRouterDiagramModel(final @NotNull Project project,
                                     VirtualFile rootFile, final @NotNull AngularUiRouterDiagramProvider provider,
                                     final @NotNull List<AngularUiRouterNode> nodes,
                                     final @NotNull List<AngularUiRouterEdge> edges) {
    super(project, provider);
    myRootFile = rootFile;
    myNodes = new ArrayList<>(nodes);
    myEdges = edges;
  }

  @Override
  public @NotNull Collection<AngularUiRouterNode> getNodes() {
    return myNodes;
  }

  @Override
  public @NotNull Collection<AngularUiRouterEdge> getEdges() {
    return myEdges;
  }

  @Override
  public @NotNull String getNodeName(@NotNull DiagramNode<DiagramObject> n) {
    return StringUtil.notNullize(n.getTooltip(), "<" + CommonBundle.getErrorTitle() + ">");
  }

  @Override
  public @Nullable DiagramNode<DiagramObject> addElement(@Nullable DiagramObject element) {
    return null;
  }

  @Override
  public void refreshDataModel() {
    final AngularUiRouterDiagramBuilder builder = new AngularUiRouterDiagramBuilder(getProject());
    builder.build();
    final Map<VirtualFile, RootTemplate> rootTemplates = builder.getRootTemplates();
    final RootTemplate template = rootTemplates.get(myRootFile);
    if (template != null) {
      Map<String, UiRouterState> map = builder.getDefiningFiles2States().get(myRootFile);
      final AngularUiRouterGraphBuilder graphBuilder;
      if (map == null) {
        map = builder.getRootTemplates2States().get(myRootFile);
        if (map == null) return;
        graphBuilder = new AngularUiRouterGraphBuilder(getProject(), map, builder.getTemplatesMap(),
                                                       rootTemplates.get(myRootFile), myRootFile);
      }
      else {
        graphBuilder = new AngularUiRouterGraphBuilder(getProject(), map, builder.getTemplatesMap(), null, myRootFile);
      }
      final AngularUiRouterDiagramProvider diagramProvider = (AngularUiRouterDiagramProvider)getProvider();
      final AngularUiRouterGraphBuilder.GraphNodesBuilder model = graphBuilder.createDataModel(diagramProvider);
      if (myNodes.equals(model.getAllNodes()) && myEdges.equals(model.getEdges())) return;
      myNodes.clear();
      myEdges.clear();
      final AngularUiRouterProviderContext context = AngularUiRouterProviderContext.getInstance(getProject());
      context.reset();
      context.registerNodesBuilder(model);
      myNodes.addAll(model.getAllNodes());
      myEdges.addAll(model.getEdges());
      ApplicationManager.getApplication().invokeLater(
        () -> getBuilder().queryUpdate().withDataReload().withPresentationUpdate().withRelayout().run());
    }
  }

  @Override
  public @NotNull ModificationTracker getModificationTracker() {
    return PsiManager.getInstance(getProject()).getModificationTracker();
  }

  @Override
  public void dispose() {

  }
}
