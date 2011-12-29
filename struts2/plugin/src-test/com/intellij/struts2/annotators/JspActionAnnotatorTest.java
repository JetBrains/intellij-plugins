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

package com.intellij.struts2.annotators;

import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Test for {@link JspActionAnnotator}
 *
 * @author Yann C&eacute;bron
 */
public class JspActionAnnotatorTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/gutterJsp/actionClass";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected void customizeSetup(final WebModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/jsp/", "/");
  }

  /**
   * Checks whether the gutter target elements resolve to the given Action names.
   *
   * @param jspFile             JSP file to check.
   * @param expectedActionNames Names of the actions.
   */
  private void checkGutterActionMethodTargetElements(@NonNls final String jspFile,
                                                     @NonNls final String... expectedActionNames) {
    final GutterIconRenderer gutterIconRenderer = myFixture.findGutter(jspFile);
    assertNotNull(gutterIconRenderer);
    AnnotatorTestUtils.checkGutterTargets(gutterIconRenderer, new Function<PsiElement, String>() {
      @Override
      public String fun(final PsiElement psiElement) {
        return ((PsiMethod) psiElement).getName();
      }
    }, expectedActionNames);
  }

  @HasJavaSources
  public void testGutterActionAttribute() throws Throwable {
    createStrutsFileSet("struts-actionClass.xml");
    checkGutterActionMethodTargetElements("/jsp/test_gutter_action_attribute.jsp",
                                          "validActionMethod");
  }

  @HasJavaSources
  public void testGutterNameAttribute() throws Throwable {
    createStrutsFileSet("struts-actionClass.xml");
    checkGutterActionMethodTargetElements("/jsp/test_gutter_name_attribute.jsp",
                                          "validActionMethod");
  }

}