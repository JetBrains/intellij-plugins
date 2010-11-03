package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetModel;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.lang.TmlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    registry.registerUniversalDetector(TmlFileType.INSTANCE, VirtualFileFilter.ALL, new TapestryFacetDetector());
  }

  private static class TapestryFacetDetector extends FacetDetector<VirtualFile, TapestryFacetConfiguration> {
    public TapestryFacetDetector() {
      super("tapestry-detector");
    }

    public TapestryFacetConfiguration detectFacet(final VirtualFile source,
                                                  final Collection<TapestryFacetConfiguration> existentFacetConfigurations) {
      VirtualFile sourceParent = source.getParent();
      if (sourceParent == null || existentFacetConfigurations.size() > 0) return null;
      return new TapestryFacetConfiguration();
    }

    @Override
    public void beforeFacetAdded(@NotNull Facet facet, FacetModel facetModel, @NotNull ModifiableRootModel modifiableRootModel) {
      final TapestryFacetConfiguration configuration = (TapestryFacetConfiguration)facet.getConfiguration();
      for (VirtualFile root : modifiableRootModel.getSourceRoots()) {
        VirtualFile dir = findDirectoryByName(root, new HashSet<String>(Arrays.asList(TapestryConstants.ELEMENT_PACKAGES)), 2);
        if (dir != null) {
          String relativePath = VfsUtil.getRelativePath(dir.getParent(), root, '.');
          configuration.setApplicationPackage(relativePath);
          break;
        }
      }
      TapestryFrameworkSupportProvider.setupConfiguration(configuration, facet.getModule(),
                                                          TapestryVersion.TAPESTRY_5_1_0_5);
    }

    private VirtualFile findDirectoryByName(VirtualFile file, Set<String> names, int level) {
      for (VirtualFile child : file.getChildren()) {
        if (child.isDirectory()) {
          if (names.contains(child.getName())) {
            return child;
          }
          else if (level > 0) {
            final VirtualFile result = findDirectoryByName(child, names, level - 1);
            if (result != null) return result;
          }
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
