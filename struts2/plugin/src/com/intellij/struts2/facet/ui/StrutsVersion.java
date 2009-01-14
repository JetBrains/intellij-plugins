/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.facet.ui;

import com.intellij.facet.ui.libraries.LibraryInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createMavenJarInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createSubMavenJarInfo;

/**
 * Available Struts2-library versions and their required dependencies.
 *
 * @author Yann C&eacute;bron
 * @see com.intellij.struts2.facet.ui.FeaturesConfigurationTab
 */
public enum StrutsVersion {

  STRUTS_2_0_14("2.0.14", new LibraryInfo[]{
          createSubMavenJarInfo("/org/apache/struts", "struts2-core", "2.0.14", "org.apache.struts2.StrutsConstants"),
          createMavenJarInfo("commons-logging", "1.0.4", "org.apache.commons.logging.Log"),
          createMavenJarInfo("freemarker", "2.3.8", "freemarker.core.TemplateElement"),
          createSubMavenJarInfo("/com/opensymphony/", "xwork", "2.0.7", "com.opensymphony.xwork2.XWorkException"),
          createSubMavenJarInfo("/opensymphony/", "ognl", "2.6.11", "ognl.Ognl")
  }),

  STRUTS_2_1_6("2.1.6", new LibraryInfo[]{
          createSubMavenJarInfo("/org/apache/struts", "struts2-core", "2.1.6", "org.apache.struts2.StrutsConstants"),
          createMavenJarInfo("commons-fileupload", "1.2.1"),
          createMavenJarInfo("commons-io",  "1.3.2"),
          createMavenJarInfo("commons-logging", "1.1", "org.apache.commons.logging.Log"),
          createSubMavenJarInfo("/org/freemarker", "freemarker", "2.3.13", "freemarker.core.TemplateElement"),
          createSubMavenJarInfo("/com/opensymphony/", "xwork", "2.1.2", "com.opensymphony.xwork2.XWorkException"),
          createSubMavenJarInfo("/opensymphony/", "ognl", "2.6.11", "ognl.Ognl")
  });

  private final String version;
  private final LibraryInfo[] libraryInfos;

  StrutsVersion(final String version, final LibraryInfo... libraryInfos) {
    this.version = version;
    this.libraryInfos = libraryInfos;
  }

  public String getVersion() {
    return version;
  }

  public LibraryInfo[] getLibraryInfos() {
    return libraryInfos;
  }

  public String toString() {
    return version;
  }

}