// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.ui.navigation.Place;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FlexProjectStructureUtil {

  public static Place createPlace(FlexBCConfigurable configurable, @Nullable String tabName) {
    Place place = new Place()
      .putPath(ProjectStructureConfigurable.CATEGORY, ProjectStructureConfigurable.getInstance(configurable.getModule().getProject()).getModulesConfig())
      .putPath(MasterDetailsComponent.TREE_OBJECT, configurable.getEditableObject());
    if (tabName != null) {
      place.putPath(CompositeConfigurable.TAB_NAME, tabName);
    }
    return place;
  }

  public static Place createPlace(Module module, DependenciesConfigurable configurable) {
    final FlexBCConfigurable bcConfigurable = findBcConfigurable(module, configurable);
    return createPlace(bcConfigurable, DependenciesConfigurable.getTabName());
  }

  public static Place createPlace(final Module module, final FlexBuildConfiguration bc, @Nullable final String tabName) {
    final FlexBCConfigurable bcConfigurable = findBcConfigurable(module, bc);
    return createPlace(bcConfigurable, tabName);

  }

  @Nullable
  private static FlexBCConfigurable findBcConfigurable(Module module, final DependenciesConfigurable configurable) {
    final List<CompositeConfigurable> configurables =
      FlexBuildConfigurationsExtension.getInstance().getConfigurator().getBCConfigurables(module);

    for (CompositeConfigurable compositeConfigurable : configurables) {
      final FlexBCConfigurable bcConfigurable = FlexBCConfigurable.unwrap(compositeConfigurable);
      if (bcConfigurable.getEditableObject().getDependencies() == configurable.getEditableObject()) {
        return bcConfigurable;
      }
    }
    return null;
  }

  @Nullable
  private static FlexBCConfigurable findBcConfigurable(Module module, final FlexBuildConfiguration bc) {
    final List<CompositeConfigurable> configurables =
      FlexBuildConfigurationsExtension.getInstance().getConfigurator().getBCConfigurables(module);

    for (CompositeConfigurable compositeConfigurable : configurables) {
      final FlexBCConfigurable bcConfigurable = FlexBCConfigurable.unwrap(compositeConfigurable);
      if (bcConfigurable.getEditableObject() == bc) {
        return bcConfigurable;
      }
    }
    return null;
  }
}
