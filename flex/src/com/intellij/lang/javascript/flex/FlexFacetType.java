package com.intellij.lang.javascript.flex;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlexFacetType extends FacetType<FlexFacet, FlexFacetConfiguration> {
  public static final Icon ourFlexIcon = IconLoader.getIcon("flex.png", FlexFacetType.class);

  public FlexFacetType() {
    super(FlexFacet.ID, "flex", "Flex");
  }

  public static FlexFacetType getInstance() {
    return findInstance(FlexFacetType.class);
  }

  public FlexFacetConfiguration createDefaultConfiguration() {
    return new FlexFacetConfigurationImpl();
  }

  public FlexFacet createFacet(@NotNull final Module module, final String name, @NotNull final FlexFacetConfiguration configuration, @Nullable final Facet underlyingFacet) {
    return new FlexFacet(module, name, configuration);
  }

  public Icon getIcon() {
    return ourFlexIcon;
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return !(moduleType instanceof FlexModuleType);
  }

  public boolean isOnlyOneFacetAllowed() {
    return false;
  }
}
