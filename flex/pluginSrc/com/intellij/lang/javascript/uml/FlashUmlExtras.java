package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.settings.DiagramConfigElement;
import com.intellij.diagram.settings.DiagramConfigGroup;
import com.intellij.diagram.util.DiagramUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public class FlashUmlExtras extends DiagramExtras<Object> {
  private static final DiagramElementsProvider[] PROVIDERS = {new FlashUmlSupersProvider(), new FlashUmlImplementationsProvider()};

  private static final FlashUmlDndProvider DND_PROVIDER = new FlashUmlDndProvider();


  private static final DiagramConfigGroup[] ADDITIONAL_SETTINGS_GROUPS;

  static {
    DiagramConfigGroup dependenciesGroup = new DiagramConfigGroup(FlexBundle.message("uml.dependencies.settings.group.title"));
    for (FlashUmlDependenciesSettingsOption option : FlashUmlDependenciesSettingsOption.values()) {
      dependenciesGroup.addElement(new DiagramConfigElement(option.getDisplayName(), true));
    }
    ADDITIONAL_SETTINGS_GROUPS = new DiagramConfigGroup[]{dependenciesGroup};
  }

  @Override
  public DiagramElementsProvider<Object>[] getElementsProviders() {
    //noinspection unchecked
    return PROVIDERS;
  }

  @Override
  public FlashUmlDndProvider getDnDProvider() {
    return DND_PROVIDER;
  }

  @Override
  public DiagramAddElementAction getAddElementHandler() {
    return DEFAULT_ADD_HANDLER;
  }

  @NotNull
  @Override
  public DiagramConfigGroup[] getAdditionalDiagramSettings() {
    return ADDITIONAL_SETTINGS_GROUPS;
  }

  @Override
  public Object getData(final String dataId, final List<DiagramNode<Object>> diagramNodes, final DiagramBuilder builder) {
    if (!PlatformDataKeys.NAVIGATABLE.is(dataId)) {
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
