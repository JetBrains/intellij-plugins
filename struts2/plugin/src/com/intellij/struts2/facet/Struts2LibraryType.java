/*
 * Copyright 2012 The authors
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

import com.intellij.facet.frameworks.LibrariesDownloadAssistant;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.framework.library.DownloadableLibraryTypeBase;
import com.intellij.struts2.StrutsIcons;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.DownloadableFileSetDescription;
import com.intellij.util.download.DownloadableFileSetVersions;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Support for Struts 2 library setup in project settings.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2LibraryType extends DownloadableLibraryTypeBase {

  public static final String STRUTS_VERSION_CLASS = "org.apache.struts2.StrutsConstants";

  private static final String GROUP_ID = "struts2";

  public Struts2LibraryType() {
    super("Struts 2", "struts2", GROUP_ID, StrutsIcons.ACTION, getLibrariesUrl());
  }

  @Override
  public String[] getDetectionClassNames() {
    return new String[]{STRUTS_VERSION_CLASS};
  }

  public static DownloadableFileSetVersions<DownloadableFileSetDescription> getVersions() {
    return DownloadableFileService.getInstance().createFileSetVersions(null, getLibrariesUrl());
  }

  public static LibraryInfo[] getLibraryInfo(@NotNull final DownloadableFileSetDescription version) {
    return LibrariesDownloadAssistant.getLibraryInfos(getLibrariesUrl(), version.getVersionString());
  }

  private static URL getLibrariesUrl() {
    return Struts2LibraryType.class.getResource("struts2.xml");
  }

}