package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

public abstract class PostCssQuickFixTest extends PostCssFixtureTestCase {
  protected void doTest(String message){
    myFixture.configureByFile(getTestName(true) + ".pcss");
    final IntentionAction intentionAction = myFixture.findSingleIntention(message);
    myFixture.launchAction(intentionAction);
    myFixture.checkResultByFile(getTestName(true) + "_after.pcss");
  }
}