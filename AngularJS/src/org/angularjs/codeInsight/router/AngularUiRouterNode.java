package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.diagram.DiagramProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AngularUiRouterNode extends DiagramNodeBase<DiagramObject> {
  private final @NotNull DiagramObject myDiagramObject;

  public AngularUiRouterNode(@NotNull DiagramObject diagramObject, @NotNull DiagramProvider<DiagramObject> provider) {
    super(provider);
    myDiagramObject = diagramObject;
  }

  @Override
  public @Nullable String getTooltip() {
    return myDiagramObject.getName();
  }

  @Override
  public @Nullable Icon getIcon() {
    return null;
  }

  @Override
  public @NotNull DiagramObject getIdentifyingElement() {
    return myDiagramObject;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    AngularUiRouterNode node = (AngularUiRouterNode)o;

    if (!myDiagramObject.equals(node.myDiagramObject)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + myDiagramObject.hashCode();
    return result;
  }
}
