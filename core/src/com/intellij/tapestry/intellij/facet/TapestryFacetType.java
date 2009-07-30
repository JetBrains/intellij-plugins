package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.core.TapestryConstants;
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
        return virtualFile.getFileType() instanceof TmlFileType;
      }
    };
    final TapestryFacetDetector detector = new TapestryFacetDetector();
    registry.registerOnTheFlyDetector(TmlFileType.INSTANCE, filter, Condition.TRUE, detector);
  }

  private static class TapestryFacetDetector extends FacetDetector<PsiFile, TapestryFacetConfiguration> {
    public TapestryFacetDetector() {
      super("tapestry-detector");
    }

    public TapestryFacetConfiguration detectFacet(final PsiFile source,
                                                  final Collection<TapestryFacetConfiguration> existentFacetConfigurations) {
      PsiDirectory sourceParent = source.getParent();
      if (sourceParent == null || existentFacetConfigurations.size() > 0) return null;

      Module module = ModuleUtil.findModuleForPsiElement(sourceParent);
      String relativePath;
      for (VirtualFile srcRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
        relativePath = VfsUtil.getRelativePath(sourceParent.getVirtualFile(), srcRoot, '.');
        if (relativePath == null) continue;
        for (String packageName : TapestryConstants.ELEMENT_PACKAGES) {
          final int i = indexOf(relativePath, packageName);
          if (i < 0) continue;
          final TapestryFacetConfiguration conf = new TapestryFacetConfiguration();
          conf.setApplicationPackage(relativePath.substring(0, i));
          TapestryFrameworkSupportProvider.setupConfiguration(conf, module, TapestryVersion.TAPESTRY_5_1_0_5.toString());
          return conf;
        }
      }
      return null;
    }

    private static int indexOf(String relativePath, String packageName) {
      if(relativePath.startsWith(packageName + ".")) return 0;
      int start = relativePath.indexOf("." + packageName);
      final int end = start + packageName.length() + 1;
      if (start > 0 && (end == relativePath.length() || relativePath.charAt(end) == '.')) return start;
      return -1;
    }
  }
}
