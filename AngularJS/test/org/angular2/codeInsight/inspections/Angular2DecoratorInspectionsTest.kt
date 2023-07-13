// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.*
import org.angularjs.AngularTestUtil

/**
 * @see Angular2TsInspectionsTest
 *
 * @see Angular2TemplateInspectionsTest
 */
class Angular2DecoratorInspectionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/decorator"
  }

  fun testComponentTemplateProperty1() {
    doTest(1, "@Component({<caret>})", "Add 'template' property", AngularIncorrectTemplateDefinitionInspection::class.java,
           "component-template-property.ts")
  }

  fun testComponentTemplateProperty2() {
    doTest(2, "@Component({<caret>})", "Add 'templateUrl' property", AngularIncorrectTemplateDefinitionInspection::class.java,
           "component-template-property.ts")
  }

  fun testComponentTemplateProperty3() {
    doTest(3, "template<caret>: \"foo\"", "Remove 'template' property", AngularIncorrectTemplateDefinitionInspection::class.java,
           "component-template-property.ts")
  }

  fun testComponentTemplateProperty4() {
    doTest(4, "templateUrl<caret>: \"bar\"", "Remove 'templateUrl' property", AngularIncorrectTemplateDefinitionInspection::class.java,
           "component-template-property.ts")
  }

  fun testDirectiveSelector() {
    doTest(1, "outputs: <caret>", "Add 'selector' property", AngularInvalidSelectorInspection::class.java,
           "directive-selector.ts")
  }

  fun testDeclarationMembershipInModule() {
    doTest(AngularMissingOrInvalidDeclarationInModuleInspection::class.java,
           "declaration-membership-in-module.ts")
  }

  fun testDeclarationMembershipInModule2() {
    doTest(AngularMissingOrInvalidDeclarationInModuleInspection::class.java,
           "declaration-membership-in-module.2a.ts", "declaration-membership-in-module.2b.ts")
  }

  fun testDeclarationMembershipInModuleSpecFile() {
    doTest(AngularMissingOrInvalidDeclarationInModuleInspection::class.java,
           "app.component.check.ts")
  }

  fun testModuleEntityTypeMismatch() {
    doTest(AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testRecursiveImportExport() {
    doTest(AngularRecursiveModuleImportExportInspection::class.java,
           "recursive-import-export.a.ts", "recursive-import-export.b.ts")
  }

  fun testUndeclaredExport() {
    doTest(AngularUndefinedModuleExportInspection::class.java,
           "undeclared-export.ts")
  }

  fun testInvalidEntryComponent() {
    doTest(AngularInvalidEntryComponentInspection::class.java,
           "invalid-entry-component.ts")
  }

  fun testNotModuleSameLineOtherFile() {
    doTest(AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "not-module-same-line-other-file.a.ts",
           "not-module-same-line-other-file.b.ts")
  }

  fun testInspectionsNonAngular() {
    myFixture.enableInspections(AngularIncorrectTemplateDefinitionInspection::class.java,
                                AngularInvalidSelectorInspection::class.java,
                                AngularMissingOrInvalidDeclarationInModuleInspection::class.java,
                                AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
                                AngularRecursiveModuleImportExportInspection::class.java,
                                AngularUndefinedModuleExportInspection::class.java,
                                AngularInvalidEntryComponentInspection::class.java,
                                AngularInvalidImportedOrDeclaredSymbolInspection::class.java)
    myFixture.configureByFiles("non-angular.a.ts", "non-angular.b.ts", "package.json")
    myFixture.checkHighlighting()
  }

  private fun doTest(inspection: Class<out LocalInspectionTool>,
                     vararg files: String) {
    doTest(1, null, null, inspection, *files)
  }

  private fun doTest(testNr: Int,
                     location: String?,
                     quickFixName: String?,
                     inspection: Class<out LocalInspectionTool>,
                     vararg files: String) {
    myFixture.enableInspections(inspection)
    myFixture.configureByFiles("package.json")
    myFixture.configureByFiles(*files)
    myFixture.checkHighlighting()
    if (location == null || quickFixName == null) {
      return
    }
    myFixture.moveToOffsetBySignature(location)
    myFixture.launchAction(myFixture.findSingleIntention(quickFixName))
    val lastDot = files[0].lastIndexOf('.')
    myFixture.checkResultByFile(files[0].substring(0, lastDot) + ".after" + testNr + files[0].substring(lastDot))
  }
}
