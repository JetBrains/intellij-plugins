package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileContent;
import icons.TapestryIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

public class TapestryFacetType extends FacetType<TapestryFacet, TapestryFacetConfiguration> {

  public static final FacetTypeId<TapestryFacet> ID = new FacetTypeId<>("tapestry");

  TapestryFacetType() {
    super(ID, "tapestry", "Tapestry");
  }

  public static TapestryFacetType getInstance() {
    return findInstance(TapestryFacetType.class);
  }

  @Override
  public TapestryFacetConfiguration createDefaultConfiguration() {
    return new TapestryFacetConfiguration();
  }

  @Override
  public TapestryFacet createFacet(@NotNull Module module,
                                   String name,
                                   @NotNull TapestryFacetConfiguration configuration,
                                   @Nullable Facet underlyingFacet) {
    return new TapestryFacet(this, module, name, configuration, underlyingFacet);
  }

  @Override
  public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  @Override
  public Icon getIcon() {
    return TapestryIcons.Tapestry_logo_small;
  }

  public static class TapestryFrameworkDetector extends FacetBasedFrameworkDetector<TapestryFacet, TapestryFacetConfiguration> {
    public TapestryFrameworkDetector() {
      super("tapestry");
    }

    @NotNull
    @Override
    public FacetType<TapestryFacet, TapestryFacetConfiguration> getFacetType() {
      return TapestryFacetType.getInstance();
    }

    @NotNull
    @Override
    public FileType getFileType() {
      return TmlFileType.INSTANCE;
    }

    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern() {
      return FileContentPattern.fileContent();
    }

    @Override
    public void setupFacet(@NotNull TapestryFacet facet, ModifiableRootModel model) {
      final TapestryFacetConfiguration configuration = facet.getConfiguration();
      Set<String> componentDirectories = ContainerUtil.set(TapestryConstants.ELEMENT_PACKAGES);

      for(VirtualFile file:FileTypeIndex.getFiles(TmlFileType.INSTANCE, GlobalSearchScope.moduleScope(facet.getModule()))) {
        final VirtualFile parent = file.getParent();
        if (componentDirectories.contains(parent.getName())) {
          final VirtualFile sourceRootForFile = ProjectRootManager.getInstance(model.getProject()).getFileIndex().getSourceRootForFile(parent);
          if (sourceRootForFile == null) continue;
          String relativePath = VfsUtilCore.getRelativePath(parent.getParent(), sourceRootForFile, '.');
          configuration.setApplicationPackage(relativePath);
          break;
        }
      }

      TapestryFrameworkSupportProvider.setupConfiguration(configuration, facet.getModule(),
                                                          TapestryVersion.TAPESTRY_5_3_6);
    }
  }
}
