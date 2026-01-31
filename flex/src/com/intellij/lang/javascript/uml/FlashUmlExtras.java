// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramEdge;
import com.intellij.diagram.DiagramElementsProvider;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.settings.DiagramConfigElement;
import com.intellij.diagram.settings.DiagramConfigGroup;
import com.intellij.diagram.util.DiagramSelectionService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.pom.Navigatable;
import com.intellij.uml.utils.DiagramBundle;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public final class FlashUmlExtras extends DiagramExtras<Object> {
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
  public DiagramElementsProvider<Object> @NotNull [] getElementsProviders() {
    //noinspection unchecked
    return myProviders;
  }

  @Override
  public FlashUmlDndProvider getDnDProvider() {
    return myDndProvider;
  }

  @Override
  public AnAction getAddElementHandler() {
    return DiagramAddElementAction.DEFAULT_ADD_HANDLER;
  }

  @Override
  public DiagramConfigGroup @NotNull [] getAdditionalDiagramSettings() {
    return myAdditionalSettingsGroups;
  }

  @Override
  public void uiDataSnapshot(@NotNull DataSink sink, @NotNull List<DiagramNode<Object>> nodes, @NotNull DiagramBuilder builder) {
    List<DiagramEdge<?>> edges = DiagramSelectionService.getInstance().getSelectedEdges(builder);
    DiagramEdge<?> edge = ContainerUtil.getOnlyItem(edges);
    if (!(edge instanceof FlashUmlEdge umlEdge)) return;
    sink.lazy(CommonDataKeys.NAVIGATABLE, () -> {
      DiagramRelationshipInfo relationship = umlEdge.getRelationship();
      return relationship instanceof FlashUmlRelationship o &&
             o.getElement() instanceof Navigatable oo ? oo : null;
    });
  }

  @Override
  public boolean isExpandCollapseActionsImplemented() {
    return true;
  }
}
