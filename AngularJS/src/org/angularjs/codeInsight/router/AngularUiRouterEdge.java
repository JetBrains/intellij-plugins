package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.presentation.DiagramLineType;
import org.jetbrains.annotations.Nullable;

public final class AngularUiRouterEdge extends DiagramEdgeBase<DiagramObject> {
  private final String myLabel;
  private final Type myType;
  private @Nullable String mySourceName;
  private @Nullable String myTargetName;

  public AngularUiRouterEdge(DiagramNode<DiagramObject> source,
                             DiagramNode<DiagramObject> target,
                             final String label,
                             Type type) {
    super(source, target,
          new DiagramRelationshipInfoAdapter.Builder()
            .setName("BUILTIN")
            .setLineType(DiagramLineType.SOLID)
            .setSourceArrow(DiagramRelationshipInfo.NONE)
            .setTargetArrow(DiagramRelationshipInfo.STANDARD)
            .create());
    myLabel = label;
    myType = type;
  }

  public @Nullable String getSourceName() {
    return mySourceName;
  }

  public AngularUiRouterEdge setSourceName(@Nullable String sourceName) {
    mySourceName = sourceName;
    return this;
  }

  public @Nullable String getTargetName() {
    return myTargetName;
  }

  public String getLabel() {
    return myLabel;
  }

  public AngularUiRouterEdge setTargetName(@Nullable String targetName) {
    myTargetName = targetName;
    return this;
  }

  public Type getType() {
    return myType;
  }

  public enum Type {
    providesTemplate, fillsTemplate, parent
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    AngularUiRouterEdge edge = (AngularUiRouterEdge)o;

    if (myLabel != null ? !myLabel.equals(edge.myLabel) : edge.myLabel != null) return false;
    if (myType != edge.myType) return false;
    if (mySourceName != null ? !mySourceName.equals(edge.mySourceName) : edge.mySourceName != null) return false;
    if (myTargetName != null ? !myTargetName.equals(edge.myTargetName) : edge.myTargetName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (myLabel != null ? myLabel.hashCode() : 0);
    result = 31 * result + (myType != null ? myType.hashCode() : 0);
    result = 31 * result + (mySourceName != null ? mySourceName.hashCode() : 0);
    result = 31 * result + (myTargetName != null ? myTargetName.hashCode() : 0);
    return result;
  }
}
