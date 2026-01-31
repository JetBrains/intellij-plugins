// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.jetbrains.lang.manifest.ManifestFileType;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osgi.framework.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public final class OsmorcFrameworkDetector extends FacetBasedFrameworkDetector<OsmorcFacet, OsmorcFacetConfiguration> {
  private final String[] DETECTION_HEADERS = {Constants.BUNDLE_SYMBOLICNAME};

  public OsmorcFrameworkDetector() {
    super("osmorc");
  }

  @Override
  public @NotNull FacetType<OsmorcFacet, OsmorcFacetConfiguration> getFacetType() {
    return OsmorcFacetType.getInstance();
  }

  @VisibleForTesting
  @Override
  public OsmorcFacetConfiguration createConfiguration(Collection<? extends VirtualFile> files) {
    OsmorcFacetConfiguration osmorcFacetConfiguration = getFacetType().createDefaultConfiguration();
    osmorcFacetConfiguration.setManifestGenerationMode(ManifestGenerationMode.Manually);
    osmorcFacetConfiguration.setManifestLocation(ContainerUtil.getFirstItem(files).getPath());
    osmorcFacetConfiguration.setUseProjectDefaultManifestFileLocation(false);
    return osmorcFacetConfiguration;
  }

  @Override
  public void setupFacet(@NotNull OsmorcFacet facet, ModifiableRootModel model) {
    VirtualFile[] contentRoots = model.getContentRoots();
    OsmorcFacetConfiguration osmorcFacetConfiguration = facet.getConfiguration();
    VirtualFile manifestFile = LocalFileSystem.getInstance().findFileByPath(osmorcFacetConfiguration.getManifestLocation());
    if (manifestFile != null) {
      for (VirtualFile contentRoot : contentRoots) {
        if (VfsUtilCore.isAncestor(contentRoot, manifestFile, false)) {
          // IDEADEV-40357
          osmorcFacetConfiguration.setManifestLocation(VfsUtilCore.getRelativePath(manifestFile, contentRoot, '/'));
          break;
        }
      }
    }
    else {
      osmorcFacetConfiguration.setManifestLocation("");
      osmorcFacetConfiguration.setUseProjectDefaultManifestFileLocation(true);
    }
    String manifestFileName = osmorcFacetConfiguration.getManifestLocation();
    if (manifestFileName.endsWith("template.mf")) { // this is a bundlor manifest template, so make the facet do bundlor
      osmorcFacetConfiguration.setManifestLocation("");
      osmorcFacetConfiguration.setBundlorFileLocation(manifestFileName);
      osmorcFacetConfiguration.setManifestGenerationMode(ManifestGenerationMode.Bundlor);
    }
  }

  @Override
  public @NotNull ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().with(new PatternCondition<>("osmorc manifest file") {
      @Override
      public boolean accepts(@NotNull FileContent content, ProcessingContext context) {
        return isSuitableFile(content.getContentAsText());
      }
    });
  }

  @Override
  public @NotNull FileType getFileType() {
    return ManifestFileType.INSTANCE;
  }

  private boolean isSuitableFile(CharSequence fileContent) {
    List<String> headersToDetect = new ArrayList<>(Arrays.asList(DETECTION_HEADERS));
    StringTokenizer linesTokenizer = new StringTokenizer(fileContent.toString(), "\r\n");
    
    while (linesTokenizer.hasMoreTokens()) {
      String line = linesTokenizer.nextToken();
      for (Iterator<String> iterator = headersToDetect.iterator(); iterator.hasNext(); ) {
        String headerToDetect = iterator.next();
        if (line.startsWith(headerToDetect)) {
          iterator.remove();
          break;
        }
      }
    }

    return headersToDetect.isEmpty();
  }
}
