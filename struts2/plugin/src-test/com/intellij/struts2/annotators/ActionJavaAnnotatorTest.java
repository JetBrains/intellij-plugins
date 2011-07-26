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
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link ActionJavaAnnotator}
 *
 * @author Yann C&eacute;bron
 */
public class ActionJavaAnnotatorTest extends BasicHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/gutterJava/actionClass";
  }

  @Override
  protected boolean hasJavaSources() {
    return true;
  }

  /**
   * Checks whether the gutter target elements in the given class resolve to the given Action names.
   *
   * @param javaFile            Path to class.
   * @param expectedActionNames Names of the actions.
   */
  private void checkGutterActionTargetElements(@NonNls final String javaFile,
                                               @NonNls final String... expectedActionNames) {
    final GutterIconRenderer renderer = myFixture.findGutter(javaFile);
    assertNotNull(renderer);

    AnnotatorTestUtils.checkGutterTargets(renderer, new Function<PsiElement, String>() {
      @Override
      public String fun(final PsiElement psiElement) {
        return ((XmlTag) psiElement).getAttributeValue("name");
      }
    }, expectedActionNames);
  }

  public void testGutterMyAction() throws Throwable {
    createStrutsFileSet("struts-actionClass.xml");
    checkGutterActionTargetElements("/src/MyAction.java", "myActionPath");
  }


  public void testGutterMyActionMultipleMappings() throws Throwable {
    createStrutsFileSet("struts-actionClass-multiple_mappings.xml");
    checkGutterActionTargetElements("/src/MyAction.java",
                                    "myActionPath1", "myActionPath2", "myActionPath3");
  }

}