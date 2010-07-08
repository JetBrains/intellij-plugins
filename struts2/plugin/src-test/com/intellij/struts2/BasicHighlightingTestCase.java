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

package com.intellij.struts2;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.facet.FacetManager;
import com.intellij.javaee.JavaeeUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetType;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.struts2.facet.StrutsFacetType;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

/**
 * Base class for highlighting tests.
 *
 * @author Yann C&eacute;bron
 */
public abstract class BasicHighlightingTestCase<T extends JavaModuleFixtureBuilder> extends BasicStrutsTestCase {

  protected CodeInsightTestFixture myFixture;
  protected ModuleFixture myModuleTestFixture;
  protected Project myProject;
  protected Module myModule;
  protected StrutsFacet myFacet;

  /**
   * Absolute path to /testData.
   */
  protected String testDataRootPath;

  @NonNls
  protected static final String SOURCE_DIR = "src";

  @NonNls
  protected static final String SOURCE_PATH = "/" + SOURCE_DIR;

  @NonNls
  protected static final String STRUTS_XML = "struts.xml";

  @NonNls
  private static final String STRUTS2_CORE_JAR_FILENAME = "struts2-core-2.1.8.jar";

  @NonNls
  protected static final String STRUTS2_SPRING_PLUGIN_JAR = "struts2-spring-plugin-2.1.8.jar";

  protected Class<T> getModuleFixtureBuilderClass() {
    //noinspection unchecked
    return (Class<T>) JavaModuleFixtureBuilder.class;
  }

  /**
   * Inspections to run for highlighting tests.
   *
   * @return Inspection tools, default = none.
   */
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[0];
  };

  /**
   * Return true if test uses JAVA sources.
   *
   * @return false.
   */
  protected boolean hasJavaSources() {
    return false;
  }

  protected void setUp() throws Exception {
    super.setUp();

    // little hack..
    testDataRootPath = new File(getTestDataBasePath()).getAbsolutePath();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder =
        JavaTestFixtureFactory.createFixtureBuilder();
    final T moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());
    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());

    myFixture.setTestDataPath(getTestDataPath());

    configureModule(moduleBuilder);
    myFixture.enableInspections(getHighlightingInspections());

    myFixture.setUp();

    myProject = myFixture.getProject();
    myModuleTestFixture = moduleBuilder.getFixture();
    myModule = myModuleTestFixture.getModule();
    myFacet = createFacet();
  }

  protected void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
    myModuleTestFixture = null;
    myProject = null;
    myModule = null;
    myFacet = null;
    super.tearDown();
  }

  protected void configureModule(final T moduleBuilder) throws Exception {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath());
    moduleBuilder.addContentRoot(getTestDataPath());

    if (hasJavaSources()) {
      final String path = myFixture.getTempDirPath();
      new File(path + SOURCE_PATH).mkdir(); // ?? necessary

      moduleBuilder.addContentRoot(getTestDataPath() + SOURCE_PATH);
      moduleBuilder.addSourceRoot(SOURCE_DIR);
    }
    addStrutsJars(moduleBuilder);
  }

  /**
   * Adds the S2 jars.
   *
   * @param moduleBuilder Current module builder.
   * @throws Exception On internal errors.
   */
  protected final void addStrutsJars(final T moduleBuilder) throws Exception {
    addLibrary(moduleBuilder, "struts2",
               STRUTS2_CORE_JAR_FILENAME,
               "freemarker-2.3.15.jar",
               "ognl-2.7.3.jar",
               "xwork-core-2.1.6.jar");
  }

  protected void addLibrary(final T moduleBuilder, @NonNls final String libraryName, @NonNls final String... jarPaths) {
    moduleBuilder.addLibraryJars(libraryName, testDataRootPath, jarPaths);
  }

  protected final StrutsFacet createFacet() {
    final RunResult<StrutsFacet> runResult = new WriteCommandAction<StrutsFacet>(myProject) {
      protected void run(final Result<StrutsFacet> result) throws Throwable {
        final String name = StrutsFacetType.INSTANCE.getPresentableName();
        final WebFacet webFacet = JavaeeUtil.addFacet(myModule, WebFacetType.INSTANCE);
        final StrutsFacet facet = FacetManager.getInstance(myModule).addFacet(StrutsFacetType.INSTANCE, name, webFacet);
        result.setResult(facet);
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException("error setting up StrutsFacet", throwable);
    }

    return runResult.getResultObject();
  }

  private void addToFileSet(final StrutsFileSet fileSet, @NonNls final String path) {
    final VirtualFile file = myFixture.copyFileToProject(path);
    assertNotNull("could not find file: '" + path + "'", file);
    fileSet.addFile(file);
  }

  protected void createStrutsFileSet(@NonNls final String... fileNames) {
    final StrutsFacetConfiguration facetConfiguration = myFacet.getConfiguration();

    final StrutsFileSet fileSet = new StrutsFileSet("test", "test", facetConfiguration);
    for (final String fileName : fileNames) {
      addToFileSet(fileSet, fileName);
    }
    final Set<StrutsFileSet> strutsFileSetSet = facetConfiguration.getFileSets();
    strutsFileSetSet.clear();
    strutsFileSetSet.add(fileSet);
  }

  /**
   * Adds {@code struts.xml} files located in JARs.
   * <p/>
   * Must be called <em>after</em> {@link #createStrutsFileSet(String...)}.
   *
   * @param jarPath Path to struts.xml contained in JAR ({@code [PATH_TO_JAR]!/[PATH_TO_STRUTS_XML]}.
   */
  protected void addStrutsXmlFromJar(@NotNull @NonNls final String jarPath) {
    final StrutsFacetConfiguration facetConfiguration = myFacet.getConfiguration();
    final Set<StrutsFileSet> fileSets = facetConfiguration.getFileSets();
    assert !fileSets.isEmpty() : "call createStrutsFileSet() before";
    final StrutsFileSet fileSet = fileSets.iterator().next();

    final VirtualFile virtualFile = JarFileSystem.getInstance().findFileByPath(testDataRootPath + "/" + jarPath);
    assert virtualFile != null : "could not find '" + jarPath + "'";
    fileSet.addFile(virtualFile);
  }

}