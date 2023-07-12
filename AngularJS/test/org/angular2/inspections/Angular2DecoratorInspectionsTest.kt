// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.codeInsight.InspectionsTest;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see InspectionsTest
 * @see Angular2TemplateInspectionsTest
 */
public class Angular2DecoratorInspectionsTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "decorator";
  }

  public void testComponentTemplateProperty1() {
    doTest(1, "@Component({<caret>})", "Add 'template' property", AngularIncorrectTemplateDefinitionInspection.class,
           "component-template-property.ts");
  }

  public void testComponentTemplateProperty2() {
    doTest(2, "@Component({<caret>})", "Add 'templateUrl' property", AngularIncorrectTemplateDefinitionInspection.class,
           "component-template-property.ts");
  }

  public void testComponentTemplateProperty3() {
    doTest(3, "template<caret>: \"foo\"", "Remove 'template' property", AngularIncorrectTemplateDefinitionInspection.class,
           "component-template-property.ts");
  }

  public void testComponentTemplateProperty4() {
    doTest(4, "templateUrl<caret>: \"bar\"", "Remove 'templateUrl' property", AngularIncorrectTemplateDefinitionInspection.class,
           "component-template-property.ts");
  }

  public void testDirectiveSelector() {
    doTest(1, "outputs: <caret>", "Add 'selector' property", AngularInvalidSelectorInspection.class,
           "directive-selector.ts");
  }

  public void testDeclarationMembershipInModule() {
    doTest(AngularMissingOrInvalidDeclarationInModuleInspection.class,
           "declaration-membership-in-module.ts");
  }

  public void testDeclarationMembershipInModule2() {
    doTest(AngularMissingOrInvalidDeclarationInModuleInspection.class,
           "declaration-membership-in-module.2a.ts", "declaration-membership-in-module.2b.ts");
  }

  public void testDeclarationMembershipInModuleSpecFile() {
    doTest(AngularMissingOrInvalidDeclarationInModuleInspection.class,
           "app.component.check.ts");
  }

  public void testModuleEntityTypeMismatch() {
    doTest(AngularInvalidImportedOrDeclaredSymbolInspection.class,
           "module-entity-type-mismatch.ts");
  }

  public void testRecursiveImportExport() {
    doTest(AngularRecursiveModuleImportExportInspection.class,
           "recursive-import-export.a.ts", "recursive-import-export.b.ts");
  }

  public void testUndeclaredExport() {
    doTest(AngularUndefinedModuleExportInspection.class,
           "undeclared-export.ts");
  }

  public void testInvalidEntryComponent() {
    doTest(AngularInvalidEntryComponentInspection.class,
           "invalid-entry-component.ts");
  }

  public void testNotModuleSameLineOtherFile() {
    doTest(AngularInvalidImportedOrDeclaredSymbolInspection.class,
           "not-module-same-line-other-file.a.ts",
           "not-module-same-line-other-file.b.ts");
  }

  public void testInspectionsNonAngular() {
    myFixture.enableInspections(AngularIncorrectTemplateDefinitionInspection.class,
                                AngularInvalidSelectorInspection.class,
                                AngularMissingOrInvalidDeclarationInModuleInspection.class,
                                AngularInvalidImportedOrDeclaredSymbolInspection.class,
                                AngularRecursiveModuleImportExportInspection.class,
                                AngularUndefinedModuleExportInspection.class,
                                AngularInvalidEntryComponentInspection.class,
                                AngularInvalidImportedOrDeclaredSymbolInspection.class);
    myFixture.configureByFiles("non-angular.a.ts", "non-angular.b.ts", "package.json");
    myFixture.checkHighlighting();
  }

  private void doTest(@NotNull Class<? extends LocalInspectionTool> inspection,
                      String... files) {
    doTest(1, null, null, inspection, files);
  }

  private void doTest(int testNr,
                      @Nullable String location,
                      @Nullable String quickFixName,
                      @NotNull Class<? extends LocalInspectionTool> inspection,
                      String... files) {
    myFixture.enableInspections(inspection);
    myFixture.configureByFiles("package.json");
    myFixture.configureByFiles(files);
    myFixture.checkHighlighting();
    if (location == null || quickFixName == null) {
      return;
    }
    AngularTestUtil.moveToOffsetBySignature(location, myFixture);
    myFixture.launchAction(myFixture.findSingleIntention(quickFixName));
    int lastDot = files[0].lastIndexOf('.');
    myFixture.checkResultByFile(files[0].substring(0, lastDot) + ".after" + testNr + files[0].substring(lastDot));
  }
}
