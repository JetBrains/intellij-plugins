package org.angularjs.performance;

import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Konstantin.Ulitin
 */
public class AngularJSPerformanceTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testManyInjectionsHighlighting() {
    myFixture.configureByFiles("manyInjections.highlight.html", "angular.js", "custom.js");
    PlatformTestUtil.startPerformanceTest(getTestName(false), 100_000, () -> myFixture.checkHighlighting()).attempts(1).usesAllCPUCores().assertTiming();
  }

}
