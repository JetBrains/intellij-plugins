/*
 * Copyright 2007 The authors
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

/**
 * Tests for {@link com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter}.
 *
 * @author Yann CŽbron
 */
public class StrutsResultResolvingTest extends BasicStrutsHighlightingTestCase<WebModuleFixtureBuilder> {

  protected String getTestDataLocation() {
    return "strutsXmlResult";
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.addWebRoot(getTestDataPath() + "/jsp/", "/");
    moduleBuilder.addWebRoot(getTestDataPath() + "/jsp2/", "/2ndWebRoot/");
  }

  /**
   * {@link com.intellij.struts2.dom.struts.impl.path.DispatchPathReferenceProvider}
   *
   * @throws Throwable On errors.
   */
  public void testPathDispatcher() throws Throwable {
    performHighlightingTest("struts-path-dispatcher.xml");
  }

  /**
   * {@link com.intellij.struts2.dom.struts.impl.path.ActionPathReferenceProvider}
   *
   * @throws Throwable On errors.
   */
  public void testActionPath() throws Throwable {
    performHighlightingTest("struts-actionpath.xml");
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
   * {@link com.intellij.struts2.dom.struts.impl.path.ActionChainOrRedirectResultContributor}
   *
   * @throws Throwable On errors.
   */
  public void testActionRedirect() throws Throwable {
    performHighlightingTest("struts-actionRedirect.xml");
  }

  public void testCompletionVariantsDispatcherActionPath() throws Throwable {
    performCompletionVariantTest("struts-completionvariants.xml",
                                 "/anotherActionPathTest/anotherActionPath1.action",
                                 "2ndWebRoot",
                                 "WEB-INF",
                                 "actionPath1.action",
                                 "index.jsp",
                                 "jsp2-index.jsp");
  }

  public void testCompletionVariantsChain() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-chain.xml",
                                 "/anotherActionPathTest/anotherActionPath1",
                                 "2ndWebRoot",
                                 "WEB-INF",
                                 "actionPath1",
                                 "index.jsp",
                                 "jsp2-index.jsp");
  }

}