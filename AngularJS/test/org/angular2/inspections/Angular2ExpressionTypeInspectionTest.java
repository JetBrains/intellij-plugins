// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class Angular2ExpressionTypeInspectionTest extends Angular2CodeInsightFixtureTestCase {
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
                                TypeScriptValidateTypesInspection.class,
                                TypeScriptUnresolvedFunctionInspection.class);
  }

  public void testSimpleTypes() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "common", "forms");
      myFixture.configureByFiles("async_pipe.d.ts", "ng_model.d.ts", "Observable.d.ts", "case_conversion_pipes.d.ts");
      myFixture.configureByFiles("simple.html", "componentWithTypes.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testExpressions() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "common", "forms");
      myFixture.configureByFiles("async_pipe.d.ts", "ng_model.d.ts", "Observable.d.ts", "case_conversion_pipes.d.ts");
      myFixture.configureByFiles("expressions.html", "expressions.ts", "componentWithTypes.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testTemplateBindings() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.copyDirectoryToProject("node_modules", "./node_modules");
      myFixture.configureByFiles("template.html", "template.ts", "ng_for_of.ts",
                                 "ng_if.ts", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testGenericsValidation() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("generics.html", "generics.ts", "package.json");
      myFixture.checkHighlighting();
    });
  }
}
