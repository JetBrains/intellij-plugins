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
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.containers.ContainerUtil;
import junit.framework.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Tests highlighting with Spring plugin.
 */
public class StrutsHighlightingSpringTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  @NonNls
  private static final String SPRING_XML = "spring.xml";

  @NotNull
  protected String getTestDataLocation() {
    return "strutsXmlHighlightingSpring";
  }

  @Override
  protected boolean hasJavaSources() {
    return true;
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addLibrary(moduleBuilder, "spring", "spring.jar");
    addLibrary(moduleBuilder, "struts2-spring-plugin", STRUTS2_SPRING_PLUGIN_JAR);
  }

  public void testStrutsSpringHighlighting() throws Throwable {
    final SpringFileSet springFileSet = configureSpringFileSet();
    addFile(springFileSet, SPRING_XML);

    performHighlightingTest("struts-spring.xml");
  }

  public void testStrutsSpringCompletionVariants() throws Throwable {
    @NonNls final String strutsXml = "struts-completionvariants-spring.xml";
    createStrutsFileSet(strutsXml);

    final SpringFileSet springFileSet = configureSpringFileSet();
    addFile(springFileSet, SPRING_XML);

    // TODO <alias> does not appear here, see com.intellij.spring.impl.SpringModelImpl#myOwnBeans
    final List<String> variants = myFixture.getCompletionVariants(strutsXml);

    Assert.assertTrue(CollectionUtils.isSubCollection(Arrays.asList("MyClass", "bean1", "bean2", "springInterceptor",
                                                                    "springResultType"),
                                                      variants));
  }

  public void testStrutsSpringCompletionVariantsSubclass() throws Throwable {
    @NonNls final String strutsXml = "struts-completionvariants-subclass-spring.xml";
    createStrutsFileSet(strutsXml);

    final SpringFileSet springFileSet = configureSpringFileSet();
    addFile(springFileSet, SPRING_XML);

    final List<String> variants = myFixture.getCompletionVariants(strutsXml);

    final List<String> springCompletionVariants = filterSpringBeanCompletionVariants(variants);

    assertSameElements(springCompletionVariants, "springInterceptor");
  }

  /**
   * Returns all completions variants without '.', i.e. all Spring bean names.
   *
   * @param variants Original variants.
   * @return Remaining (Spring Bean) variants.
   */
  private List<String> filterSpringBeanCompletionVariants(final List<String> variants) {
    return ContainerUtil.findAll(variants, new Condition<String>() {
      public boolean value(final String s) {
        return !s.contains(".");
      }
    });
  }

  // stuff below is Spring related ===============================================

  protected SpringFileSet configureSpringFileSet() throws Throwable {
    final SpringFacet mySpringFacet = createSpringFacet();

    final SpringFacetConfiguration configuration = mySpringFacet.getConfiguration();
    final Set<SpringFileSet> list = configuration.getFileSets();
    @NonNls final SpringFileSet fileSet = new SpringFileSet("", "default", configuration);
    list.add(fileSet);
    return fileSet;
  }

  protected void addFile(final SpringFileSet fileSet, @NonNls final String path) {
    myFixture.copyFileToProject(path);

    final VirtualFile file = myFixture.getTempDirFixture().getFile(path);
    assert file != null;
    fileSet.addFile(file);

    final SpringFacet springFacet = SpringFacet.getInstance(myModule);
    assert springFacet != null;
    springFacet.getConfiguration().setModified();
  }

  protected SpringFacet createSpringFacet() {
    final RunResult<SpringFacet> runResult = new WriteCommandAction<SpringFacet>(myFixture.getProject()) {
      protected void run(final Result<SpringFacet> result) throws Throwable {
        final String name = SpringFacetType.INSTANCE.getPresentableName();
        final SpringFacet facet = FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, name, null);
        result.setResult(facet);
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException(throwable);
    }

    return runResult.getResultObject();
  }

}