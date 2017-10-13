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

import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.reference.jsp.ActionReferenceProvider}.
 *
 * @author Yann C&eacute;bron
 */
public class ActionReferenceProviderTest extends BasicLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "reference/jsp/action";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return WEB;
  }

  public void testActionHighlighting() {
    createStrutsFileSet("struts-action.xml");
    myFixture.copyFileToProject("ActionMethods.java");

    myFixture.testHighlighting(true, false, false, "/jsp/action-highlighting.jsp");
  }

  public void testActionCompletionVariants() {
    createStrutsFileSet("struts-action.xml");
    myFixture.copyFileToProject("ActionMethods.java");

    myFixture.testCompletionVariants("/jsp/action-completionvariants.jsp",
                                     "bangAction", "namespace1Action", "namespace2Action", "myWildCard*");
  }

  public void testActionCompletionVariantsBang() {
    createStrutsFileSet("struts-action.xml");
    myFixture.copyFileToProject("ActionMethods.java");

    myFixture.testCompletionVariants("/jsp/action-completionvariants-bang.jsp",
                                     "methodA", "methodB");
  }

  public void testActionCompletionVariantsNamespace() {
    createStrutsFileSet("struts-action.xml");
    myFixture.copyFileToProject("ActionMethods.java");

    myFixture.testCompletionVariants("/jsp/action-completionvariants_namespace.jsp",
                                     "myWildCard*");
  }

  public void testActionCompletionVariantsMethod() {
    createStrutsFileSet("struts-action.xml");
    myFixture.copyFileToProject("ActionMethods.java");

    myFixture.testCompletionVariants("/jsp/action-completionvariants_method.jsp",
                                     "methodA", "methodB");
  }
}