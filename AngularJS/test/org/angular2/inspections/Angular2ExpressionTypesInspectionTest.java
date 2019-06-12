// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class Angular2ExpressionTypesInspectionTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "expressionType";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(AngularInvalidExpressionResultTypeInspection.class,
                                AngularUndefinedBindingInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                TypeScriptValidateTypesInspection.class,
                                TypeScriptUnresolvedVariableInspection.class,
                                TypeScriptUnresolvedFunctionInspection.class);
  }

  public void testSimpleTypes() {
    AngularTestUtil.configureWithMetadataFiles(myFixture, "common", "forms");
    myFixture.configureByFiles("async_pipe.d.ts", "ng_model.d.ts", "Observable.d.ts", "case_conversion_pipes.d.ts");
    myFixture.configureByFiles("simple.html", "componentWithTypes.ts");
    myFixture.checkHighlighting();
  }

  public void testExpressions() {
    AngularTestUtil.configureWithMetadataFiles(myFixture, "common", "forms");
    myFixture.configureByFiles("async_pipe.d.ts", "ng_model.d.ts", "Observable.d.ts", "case_conversion_pipes.d.ts");
    myFixture.configureByFiles("expressions.html", "expressions.ts", "componentWithTypes.ts");
    myFixture.checkHighlighting();
  }

  public void testTemplateBindings() {
    myFixture.copyDirectoryToProject("node_modules", "./node_modules");
    myFixture.configureByFiles("template.html", "template.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testGenericsValidation() {
    myFixture.configureByFiles("generics.html", "generics.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testNgForOfAnyType() {
    myFixture.copyDirectoryToProject("node_modules", "./node_modules");
    AngularTestUtil.configureWithMetadataFiles(myFixture, "common");
    myFixture.configureByFiles("ngForOfAnyType.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testAnyType() {
    myFixture.configureByFiles("any-type.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testSlicePipe() {
    myFixture.configureByFiles("slice_pipe_test.ts", "slice_pipe.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testQueryList() {
    myFixture.copyDirectoryToProject("node_modules", "./node_modules");
    myFixture.configureByFiles("query-list-test.ts", "package.json");
    myFixture.checkHighlighting();
  }
}
