// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class DirectiveRenameTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "rename";
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
    myFixture.configureByFiles("binding.html", "object.ts", "angular2.js");
    myFixture.testRename("binding.after.html", "model2");
    myFixture.checkResultByFile("object.ts", "object.binding.ts", false);
  }

  public void testDirective() {
    myFixture.configureByFiles("directive2.ts", "directive2.html", "angular2.js");
    myFixture.testRename("directive2.after.ts", "foo-bar2");
    myFixture.checkResultByFile("directive2.html", "directive2.after.html", false);
  }

  public void testEventHandler() {
    myFixture.configureByFiles("event.html", "object.ts", "angular2.js");
    myFixture.testRename("event.after.html", "complete2");
    myFixture.checkResultByFile("object.ts", "object.event.ts", false);
  }
}
