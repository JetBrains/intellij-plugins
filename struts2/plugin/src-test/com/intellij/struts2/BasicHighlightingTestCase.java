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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.StrutsFacetType;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
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

  protected Class<T> getModuleFixtureBuilderClass() {
    //noinspection unchecked
    return (Class<T>) JavaModuleFixtureBuilder.class;
  }

  /**
   * Inspections to run for highlighting tests.
   *
   * @return Inspection tools.
   */
  protected abstract LocalInspectionTool[] getHighlightingInspections();

  /**
   * Return true if the test uses JAVA sources.
   *
   * @return false.
   */
  protected boolean hasJavaSources() {
    return false;
  }

  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder =
        IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();
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

  protected void configureModule(final T moduleBuilder) throws Exception {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath());
    moduleBuilder.addContentRoot(getTestDataPath());

    if (hasJavaSources()) {
      final String path = myFixture.getTempDirPath();
      new File(path + "/src").mkdir(); // ?? necessary

      moduleBuilder.addContentRoot(getTestDataPath() + "/src");
      moduleBuilder.addSourceRoot("src");
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
               "struts2-core-2.1.6.jar",
               "freemarker-2.3.13.jar",
               "ognl-2.6.11.jar",
               "xwork-2.1.2.jar");
  }

  protected void addLibrary(final T moduleBuilder, @NonNls final String libraryName, @NonNls final String... jarPaths) {
    final File testDataBasePathFile = new File(getTestDataBasePath()); // little hack to get absolute path..
    moduleBuilder.addLibraryJars(libraryName,
                                 testDataBasePathFile.getAbsolutePath(),
                                 jarPaths);
  }

  protected final StrutsFacet createFacet() {
    final RunResult<StrutsFacet> runResult = new WriteCommandAction<StrutsFacet>(myProject) {
      protected void run(final Result<StrutsFacet> result) throws Throwable {
        String name = StrutsFacetType.INSTANCE.getPresentableName();
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

  protected void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
    myModuleTestFixture = null;
    myProject = null;
    myModule = null;
    myFacet = null;
    super.tearDown();
  }

  private void addToFileSet(final StrutsFileSet fileSet, @NonNls final String path) {
    final VirtualFile file;
    try {
      file = myFixture.copyFileToProject(path);
    }
    catch (IOException e) {
      throw new RuntimeException("error copying struts.xml from '" + path + "'", e);
    }
    assertNotNull("could not find file: '" + path + "'", file);
    fileSet.addFile(file);
  }

  protected void createStrutsFileSet(@NonNls final String... fileNames) {
    final StrutsFileSet fileSet = new StrutsFileSet("test", "test");
    for (final String fileName : fileNames) {
      addToFileSet(fileSet, fileName);
    }
    final Set<StrutsFileSet> strutsFileSetSet = myFacet.getConfiguration().getFileSets();
    strutsFileSetSet.clear();
    strutsFileSetSet.add(fileSet);
  }

}
