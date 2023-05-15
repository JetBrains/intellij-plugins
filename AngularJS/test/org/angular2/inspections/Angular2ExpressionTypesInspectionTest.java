// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.lang.javascript.TypeScriptTestUtil;
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
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider(true));
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

  public void testNgForOfAnyTypeNonStrictTemplates() {
    TypeScriptTestUtil.forceDefaultTsConfig(getProject(), getTestRootDisposable());
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("ngForOfAnyType.ts");
    myFixture.checkHighlighting();
  }

  public void testNgForOfAnyTypeStrict() {
    configureCommonFiles();
    myFixture.configureByFiles("ngForOfAnyTypeStrict.ts");
    myFixture.checkHighlighting();
  }

  public void testAnyType() {
    TypeScriptTestUtil.forceDefaultTsConfig(getProject(), getTestRootDisposable());
    configureLink(myFixture);
    myFixture.configureByFiles("any-type.ts");
    myFixture.checkHighlighting();
  }

  public void testSlicePipe() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("slice_pipe_test.ts");
    myFixture.checkHighlighting();
  }

  public void testNgForOfQueryList() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14);
    myFixture.configureByFiles("ngForOfQueryList.ts");
    myFixture.checkHighlighting();
  }

  public void testInputValue() {
    configureLink(myFixture);
    myFixture.configureByFiles("inputValue.html", "inputValue.ts");
    myFixture.checkHighlighting();
  }

  public void testNullChecks() {
    configureCommonFiles();
    myFixture.configureByFiles("NullChecks.html", "NullChecks.ts");
    myFixture.checkHighlighting();
  }

  public void testNullChecksInline() {
    configureCommonFiles();
    myFixture.configureByFiles("NullChecksInline.ts");
    myFixture.checkHighlighting();
  }

  public void testNgIfAsContextGuardStrictNullChecks() {
    configureCommonFiles();
    myFixture.configureByFiles("NgIfAsContextGuardStrictNullChecks.ts");
    myFixture.checkHighlighting();
  }

  public void testNgIfAsContextGuardRemovesFalsy() {
    configureCommonFiles();
    myFixture.configureByFiles("NgIfAsContextGuardRemovesFalsy.ts");
    myFixture.checkHighlighting();
  }

  public void testNgForContextGuard() {
    configureCommonFiles();
    myFixture.configureByFiles("NgForContextGuard.ts");
    myFixture.checkHighlighting();
  }

  public void testNgrxLetContextGuard() {
    configureCommonFiles();
    myFixture.configureByFile("let.directive.ts");
    myFixture.configureByFiles("NgrxLetContextGuard.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardNonGeneric() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardNonGeneric.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardDoubleGeneric() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardDoubleGeneric.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardPartialGeneric() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardPartialGeneric.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardOmitted() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardOmitted.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardMalformed() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardMalformed.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardEmptyInputs() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardEmptyInputs.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardTwoDirectivesOneGuard() {
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardTwoDirectivesOneGuard.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardTwoGuards() {
    // the test documents an edge case, in Angular language-tools results are different, we have false-negative
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardTwoGuards.ts");
    myFixture.checkHighlighting();
  }

  public void testNgTemplateContextGuardInferenceFromTwoInputs() {
    // There are 2 things of interest:
    // * type of person
    // * type checking for assignment of expressions to directive inputs
    configureCommonFiles();
    myFixture.configureByFiles("ngTemplateContextGuardInferenceFromTwoInputs.ts");
    myFixture.checkHighlighting();
  }

  private void configureCommonFiles() {
    configureCopy(myFixture, ANGULAR_CORE_15_1_5, ANGULAR_COMMON_15_1_5, RXJS_6_4_0);
    myFixture.configureByFile("tsconfig.json");
    myFixture.configureByFile("tsconfig.app.json");
  }
}
