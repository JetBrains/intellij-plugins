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

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Created by Lera Nikolaenko
 * Date: 12.01.2009
 */
public class CfmlTypedHandlerTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
  }

  public void testSimpleTagGTCompletion() throws Throwable { doTest('>'); }

    public void testInnerTagGTCompletion() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTCompletion1() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTCompletion2() throws Throwable {
        doTest('>');
    }

    public void testSimpleTagGTNoCompletion() throws Throwable {
        doTest('>');
    }

    public void testInnerTagGTNoCompletion() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTNoCompletion1() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTNoCompletion2() throws Throwable {
        doTest('>');
    }

    public void testSeveralTagsGTCompletion() throws Throwable {
        doTest('>');
    }

    public void testInvokeClosingNotInsertion() throws Throwable {
        doTest('>');
    }

    public void testModuleClosingNotInsertion() throws Throwable {
        doTest('>');
    }

    public void testQuoteCompletion() throws Throwable {
        doTest('\"');
    }

    public void testQuoteDeletion() throws Throwable {
        doTest('\b');
    }

    public void testEnterHandler() throws Throwable {
        doTest('\n');
    }

  public void testNpeEnterHandler() throws Throwable {
    doTest('\n');
  }

  public void testEnterHandlerInsideCfFunction() throws Throwable {
    doTest('\n');
  }

  public void testEnterHandlerAfterTemplateText() throws Throwable {
    doTest('\n');
  }

  public void testEnterHandlerInsideHtmlBlock() throws Throwable {
    doTest('\n');
  }

    // public void testLiveTemplate() throws Throwable { doTest('\t'); }

    public void testSharpCompletion() throws Throwable {
        doTest('#');
    }

    public void testInnerSharpCompletion() throws Throwable {
        doTest('#');
    }

    public void testRightBracketInQuotes() throws Throwable {
        doTest(')');
    }

  public void testNoInsertionRCurlyBracketIfIncorrect() throws Throwable {
    doTest('\n');
  }

  public void testRightBracketInsertion() throws Throwable { doTest('('); }
    public void testRightSquareBracketInsertion() throws Throwable { doTest('['); }
    public void testRightCurlyBracketInsertion() throws Throwable { doTest('{'); }

    public void testLeftBracketDeletion() throws Throwable { doTest('\b'); }
    public void testLeftSquareBracketDeletion() throws Throwable { doTest('\b'); }
    public void testLeftCurlyBracketDeletion() throws Throwable { doTest('\b'); }

    private void doTest(final char typed) throws Throwable {
        myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
        myFixture.type(typed);
        myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
    }

    @Override
    protected String getBasePath() {
        return "/typedHandler";
    }
}
