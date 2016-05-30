package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.presentation.DiagramLineType;
import org.jetbrains.annotations.Nullable;

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

  private final Type myType;
  @Nullable
  private String mySourceName;
  @Nullable
  private String myTargetName;

  public AngularUiRouterEdge(DiagramNode<DiagramObject> source, DiagramNode<DiagramObject> target, Type type) {
    super(source, target, DEPENDS);
    myType = type;
  }

  public AngularUiRouterEdge(DiagramNode<DiagramObject> source,
                             DiagramNode<DiagramObject> target,
                             final String label,
                             Type type) {
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
    myType = type;
  }

  @Nullable
  public String getSourceName() {
    return mySourceName;
  }

  public AngularUiRouterEdge setSourceName(@Nullable String sourceName) {
    mySourceName = sourceName;
    return this;
  }

  @Nullable
  public String getTargetName() {
    return myTargetName;
  }

  public AngularUiRouterEdge setTargetName(@Nullable String targetName) {
    myTargetName = targetName;
    return this;
  }

  public Type getType() {
    return myType;
  }

  public static enum Type {
    providesTemplate, fillsTemplate, parent
  }
}
