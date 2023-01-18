/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.editor.markup.RangeHighlighter;

public class CfmlUsagesHighlighterTest extends CfmlCodeInsightFixtureTestCase {

  private RangeHighlighter[] getUsages() {
    return myFixture.testHighlightUsages(Util.getInputDataFileName(getTestName(true)));
  }

  public void testFunctionArguments() {
    assertEquals(3, getUsages().length);
  }

  public void testFunctionUsages() {
    assertEquals(3, getUsages().length);
  }

  public void testFunctionUsagesFromString() {
    assertEquals(3, getUsages().length);
  }

  public void testHighlightVariablesByFunctionScope() {
    assertEquals(3 + 1, getUsages().length); // definition counts twice
  }

  public void testFindUsagesFromDefinitionInString() {
    assertEquals(3, getUsages().length);
  }

  public void testFromNamedAttribute() {
    assertEquals(2, getUsages().length);
  }

  public void testHighlightVariableFromDefeinition() {
    assertEquals(2 + 1, getUsages().length); // definition counts twice
  }

  public void testScriptFunctionUsages() {
    assertEquals(4, getUsages().length);
  }

  public void testHighlightFromScopedVariableInComments() {
    assertEquals(3, getUsages().length);
  }

  public void testIncorrectAttributeName() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.checkHighlighting();
    TargetElementUtil.getInstance()
      .findTargetElement(myFixture.getEditor(), TargetElementUtil.ELEMENT_NAME_ACCEPTED, myFixture.getCaretOffset());
  }

  public void testIncorrectAttributeNameInArgumentTag() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.checkHighlighting();
    TargetElementUtil.getInstance()
      .findTargetElement(myFixture.getEditor(), TargetElementUtil.ELEMENT_NAME_ACCEPTED, myFixture.getCaretOffset());
  }

  @Override
  protected String getBasePath() {
      return "/usagesHighlighter";
  }
}
