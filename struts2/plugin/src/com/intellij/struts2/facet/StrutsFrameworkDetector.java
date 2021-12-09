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

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.javaee.web.WebUtilImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsFrameworkDetector extends FacetBasedFrameworkDetector<StrutsFacet, StrutsFacetConfiguration> {

  public StrutsFrameworkDetector() {
    super("struts2");
  }

  @NotNull
  @Override
  public FacetType<StrutsFacet, StrutsFacetConfiguration> getFacetType() {
    return StrutsFacetType.getInstance();
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return XmlFileType.INSTANCE;
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
                                                        final Set<? extends VirtualFile> files) {
    return WebUtilImpl.isWebFacetConfigurationContainingFiles(underlying, files);
  }
}
