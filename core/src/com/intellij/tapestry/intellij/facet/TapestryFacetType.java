package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.lang.TmlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class TapestryFacetType extends FacetType<TapestryFacet, TapestryFacetConfiguration> {

  public static final FacetTypeId<TapestryFacet> ID = new FacetTypeId<TapestryFacet>("tapestry");
  public static final TapestryFacetType INSTANCE = new TapestryFacetType();

  public TapestryFacetType() {
    super(ID, "tapestry", "Tapestry");
  }

  public TapestryFacetConfiguration createDefaultConfiguration() {
    return new TapestryFacetConfiguration();
  }

  public TapestryFacet createFacet(@NotNull Module module,
                                   String name,
                                   @NotNull TapestryFacetConfiguration configuration,
                                   @Nullable Facet underlyingFacet) {
    return new TapestryFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  @Override
  public Icon getIcon() {
    return Icons.TAPESTRY_LOGO_SMALL;
  }

  @Override
  public void registerDetectors(final FacetDetectorRegistry<TapestryFacetConfiguration> registry) {
    VirtualFileFilter filter = new VirtualFileFilter() {
      public boolean accept(VirtualFile virtualFile) {
        final String extension = virtualFile.getExtension();
        return extension != null && FileTypeManager.getInstance().getStdFileType(extension) instanceof TmlFileType;
      }
    };
    registry.registerUniversalDetector(new TmlFileType(), filter, new TapestryFacetDetector());

  }

  private static class TapestryFacetDetector extends FacetDetector<VirtualFile, TapestryFacetConfiguration> {

    public TapestryFacetConfiguration detectFacet(final VirtualFile source,
                                                  final Collection<TapestryFacetConfiguration> existentFacetConfigurations) {
      if (existentFacetConfigurations.size() > 0) return null;
      //PsiFile psi = myManager.findFile(source);
      //
      //if (psi != null && psi.getParent() instanceof PsiPackage) {
      //  return new TapestryFacetConfiguration();
      //}
      return new TapestryFacetConfiguration();
    }
  }
}
