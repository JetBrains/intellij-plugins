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
package com.intellij.struts2.jsp.ognl;

import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class Struts2OgnlJspTest extends BasicLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/jsp/ognl";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return WEB;
  }

  /**
   * @see com.intellij.struts2.jsp.ognl.OgnlStruts2JspVariableReferencesContributor
   */
  public void testStruts2JspVariablesCompletion() {
    myFixture.testCompletionVariants("struts2-jsp-variables-completion.jsp",
                                     "application",
                                     "attr",
                                     "context",
                                     "parameters",
                                     "request",
                                     "root",
                                     "session",
                                     "this");
  }

  public void testStruts2TaglibOgnlInjection() {
    myFixture.copyDirectoryToProject("WEB-INF", "WEB-INF");
    myFixture.testHighlighting(true, true, false, "taglib-ognl-injection.jsp");
  }
}
