package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramDataKeys;
import com.intellij.diagram.components.DiagramNodeBodyComponent;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.layout.LayoutGraph;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.LineType;
import com.intellij.openapi.graph.view.QuadCurveEdgeRealizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
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
  @NotNull private final Map<AngularUiRouterNode, DiagramNodeBodyComponent> myNodeBodiesMap;

  public AngularJSDiagramLayouter(@NotNull Project project,
                                  @NotNull Graph2D graph,
                                  @NotNull Layouter layouter,
                                  @NotNull final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> data,
                                  @NotNull Map<AngularUiRouterNode, DiagramNodeBodyComponent> nodeBodiesMap) {
    myProject = project;
    myGraph = graph;
    myDefaultLayouter = layouter;
    myData = data;
    myNodeBodiesMap = nodeBodiesMap;
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

    final AngularUiRouterGraphBuilder.GraphNodesBuilder dataObject = findDataObject(model);
    if (dataObject == null) return false;

    final AngularUiRouterNode rootNode = dataObject.getRootNode();
    //myGraph.setCenter(nodesMap.get(rootNode), centerX, centerY);

    final List<Ring> ringsList = fillRings(dataObject, rootNode);

    final Rectangle box = myGraph.getBoundingBox();

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

    layoutEdges(umlGraphBuilder, box);

    return true;
  }

  private void layoutEdges(UmlGraphBuilder umlGraphBuilder, Rectangle initialBox) {
    final Edge[] edgeArray = myGraph.getEdgeArray();
    for (Edge edge : edgeArray) {
      final DiagramNodeBodyComponent sourceComponent = myNodeBodiesMap.get(umlGraphBuilder.getNodeObject(edge.source()));
      final DiagramNodeBodyComponent targetComponent = myNodeBodiesMap.get(umlGraphBuilder.getNodeObject(edge.target()));
      if (sourceComponent == null || targetComponent == null) continue;
      final AngularUiRouterEdge edgeObject = (AngularUiRouterEdge)umlGraphBuilder.getEdgeObject(edge);
      if (edgeObject == null) continue;

      final double sourceXcenter = myGraph.getCenterX(edge.source());
      final double targetXcenter = myGraph.getCenterX(edge.target());
      boolean sourceOnTheLeft = sourceXcenter < targetXcenter;
      double sourceX;
      double targetX;
      double sourceY;
      double targetY;

      final QuadCurveEdgeRealizer realizer = GraphManager.getGraphManager().createQuadCurveEdgeRealizer();
      realizer.setLineColor(Gray._70);
      //top
      sourceY = myGraph.getCenterY(edge.source()) - myGraph.getHeight(edge.source())/2;
      targetY = myGraph.getCenterY(edge.target()) - myGraph.getHeight(edge.target())/2;
      if (AngularUiRouterEdge.Type.parent.equals(edgeObject.getType())) {
        sourceX = sourceXcenter;
        targetX = targetXcenter;
        //realizer.setLineColor(new Color(255, 224, 69));
        realizer.setLineType(LineType.DOTTED_1);
      } else {
        //if (AngularUiRouterEdge.Type.providesTemplate.equals(edgeObject.getType())) realizer.setLineColor(new Color(113, 136, 255));
        if (AngularUiRouterEdge.Type.fillsTemplate.equals(edgeObject.getType())) {
          //realizer.setLineColor(new Color(110, 255, 192));
          realizer.setLineType(LineType.DASHED_1);
        }

        final double sourceWidth = myGraph.getWidth(edge.source());
        final double targetWidth = myGraph.getWidth(edge.target());
        sourceX = sourceOnTheLeft ? (sourceXcenter + sourceWidth / 2) : (sourceXcenter - sourceWidth / 2);
        targetX = sourceOnTheLeft ? (targetXcenter - targetWidth / 2) : (targetXcenter + targetWidth / 2);

        final AngularUiRouterNode source = (AngularUiRouterNode)edgeObject.getSource();
        int idxSource = getOffsetInElements(source, edgeObject.getSourceName());
        final AngularUiRouterNode target = (AngularUiRouterNode)edgeObject.getTarget();
        int idxTarget = getOffsetInElements(target, edgeObject.getTargetName());

        final double sourceHeight = myGraph.getHeight(edge.source());
        final double targetHeight = myGraph.getHeight(edge.target());

        final int sourceSize = source.getIdentifyingElement().getChildrenList().size();
        final double step = sourceHeight / (sourceSize + 1);
        sourceY += sourceSize == 0 || idxSource < 0 ? (step * 0.5) : (idxSource * step + step * 1.5);

        final int targetSize = target.getIdentifyingElement().getChildrenList().size();
        final double targetStep = targetHeight / (targetSize + 1);
        targetY += targetSize == 0 || idxTarget < 0 ? (targetStep * 0.5) : (idxTarget * targetStep + targetStep * 1.5);
      }

      /*final Rectangle currentBox = myGraph.getBoundingBox();
      int deltaX = currentBox.getX()*/
      //realizer.getSourcePort().setOffsets(sourceX, sourceY);
      //realizer.getTargetPort().setOffsets(targetX, targetY);
      //realizer.setSourcePoint(GraphManager.getGraphManager().createYPoint(sourceX, sourceY));
      //realizer.setTargetPoint(GraphManager.getGraphManager().createYPoint(targetX, targetY));

      myGraph.setRealizer(edge, realizer);
      final GraphManager gm = GraphManager.getGraphManager();
      myGraph.setEndPointsAbs(edge, gm.createYPoint(sourceX, sourceY), gm.createYPoint(targetX, targetY));
    }
  }

  private static int getOffsetInElements(final AngularUiRouterNode node, final String name) {
    final List<DiagramObject> list = node.getIdentifyingElement().getChildrenList();
    int idx = -1;
    if (name != null && list != null) {
      for (int i = 0; i < list.size(); i++) {
        DiagramObject object = list.get(i);
        if (object.getName().equals(name)) {
          idx = i;
          break;
        }
      }
    }
    return idx;
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
