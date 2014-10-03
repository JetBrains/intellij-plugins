package org.angularjs.codeInsight;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class NgRepeatTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngRepeat";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testNgInit() {
    final List<String> variants = myFixture.getCompletionVariants("ngInit.html", "angular.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 3);
    assertEquals("age", variants.get(0));
    assertEquals("gender", variants.get(1));
    assertEquals("name", variants.get(2));
  }

  public void testControllerWithAlias() {
    final List<String> variants = myFixture.getCompletionVariants("controllerWithAlias.html", "angular.js", "custom.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("firstName", variants.get(0));
    assertEquals("lastName", variants.get(1));
  }

  public void testControllerWithAliasNoVar() {
    final List<String> variants = myFixture.getCompletionVariants("controllerWithAlias.html", "angular.js", "customNoVar.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("firstName", variants.get(0));
    assertEquals("lastName", variants.get(1));
  }

  public void testControllerWithoutAlias() {
    final List<String> variants = myFixture.getCompletionVariants("controllerWithoutAlias.html", "angular.js", "custom.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("firstName", variants.get(0));
    assertEquals("lastName", variants.get(1));
  }
}
