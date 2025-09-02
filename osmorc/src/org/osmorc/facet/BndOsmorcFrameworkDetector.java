// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.jetbrains.osgi.bnd.BndFileType;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;

import java.util.Collection;

/**
 * Framework detector for Bnd/Bndtools files.
 */
public class BndOsmorcFrameworkDetector extends FacetBasedFrameworkDetector<OsmorcFacet, OsmorcFacetConfiguration> {
  private static final String BND_FILE_NAME = "bnd.bnd";

  public BndOsmorcFrameworkDetector() {
    super("osmorc");
  }

  @Override
  public @NotNull FacetType<OsmorcFacet, OsmorcFacetConfiguration> getFacetType() {
    return OsmorcFacetType.getInstance();
  }

  @VisibleForTesting
  @Override
  public OsmorcFacetConfiguration createConfiguration(Collection<? extends VirtualFile> files) {
    var facetConfiguration = getFacetType().createDefaultConfiguration();
    facetConfiguration.setManifestGenerationMode(ManifestGenerationMode.Bnd);
    facetConfiguration.setBndFileLocation(files.iterator().next().getPath());
    facetConfiguration.setUseProjectDefaultManifestFileLocation(false);
    return facetConfiguration;
  }

  @Override
  public void setupFacet(@NotNull OsmorcFacet facet, ModifiableRootModel model) {
    var facetConfiguration = facet.getConfiguration();
    var bndFile = LocalFileSystem.getInstance().findFileByPath(facetConfiguration.getBndFileLocation());
    if (bndFile != null) {
      for (var contentRoot : model.getContentRoots()) {
        if (VfsUtilCore.isAncestor(contentRoot, bndFile, false)) {
          facetConfiguration.setBndFileLocation(VfsUtilCore.getRelativePath(bndFile, contentRoot, '/'));
          break;
        }
      }
    }
  }

  @Override
  public @NotNull ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().with(new PatternCondition<>("bnd file") {
      @Override
      public boolean accepts(@NotNull FileContent content, ProcessingContext context) {
        return content.getFileName().equals(BND_FILE_NAME);
      }
    });
  }

  @Override
  public @NotNull FileType getFileType() {
    return BndFileType.INSTANCE;
  }
}
