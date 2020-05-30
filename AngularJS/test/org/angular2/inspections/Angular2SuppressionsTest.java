// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import static java.util.Arrays.asList;

public class Angular2SuppressionsTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "suppressions";
  }

  public void testTemplateSuppressions() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("template.html", "template.after.html", "package.json");

    for (String location : asList("test1", "bar1", "pipe1")) {
      try {
        AngularTestUtil.moveToOffsetBySignature(location.charAt(0) + "<caret>" + location.substring(1), myFixture);
        myFixture.launchAction(myFixture.findSingleIntention("Suppress for expression"));
      }
      catch (AssertionError err) {
        throw new AssertionError("Failed at " + location, err);
      }
    }

    for (String location : asList("foo1", "var1")) {
      try {
        AngularTestUtil.moveToOffsetBySignature(location.charAt(0) + "<caret>" + location.substring(1), myFixture);
        assertEmpty(myFixture.filterAvailableIntentions("Suppress for expression"));
        myFixture.launchAction(myFixture.findSingleIntention("Suppress for tag"));
      }
      catch (AssertionError err) {
        throw new AssertionError("Failed at " + location, err);
      }
    }

    PsiFile after = myFixture.getPsiManager().findFile(myFixture.getTempDirFixture().getFile("template.after.html"));
    ExpectedHighlightingData data = new ExpectedHighlightingData(
      myFixture.getDocument(after), true, true, false);
    data.init();
    ((CodeInsightTestFixtureImpl)myFixture).collectAndCheckHighlighting(data);
  }
}
