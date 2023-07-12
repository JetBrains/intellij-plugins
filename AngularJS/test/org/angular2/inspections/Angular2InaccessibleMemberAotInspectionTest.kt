// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class Angular2InaccessibleMemberAotInspectionTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "aot";
  }

  public void testAotInaccessibleMemberTs() {
    myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection.class);
    myFixture.configureByFiles("private-ts.ts", "private-ts.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testAotInaccessibleMemberHtml() {
    myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection.class);
    myFixture.configureByFiles("private-html.html", "private-html.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testAotInaccessibleMemberInline() {
    myFixture.setCaresAboutInjection(false);
    myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection.class);
    myFixture.configureByFiles("private-inline.ts", "package.json");
    myFixture.checkHighlighting();
  }
}
