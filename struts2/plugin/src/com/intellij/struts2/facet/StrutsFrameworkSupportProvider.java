/*
 * Copyright 2008 The authors
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

import com.intellij.facet.impl.ui.FacetTypeFrameworkSupportProvider;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.struts2.facet.ui.StrutsVersion;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * "Add Framework" support.
 *
 * @author Yann CŽbron
 */
public class StrutsFrameworkSupportProvider extends FacetTypeFrameworkSupportProvider<StrutsFacet> {

  private static final Logger LOG = Logger.getInstance("#com.intellij.struts2.facet.StrutsFrameworkSupportProvider");

  protected StrutsFrameworkSupportProvider() {
    super(StrutsFacetType.INSTANCE);
  }

  @NotNull
  protected String getLibraryName(final String version) {
    return "struts2-" + version;
  }

  public String getTitle() {
    return UIUtil.replaceMnemonicAmpersand("Struts &2");
  }

  @NotNull
  public String[] getVersions() {
    final List<String> versions = new ArrayList<String>();
    for (final StrutsVersion version : StrutsVersion.values()) {
      versions.add(version.toString());
    }
    return versions.toArray(new String[versions.size()]);
  }

  @NotNull
  private static StrutsVersion getVersion(final String versionName) {
    for (final StrutsVersion version : StrutsVersion.values()) {
      if (versionName.equals(version.toString())) {
        return version;
      }
    }

    throw new IllegalArgumentException("Invalid S2 version '" + versionName + "'");
  }

  @NotNull
  protected LibraryInfo[] getLibraries(final String selectedVersion) {
    final StrutsVersion version = getVersion(selectedVersion);
    return version.getLibraryInfos();
  }

  protected void onLibraryAdded(final StrutsFacet facet, @NotNull final Library library) {
    facet.getWebFacet().getPackagingConfiguration().addLibraryLink(library);
  }

  protected void setupConfiguration(final StrutsFacet strutsFacet,
                                    final ModifiableRootModel modifiableRootModel, final String version) {
    final Module module = strutsFacet.getModule();
    StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        if (sourceRoots.length > 0) {
          final PsiDirectory directory = PsiManager.getInstance(module.getProject()).findDirectory(sourceRoots[0]);
          if (directory != null &&
              directory.findFile(StrutsConstants.STRUTS_DEFAULT_FILENAME) == null) {
            final FileTemplate strutsXmlTemplate = FileTemplateManager.getInstance().getJ2eeTemplate("struts.xml");
            try {
              final PsiElement psiElement = FileTemplateUtil.createFromTemplate(strutsXmlTemplate,
                                                                                StrutsConstants.STRUTS_DEFAULT_FILENAME,
                                                                                null,
                                                                                directory);
              if (psiElement instanceof XmlFile) {
                final Set<StrutsFileSet> empty = Collections.emptySet();
                final StrutsFileSet fileSet = new StrutsFileSet(StrutsFileSet.getUniqueId(empty),
                                                                StrutsFileSet.getUniqueName("Default File Set", empty));
                fileSet.addFile(((XmlFile) psiElement).getVirtualFile());
                strutsFacet.getConfiguration().getFileSets().add(fileSet);
              }
            } catch (Exception e) {
              LOG.error("error creating struts.xml from template", e);
            }
          }
        }
      }
    });
  }

}