package org.osmorc.facet;

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.framework.library.DownloadableLibraryService;
import com.intellij.framework.library.FrameworkSupportWithLibrary;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurableBase;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportProviderBase;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OsgiFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<OsmorcFacet> {
  public OsgiFrameworkSupportProvider() {
    super(OsmorcFacetType.getInstance());
  }

  @NotNull
  @Override
  public FrameworkSupportConfigurableBase createConfigurable(@NotNull FrameworkSupportModel model) {
    return new FrameworkConfigurable(this, model);
  }

  @Override
  protected void setupConfiguration(OsmorcFacet facet, ModifiableRootModel rootModel, FrameworkVersion version) { }


  private static class FrameworkConfigurable extends FrameworkSupportConfigurableBase implements FrameworkSupportWithLibrary {
    FrameworkConfigurable(FrameworkSupportProviderBase provider, FrameworkSupportModel model) {
      super(provider, model);
    }

    @Nullable
    @Override
    public CustomLibraryDescription createLibraryDescription() {
      return DownloadableLibraryService.getInstance().createDescriptionForType(OsgiCoreLibraryType.class);
    }

    @Override
    public boolean isLibraryOnly() {
      return false;
    }
  }
}
