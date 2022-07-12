// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.diagram;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramColorManagerBase;
import com.intellij.diagram.DiagramColors;
import com.intellij.diagram.DiagramNode;
import com.intellij.openapi.components.Service;
import com.intellij.ui.JBColor;
import com.intellij.ui.LightColors;
import org.angularjs.codeInsight.router.DiagramObject;
import org.angularjs.codeInsight.router.Type;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

@Service(Service.Level.APP)
public final class AngularUiRouterDiagramColorManager extends DiagramColorManagerBase {
  public static final JBColor VIEW_COLOR = new JBColor(new Color(0xE1FFFC), new Color(0x589df6));

  @Override
  public @NotNull Color getNodeHeaderBackground(@NotNull DiagramBuilder builder, @NotNull DiagramNode node, Object element) {
    return getColor(builder, element);
  }

  @Override
  public @NotNull Color getNodeBackground(@NotNull DiagramBuilder builder,
                                          @NotNull DiagramNode node,
                                          Object element,
                                          boolean selected) {
    return getColor(builder, element);
  }

  private @NotNull Color getColor(DiagramBuilder builder, Object nodeElement) {
    if (nodeElement instanceof DiagramObject) {
      DiagramObject element = ((DiagramObject)nodeElement);
      if (Type.state.equals(element.getType())) {
        return LightColors.YELLOW;
      }
      else if (Type.view.equals(element.getType())) {
        return VIEW_COLOR;
      }
      else if (Type.template.equals(element.getType())) {
        return LightColors.GREEN;
      }
      else if (Type.templatePlaceholder.equals(element.getType())) {
        return LightColors.SLIGHTLY_GREEN;
      }
    }
    return Objects.requireNonNull(builder.getColorScheme().getColor(DiagramColors.NODE_HEADER));
  }
}