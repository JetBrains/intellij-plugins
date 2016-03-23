package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.presentation.DiagramLineType;

import java.awt.*;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class AngularUiRouterEdge extends DiagramEdgeBase<DiagramObject> {
  private final static DiagramRelationshipInfo DEPENDS = new DiagramRelationshipInfoAdapter("DEPENDS", DiagramLineType.SOLID) {
    @Override
    public Shape getStartArrow() {
      return NONE;
    }

    @Override
    public Shape getEndArrow() {
      return STANDARD;
    }
  };

  public AngularUiRouterEdge(DiagramNode<DiagramObject> source, DiagramNode<DiagramObject> target) {
    super(source, target, DEPENDS);
  }

  public AngularUiRouterEdge(DiagramNode<DiagramObject> source, DiagramNode<DiagramObject> target, final String label) {
    super(source, target, new DiagramRelationshipInfoAdapter("BUILTIN", DiagramLineType.SOLID) {
      @Override
      public String getLabel() {
        return label;
      }

      @Override
      public Shape getStartArrow() {
        return NONE;
      }

      @Override
      public Shape getEndArrow() {
        return STANDARD;
      }
    });
  }
}
