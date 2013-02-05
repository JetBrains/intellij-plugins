/*
 * Copyright 2013 The authors
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

package com.intellij.struts2;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.util.xml.impl.DomApplicationComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

/**
 * Base class for highlighting tests.
 *
 * @author Yann C&eacute;bron
 */
public abstract class BasicLightHighlightingTestCase extends LightCodeInsightFixtureTestCase {

  private static final String TEST_DATA_PATH = "/contrib/struts2/plugin/testData/";

  private static final LightProjectDescriptor STRUTS =
    new Struts2ProjectDescriptorBuilder().withStrutsLibrary().withStrutsFacet().build();

  protected static final String STRUTS_XML = "struts.xml";

  @NonNls
  protected static final String STRUTS2_VERSION = "2.3.1";

  /**
   * Inspections to run for highlighting tests.
   *
   * @return Inspection tools, default = none.
   */
  protected LocalInspectionTool[] getHighlightingInspections() {
    return LocalInspectionTool.EMPTY_ARRAY;
  }

  @Override
  protected final String getBasePath() {
    return TEST_DATA_PATH + getTestDataLocation();
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return STRUTS;
  }

  /**
   * Sets the relative directory for test data to use.
   *
   * @return relative directory location.
   */
  @NonNls
  @NotNull
  protected abstract String getTestDataLocation();

  @Override
  protected final void setUp() throws Exception {
    super.setUp();

    myFixture.enableInspections(getHighlightingInspections());

    if (!getTestDataLocation().isEmpty()) {
      final File srcDir = new File(getTestDataPath() + "/src");
      if (srcDir.exists()) {
        myFixture.copyDirectoryToProject("src", "");
      }
    }

    myFixture.allowTreeAccessForAllFiles();

    performSetUp();
  }

  /**
   * Perform custom setup.
   *
   * @throws Exception On errors.
   */
  protected void performSetUp() throws Exception {
  }

  @Override
  protected final void tearDown() throws Exception {
    // clear existing StrutsFacet configuration before running next test
    final StrutsFacet existing = StrutsFacet.getInstance(myModule);
    if (existing != null) {
      existing.getConfiguration().getFileSets().clear();
    }

    performTearDown();

    super.tearDown();
  }

  /**
   * Perform custom tear down.
   *
   * @throws Exception On errors.
   */
  protected void performTearDown() throws Exception {
  }

  private static final String LIBRARY_PATH =
    FileUtil.toSystemIndependentName(PathManager.getHomePath() + TEST_DATA_PATH + "/lib/");

  /**
   * Adds the S2 jars.
   *
   * @param moduleBuilder Current module builder.
   * @throws Exception On internal errors.
   */
  static void addStrutsJars(Module module, ModifiableRootModel model) {
    addLibrary(module, model, "struts2",
               "struts2-core-" + STRUTS2_VERSION + ".jar",
               "freemarker-2.3.18.jar",
               "ognl-3.0.3.jar",
               "xwork-core-2.3.1.jar");
  }

  static void addLibrary(Module module, ModifiableRootModel model,
                         @NonNls final String libraryName, @NonNls final String... jarPaths) {
    PsiTestUtil.addLibrary(module, model, libraryName, LIBRARY_PATH, jarPaths);
  }

  /**
   * For files located in JAR: {@code [PATH_TO_JAR]!/[PATH_TO_STRUTS_XML]}.
   *
   * @param strutsXmlPaths Paths to files.
   */
  protected void createStrutsFileSet(@NonNls final String... strutsXmlPaths) {
    final StrutsFacet strutsFacet = StrutsFacet.getInstance(myModule);
    assertNotNull(strutsFacet);
    final StrutsFacetConfiguration facetConfiguration = strutsFacet.getConfiguration();

    final StrutsFileSet fileSet = new StrutsFileSet("test", "test", facetConfiguration);
    for (final String fileName : strutsXmlPaths) {
      final VirtualFile file;
      final String path;
      if (fileName.contains("!")) {
        path = PathManager.getHomePath() + TEST_DATA_PATH + "/" + fileName;
        file = JarFileSystem.getInstance().refreshAndFindFileByPath(path);
      }
      else {
        path = fileName;
        file = myFixture.copyFileToProject(fileName);
      }

      assertNotNull("could not find file: '" + path + "'", file);
      fileSet.addFile(file);
    }
    final Set<StrutsFileSet> strutsFileSetSet = facetConfiguration.getFileSets();
    strutsFileSetSet.add(fileSet);
  }
}