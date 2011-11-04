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

import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link NamespaceReferenceProvider}.
 *
 * @author Yann C&eacute;bron
 */
public class NamespaceReferenceProviderTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "reference/jsp/namespace";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected void customizeSetup(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/jsp", "/");
  }

  public void testNamespaceHighlighting() throws Throwable {
    createStrutsFileSet("struts-namespace.xml");
    myFixture.testHighlighting(true, false, false, "/jsp/namespace-highlighting.jsp");
  }

  public void testNamespaceCompletionVariants() throws Throwable {
    createStrutsFileSet("struts-namespace.xml");
    myFixture.testCompletionVariants("/jsp/namespace-completionvariants.jsp",
                                     "/namespace1", "/namespace2");
  }

}