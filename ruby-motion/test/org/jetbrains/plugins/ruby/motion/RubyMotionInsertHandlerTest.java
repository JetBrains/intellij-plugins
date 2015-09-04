package org.jetbrains.plugins.ruby.motion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.Lookup;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionInsertHandlerTest extends RubyMotionLightFixtureTestCase {
  @Override
  protected String getTestDataRelativePath() {
    return "testApp";
  }

  public void testNoArg() {
    defaultConfigure();
    doTest("init");
  }

  public void testSingleArg() {
    defaultConfigure();
    doTest("performSelector");
  }

  public void testMultiArg() {
    defaultConfigure();
    doTest("performSelector:onThread:withObject:waitUntilDone");
  }

  private void doTest(final String selector) {
    myFixture.getEditor().getCaretModel().moveToOffset(findOffsetBySignature("<caret>true"));
    myFixture.complete(CompletionType.BASIC);
    myFixture.type(selector);
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile("app/" + getTestName(true).toLowerCase() + ".rb");
  }
}
