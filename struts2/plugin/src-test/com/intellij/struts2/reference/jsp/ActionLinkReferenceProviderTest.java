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
package com.intellij.struts2.reference.jsp;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

public class ActionLinkReferenceProviderTest extends BasicLightHighlightingTestCase {
  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "/reference/jsp/actionLink";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return WEB;
  }

  public void testActionLinkHighlightingJsp() {
    myFixture.enableInspections(new HtmlUnknownTargetInspection());
    createStrutsFileSet("struts-actionLink.xml");

    myFixture.copyFileToProject("jsp/index.jsp", "index.jsp");
    myFixture.testHighlighting(true, false, false, "jsp/actionLink-highlighting.jsp");
  }

  // TODO no reference, no highlighting..
/*  public void testActionLinkHighlightingWebXml() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.copyFileToProject("/WEB-INF/web.xml");

    myFixture.enableInspections(WebXmlInspection.class, WebWarningInspection.class);
    myFixture.testHighlighting(true, false, false, "/WEB-INF/web.xml");
  }
*/

  public void testActionLinkCompletionVariantsNamespaceGiven() {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.testCompletionVariants("jsp/actionLink-completionvariants-namespace_given.jsp",
                                     "actionLink1.action",
                                     "actionLink2.action");
  }

  public void testActionLinkCompletionVariantsNoNamespace() {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.testCompletionVariants("jsp/actionLink-completionvariants-no-namespace.jsp",
                                     "jsp",
                                     "rootActionLink.action",
                                     "struts-actionLink.xml"
    );
  }

  public void testActionLinkReferences() {
    createStrutsFileSet("struts-actionLink.xml");
    checkActionReference("jsp/actionLink-reference_1.jsp", "actionLink1");
    checkActionReference("jsp/actionLink-reference_2.jsp", "rootActionLink");
  }

  /**
   * Checks the Action-reference.
   *
   * @param filename   File to check.
   * @param actionName Name of the Action to resolve to.
   */
  private void checkActionReference(final String filename, final String actionName) {
    final PsiReference psiReference = myFixture.getReferenceAtCaretPositionWithAssertion(filename);
    final PsiElement psiElement = psiReference.resolve();
    assertNotNull("no resolve element " + actionName, psiElement);
    assertTrue(psiElement instanceof XmlTag);

    final DomElement actionElement = DomManager.getDomManager(getProject()).getDomElement((XmlTag)psiElement);
    assertNotNull(actionElement);
    assertInstanceOf(actionElement, Action.class);
    assertEquals("Action name differs for " + actionName,
                 actionName, ((Action)actionElement).getName().getStringValue());
  }
}