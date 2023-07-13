// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.typescript.inspection.TypeScriptExplicitMemberTypeInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureCopy
import org.angularjs.AngularTestUtil

/**
 * @see Angular2TsInspectionsTest
 *
 * @see Angular2DecoratorInspectionsTest
 */
class Angular2TemplateInspectionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/template"
  }

  fun testEmptyEventBinding1() {
    doTest(1, "onc<caret>lick", "Add attribute value", AngularMissingEventHandlerInspection::class.java,
           "empty-event-binding.html")
  }

  fun testEmptyEventBinding2() {
    doTest(2, "on<caret>tap", "Add attribute value", AngularMissingEventHandlerInspection::class.java,
           "empty-event-binding.html")
  }

  fun testBindingToEvent1() {
    doTest(1, "[on<caret>foo]", "Bind to event (foo)", AngularInsecureBindingToEventInspection::class.java,
           "binding-to-event.html", "component.ts")
  }

  fun testBindingToEvent2() {
    doTest(2, "[on<caret>foo]", "Remove attribute [onfoo]", AngularInsecureBindingToEventInspection::class.java,
           "binding-to-event.html", "component.ts")
  }

  fun testBindingToEvent3() {
    doTest(3, "[attr.on<caret>Foo]", "Bind to event (Foo)", AngularInsecureBindingToEventInspection::class.java,
           "binding-to-event.html", "component.ts")
  }

  fun testNonEmptyNgContent() {
    doTest(1, "ff<caret>f", "Remove content", AngularNonEmptyNgContentInspection::class.java,
           "non-empty-ng-content.html")
  }

  fun testMultipleTemplateBindings() {
    doTest(1, "*some<caret>thing", "Remove attribute *something", AngularMultipleStructuralDirectivesInspection::class.java,
           "multiple-template-bindings.html")
  }

  fun testAnimationTriggerAssignment1() {
    doTest(1, "@trigger=\"<caret>foo", "Bind to property [@trigger]", AngularInvalidAnimationTriggerAssignmentInspection::class.java,
           "animation-trigger-assignment.html")
  }

  fun testAnimationTriggerAssignment2() {
    doTest(2, "@trigger=\"<caret>foo", "Remove attribute value", AngularInvalidAnimationTriggerAssignmentInspection::class.java,
           "animation-trigger-assignment.html")
  }

  fun testTemplateReferenceVariable() {
    doTest(1, "#abc=\"fo<caret>o\"", "Remove attribute #abc", AngularInvalidTemplateReferenceVariableInspection::class.java,
           "template-reference-variable.html", "component.ts")
  }

  fun testTemplateReferenceVariableWithModule() {
    doTest(1, "#abc=\"fo<caret>o\"", "Remove attribute #abc", AngularInvalidTemplateReferenceVariableInspection::class.java,
           "template-reference-variable-with-module.html", "component.ts", "template-reference-variable-module.ts", "forms.d.ts")
  }

  fun testMatchingComponents() {
    doTest(AngularAmbiguousComponentTagInspection::class.java,
           "matching-components.html", "component.ts")
  }

  fun testMatchingComponentsWithModule() {
    doTest(AngularAmbiguousComponentTagInspection::class.java,
           "matching-components-with-module.html", "component.ts", "matching-components-module.ts")
  }

  fun testBindings() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java)
    doTest(AngularUndefinedBindingInspection::class.java,
           "bindings.html", "component.ts")
  }

  fun testBindingsWithModule() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java)
    doTest(AngularUndefinedBindingInspection::class.java,
           "bindings-with-module.html", "component.ts", "bindings-module.ts")
  }

  fun testTags() {
    myFixture.enableInspections(HtmlUnknownTagInspection::class.java)
    doTest(AngularUndefinedTagInspection::class.java,
           "tags.html", "component.ts")
  }

  fun testTagsWithModule() {
    myFixture.enableInspections(HtmlUnknownTagInspection::class.java)
    doTest(AngularUndefinedTagInspection::class.java,
           "tags-with-module.html", "component.ts", "tags-module.ts")
  }

  fun testStandaloneDeclarables() {
    myFixture.enableInspections(HtmlUnknownTagInspection::class.java)
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java)
    myFixture.enableInspections(AngularUndefinedTagInspection::class.java)
    myFixture.enableInspections(AngularUndefinedBindingInspection::class.java)
    doTest(TypeScriptUnresolvedReferenceInspection::class.java,
           "standalone-declarables.html", "standalone-declarables.ts", "component.ts")
  }

  fun testStandaloneDeclarablesInClassic() {
    myFixture.enableInspections(HtmlUnknownTagInspection::class.java)
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java)
    myFixture.enableInspections(AngularUndefinedTagInspection::class.java)
    myFixture.enableInspections(AngularUndefinedBindingInspection::class.java)
    doTest(TypeScriptUnresolvedReferenceInspection::class.java,
           "standalone-declarables-in-classic.html", "standalone-declarables-in-classic.ts", "component.ts")
  }

  fun testNgContentSelector() {
    doTest(AngularInvalidSelectorInspection::class.java,
           "ng-content-selector.html")
  }

  fun testI18n1() {
    doTest(1, "i18n<caret>-\n", "Rename attribute to 'i18n-bar'", AngularInvalidI18nAttributeInspection::class.java,
           "i18n.html")
  }

  fun testI18n2() {
    doTest(2, "i18n-<caret>boo", "Create 'boo' attribute", AngularInvalidI18nAttributeInspection::class.java,
           "i18n.html")
  }

  fun testI18n3() {
    doTest(3, "i18n-<caret>b:boo", "Rename attribute to 'i18n-c:boo'", AngularInvalidI18nAttributeInspection::class.java,
           "i18n.html")
  }

  fun testHammerJS() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java)
    doTest(AngularUndefinedBindingInspection::class.java, "hammerJs.html")
  }

  fun testMissingRequiredInputBinding1() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4,
                  Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4)
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspection::class.java)
    doTest(1, "<ng-<caret>template", "Create '[ngForOf]' attribute",
           AngularMissingRequiredDirectiveInputBindingInspection::class.java,
           "missing-required-directive-input-bindings.html", "missing-required-directive-input-bindings-module.ts", "foo-bar.directive.ts")
  }

  fun testMissingRequiredInputBinding2() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4,
                  Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4)
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspection::class.java)
    doTest(2, "<d<caret>iv appFooBar>", "Create '[appFooBar2]' attribute",
           AngularMissingRequiredDirectiveInputBindingInspection::class.java,
           "missing-required-directive-input-bindings.html", "missing-required-directive-input-bindings-module.ts", "foo-bar.directive.ts")
  }

  fun testMissingRequiredInputBinding3() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4,
                  Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4)
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspection::class.java)
    doTest(3, "<d<caret>iv appFooBar=\"foo\"", "Create 'appFooBar2' attribute",
           AngularMissingRequiredDirectiveInputBindingInspection::class.java,
           "missing-required-directive-input-bindings.html", "missing-required-directive-input-bindings-module.ts", "foo-bar.directive.ts")
  }

  fun testTypeScriptSpecifyTypeNoFix() {
    doTestNoFix("no-specify-type-variable.html",
                TypeScriptExplicitMemberTypeInspection::class.java,
                JavaScriptBundle.message("typescript.specify.type.explicitly"))
  }

  fun testTypeScriptSpecifyTypeNoFixNgFor() {
    doTestNoFix("no-specify-type-variable-ng-for.html",
                TypeScriptExplicitMemberTypeInspection::class.java,
                JavaScriptBundle.message("typescript.specify.type.explicitly"))
  }

  fun testTypeScriptNoIntroduceVariable() {
    doTestNoFix("no-introduce-variable.html",
                null,
                JavaScriptBundle.message("javascript.introduce.variable.title.local"))
  }

  private fun doTestNoFix(location: String,
                          inspection: Class<out LocalInspectionTool?>?,
                          quickFixName: String) {
    if (inspection != null) {
      myFixture.enableInspections(inspection)
    }
    myFixture.configureByFiles("package.json")
    myFixture.configureByFiles(location)
    myFixture.checkHighlighting()
    assertNull(myFixture.getAvailableIntention(quickFixName))
  }

  private fun doTest(inspection: Class<out LocalInspectionTool?>,
                     vararg files: String) {
    doTest(1, null, null, inspection, *files)
  }

  private fun doTest(testNr: Int,
                     location: String?,
                     quickFixName: String?,
                     inspection: Class<out LocalInspectionTool?>,
                     vararg files: String) {
    myFixture.enableInspections(inspection)
    if (myFixture.getTempDirFixture().getFile("package.json") == null) {
      myFixture.configureByFiles("package.json")
    }
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
