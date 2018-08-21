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
    myFixture.configureByFiles("tag.html", "tag.js", "angular.js");
    myFixture.testRename("tag.after.html", "foo-bar2");
    myFixture.checkResultByFile("tag.js", "tag.after.js", false);
  }

  public void testTagNormalized() {
    myFixture.configureByFiles("tag.html", "tag.js", "angular.js");
    myFixture.testRename("tag.after.html", "fooBar2");
    myFixture.checkResultByFile("tag.js", "tag.after.js", false);
  }

  public void testAttribute() {
    myFixture.configureByFiles("attribute.html", "attribute.js", "angular.js");
    myFixture.testRename("attribute.after.html", "foo-bar2");
    myFixture.checkResultByFile("attribute.js", "attribute.after.js", false);
  }

  public void testAttributeNormalized() {
    myFixture.configureByFiles("attribute.html", "attribute.js", "angular.js");
    myFixture.testRename("attribute.after.html", "fooBar2");
    myFixture.checkResultByFile("attribute.js", "attribute.after.js", false);
  }

}
