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
package com.intellij.struts2.structure;

import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class ValidationStructureViewTest extends BasicLightHighlightingTestCase {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "structure";
  }

  public void testDefaultPresentation() {
    myFixture.configureByFile("validation-structure.xml");
    myFixture.testStructureView(component -> {
      component.setActionActive(StructureViewTreeModel.getHideParamsId(), false);
      PlatformTestUtil.expandAll(component.getTree());
      PlatformTestUtil.assertTreeEqual(component.getTree(),
                                       "-validation-structure.xml\n" +
                                       " -Validator\n" +
                                       "  validatorParam\n" +
                                       "  anything\n" +
                                       " -myField\n" +
                                       "  -fieldexpression\n" +
                                       "   expression\n" +
                                       "   anything\n");
    });
  }

  public void testHideParam() {
    myFixture.configureByFile("validation-structure.xml");
    myFixture.testStructureView(component -> {
      component.setActionActive(StructureViewTreeModel.getHideParamsId(), true);
      PlatformTestUtil.expandAll(component.getTree());
      PlatformTestUtil.assertTreeEqual(component.getTree(),
                                       "-validation-structure.xml\n" +
                                       " -Validator\n" +
                                       "  anything\n" +
                                       " -myField\n" +
                                       "  -fieldexpression\n" +
                                       "   anything\n");
    });
  }
}
