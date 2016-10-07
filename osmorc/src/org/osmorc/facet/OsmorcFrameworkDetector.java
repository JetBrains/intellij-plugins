package org.osmorc.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.diagnostic.Logger;
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
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.lang.manifest.ManifestFileTypeFactory;
import org.osgi.framework.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author nik
 */
public class OsmorcFrameworkDetector extends FacetBasedFrameworkDetector<OsmorcFacet, OsmorcFacetConfiguration> {
  private final Logger logger = Logger.getInstance("#org.osmorc.facet.OsmorcFrameworkDetector");
  private final String[] DETECTION_HEADERS = {Constants.BUNDLE_SYMBOLICNAME};

  public OsmorcFrameworkDetector() {
    super("osmorc");
  }

  @NotNull
  @Override
  public FacetType<OsmorcFacet, OsmorcFacetConfiguration> getFacetType() {
    return OsmorcFacetType.getInstance();
  }

  @Override
  protected OsmorcFacetConfiguration createConfiguration(Collection<VirtualFile> files) {
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

  @NotNull
  @Override
  public ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().with(new PatternCondition<FileContent>("osmorc manifest file") {
      @Override
      public boolean accepts(@NotNull FileContent content, ProcessingContext context) {
        return isSuitableFile(content.getFile());
      }
    });
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return ManifestFileTypeFactory.MANIFEST;
  }

  private boolean isSuitableFile(VirtualFile file) {
    List<String> headersToDetect = new ArrayList<>(Arrays.asList(DETECTION_HEADERS));

    if (file != null && file.exists() && !file.isDirectory()) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        try {
          String line;
          while ((line = reader.readLine()) != null && headersToDetect.size() > 0) {
            for (Iterator<String> iterator = headersToDetect.iterator(); iterator.hasNext(); ) {
              String headerToDetect = iterator.next();
              if (line.startsWith(headerToDetect)) {
                iterator.remove();
                break;
              }
            }
          }
        }
        finally {
          reader.close();
        }
      }
      catch (IOException e) {
        logger.warn("There was an unexpected exception when accessing " + file.getName() + " (" + e.getMessage() + ")");
        return false;
      }
    }

    return headersToDetect.size() == 0;
  }
}
