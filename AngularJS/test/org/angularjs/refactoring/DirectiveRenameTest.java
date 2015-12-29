package org.angularjs.refactoring;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class DirectiveRenameTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "rename";
  }

  public void testTag() {
    myFixture.configureByFiles("tag.html", "angular.js");
    myFixture.testRename("tag.after.html", "foo-bar2");
  }

  public void testTagNormalized() {
    myFixture.configureByFiles("tag.html", "angular.js");
    myFixture.testRename("tag.after.html", "fooBar2");
  }

  public void testAttribute() {
    myFixture.configureByFiles("attribute.html", "angular.js");
    myFixture.testRename("attribute.after.html", "foo-bar2");
  }

  public void testAttributeNormalized() {
    myFixture.configureByFiles("attribute.html", "angular.js");
    myFixture.testRename("attribute.after.html", "fooBar2");
  }
}
