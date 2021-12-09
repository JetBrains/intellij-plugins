/*
 * Copyright 2017 The authors
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

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.MavenDependencyUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base class for highlighting tests.
 *
 * @author Yann C&eacute;bron
 */
public abstract class BasicLightHighlightingTestCase extends LightJavaCodeInsightFixtureTestCase {

  @NonNls
  public static final String TEST_DATA_PATH = "/contrib/struts2/plugin/testData/";

  private static final LightProjectDescriptor STRUTS =
    new Struts2ProjectDescriptorBuilder().withStrutsLibrary().withStrutsFacet().build();

  protected static final LightProjectDescriptor WEB =
    new Struts2ProjectDescriptorBuilder().withStrutsLibrary().withStrutsFacet().withWebModuleType().build();

  protected static final String STRUTS_XML = "struts.xml";

  @NonNls
  protected static final String STRUTS2_VERSION = "2.3.1";

  /**
   * Inspections to run for highlighting tests.
   *
   * @return Inspection tools, default = none.
   */
  protected InspectionProfileEntry[] getHighlightingInspections() {
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
    myFixture.allowTreeAccessForAllFiles();

    performSetUp();
  }

  /**
   * Perform custom setup.
   */
  protected void performSetUp() {
  }

  private final List<StrutsFileSet> myStrutsFileSets = new ArrayList<>();
  @Override
  protected final void tearDown() throws Exception {
    for (StrutsFileSet set : myStrutsFileSets) {
      Disposer.dispose(set);
    }
    myStrutsFileSets.clear();
    try {
      performTearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      // clear existing StrutsFacet configuration before running next test
      final StrutsFacet existing = StrutsFacet.getInstance(getModule());
      if (existing != null) {
        existing.getConfiguration().getFileSets().clear();
      }
      super.tearDown();
    }
  }

  /**
   * Perform custom tear down.
   */
  protected void performTearDown() {
  }

  static void addStrutsJars(ModifiableRootModel model) {
    MavenDependencyUtil.addFromMaven(model, "org.apache.struts:struts2-core:2.3.1");
    MavenDependencyUtil.addFromMaven(model, "org.apache.struts.xwork:xwork-core:2.3.1");
    MavenDependencyUtil.addFromMaven(model, "org.freemarker:freemarker:2.3.18");
    MavenDependencyUtil.addFromMaven(model, "ognl:ognl:3.0.3");
  }

  /**
   * @param strutsXmlPaths Paths to files or URL inside JAR from VFS
   */
  protected void createStrutsFileSet(@NonNls String... strutsXmlPaths) {
    final StrutsFacet strutsFacet = StrutsFacet.getInstance(getModule());
    assertNotNull(strutsFacet);
    final StrutsFacetConfiguration facetConfiguration = strutsFacet.getConfiguration();

    final StrutsFileSet fileSet = new StrutsFileSet("test", "test", facetConfiguration);
    myStrutsFileSets.add(fileSet);
    for (String fileName : strutsXmlPaths) {
      VirtualFile file;
      if (fileName.contains("!")) {
        file = VirtualFileManager.getInstance().findFileByUrl(fileName);
      }
      else {
        try {
          file = myFixture.copyFileToProject(fileName);
        } catch (UncheckedIOException e) {
          throw new RuntimeException("Cannot process " + fileName, e);
        }
      }

      assertNotNull("could not find file: '" + fileName + "'", file);
      fileSet.addFile(file);
    }
    final Set<StrutsFileSet> strutsFileSetSet = facetConfiguration.getFileSets();
    strutsFileSetSet.add(fileSet);
  }
}