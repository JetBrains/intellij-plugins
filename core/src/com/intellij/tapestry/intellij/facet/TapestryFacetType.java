package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.util.TapestryIcons;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TapestryFacetType extends FacetType<TapestryFacet, TapestryFacetConfiguration> {

  public static final FacetTypeId<TapestryFacet> ID = new FacetTypeId<TapestryFacet>("tapestry");

  TapestryFacetType() {
    super(ID, "tapestry", "Tapestry");
  }

  public static TapestryFacetType getInstance() {
    return findInstance(TapestryFacetType.class);
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
    return TapestryIcons.TAPESTRY_LOGO_SMALL;
  }

  public static class TapestryFrameworkDetector extends FacetBasedFrameworkDetector<TapestryFacet, TapestryFacetConfiguration> {
    public TapestryFrameworkDetector() {
      super("tapestry");
    }

    @Override
    public FacetType<TapestryFacet, TapestryFacetConfiguration> getFacetType() {
      return TapestryFacetType.getInstance();
    }

    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern() {
      return FileContentPattern.fileContent();
    }

    @Override
    public void setupFacet(@NotNull TapestryFacet facet, ModifiableRootModel model) {
      final TapestryFacetConfiguration configuration = facet.getConfiguration();
      for (VirtualFile root : model.getSourceRoots()) {
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

    @Nullable
    private static VirtualFile findDirectoryByName(VirtualFile file, Set<String> names, int level) {
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
  }
}
