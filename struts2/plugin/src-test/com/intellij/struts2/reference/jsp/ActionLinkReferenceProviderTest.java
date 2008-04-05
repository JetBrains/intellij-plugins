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
package com.intellij.struts2.reference.jsp;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;

public class ActionLinkReferenceProviderTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[0];
  }

  protected String getTestDataLocation() {
    return "/reference/jsp/actionLink";
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/jsp", "/");
  }

  // TODO how to test/disable IDEA internal warnings for unresolved web-paths?!
  /*  public void testActionHtmlLinkHighlighting() throws Throwable {
      createStrutsFileSet("struts-actionLink.xml");
      myFixture.testHighlighting(true, false, false, "/jsp/actionLink-highlighting.jsp");
    }
  */

  public void testActionLinkCompletionVariants() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.testCompletionVariants("/jsp/actionLink-completionvariants.jsp",
                                     "/actionLink/actionLink1.action",
                                     "/actionLink/actionLink2.action");
  }

  public void testActionLinkReferences() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");
    checkActionReference("/jsp/actionLink-reference_1.jsp", "actionLink1");
    checkActionReference("/jsp/actionLink-reference_2.jsp", "rootActionLink");
  }

  /**
   * Checks the Action-reference.
   *
   * @param filename   File to check.
   * @param actionName Name of the Action to resolve to.
   * @throws Throwable On errors.
   */
  private void checkActionReference(final String filename, final String actionName) throws Throwable {
    final PsiReference psiReference = myFixture.getReferenceAtCaretPositionWithAssertion(filename);
    final PsiElement psiElement = psiReference.resolve();
    assertNotNull("no resolve element " + actionName, psiElement);
    assertTrue(psiElement instanceof XmlTag);

    final DomElement actionElement = DomManager.getDomManager(myProject).getDomElement((XmlTag) psiElement);
    assertNotNull(actionElement);
    assertTrue(actionElement instanceof Action);
    assertEquals("Action name differs for " + actionName,
                 ((Action) actionElement).getName().getStringValue(), actionName);
  }

}