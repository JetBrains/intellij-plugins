/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.reference.web;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.j2ee.web.highlighting.WebWarningInspection;
import com.intellij.j2ee.web.highlighting.WebXmlInspection;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@code <param-name>/<param-value>} in {@code web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class WebXmlConstantTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{new WebXmlInspection(), new WebWarningInspection()};
  }

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/reference/web/constant/";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addLibrary(moduleBuilder, "servlet-api", "servlet-api.jar");

    moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
  }

  public void testHighlighting() throws Throwable {
    myFixture.copyFileToProject("/WEB-INF/web.xml");
    myFixture.testHighlighting(true, false, false, "/WEB-INF/web.xml");
  }

  /**
   * Completion for {@code <param-name>}.
   */
  public void testNameCompletion() throws Throwable {
    myFixture.copyFileToProject("/WEB-INF/web_name_completion.xml");

    final StrutsCoreConstantContributor coreConstantContributor = new StrutsCoreConstantContributor();
    final List<StrutsConstant> constants = coreConstantContributor.getStrutsConstantDefinitions(myModule);
    final String[] variants = ContainerUtil.map2Array(constants, String.class, new Function<StrutsConstant, String>() {
      @Override
      public String fun(final StrutsConstant strutsConstant) {
        return strutsConstant.getName();
      }
    });
    myFixture.testCompletionVariants("/WEB-INF/web_name_completion.xml", variants);
  }

  /**
   * Completion for {@code <param-value>}.
   */
  public void testValueCompletion() throws Throwable {
    myFixture.copyFileToProject("/WEB-INF/web_value_completion.xml");
    myFixture.testCompletionVariants("/WEB-INF/web_value_completion.xml",
                                     "none", "get", "all");
  }

  /**
   * Completion for {@code <param-value>} with some additional variants.
   */
  public void testValueCompletion2() throws Throwable {
    myFixture.copyFileToProject("/WEB-INF/web_value_completion_2.xml");
    myFixture.testCompletionVariants("/WEB-INF/web_value_completion_2.xml",
                                     "com.opensymphony.xwork2.ObjectFactory",
                                     "com.opensymphony.xwork2.spring.SpringObjectFactory",
                                     "com.opensymphony.xwork2.spring.SpringProxyableObjectFactory",
                                     "org.apache.struts2.impl.StrutsObjectFactory",
                                     "spring",
                                     "struts");
  }

}
