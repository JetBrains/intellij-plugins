package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.diagram.DiagramProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class AngularUiRouterNode extends DiagramNodeBase<DiagramObject> {
  @NotNull private final DiagramObject myDiagramObject;

  public AngularUiRouterNode(@NotNull DiagramObject diagramObject, @NotNull DiagramProvider<DiagramObject> provider) {
    super(provider);
    myDiagramObject = diagramObject;
  }

  @Nullable
  @Override
  public String getTooltip() {
    return myDiagramObject.getName();
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public DiagramObject getIdentifyingElement() {
    return myDiagramObject;
  }
}
