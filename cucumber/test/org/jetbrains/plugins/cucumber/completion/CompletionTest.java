package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

public class CompletionTest extends CucumberCodeInsightTestCase {
  public void testOutline() {
    doTest();
  }

 public void testOutlineWithoutColon() {
    doTest();
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/completion/insert";
  }

  private void doTest() {
    final String testName = getTestName(true);
    myFixture.configureByFile(testName + ".feature");
    LookupElement[] result = myFixture.completeBasic();

    LookupElement scenarioOutlineLookupElement = null;
    for (LookupElement lookupElement : result) {
      if (lookupElement.getUserDataString().contains("Scenario")) {
        scenarioOutlineLookupElement = lookupElement;
        break;
      }
    }
    assert scenarioOutlineLookupElement != null;
    myFixture.getLookup().setCurrentItem(scenarioOutlineLookupElement);
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR);

    myFixture.checkResultByFile(testName + "_after.feature");
  }
}
