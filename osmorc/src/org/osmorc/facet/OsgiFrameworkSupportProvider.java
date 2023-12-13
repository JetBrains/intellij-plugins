// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.facet;

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.framework.library.DownloadableLibraryService;
import com.intellij.framework.library.FrameworkSupportWithLibrary;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurableBase;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportProviderBase;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OsgiFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<OsmorcFacet> {
  public OsgiFrameworkSupportProvider() {
    super(OsmorcFacetType.getInstance());
  }

  @Override
  public @NotNull FrameworkSupportConfigurableBase createConfigurable(@NotNull FrameworkSupportModel model) {
    return new FrameworkConfigurable(this, model);
  }

  @Override
  protected void setupConfiguration(OsmorcFacet facet, ModifiableRootModel rootModel, FrameworkVersion version) { }

  @Override
  public boolean isEnabledForModuleBuilder(@NotNull ModuleBuilder builder) {
    String id = builder.getBuilderId();
    return "LegacyJavaEE".equals(id) || "LegacySpring".equals(id) || "LegacyJBoss".equals(id);
  }

  private static class FrameworkConfigurable extends FrameworkSupportConfigurableBase implements FrameworkSupportWithLibrary {
    FrameworkConfigurable(FrameworkSupportProviderBase provider, FrameworkSupportModel model) {
      super(provider, model);
    }

    @Override
    public @Nullable CustomLibraryDescription createLibraryDescription() {
      return DownloadableLibraryService.getInstance().createDescriptionForType(OsgiCoreLibraryType.class);
    }

    @Override
    public boolean isLibraryOnly() {
      return false;
    }
  }
}
