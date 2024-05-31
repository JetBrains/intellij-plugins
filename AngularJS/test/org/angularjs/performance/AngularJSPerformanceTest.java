package org.angularjs.performance;

import com.intellij.tools.ide.metrics.benchmark.PerformanceTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Konstantin.Ulitin
 */
public class AngularJSPerformanceTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testManyInjectionsHighlighting() {
    myFixture.configureByFiles("manyInjections.highlight.html", "angular.js", "custom.js");
    PerformanceTestUtil.newPerformanceTest(getTestName(false), () -> myFixture.checkHighlighting()).attempts(1).start();
  }

}
