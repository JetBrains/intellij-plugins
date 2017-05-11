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

import com.intellij.coldFusion.UI.inspections.CfmlReferenceInspection;
import com.intellij.coldFusion.model.CfmlLanguage;

import java.util.concurrent.Callable;

public class CfmlHighlighterTest extends CfmlCodeInsightFixtureTestCase {

  protected void doTest() throws Exception {
    myFixture.testHighlighting(true, false, true, Util.getInputDataFileName(getTestName(true)));
  }

  public void testHighlightCfmlMixedWithJavascript() throws Exception {
    doTest();
  }

  public void testHighlightCfml10Tags() throws Exception {
    myFixture.enableInspections(new CfmlReferenceInspection());
    doTest();
  }

  public void testHighlightCfml11Tags() throws Exception {
    myFixture.enableInspections(new CfmlReferenceInspection());
    Util.runTestWithLanguageLevel(() -> {
      doTest();
      return null;
    }, CfmlLanguage.CF11, getProject());
  }

  public void testSyntaxError() throws Exception {
    myFixture.enableInspections(new CfmlReferenceInspection());
    doTest();
  }

  public void testAssertion() throws Exception {
    myFixture.enableInspections(new CfmlReferenceInspection());
    doTest();
  }

  public void testAssertion2() throws Exception {
    myFixture.enableInspections(new CfmlReferenceInspection());
    doTest();
  }

  @Override
  protected String getBasePath() {
    return "/highlighter";
  }
}
