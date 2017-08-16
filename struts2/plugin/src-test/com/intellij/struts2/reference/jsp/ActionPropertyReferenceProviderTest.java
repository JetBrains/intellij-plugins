/*
 * Copyright 2014 The authors
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
import org.jetbrains.annotations.NotNull;

/**
 * {@link com.intellij.struts2.reference.jsp.ActionPropertyReferenceProvider}
 *
 * @author Yann C&eacute;bron
 */
public class ActionPropertyReferenceProviderTest extends BasicLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "reference/jsp/actionproperty";
  }

  public void testActionPropertyParamHighlighting() {
    myFixture.copyFileToProject("MyAction.java");
    createStrutsFileSet("struts-actionproperty.xml");
    myFixture.testHighlighting(true, false, false, "/jsp/actionproperty-param-highlighting.jsp");
  }

  public void testActionPropertyFormInputHighlighting() {
    myFixture.copyFileToProject("MyAction.java");
    createStrutsFileSet("struts-actionproperty.xml");
    myFixture.testHighlighting(true, false, false, "/jsp/actionproperty-forminput-highlighting.jsp");
  }

  public void testActionPropertyFormInputCompletionVariants() {
    myFixture.copyFileToProject("MyAction.java");
    createStrutsFileSet("struts-actionproperty.xml");
    myFixture.testCompletionVariants("/jsp/actionproperty-forminput-completionvariants.jsp",
                                     "myBooleanField", "myField", "mySetterOnlyField", "readonlyList", "user");
  }

  public void testActionPropertyFormInputReadOnlyCompletionVariants() {
    myFixture.copyFileToProject("MyAction.java");
    createStrutsFileSet("struts-actionproperty.xml");
    myFixture.testCompletionVariants("/jsp/actionproperty-forminput-readonly-completionvariants.jsp",
                                     "myField", "myBooleanField", "mySetterOnlyField", "readonlyList", "user");
  }
}