// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedFunctionInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class Angular2ExpressionTypeInspectionTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "expressionType";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(Angular2ExpressionTypeInspection.class,
                                Angular2BindingsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                JSCheckFunctionSignaturesInspection.class,
                                JSUnresolvedFunctionInspection.class);
  }

  public void testSimpleTypesValidation() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "common", "forms");
      myFixture.configureByFiles("async_pipe.d.ts", "ng_model.d.ts", "Observable.d.ts", "case_conversion_pipes.d.ts");
      myFixture.configureByFiles("simple.html", "componentWithTypes.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testExpressionsValidation() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "common", "forms");
      myFixture.configureByFiles("async_pipe.d.ts", "ng_model.d.ts", "Observable.d.ts", "case_conversion_pipes.d.ts");
      myFixture.configureByFiles("expressions.html", "expressions.ts", "componentWithTypes.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testTemplateBindingsValidation() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("template.html", "template.ts", "ng_for_of.ts", "ng_if.ts", "iterable_differs.ts",
                                 "template_ref.ts", "package.json");
      myFixture.checkHighlighting();
    });
  }
}
