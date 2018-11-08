// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class NgMaterialTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngMaterial";
  }

  public void testTemplatesWithSuperConstructors() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.enableInspections(UnterminatedStatementJSInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      AngularTestUtil.configureWithMetadataFiles(myFixture, "table", "cdk-index");
      myFixture.configureByFiles("templateTest.html", "cell.d.ts", "row.ts", "cdk_cell.d.ts", "cdk_row.ts");
      myFixture.checkHighlighting();
    });
  }
}
