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

package com.intellij.struts2.dom.struts;

import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsResultResolvingTest extends BasicStrutsHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "strutsXml/result";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected void customizeSetup(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    moduleBuilder.addWebRoot(getTestDataPath() + "/jsp/", "/");
    moduleBuilder.addWebRoot(getTestDataPath() + "/jsp2/", "/2ndWebRoot/");
    moduleBuilder.addSourceRoot(myFixture.getTempDirPath()); // TODO
  }

  /**
   * @throws Throwable On errors.
   * @see com.intellij.struts2.dom.struts.impl.path.DispatchPathResultContributor
   */
  public void testPathDispatcher() throws Throwable {
    performHighlightingTest("struts-path-dispatcher.xml");
  }

  /**
   * @throws Throwable On errors.
   * @see com.intellij.struts2.dom.struts.impl.path.ActionPathResultContributor
   */
  public void testActionPath() throws Throwable {
    performHighlightingTest("struts-actionpath.xml");
  }

  /**
   * @throws Throwable On errors.
   * @see com.intellij.struts2.reference.jsp.ActionLinkReferenceProvider
   */
  public void testActionPathFQ() throws Throwable {
    performHighlightingTest("struts-actionpath-fq.xml");
  }

  /**
   * {@link com.intellij.struts2.dom.struts.impl.path.ActionChainOrRedirectResultContributor}
   *
   * @throws Throwable On errors.
   */
  public void testActionChain() throws Throwable {
    performHighlightingTest("struts-actionchain.xml");
  }

  /**
   * @throws Throwable On errors.
   * @see com.intellij.struts2.dom.struts.impl.path.ActionChainOrRedirectResultContributor
   */
  public void testActionRedirect() throws Throwable {
    performHighlightingTest("struts-actionRedirect.xml");
  }

  public void testUnknownResultTypes() throws Throwable {
    performHighlightingTest("struts-unknownResultType.xml");
  }

  public void testCompletionVariantsDispatcherActionPath() throws Throwable {
    performCompletionVariantTest("struts-completionvariants.xml",
                                 "/anotherActionPathTest/anotherActionPath1.action",
                                 "2ndWebRoot",
                                 "actionPath1.action",
                                 "index.jsp",
                                 "jsp2-index.jsp");
  }

  public void testCompletionVariantsChain() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-chain.xml",
                                 "/anotherActionPathTest/anotherActionPath1",
                                 "actionPath1");
  }

}