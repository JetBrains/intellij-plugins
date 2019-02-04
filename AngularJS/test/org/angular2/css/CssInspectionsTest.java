// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.psi.css.inspections.CssUnusedSymbolInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class CssInspectionsTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "inspections";
  }

  public void testLocalStylesheet() {
    myFixture.enableInspections(new CssUnusedSymbolInspection().getSharedLocalInspectionTool());
    myFixture.configureByFiles("local-stylesheet.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testLocalStylesheetExtUsage() {
    myFixture.enableInspections(new CssUnusedSymbolInspection().getSharedLocalInspectionTool());
    myFixture.configureByFiles("local-stylesheet-ext.ts", "local-stylesheet-ext.html", "local-stylesheet-ext.css", "package.json");
    myFixture.checkHighlighting();
  }
}
