// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angularjs.AngularTestUtil;

public class NgMaterialTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngMaterial";
  }

  public void testTemplatesWithSuperConstructors() {
    myFixture.enableInspections(UnterminatedStatementJSInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.copyDirectoryToProject("node_modules",".");
    myFixture.configureByFiles("templateTest.html", "package.json");
    myFixture.checkHighlighting();
  }
}
