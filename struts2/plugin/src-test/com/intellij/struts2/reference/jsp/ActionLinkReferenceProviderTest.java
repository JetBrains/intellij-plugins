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
package com.intellij.struts2.reference.jsp;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownTargetInspection;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ActionLinkReferenceProviderTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/reference/jsp/actionLink";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected void customizeSetup(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/jsp", "/");
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
  }

  public void testActionLinkHighlightingJsp() throws Throwable {
    myFixture.enableInspections(new HtmlUnknownTargetInspection());
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.testHighlighting(true,
                               false,
                               false,
                               "/jsp/actionLink-highlighting.jsp",
                               "/WEB-INF/web.xml",
                               "jsp/index.jsp");
  }

  // TODO no reference, no highlighting..
/*  public void testActionLinkHighlightingWebXml() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.copyFileToProject("/WEB-INF/web.xml");

    myFixture.enableInspections(WebXmlInspection.class, WebWarningInspection.class);
    myFixture.testHighlighting(true, false, false, "/WEB-INF/web.xml");
  }
*/

  public void testActionLinkCompletionVariantsNamespaceGiven() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.copyFileToProject("/WEB-INF/web.xml");
    myFixture.testCompletionVariants("/jsp/actionLink-completionvariants-namespace_given.jsp",
                                     "actionLink1.action",
                                     "actionLink2.action");
  }

  public void testActionLinkCompletionVariantsNoNamespace() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");
    myFixture.copyFileToProject("/WEB-INF/web.xml");
    myFixture.testCompletionVariants("/jsp/actionLink-completionvariants-no-namespace.jsp",
                                     "actionLink-completionvariants-no-namespace.jsp",
                                     "rootActionLink.action"
                                    );
  }

  public void testActionLinkReferences() throws Throwable {
    myFixture.copyFileToProject("/WEB-INF/web.xml");
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
  private void checkActionReference(@NonNls final String filename, @NonNls final String actionName) throws Throwable {
    final PsiReference psiReference = myFixture.getReferenceAtCaretPositionWithAssertion(filename);
    final PsiElement psiElement = psiReference.resolve();
    assertNotNull("no resolve element " + actionName, psiElement);
    assertTrue(psiElement instanceof XmlTag);

    final DomElement actionElement = DomManager.getDomManager(myProject).getDomElement((XmlTag) psiElement);
    assertNotNull(actionElement);
    assertInstanceOf(actionElement, Action.class);
    assertEquals("Action name differs for " + actionName,
                 actionName, ((Action) actionElement).getName().getStringValue());
  }

}