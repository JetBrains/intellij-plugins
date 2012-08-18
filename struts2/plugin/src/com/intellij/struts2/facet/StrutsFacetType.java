/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.j2ee.web.WebUtilImpl;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

/**
 * Description-type for {@link StrutsFacet}.
 * Adds autodetection feature for struts.xml files found in project.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFacetType extends FacetType<StrutsFacet, StrutsFacetConfiguration> {
  StrutsFacetType() {
    super(StrutsFacet.FACET_TYPE_ID, "Struts2", "Struts 2", WebFacet.ID);
  }

  public static FacetType<StrutsFacet, StrutsFacetConfiguration> getInstance() {
    return findInstance(StrutsFacetType.class);
  }

  public StrutsFacetConfiguration createDefaultConfiguration() {
    return new StrutsFacetConfiguration();
  }

  public StrutsFacet createFacet(@NotNull final Module module,
                                 final String name,
                                 @NotNull final StrutsFacetConfiguration configuration,
                                 @Nullable final Facet underlyingFacet) {
    return new StrutsFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  public Icon getIcon() {
    return StrutsIcons.ACTION;
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts2.facet";
  }


  public static class StrutsFrameworkDetector
      extends FacetBasedFrameworkDetector<StrutsFacet, StrutsFacetConfiguration> {
    public StrutsFrameworkDetector() {
      super("struts2");
    }

    @Override
    public FacetType<StrutsFacet, StrutsFacetConfiguration> getFacetType() {
      return getInstance();
    }

    @NotNull
    @Override
    public FileType getFileType() {
      return StdFileTypes.XML;
    }

    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern() {
      return FileContentPattern.fileContent()
                               .withName(StrutsConstants.STRUTS_XML_DEFAULT_FILENAME)
                               .xmlWithRootTag(StrutsRoot.TAG_NAME);
    }

    @Override
    public boolean isSuitableUnderlyingFacetConfiguration(final FacetConfiguration underlying,
                                                          final StrutsFacetConfiguration configuration,
                                                          final Set<VirtualFile> files) {
      return WebUtilImpl.isWebFacetConfigurationContainingFiles(underlying, files);
    }
  }

}