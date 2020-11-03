/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osmorc.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.BndFileType;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;

import java.util.Collection;

/**
 * Framework detector for bnd files. File needs to be named bnd.bnd
 *
 * @author <a href="mailto:tibor@malanik.eu">Tibor Malanik</a>
 */
public class BndOsmorcFrameworkDetector extends FacetBasedFrameworkDetector<OsmorcFacet, OsmorcFacetConfiguration> {

  private static final String BND_FILE_NAME = "bnd.bnd";
  private final Logger logger = Logger.getInstance("#org.osmorc.facet.BndOsmorcFrameworkDetector");

  public BndOsmorcFrameworkDetector() {
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
    osmorcFacetConfiguration.setManifestGenerationMode(ManifestGenerationMode.Bnd);
    osmorcFacetConfiguration.setBndFileLocation(BND_FILE_NAME);
    return osmorcFacetConfiguration;
  }

  @NotNull
  @Override
  public ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().with(new PatternCondition<FileContent>("bnd file") {
      @Override
      public boolean accepts(@NotNull FileContent content, ProcessingContext context) {
        return content.getFileName().equals(BND_FILE_NAME);
      }
    });
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return BndFileType.INSTANCE;
  }

}
