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

package com.intellij.lang.ognl.formatting;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Tests for {@link OgnlFormattingModelBuilder}.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlFormattingTest extends LightCodeInsightFixtureTestCase {

  public void testComma() {
    doTest("{1 ,2}",
           "{1, 2}");
  }

  public void testQuestion() {
    doTest("true?1 : 2",
           "true ? 1 : 2");
  }

  public void testColon() {
    doTest("true?1:2",
           "true ? 1 : 2");
  }

  public void testWithinParentheses() {
    doTest("( 1+2  )",
           "(1+2)");
  }

  public void testWithinBraces() {
    doTest("{ 1, 2 }",
           "{1, 2}");
  }

  public void testWithinBrackets() {
    doTest("id[ 5 ]",
           "id[5]");
  }

  private void doTest(final String before,
                      final String after) {
    myFixture.configureByText(OgnlFileType.INSTANCE, before);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        CodeStyleManager.getInstance(myFixture.getProject()).reformat(myFixture.getFile());
      }
    });
    myFixture.checkResult(after);
  }

}