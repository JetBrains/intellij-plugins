package org.angularjs.performance;

import com.intellij.testFramework.PlatformTestUtil;
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
    PlatformTestUtil.newPerformanceTest(getTestName(false), () -> myFixture.checkHighlighting()).attempts(1).start();
  }

}
