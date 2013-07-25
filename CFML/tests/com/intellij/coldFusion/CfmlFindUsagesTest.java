package com.intellij.coldFusion;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.usageView.UsageInfo;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlFindUsagesTest extends CfmlCodeInsightFixtureTestCase {
  private Collection<UsageInfo> getUsages() {
    return myFixture.testFindUsages(Util.getInputDataFileName(getTestName(true)));
  }

  public void testFunctionUsagesInScript() {
    assertEquals(3, getUsages().size());
  }

  public void testFunctionUsages() {
    assertEquals(1, getUsages().size());
  }

  public void testFunctionArgumentUsages() {
    assertEquals(5, getUsages().size());
  }

  public void testFunctionArgumentUsagesInScript() {
    assertEquals(2, getUsages().size());
  }


  @Override
  protected String getBasePath() {
    return "/findUsages";
  }
}
