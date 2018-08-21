// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class EmptyEventHandlerInspectionTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "emptyEventHandler";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(EmptyEventHandlerInspection.class);
  }

  public void testSimple() {
    doTest();
  }

  public void testEq() {
    doTest();
  }

  protected void doTest() {
    myFixture.configureByFiles(getTestName(true) + ".html", "angular2.js");
    myFixture.checkHighlighting();
    myFixture.launchAction(myFixture.findSingleIntention("Add attribute value"));
    myFixture.checkResultByFile(getTestName(true) + ".after.html");
  }
}
