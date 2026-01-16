package org.angularjs.performance;

import com.intellij.testFramework.PerformanceUnitTest;
import com.intellij.tools.ide.metrics.benchmark.Benchmark;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Konstantin.Ulitin
 */
@PerformanceUnitTest
public class AngularJSPerformanceTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testManyInjectionsHighlighting() {
    myFixture.configureByFiles("manyInjections.highlight.html", "angular.js", "custom.js");
    Benchmark.newBenchmark(getTestName(false), () -> myFixture.checkHighlighting()).attempts(1).start();
  }

}
