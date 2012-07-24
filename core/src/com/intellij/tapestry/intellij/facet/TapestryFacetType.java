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
import com.intellij.tapestry.core.util.TapestryIcons;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;

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
      final HashSet<String> componentDirectories = new HashSet<String>(Arrays.asList(TapestryConstants.ELEMENT_PACKAGES));

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
                                                          TapestryVersion.TAPESTRY_5_3_3);
    }
  }
}
