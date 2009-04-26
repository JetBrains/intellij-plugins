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
 *
 */

package com.intellij.struts2.facet.ui;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Tests for {@link com.intellij.struts2.facet.ui.StrutsConfigsSearcher}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConfigsSearcherTest extends BasicHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[0];
  }

  @NotNull
  protected String getTestDataLocation() {
    return "configsSearcher";
  }


  private final FacetEditorContext myFacetEditorContext = new FacetEditorContext() {

    @Nullable
    public Project getProject() {
      return myProject;
    }

    @Nullable
    public Library findLibrary(@NotNull final String name) {
      return null;
    }

    @Nullable
    public ModuleBuilder getModuleBuilder() {
      return null;
    }

    @Nullable
    public Facet getFacet() {
      return null;
    }

    @Nullable
    public Facet getParentFacet() {
      return null;
    }

    @NotNull
    public FacetsProvider getFacetsProvider() {
      throw new UnsupportedOperationException("'getFacetsProvider' not implemented in " + getClass().getName());
    }

    @NotNull
    public ModulesProvider getModulesProvider() {
      throw new UnsupportedOperationException("'getModulesProvider' not implemented in " + getClass().getName());
    }

    @Nullable
    public ModifiableRootModel getModifiableRootModel() {
      return null;
    }

    @Nullable
    public ModuleRootModel getRootModel() {
      return null;
    }

    public boolean isNewFacet() {
      return false;
    }

    @Nullable
    public Module getModule() {
      return myModule;
    }

    public Library[] getLibraries() {
      return new Library[0];
    }

    @Nullable
    public WizardContext getWizardContext() {
      return null;
    }

    public Library createProjectLibrary(final String name, final VirtualFile[] roots, final VirtualFile[] sources) {
      throw new UnsupportedOperationException("'createProjectLibrary' not implemented in " + getClass().getName());
    }

    public VirtualFile[] getLibraryFiles(final Library library, final OrderRootType rootType) {
      return VirtualFile.EMPTY_ARRAY;
    }

    @NotNull
    public String getFacetName() {
      return "";
    }

    public <T> T getUserData(final Key<T> key) {
      return null;
    }

    public <T> void putUserData(final Key<T> key, final T value) {
    }
  };

  public void testSearch() throws Exception {
    final StrutsConfigsSearcher configsSearcher = new StrutsConfigsSearcher(myFacetEditorContext);
    configsSearcher.search();

    final Map<Module, List<PsiFile>> map = configsSearcher.getFilesByModules();
    assertEquals(1, map.size());
    assertEquals(1, map.get(myModule).size()); // /src/struts.xml

    final Map<VirtualFile, List<PsiFile>> configsInJars = configsSearcher.getJars();
    assertEquals(1, configsInJars.size()); // default-xxx.xml in struts2-core.jar
  }

}