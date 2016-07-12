package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class AngularUiRouterDiagramModel extends DiagramDataModel<DiagramObject> {
  private final VirtualFile myRootFile;
  @NotNull private final List<AngularUiRouterNode> myNodes;
  @NotNull private final List<AngularUiRouterEdge> myEdges;

  public AngularUiRouterDiagramModel(@NotNull final Project project,
                                     VirtualFile rootFile, @NotNull final AngularUiRouterDiagramProvider provider,
                                     @NotNull final List<AngularUiRouterNode> nodes,
                                     @NotNull final List<AngularUiRouterEdge> edges) {
    super(project, provider);
    myRootFile = rootFile;
    myNodes = new ArrayList<>(nodes);
    myEdges = edges;
  }

  @NotNull
  @Override
  public Collection<AngularUiRouterNode> getNodes() {
    return myNodes;
  }

  @NotNull
  @Override
  public Collection<AngularUiRouterEdge> getEdges() {
    return myEdges;
  }

  @NotNull
  @Override
  public String getNodeName(DiagramNode<DiagramObject> n) {
    return n.getTooltip();
  }

  @Nullable
  @Override
  public DiagramNode<DiagramObject> addElement(DiagramObject element) {
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
        } else {
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
      ApplicationManager.getApplication().invokeLater(() -> getBuilder().update(true, true));
    }
  }

  @NotNull
  @Override
  public ModificationTracker getModificationTracker() {
    return PsiManager.getInstance(getProject()).getModificationTracker();
  }

  @Override
  public void dispose() {

  }
}
