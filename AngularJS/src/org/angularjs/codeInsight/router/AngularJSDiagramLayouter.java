package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramDataKeys;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.layout.LayoutGraph;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uml.UmlGraphBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Irina.Chernushina on 5/23/2016.
 */
public class AngularJSDiagramLayouter implements Layouter {
  @NotNull private final Project myProject;
  @NotNull private final Graph2D myGraph;
  @NotNull private final Layouter myDefaultLayouter;
  @NotNull private final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> myData;

  public AngularJSDiagramLayouter(@NotNull Project project,
                                  @NotNull Graph2D graph,
                                  @NotNull Layouter layouter,
                                  @NotNull final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> data) {
    myProject = project;
    myGraph = graph;
    myDefaultLayouter = layouter;
    myData = data;
  }

  @Override
  public boolean canLayout(LayoutGraph _graph) {
    return true;
  }

  @Override
  public void doLayout(LayoutGraph _graph) {
    if (!doCustomLayout()) {
      myDefaultLayouter.doLayout(_graph);
    }
  }

  private boolean doCustomLayout() {
    final UmlGraphBuilder umlGraphBuilder = (UmlGraphBuilder)myGraph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER).get(null);
    final AngularUiRouterDiagramModel model = (AngularUiRouterDiagramModel)umlGraphBuilder.getDataModel();

    double maxWidth = 0;
    final Map<AngularUiRouterNode, Node> nodesMap = new HashMap<>();
    final Node[] array = myGraph.getNodeArray();
    for (Node node : array) {
      final AngularUiRouterNode object = (AngularUiRouterNode)umlGraphBuilder.getNodeObject(node);
      nodesMap.put(object, node);
      maxWidth = Math.max(maxWidth, myGraph.getWidth(node));
    }
    /*final Edge[] edgeArray = myGraph.getEdgeArray();
    final EdgeRealizer realizer = myGraph.getDefaultEdgeRealizer();
    for (Edge edge : edgeArray) {
      myGraph.setRealizer(edge, GraphManager.getGraphManager().createQuadCurveEdgeRealizer());
    }*/
    //myGraph.getNodeLayout()

    final AngularUiRouterGraphBuilder.GraphNodesBuilder dataObject = findDataObject(model);
    if (dataObject == null) return false;

    final AngularUiRouterNode rootNode = dataObject.getRootNode();
    //myGraph.setCenter(nodesMap.get(rootNode), centerX, centerY);

    final List<Ring> ringsList = fillRings(dataObject, rootNode);

    final Rectangle box = myGraph.getBoundingBox();
    final double centerX = box.getWidth() / 2;
    final double centerY = box.getHeight() / 2;

    final int inset = JBUI.scale(5);

    calculateSectorHeights(nodesMap, ringsList, inset);

    double curX = inset;
    double curY = inset;
    for (Ring ring : ringsList) {
      curY = inset;
      final List<CircleSector> sectors = ring.mySectors;
      boolean hadTemplateLevel = false;
      for (CircleSector sector : sectors) {
        if (sector.myParentSector != null && sector.myParentNode != null) {
          final Node graphNode = nodesMap.get(sector.myParentNode);
          final double nodeHeight = myGraph.getHeight(graphNode);
          final double parentY = myGraph.getCenterY(graphNode) - nodeHeight / 2;
          if (parentY > curY) curY = parentY;
        }
        sector.myStartY = curY;
        for (NodeGroup group : sector.myOrderedNodes) {
          final AngularUiRouterNode node = group.myNode;
          final double nodeHeight = layoutNode(maxWidth, nodesMap, curX, curY, node);
          hadTemplateLevel |= layoutTemplates(curX, maxWidth, curY, group, nodesMap, inset);
          curY += nodeHeight + inset;
        }
        curY += sector.mySumHeight;
      }
      curX += maxWidth + inset * 2;
      if (hadTemplateLevel) curX += maxWidth + inset * 2;
    }

