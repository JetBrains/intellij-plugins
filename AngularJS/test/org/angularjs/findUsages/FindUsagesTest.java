package org.angularjs.findUsages;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.ThrowableRunnable;
import org.angularjs.AngularTestUtil;

import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class FindUsagesTest extends LightPlatformCodeInsightFixtureTestCase {
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

  public void testId() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class, JSUnusedGlobalSymbolsInspection.class);
      myFixture.configureByFiles("object.ts");
      myFixture.checkHighlighting();
    });
  }


  private void doTest() {
    final Collection<UsageInfo> infos = myFixture.testFindUsages(getTestName(true) + ".html", "angular.js");
    assertEquals(3, infos.size());
  }
}
