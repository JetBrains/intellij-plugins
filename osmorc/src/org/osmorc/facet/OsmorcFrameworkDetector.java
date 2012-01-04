package org.osmorc.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osmorc.manifest.ManifestFileTypeFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

  @Override
  public FacetType<OsmorcFacet, OsmorcFacetConfiguration> getFacetType() {
    return OsmorcFacetType.getInstance();
  }

  @Override
  protected OsmorcFacetConfiguration createConfiguration(Collection<VirtualFile> files) {
    OsmorcFacetConfiguration osmorcFacetConfiguration = getFacetType().createDefaultConfiguration();
    osmorcFacetConfiguration.setManifestGenerationMode(OsmorcFacetConfiguration.ManifestGenerationMode.Manually);
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
        if (VfsUtil.isAncestor(contentRoot, manifestFile, false)) {
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
      osmorcFacetConfiguration.setManifestGenerationMode(OsmorcFacetConfiguration.ManifestGenerationMode.Bundlor);
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
    List<String> headersToDetect = new ArrayList<String>(Arrays.asList(DETECTION_HEADERS));

    if (file != null && file.exists() && !file.isDirectory()) {
      BufferedReader bufferedReader = null;
      try {
        InputStream inputStream = file.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        while ((line = bufferedReader.readLine()) != null && headersToDetect.size() > 0) {
          for (Iterator<String> headersToDetectIterator = headersToDetect.iterator();
               headersToDetectIterator.hasNext();) {
            String headerToDetect = headersToDetectIterator.next();
            if (line.startsWith(headerToDetect)) {
              headersToDetectIterator.remove();
              break;
            }
          }
        }
      }
      catch (IOException e) {
        // this should fix   IDEA-17977 (EA reported exception).
        logger.warn("There was an unexpected exception when accessing " + file.getName() + " (" + e.getMessage() + ")");
        return false;
      }
      finally {
        if (bufferedReader != null) {
          try {
            bufferedReader.close();
          }
          catch (IOException e) {
            logger.warn("There was an unexpected exception when closing stream to " + file.getName() + " (" + e.getMessage() + ")");
            return false;
          }
        }
      }
    }

    return headersToDetect.size() == 0;

  }
}