    return true;
  }

  private double layoutNode(double maxWidth, Map<AngularUiRouterNode, Node> nodesMap, double curX, double curY, AngularUiRouterNode node) {
    final Node graphNode = nodesMap.get(node);
    final double nodeHeight = myGraph.getHeight(graphNode);
    final double width = myGraph.getWidth(graphNode);
    final double delta = (maxWidth - width) / 2;
    myGraph.setCenter(graphNode, curX - delta + maxWidth / 2, curY + nodeHeight / 2);
    return nodeHeight;
  }

  @NotNull
  private static List<Ring> fillRings(AngularUiRouterGraphBuilder.GraphNodesBuilder dataObject,
                                      AngularUiRouterNode rootNode) {
    Ring center = new Ring(null);
    final CircleSector centerSector = new CircleSector(null, rootNode);
    centerSector.myCirclePart = 1.0;
    centerSector.myStartAngle = 0;
    centerSector.myOrderedNodes.add(new NodeGroup(rootNode));
    center.myInnerRadius = 0;
    center.mySectors.add(centerSector);

    final List<Ring> ringsList = new ArrayList<>();
    ringsList.add(center);

    Ring parent = center;
    while (true) {
      final Ring current = new Ring(parent);
      final List<CircleSector> parentSectors = parent.mySectors;
      for (CircleSector parentSector : parentSectors) {
        if (parentSector.myParentSector == null) {
          final List<AngularUiRouterNode> zeroLevelStates = dataObject.getZeroLevelStates();
          for (AngularUiRouterNode state : zeroLevelStates) {
            final CircleSector circleSector = new CircleSector(parentSector, rootNode);
            current.mySectors.add(circleSector);
            wrapStateAddToSector(dataObject, state, circleSector);
          }
        } else {
          for (NodeGroup group : parentSector.myOrderedNodes) {
            final List<AngularUiRouterNode> states = dataObject.getImmediateChildrenStates(group.myNode);
            if (states != null && !states.isEmpty()) {
              final CircleSector circleSector = new CircleSector(parentSector, group.myNode);
              current.mySectors.add(circleSector);
              for (AngularUiRouterNode state : states) {
                wrapStateAddToSector(dataObject, state, circleSector);
              }
            }
          }
        }
      }
      if (current.mySectors.isEmpty()) break;
      ringsList.add(current);
      parent = current;
    }
    return ringsList;
  }

  private boolean layoutTemplates(double curX,
                                  double maxWidth,
                                  double curY,
                                  NodeGroup group,
                                  Map<AngularUiRouterNode, Node> nodesMap,
                                  int inset) {
    for (AngularUiRouterNode node : group.myTemplateNodes) {
      final double nodeHeight = layoutNode(maxWidth, nodesMap, curX + maxWidth, curY, node);
      //final Node graphNode = nodesMap.get(node);
      //final double nodeHeight = myGraph.getHeight(graphNode);
      //myGraph.setCenter(graphNode, curX + maxWidth + maxWidth / 2, curY + nodeHeight / 2);
      curY += nodeHeight + inset;
    }
    return !group.myTemplateNodes.isEmpty();
  }

  private static void wrapStateAddToSector(AngularUiRouterGraphBuilder.GraphNodesBuilder dataObject,
                                           AngularUiRouterNode state,
                                           CircleSector circleSector) {
    final List<AngularUiRouterNode> templates = dataObject.getStateTemplates(state);
    final NodeGroup nodeGroup = new NodeGroup(state);
    nodeGroup.myTemplateNodes.addAll(templates);
    circleSector.myOrderedNodes.add(nodeGroup);
  }

  private void calculateSectorHeights(Map<AngularUiRouterNode, Node> nodesMap, List<Ring> ringsList, int inset) {
    for (Ring ring : ringsList) {
      final List<CircleSector> sectors = ring.mySectors;
      for (CircleSector sector : sectors) {
        double height = 0;
        for (NodeGroup group : sector.myOrderedNodes) {
          double nodeHeight = getNodeHeight(nodesMap, group.myNode);
          double templatesHeight = 0;
          for (AngularUiRouterNode node : group.myTemplateNodes) {
            templatesHeight += getNodeHeight(nodesMap, node);
          }
          group.mySumHeight = Math.max(nodeHeight, templatesHeight);
          nodeHeight = group.mySumHeight;
          height += nodeHeight + inset;
        }
        sector.mySumHeight = height;
      }
    }
    for (int i = ringsList.size() - 1; i >= 0; i--) {
      final Ring ring = ringsList.get(i);
      final List<CircleSector> sectors = ring.mySectors;
      for (CircleSector sector : sectors) {
        double height = 0;
        final Set<CircleSector> circleSectorSet = sector.myChildren;
        for (CircleSector circleSector : circleSectorSet) {
          height += circleSector.mySumHeight + inset;
        }
        sector.mySumHeight = Math.max(sector.mySumHeight, height);
      }
    }
  }

  private double getNodeHeight(Map<AngularUiRouterNode, Node> nodesMap, AngularUiRouterNode node) {
    final Node graphNode = nodesMap.get(node);
    return myGraph.getHeight(graphNode);
  }

  private static class Ring {
    private final Ring myParentRing;
    private final List<CircleSector> mySectors;
    private double myInnerRadius = -1;
    private double myOuterRadius = -1;

    public Ring(Ring ring) {
      myParentRing = ring;
      mySectors = new ArrayList<>();
    }
  }

  private static class CircleSector {
    private final Set<CircleSector> myChildren;
    private final CircleSector myParentSector;
    private final AngularUiRouterNode myParentNode;
    private final List<NodeGroup> myOrderedNodes;
    private double myCirclePart = -1;
    private double myStartAngle = -1;

    private double mySumHeight;
    private double myStartY;

    public CircleSector(CircleSector sector, AngularUiRouterNode node) {
      myParentSector = sector;
      myParentNode = node;
      if (myParentSector != null) myParentSector.myChildren.add(this);
      myOrderedNodes = new ArrayList<>();
      myChildren = new HashSet<>();
    }
  }

  private static class NodeGroup {
    private final AngularUiRouterNode myNode;
    private final List<AngularUiRouterNode> myTemplateNodes;
    private double mySumHeight;
    private double myStartY;

    public NodeGroup(AngularUiRouterNode node) {
      myNode = node;
      myTemplateNodes = new ArrayList<>();
    }
  }

  private AngularUiRouterGraphBuilder.GraphNodesBuilder findDataObject(final AngularUiRouterDiagramModel model) {
    final Collection<AngularUiRouterNode> nodes = model.getNodes();
    for (AngularUiRouterNode node : nodes) {
      if (Type.topLevelTemplate.equals(node.getIdentifyingElement().getType())) {
        final VirtualFile rootFile = node.getIdentifyingElement().getNavigationTarget().getContainingFile().getVirtualFile();
        return myData.get(rootFile);
      }
    }
    return null;
  }
}
