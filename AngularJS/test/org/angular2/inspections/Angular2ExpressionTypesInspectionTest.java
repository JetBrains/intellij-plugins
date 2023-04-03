// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.lang.javascript.TypeScriptTestUtil;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateJSTypesInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import static org.angular2.modules.Angular2TestModule.*;

public class Angular2ExpressionTypesInspectionTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "expressionType";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(AngularUndefinedBindingInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                JSValidateTypesInspection.class,
                                TypeScriptValidateTypesInspection.class,
                                TypeScriptValidateJSTypesInspection.class,
                                TypeScriptUnresolvedReferenceInspection.class);
  }

  public void testSimpleTypes() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14, ANGULAR_FORMS_8_2_14);
    myFixture.configureByFiles("simple.html", "simpleComponent.ts", "componentWithTypes.ts");
    myFixture.checkHighlighting();
  }

  public void testExpressions() {
    TypeScriptTestUtil.forceConfig(getProject(), null, getTestRootDisposable());
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14, ANGULAR_FORMS_8_2_14, RXJS_6_4_0);
    myFixture.configureByFiles("expressions.html", "expressions.ts", "componentWithTypes.ts");
    myFixture.checkHighlighting();
  }

  public void testTemplateBindings() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("template.html", "template.ts");
    myFixture.checkHighlighting();
  }

  public void testGenericsValidation() {
    configureLink(myFixture);
    myFixture.configureByFiles("generics.html", "generics.ts");
    myFixture.checkHighlighting();
  }

  public void testNgForOfAnyType() {
    TypeScriptTestUtil.forceDefaultTsConfig(getProject(), getTestRootDisposable());
    configureLink(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("ngForOfAnyType.ts");
    myFixture.checkHighlighting();
  }

  public void testAnyType() {
    TypeScriptTestUtil.forceDefaultTsConfig(getProject(), getTestRootDisposable());
    configureLink(myFixture);
    myFixture.configureByFiles("any-type.ts");
    myFixture.checkHighlighting();
  }

  public void testSlicePipe() {
    configureLink(myFixture, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("slice_pipe_test.ts");
    myFixture.checkHighlighting();
  }

  public void testQueryList() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("query-list-test.ts");
    myFixture.checkHighlighting();
  }

  public void testInputValue() {
    configureLink(myFixture);
    myFixture.configureByFiles("inputValue.html", "inputValue.ts");
    myFixture.checkHighlighting();
  }

  public void testNullChecks() {
    configureCopy(myFixture, ANGULAR_CORE_15_1_5, ANGULAR_COMMON_15_1_5);
    myFixture.configureByFile("tsconfig.json");
    myFixture.configureByFile("tsconfig.app.json");
    myFixture.configureByFiles("null-checks.html", "null-checks.ts");
    myFixture.checkHighlighting();
  }

  public void testNullChecksInline() {
    configureCopy(myFixture, ANGULAR_CORE_15_1_5, ANGULAR_COMMON_15_1_5);
    myFixture.configureByFile("tsconfig.json");
    myFixture.configureByFile("tsconfig.app.json");
    myFixture.configureByFiles("null-checks-inline.ts");
    myFixture.checkHighlighting();
  }
}
