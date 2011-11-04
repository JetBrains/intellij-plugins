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

package com.intellij.struts2;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.facet.FacetManager;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetType;
import com.intellij.openapi.application.Result;
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

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Base class for highlighting tests.
 *
 * @author Yann C&eacute;bron
 */
public abstract class BasicHighlightingTestCase<T extends JavaModuleFixtureBuilder> extends BasicStrutsTestCase {

  protected JavaCodeInsightTestFixture myFixture;
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
  protected static final String STRUTS2_VERSION = "2.2.1";

  @NonNls
  protected static final String STRUTS2_SPRING_PLUGIN_JAR = "struts2-spring-plugin-" + STRUTS2_VERSION + ".jar";

  @NonNls
  protected static final String STRUTS2_TILES_PLUGIN_JAR = "struts2-tiles-plugin-" + STRUTS2_VERSION + ".jar";

  /**
   * If present, Struts libraries are <em>not</em> added to classpath automatically.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  public @interface SkipStrutsLibrary {
  }

  /**
   * If present, Java source path is added to module.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  public @interface HasJavaSources {
  }

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
    return LocalInspectionTool.EMPTY_ARRAY;
  }

  /**
   * Return true if test uses JAVA sources.
   *
   * @return {@code true} if test annotated with {@link HasJavaSources}.
   */
  protected boolean hasJavaSources() {
    return annotatedWith(HasJavaSources.class);
  }

  /**
   * Returns true if test uses Struts JARs.
   *
   * @return true, false if test annotated with {@link HasJavaSources}.
   */
  protected boolean usesStrutsLibrary() {
    return !annotatedWith(SkipStrutsLibrary.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // little hack..
    testDataRootPath = new File(getTestDataBasePath()).getAbsolutePath();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = JavaTestFixtureFactory.createFixtureBuilder();
    final T moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());
    myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());

    myFixture.setTestDataPath(getTestDataPath());

    configureModule(moduleBuilder);
    myFixture.enableInspections(getHighlightingInspections());

    myFixture.setUp();

    myProject = myFixture.getProject();
    myModuleTestFixture = moduleBuilder.getFixture();
    myModule = myModuleTestFixture.getModule();
    myFacet = createFacet(myModule);
  }

  @Override
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

    if (hasJavaSources()) {
      moduleBuilder.addContentRoot(myFixture.getTestDataPath());
      moduleBuilder.addSourceRoot(SOURCE_DIR);
    }

    if (usesStrutsLibrary()) {
      addStrutsJars(moduleBuilder);
    }
  }

  // TODO stupid hack needed for some tests
  protected final void installSrcHack() {
    final String path = myFixture.getTempDirPath();
    new File(path + SOURCE_PATH).mkdir();
  }

  /**
   * Adds the S2 jars.
   *
   * @param moduleBuilder Current module builder.
   * @throws Exception On internal errors.
   */
  protected final void addStrutsJars(final T moduleBuilder) throws Exception {
    addLibrary(moduleBuilder, "struts2",
               "struts2-core-" + STRUTS2_VERSION + ".jar",
               "freemarker-2.3.16.jar",
               "ognl-3.0.jar",
               "xwork-core-2.2.1.jar");
  }

  protected void addLibrary(final T moduleBuilder, @NonNls final String libraryName, @NonNls final String... jarPaths) {
    moduleBuilder.addLibraryJars(libraryName, testDataRootPath, jarPaths);
  }

  public static StrutsFacet createFacet(final Module module) {
    return new WriteCommandAction<StrutsFacet>(module.getProject()) {
      @Override
      protected void run(final Result<StrutsFacet> result) throws Throwable {
        final FacetManager facetManager = FacetManager.getInstance(module);
        final WebFacet webFacet = facetManager.addFacet(WebFacetType.getInstance(), "web", null);
        final StrutsFacet strutsFacet = facetManager.addFacet(StrutsFacetType.getInstance(), "struts2", webFacet);
        result.setResult(strutsFacet);
      }
    }.execute().throwException().getResultObject();
  }

  /**
   * For files located in JAR: {@code [PATH_TO_JAR]!/[PATH_TO_STRUTS_XML]}.
   *
   * @param strutsXmlPaths Paths to files.
   */
  protected void createStrutsFileSet(@NonNls final String... strutsXmlPaths) {
    final StrutsFacetConfiguration facetConfiguration = myFacet.getConfiguration();

    final StrutsFileSet fileSet = new StrutsFileSet("test", "test", facetConfiguration);
    for (final String fileName : strutsXmlPaths) {
      final VirtualFile file;
      if (fileName.contains("!")) {
        file = JarFileSystem.getInstance().findFileByPath(testDataRootPath + "/" + fileName);
      } else {
        file = myFixture.copyFileToProject(fileName);
      }
      assertNotNull("could not find file: '" + fileName + "'", file);
      fileSet.addFile(file);
    }
    final Set<StrutsFileSet> strutsFileSetSet = facetConfiguration.getFileSets();
    strutsFileSetSet.add(fileSet);
  }

}