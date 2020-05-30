package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramEdge;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.view.Arrow;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.LineType;
import com.intellij.openapi.graph.view.QuadCurveEdgeRealizer;
import com.intellij.ui.Gray;
import com.intellij.uml.UmlGraphBuilder;

import java.util.List;

/**
 * @author Irina.Chernushina on 5/31/2016.
 */
public class AngularEdgeLayouter {
  public void layoutEdges(UmlGraphBuilder umlGraphBuilder, Graph2D graph) {
    final Edge[] edgeArray = graph.getEdgeArray();
    for (Edge edge : edgeArray) {
      final DiagramEdge edgeObject = umlGraphBuilder.getEdgeObject(edge);
      if (!(edgeObject instanceof AngularUiRouterEdge)) continue;
      final OneEdgeLayouter oneEdgeLayouter = new OneEdgeLayouter(edge, (AngularUiRouterEdge)edgeObject, graph);
      oneEdgeLayouter.calculateEdgeLayout();
      graph.setRealizer(edge, oneEdgeLayouter.myRealizer);
      final GraphManager gm = GraphManager.getGraphManager();
      graph.setEndPointsAbs(edge, gm.createYPoint(oneEdgeLayouter.sourceX, oneEdgeLayouter.sourceY),
                            gm.createYPoint(oneEdgeLayouter.targetX, oneEdgeLayouter.targetY));
    }
  }

  static class OneEdgeLayouter {
    private final Edge edge;
    private final AngularUiRouterEdge edgeObject;
    private final double sourceXcenter;
    private final double sourceYcenter;
    private final double targetXcenter;
    private final double targetYcenter;
    private final double sourceWidth;
    private final double targetWidth;
    private final double sourceHeight;
    private final double targetHeight;

    private double sourceX;
    private double targetX;
    private double sourceY;
    private double targetY;
    private QuadCurveEdgeRealizer myRealizer;

    OneEdgeLayouter(Edge edge, AngularUiRouterEdge edgeObject, Graph2D graph) {
      this.edge = edge;
      this.edgeObject = edgeObject;
      sourceXcenter = graph.getCenterX(edge.source());
      targetXcenter = graph.getCenterX(edge.target());
      sourceYcenter = graph.getCenterY(edge.source());
      targetYcenter = graph.getCenterY(edge.target());

      sourceWidth = graph.getWidth(edge.source());
      targetWidth = graph.getWidth(edge.target());
      sourceHeight = graph.getHeight(edge.source());
      targetHeight = graph.getHeight(edge.target());
    }

    public void calculateEdgeLayout() {
      if (edgeObject == null) return;

      myRealizer = GraphManager.getGraphManager().createQuadCurveEdgeRealizer();
      myRealizer.setLineColor(Gray._170);
      myRealizer.setSourceArrow(Arrow.SHORT);

      final AngularUiRouterNode source = (AngularUiRouterNode)edgeObject.getSource();
      final AngularUiRouterNode target = (AngularUiRouterNode)edgeObject.getTarget();

      boolean sourceOnTheLeft = sourceXcenter < targetXcenter;
      int idxSource = -1;
      int idxTarget = -1;
      if (AngularUiRouterEdge.Type.parent.equals(edgeObject.getType())) {
        //realizer.setLineColor(new Color(255, 224, 69));
        myRealizer.setLineType(LineType.DOTTED_1);
      }
      else {
        //if (AngularUiRouterEdge.Type.providesTemplate.equals(edgeObject.getType())) realizer.setLineColor(new Color(113, 136, 255));
        if (AngularUiRouterEdge.Type.fillsTemplate.equals(edgeObject.getType())) {
          //realizer.setLineColor(new Color(110, 255, 192));
          myRealizer.setLineType(LineType.DASHED_1);
        }
        idxSource = getOffsetInElements(source, edgeObject.getSourceName());
        idxTarget = getOffsetInElements(target, edgeObject.getTargetName());
      }

      sourceX = sourceOnTheLeft ? (sourceXcenter + sourceWidth / 2) : (sourceXcenter - sourceWidth / 2);
      targetX = sourceOnTheLeft ? (targetXcenter - targetWidth / 2) : (targetXcenter + targetWidth / 2);

      final int sourceSize = source.getIdentifyingElement().getChildrenList().size();
      final double step = sourceHeight / (sourceSize + 1);
      sourceY += sourceSize == 0 || idxSource < 0 ? (step * 0.5) : (idxSource * step + step * 1.5);

      final int targetSize = target.getIdentifyingElement().getChildrenList().size();
      final double targetStep = targetHeight / (targetSize + 1);
      targetY += targetSize == 0 || idxTarget < 0 ? (targetStep * 0.5) : (idxTarget * targetStep + targetStep * 1.5);
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

    public double getSourceX() {
      return sourceX;
    }

    public double getTargetX() {
      return targetX;
    }

    public double getSourceY() {
      return sourceY;
    }

    public double getTargetY() {
      return targetY;
    }

    public QuadCurveEdgeRealizer getRealizer() {
      return myRealizer;
    }
  }
}
