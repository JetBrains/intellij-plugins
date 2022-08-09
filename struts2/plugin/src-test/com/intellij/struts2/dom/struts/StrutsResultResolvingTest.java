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

package com.intellij.struts2.dom.struts;

import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.util.Disposer;
import com.intellij.struts2.Struts2ProjectDescriptorBuilder;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Tests for {@link com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsResultResolvingTest extends StrutsLightHighlightingTestCase {

  private final LightProjectDescriptor WEB = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withWebModuleType();

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "strutsXml/result";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return WEB;
  }

  @Override
  protected void performSetUp() {
    final WebFacet webFacet = ContainerUtil.getFirstItem(WebFacet.getInstances(getModule()));
    assert webFacet != null;

    myFixture.copyDirectoryToProject("jsp", "jsp");
    myFixture.copyDirectoryToProject("jsp2", "jsp2");
    myFixture.copyDirectoryToProject("WEB-INF", "WEB-INF");
    try {
      final String jspUrl = myFixture.getTempDirFixture().findOrCreateDir("/jsp/").getUrl();
      WebRoot jsp = webFacet.addWebRoot(jspUrl, "/");
      final String jsp2Url = myFixture.getTempDirFixture().findOrCreateDir("/jsp2/").getUrl();
      WebRoot jsp2 = webFacet.addWebRoot(jsp2Url, "/2ndWebRoot/");

      final String defaultWebRootUrl = myFixture.getTempDirFixture().findOrCreateDir("/").getUrl();
      Disposer.register(myFixture.getProjectDisposable(), () -> {
        webFacet.removeWebRoot(jsp);
        webFacet.addWebRoot(defaultWebRootUrl, "/"); // restore for later tests

        webFacet.removeWebRoot(jsp2);
      });
    }
    catch (IOException e) {
      fail();
    }
  }

  /**
   * @see com.intellij.struts2.dom.struts.impl.path.DispatchPathResultContributor
   */
  public void testPathDispatcher() {
    performHighlightingTest("struts-path-dispatcher.xml");
  }

  /**
   * @see com.intellij.struts2.dom.struts.impl.path.ActionPathResultContributor
   */
  public void testActionPath() {
    performHighlightingTest("struts-actionpath.xml");
  }

  /**
   * @see com.intellij.struts2.reference.jsp.ActionLinkReferenceProvider
   */
  public void testActionPathFQ() {
    performHighlightingTest("struts-actionpath-fq.xml");
  }

  /**
   * {@link com.intellij.struts2.dom.struts.impl.path.ActionChainOrRedirectResultContributor}
   *
   */
  public void testActionChain() {
    performHighlightingTest("struts-actionchain.xml");
  }

  /**
   * @see com.intellij.struts2.dom.struts.impl.path.ActionChainOrRedirectResultContributor
   */
  public void testActionRedirect() {
    performHighlightingTest("struts-actionRedirect.xml");
  }

  public void testUnknownResultTypes() {
    performHighlightingTest("struts-unknownResultType.xml");
  }

  public void testCompletionVariantsDispatcherActionPath() {
    performCompletionVariantTest("struts-completionvariants.xml",
                                 "/anotherActionPathTest/anotherActionPath1.action",
                                 "2ndWebRoot",
                                 "WEB-INF",
                                 "actionPath1.action",
                                 "index.jsp",
                                 "jsp",
                                 "jsp2",
                                 "jsp2-index.jsp",
                                 "struts-completionvariants.xml");
  }

  public void testCompletionVariantsChain() {
    performCompletionVariantTest("struts-completionvariants-chain.xml",
                                 "/anotherActionPathTest/anotherActionPath1",
                                 "actionPath1");
  }
}