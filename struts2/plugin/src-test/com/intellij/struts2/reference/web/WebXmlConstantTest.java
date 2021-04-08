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

package com.intellij.struts2.reference.web;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.javaee.web.highlighting.WebWarningInspection;
import com.intellij.javaee.web.highlighting.WebXmlInspection;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.testFramework.InspectionTestUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@code <param-name>/<param-value>} in {@code web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class WebXmlConstantTest extends BasicLightHighlightingTestCase {

  @Override
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{
      InspectionTestUtil.instantiateTool(WebXmlInspection.class),
      InspectionTestUtil.instantiateTool(WebWarningInspection.class)
    };
  }

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/reference/web/constant/";
  }

  public void testHighlighting() {
    myFixture.testHighlighting(true, false, false, "/WEB-INF/web.xml");
  }

  /**
   * Completion for {@code <param-name>}.
   */
  public void testNameCompletion() {
    final StrutsCoreConstantContributor coreConstantContributor = new StrutsCoreConstantContributor();
    final List<StrutsConstant> constants = coreConstantContributor.getStrutsConstantDefinitions(getModule());
    final String[] variants = ContainerUtil.map2Array(constants, String.class, strutsConstant -> strutsConstant.getName());
    myFixture.testCompletionVariants("/WEB-INF/web_name_completion.xml", variants);
  }

  /**
   * Completion for {@code <param-value>}.
   */
  public void testValueCompletion() {
    myFixture.testCompletionVariants("/WEB-INF/web_value_completion.xml",
                                     "none", "get", "all");
  }

  /**
   * Completion for {@code <param-value>} with some additional variants.
   */
  public void testValueCompletion2() {
    myFixture.testCompletionVariants("/WEB-INF/web_value_completion_2.xml",
                                     "com.opensymphony.xwork2.ObjectFactory",
                                     "com.opensymphony.xwork2.spring.SpringObjectFactory",
                                     "com.opensymphony.xwork2.spring.SpringProxyableObjectFactory",
                                     "org.apache.struts2.impl.StrutsObjectFactory",
                                     "spring",
                                     "struts");
  }
}
