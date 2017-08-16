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

import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler;
import com.intellij.coldFusion.UI.editorActions.surroundWith.CfmlSharpSurrounder;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.lang.LanguageSurrounders;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class CfmlSurroundWithTest extends CfmlCodeInsightFixtureTestCase {


  public void testSurroundSelectionWithSharps() {
    doTest(new CfmlSharpSurrounder());
  }

  public void testSurroundSelectionWithSharpsOfCompositeElement() {
    doTest(new CfmlSharpSurrounder());
  }

  public void testSurroundSelectionWithSharpsInStringText() {
    doTest(new CfmlSharpSurrounder());
  }

  public void testSurroundSelectionWithSharpsNoSurround() {
    doTestNoSurround();
  }

  public void testSurroundSelectionWithSharpsNoSurround2() {
    doTestNoSurround();
  }

  public void testSurroundSelectionWithSharpsOfFunctionArgument() {
    doTest(new CfmlSharpSurrounder());
  }

  public void testSurroundSelectionWithSharpsOfFunctionCall() {
    doTest(new CfmlSharpSurrounder());
  }

  public void testSurroundSelectionWithSharpsInHtmlTag() {
    doTest(new CfmlSharpSurrounder());
  }

  public void testSurroundSelectionWithSharpsInCfoutput() {
    doTest(new CfmlSharpSurrounder());
  }

  private void doTest(final Surrounder surrounder) {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    SurroundWithHandler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), surrounder);
    myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
  }

  private void doTestNoSurround() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    List<SurroundDescriptor> surroundDescriptors = new ArrayList<>();
    surroundDescriptors.addAll(LanguageSurrounders.INSTANCE.allForLanguage(CfmlLanguage.INSTANCE));
    for (SurroundDescriptor descriptor : surroundDescriptors) {
      assertEquals(descriptor
                     .getElementsToSurround(myFixture.getFile(), myFixture.getEditor().getSelectionModel().getSelectionStart(),
                                            myFixture.getEditor().getSelectionModel().getSelectionEnd()), PsiElement.EMPTY_ARRAY);
    }
  }

  @Override
  protected String getBasePath() {
    return "/surroundWith";
  }
}
