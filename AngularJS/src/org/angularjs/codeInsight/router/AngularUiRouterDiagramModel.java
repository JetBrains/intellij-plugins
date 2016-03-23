package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class AngularUiRouterDiagramModel extends DiagramDataModel<DiagramObject> {
  @NotNull private final List<AngularUiRouterNode> myNodes;
  @NotNull private final List<AngularUiRouterEdge> myEdges;

  public AngularUiRouterDiagramModel(@NotNull final Project project,
                                     @NotNull final AngularUiRouterDiagramProvider provider,
                                     @NotNull final List<AngularUiRouterNode> nodes,
                                     @NotNull final List<AngularUiRouterEdge> edges) {
    super(project, provider);
    myNodes = nodes;
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
