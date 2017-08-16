package org.angularjs.index;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.junit.Assert;

public class AngularIndexUtilTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testHasAngularJS2() {
    myFixture.configureByText("ng_for.js", "NgFor.decorators = [\n" +
                                           "    { type: core_1.Directive, args: [{ selector: '[ngFor][ngForOf]', inputs: ['ngForTrackBy', 'ngForOf', 'ngForTemplate'] },] },\n" +
                                           "];");
    Assert.assertTrue(AngularIndexUtil.hasAngularJS2(myFixture.getProject()));
  }
}
