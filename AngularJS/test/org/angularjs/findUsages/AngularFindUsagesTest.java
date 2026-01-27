package org.angularjs.findUsages;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usageView.UsageInfo;
import org.angularjs.AngularTestUtil;

import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularFindUsagesTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testDirectiveFromCSS() {
    doTest();
  }

  public void testDirectiveFromJS() {
    doTest();
  }

  public void testDirectiveFromHtml() {
    doTest();
  }

  private void doTest() {
    final Collection<UsageInfo> infos = myFixture.testFindUsages(getTestName(true) + ".html", "angular.js");
    assertEquals(3, infos.size());
  }
}
