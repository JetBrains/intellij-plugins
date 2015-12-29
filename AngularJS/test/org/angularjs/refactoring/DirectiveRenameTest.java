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

  public void testTag2() {
    myFixture.configureByFiles("tag.html", "tag2.ts", "angular2.js");
    myFixture.testRename("tag.after.html", "foo-bar2");
    myFixture.checkResultByFile("tag2.ts", "tag2.after.ts", false);
  }

  public void testTag2Normalized() {
    myFixture.configureByFiles("tag.html", "tag2.ts", "angular2.js");
    myFixture.testRename("tag2Normalized.after.html", "fooBar2");
    myFixture.checkResultByFile("tag2.ts", "tag2Normalized.after.ts", false);
  }

  public void testAttribute2() {
    myFixture.configureByFiles("attribute2.html", "attribute2.ts", "angular2.js");
    myFixture.testRename("attribute2.after.html", "foo-bar2");
    myFixture.checkResultByFile("attribute2.ts", "attribute2.after.ts", false);
  }

  public void testAttribute2Normalized() {
    myFixture.configureByFiles("attribute2.html", "attribute2.ts", "angular2.js");
    myFixture.testRename("attribute2Normalized.after.html", "fooBar2");
    myFixture.checkResultByFile("attribute2.ts", "attribute2Normalized.after.ts", false);
  }

  public void testBinding() {
    myFixture.configureByFiles("binding.html", "binding.ts", "angular2.js");
    myFixture.testRename("binding.after.html", "model2");
    myFixture.checkResultByFile("binding.ts", "binding.after.ts", false);
  }
}
