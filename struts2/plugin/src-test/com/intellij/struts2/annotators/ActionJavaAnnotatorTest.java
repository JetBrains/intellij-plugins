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

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.BasicHighlightingTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
   * Checks whether the gutter target elements resolve to the given Action names.
   *
   * @param expectedActionNames Names of the actions.
   */
  private void checkGutterActionTargetElements(@NonNls final String... expectedActionNames) {
    final Editor editor = myFixture.getEditor();
    final List<LineMarkerInfo> infoList = DaemonCodeAnalyzerImpl.getLineMarkers(editor.getDocument(), myProject);
    assertNotNull(infoList);

    final LineMarkerInfo markerInfo = infoList.get(0);
    final GutterIconNavigationHandler navigationHandler = markerInfo.getNavigationHandler();
    assertNotNull(navigationHandler);
    final List<PsiElement> targetElements = ((NavigationGutterIconRenderer) navigationHandler).getTargetElements();

    final Set<String> foundActionNames = new HashSet<String>();
    for (final PsiElement psiElement : targetElements) {
      assertInstanceOf(psiElement, XmlTag.class);
      final String actionName = ((XmlTag) psiElement).getAttributeValue("name");
      foundActionNames.add(actionName);
    }

    assertSameElements(foundActionNames, expectedActionNames);
  }

  public void testGutterMyAction() throws Throwable {
    createStrutsFileSet("struts-actionClass.xml");
    myFixture.configureByFile("/src/MyAction.java");
    myFixture.doHighlighting();
    checkGutterActionTargetElements("myActionPath");
  }


  public void testGutterMyActionMultipleMappings() throws Throwable {
    createStrutsFileSet("struts-actionClass-multiple_mappings.xml");
    myFixture.configureByFile("/src/MyAction.java");
    myFixture.doHighlighting();
    checkGutterActionTargetElements("myActionPath1", "myActionPath2", "myActionPath3");
  }

}