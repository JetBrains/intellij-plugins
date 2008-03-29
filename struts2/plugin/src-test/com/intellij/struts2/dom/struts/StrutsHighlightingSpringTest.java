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

package com.intellij.struts2.dom.struts;

import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

import java.io.IOException;
import java.util.Set;

/**
 * Tests highlighting with Spring plugin.
 */
public class StrutsHighlightingSpringTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected String getTestDataLocation() {
    return "strutsXmlHighlightingSpring";
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addLibrary(moduleBuilder, "spring", "spring.jar");
    addLibrary(moduleBuilder, "struts2-spring-plugin", "struts2-spring-plugin-2.0.11.jar");
  }

  public void testStrutsSpringHighlighting() throws Throwable {
    final SpringFileSet springFileSet = configureSpringFileSet();
    addFile(springFileSet, "spring.xml");


    performHighlightingTest("struts-spring.xml");
  }

  public void testStrutsSpringCompletionVariants() throws Throwable {
    createStrutsFileSet("struts-completionvariants-spring.xml");


    final SpringFileSet springFileSet = configureSpringFileSet();
    addFile(springFileSet, "spring.xml");

    // TODO <alias> does not appear here, see com.intellij.spring.impl.SpringModelImpl#myOwnBeans
/*
    myFixture.testCompletionVariants("struts-completionvariants-spring.xml",
                                     "META-INF",
                                     "MyClass",
                                     "bean1",
                                     "bean2",
                                     "com",
                                     "freemarker",
                                     "ognl",
                                     "org",
                                     "template");
*/
  }

  // stuff below is Spring related ===============================================

  protected SpringFileSet configureSpringFileSet() throws Throwable {
    final SpringFacet mySpringFacet = createSpringFacet();

    final SpringFacetConfiguration configuration = mySpringFacet.getConfiguration();
    final Set<SpringFileSet> list = configuration.getFileSets();
    final SpringFileSet fileSet = new SpringFileSet("", "default");
    list.add(fileSet);
    return fileSet;
  }

  protected VirtualFile addFile(final SpringFileSet fileSet, final String path) {
    try {
      myFixture.copyFileToProject(path);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    final VirtualFile file = myFixture.getTempDirFixture().getFile(path);
    assert file != null;
    fileSet.addFile(file);

    final SpringFacet springFacet = SpringFacet.getInstance(myModule);
    assert springFacet != null;
    springFacet.getConfiguration().setModified();

    return file;
  }

  protected SpringFacet createSpringFacet() {
    final RunResult<SpringFacet> runResult = new WriteCommandAction<SpringFacet>(myFixture.getProject()) {
      protected void run(final Result<SpringFacet> result) throws Throwable {
        final ModifiableFacetModel model = FacetManager.getInstance(myModule).createModifiableModel();
        final SpringFacet facet = SpringFacetType.INSTANCE.createFacet(myModule,
                                                                       SpringFacetType.INSTANCE.getPresentableName(),
                                                                       SpringFacetType.INSTANCE.createDefaultConfiguration(),
                                                                       null);
        result.setResult(facet);
        model.addFacet(facet);
        model.commit();
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException(throwable);
    }

    return runResult.getResultObject();
  }

}