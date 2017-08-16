/*
 * Copyright 2014 The authors
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

package com.intellij.struts2.annotators;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.psi.PsiMethod;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Test for {@link JspActionAnnotator}
 *
 * @author Yann C&eacute;bron
 */
public class JspActionAnnotatorTest extends BasicLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/gutterJsp/actionClass";
  }

  /**
   * Checks whether the gutter target elements resolve to the given Action names.
   *
   * @param jspFile             JSP file to check.
   * @param expectedActionNames Names of the actions.
   */
  private void checkGutterActionMethodTargetElements(@NonNls final String jspFile,
                                                     @NonNls final String... expectedActionNames) {
    final GutterMark gutterIconRenderer = myFixture.findGutter(jspFile);
    assertNotNull(gutterIconRenderer);
    AnnotatorTestUtils.checkGutterTargets(gutterIconRenderer, psiElement -> ((PsiMethod)psiElement).getName(), expectedActionNames);
  }

  public void testGutterActionAttribute() {
    createStrutsFileSet("struts-actionClass.xml");
    myFixture.copyFileToProject("MyAction.java");

    checkGutterActionMethodTargetElements("/jsp/test_gutter_action_attribute.jsp",
                                          "validActionMethod");
  }

  public void testGutterNameAttribute() {
    createStrutsFileSet("struts-actionClass.xml");
    myFixture.copyFileToProject("MyAction.java");

    checkGutterActionMethodTargetElements("/jsp/test_gutter_name_attribute.jsp",
                                          "validActionMethod");
  }
}