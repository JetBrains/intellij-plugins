// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.Angular2TestUtil
import org.angular2.inspections.*

/**
 * @see Angular2TsInspectionsTest
 *
 * @see Angular2TemplateInspectionsTest
 */
class Angular2DecoratorInspectionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "inspections/decorator"
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
    doTest(1, "Com<caret>ponent2", "Make Component2 standalone", AngularMissingOrInvalidDeclarationInModuleInspection::class.java,
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

  fun testModuleEntityTypeMismatch1a() {
    doTest(1, "Com<caret>ponent1, //import a", "Make Component1 standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch1b() {
    doTest(1, "Com<caret>ponent1, //import b", "Make Component1 standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch2a() {
    doTest(2, "Dir<caret>ective1, //import a", "Make Directive1 standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch2b() {
    doTest(2, "Dir<caret>ective1, //import b", "Make Directive1 standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch3a() {
    doTest(3, "Pi<caret>pe1, //import a", "Make Pipe1 standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch3b() {
    doTest(3, "Pi<caret>pe1, //import b", "Make Pipe1 standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch4() {
    doTest(4, "Com<caret>ponentStandalone, // move", "Import ComponentStandalone instead", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch5() {
    doTest(5, "Dir<caret>ectiveStandalone, // move", "Import DirectiveStandalone instead", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch6() {
    doTest(6, "Pi<caret>peStandalone, // move", "Import PipeStandalone instead", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch7() {
    doTest(7, "Com<caret>ponentStandalone, // move", "Make ComponentStandalone non-standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatch8() {
    doTest(8, "Dir<caret>ectiveStandalone, // move", "Make DirectiveStandalone non-standalone", AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch.ts")
  }

  fun testModuleEntityTypeMismatchNg19() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4)
    doTest(AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch-ng19.ts")
  }

  fun testModuleEntityTypeMismatchNg19_1a() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4)
    doTest(1, "Com<caret>ponentNonStandalone, //import a", "Make ComponentNonStandalone standalone",
           AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch-ng19.ts")
  }

  fun testModuleEntityTypeMismatchNg19_1b() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4)
    doTest(1, "Com<caret>ponentNonStandalone, //import b", "Make ComponentNonStandalone standalone",
           AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch-ng19.ts")
  }

  fun testModuleEntityTypeMismatchNg19_2() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4)
    doTest(2, "Com<caret>ponent1, // move", "Import Component1 instead",
           AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch-ng19.ts")
  }

  fun testModuleEntityTypeMismatchNg19_3() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4)
    doTest(3, "Com<caret>ponent1, // move", "Make Component1 non-standalone",
           AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch-ng19.ts")
  }

  fun testModuleEntityTypeMismatchNg19_4() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4)
    doTest(4, "Dire<caret>ctive1, // move", "Make Directive1 non-standalone",
           AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
           "module-entity-type-mismatch-ng19.ts")
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

  fun testImportsInNonStandaloneComponent1() {
    doTest(1, "impo<caret>rts: [", "Make Component standalone", AngularNonStandaloneComponentImportsInspection::class.java,
           "non-standalone-component-imports-inspections.ts")
  }

  fun testImportsInNonStandaloneComponent2() {
    doTest(2, "impo<caret>rts: [", "Remove 'imports' property", AngularNonStandaloneComponentImportsInspection::class.java,
           "non-standalone-component-imports-inspections.ts")
  }

  fun testUnusedImportsInStandaloneComponent1() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)
    doTest(1, "imports: [\n    Async<caret>Pipe,\n",
           "Remove 'AsyncPipe' import", AngularUnusedComponentImportInspection::class.java,
           "unused-imports-in-standalone-component.ts", "unused-imports-in-standalone-component.html")
  }

  fun testUnusedImportsInStandaloneComponent2() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)
    doTest(2, "UNUSED_PSEUDO_MODULE, //no-spread\n    Ng<caret>If,",
           "Remove 'NgIf' import", AngularUnusedComponentImportInspection::class.java,
           "unused-imports-in-standalone-component.ts", "unused-imports-in-standalone-component.html")
  }

  fun testUnusedImportsInStandaloneComponent3() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)
    doTest(3, "UNUSED_<caret>PSEUDO_MODULE, //no-spread",
           "Remove 'UNUSED_PSEUDO_MODULE' import", AngularUnusedComponentImportInspection::class.java,
           "unused-imports-in-standalone-component.ts", "unused-imports-in-standalone-component.html")
  }
  fun testUnusedImportsInStandaloneComponent4() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)
    doTest(4, "...UNUSED_<caret>PSEUDO_MODULE, //spread\n",
           "Remove 'UNUSED_PSEUDO_MODULE' import", AngularUnusedComponentImportInspection::class.java,
           "unused-imports-in-standalone-component.ts", "unused-imports-in-standalone-component.html")
  }

  fun testUnusedImportsInStandaloneComponentStructuralDirectives() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)
    doTest(AngularUnusedComponentImportInspection::class.java,
           "unused-imports-in-standalone-component-structural-directives.ts")
  }

  fun testUnusedImportsPipeWithInheritedTransform() {
    doTest(AngularUnusedComponentImportInspection::class.java,
           "unused-imports-pipe-with-inherited-transform.ts")
  }

  fun testInspectionsNonAngular() {
    myFixture.enableInspections(AngularIncorrectTemplateDefinitionInspection::class.java,
                                AngularInvalidSelectorInspection::class.java,
                                AngularMissingOrInvalidDeclarationInModuleInspection::class.java,
                                AngularInvalidImportedOrDeclaredSymbolInspection::class.java,
                                AngularRecursiveModuleImportExportInspection::class.java,
                                AngularUndefinedModuleExportInspection::class.java,
                                AngularInvalidEntryComponentInspection::class.java,
                                AngularNonStandaloneComponentImportsInspection::class.java,
                                AngularInvalidImportedOrDeclaredSymbolInspection::class.java)
    myFixture.configureByFiles("non-angular.a.ts", "non-angular.b.ts", "package.json")
    myFixture.checkHighlighting()
  }

  private fun doTest(
    inspection: Class<out LocalInspectionTool>,
    vararg files: String,
  ) {
    doTest(1, null, null, inspection, *files)
  }

  private fun doTest(
    testNr: Int,
    location: String?,
    quickFixName: String?,
    inspection: Class<out LocalInspectionTool>,
    vararg files: String,
  ) {
    myFixture.enableInspections(inspection)
    if (myFixture.tempDirFixture.getFile("package.json") == null)
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
