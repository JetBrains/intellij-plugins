package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.settings.DiagramConfigElement;
import com.intellij.diagram.settings.DiagramConfigGroup;
import com.intellij.diagram.util.DiagramUtils;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.uml.utils.DiagramBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public class FlashUmlExtras extends DiagramExtras<Object> {
  private final DiagramElementsProvider[] myProviders = {new FlashUmlSupersProvider(), new FlashUmlImplementationsProvider()};

  private final FlashUmlDndProvider myDndProvider = new FlashUmlDndProvider();


  private final DiagramConfigGroup[] myAdditionalSettingsGroups;

  FlashUmlExtras() {
    DiagramConfigGroup dependenciesGroup = new DiagramConfigGroup(DiagramBundle.message("uml.dependencies.settings.group.title"));
    for (FlashUmlDependenciesSettingsOption option : FlashUmlDependenciesSettingsOption.values()) {
      dependenciesGroup.addElement(new DiagramConfigElement(option.getDisplayName(), true));
    }
    myAdditionalSettingsGroups = new DiagramConfigGroup[]{dependenciesGroup};
  }

  @Override
  public DiagramElementsProvider<Object>[] getElementsProviders() {
    //noinspection unchecked
    return myProviders;
  }

  @Override
  public FlashUmlDndProvider getDnDProvider() {
    return myDndProvider;
  }

  @Override
  public DiagramAddElementAction getAddElementHandler() {
    return DEFAULT_ADD_HANDLER;
  }

  @Override
  public DiagramConfigGroup @NotNull [] getAdditionalDiagramSettings() {
    return myAdditionalSettingsGroups;
  }

  @Override
  public Object getData(@NotNull final String dataId, final List<DiagramNode<Object>> diagramNodes, final DiagramBuilder builder) {
    if (!CommonDataKeys.NAVIGATABLE.is(dataId)) {
      return null;
    }

    final List<DiagramEdge> edges = DiagramUtils.getSelectedEdges(builder);
    if (edges.size() != 1) {
      return null;
    }

    final DiagramEdge edge = edges.get(0);
    if (edge instanceof FlashUmlEdge) {
      DiagramRelationshipInfo relationship = edge.getRelationship();
      return relationship instanceof FlashUmlRelationship ? ((FlashUmlRelationship)relationship).getElement() : null;
    }
    return null;
  }

  @Override
  public boolean isExpandCollapseActionsImplemented() {
    return true;
  }
}
