// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.typescript.inspection.TypeScriptExplicitMemberTypeInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.*
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.Angular2TsConfigFile
import org.angular2.inspections.*

/**
 * @see Angular2TsInspectionsTest
 *
 * @see Angular2DecoratorInspectionsTest
 */
class Angular2TemplateInspectionsTest : Angular2TestCase("inspections/template", true) {

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
    doTest(1, "ff<caret>f", "Remove content",
           inspections = listOf(AngularNonEmptyNgContentInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("non-empty-ng-content.html"))
  }

  fun testNonEmptyNgContentNg18() {
    doTest(inspections = listOf(AngularNonEmptyNgContentInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_18_2_1),
           files = listOf("non-empty-ng-content-ng18.html"))
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
    doTest(inspections = listOf(AngularUndefinedBindingInspection::class.java, HtmlUnknownAttributeInspection::class.java),
           files = listOf("bindings.html", "component.ts"))
  }

  fun testBindingsWithModule() {
    doTest(inspections = listOf(AngularUndefinedBindingInspection::class.java, HtmlUnknownAttributeInspection::class.java),
           files = listOf("bindings-with-module.html", "component.ts", "bindings-module.ts"))
  }

  fun testTags() {
    doTest(inspections = listOf(AngularUndefinedTagInspection::class.java, HtmlUnknownTagInspection::class.java),
           files = listOf("tags.html", "component.ts"))
  }

  fun testTagsWithModule() {
    doTest(inspections = listOf(AngularUndefinedTagInspection::class.java, HtmlUnknownTagInspection::class.java),
           files = listOf("tags-with-module.html", "component.ts", "tags-module.ts"))
  }

  fun testStandaloneDeclarables() {
    doTest(inspections = listOf(TypeScriptUnresolvedReferenceInspection::class.java, HtmlUnknownTagInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java, AngularUndefinedTagInspection::class.java,
                                AngularUndefinedBindingInspection::class.java),
           files = listOf("standalone-declarables.html", "standalone-declarables.ts", "component.ts"))
  }

  fun testStandaloneDeclarablesInClassic() {
    doTest(inspections = listOf(TypeScriptUnresolvedReferenceInspection::class.java, HtmlUnknownTagInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java, AngularUndefinedTagInspection::class.java,
                                AngularUndefinedBindingInspection::class.java),
           files = listOf("standalone-declarables-in-classic.html", "standalone-declarables-in-classic.ts", "component.ts"))
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
    doTest(inspections = listOf(AngularUndefinedBindingInspection::class.java, HtmlUnknownAttributeInspection::class.java),
           files = listOf("hammerJs.html"))
  }

  fun testMissingRequiredInputBinding1() {
    doTest(1, "<ng-<caret>template", "Create '[ngForOf]' attribute",
           inspections = listOf(AngularMissingRequiredDirectiveInputBindingInspection::class.java, HtmlUnknownBooleanAttributeInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8),
           files = listOf("missing-required-directive-input-bindings.html", "missing-required-directive-input-bindings-module.ts", "foo-bar.directive.ts"))
  }

  fun testMissingRequiredInputBinding2() {
    doTest(2, "<d<caret>iv appFooBar>", "Create '[appFooBar2]' attribute",
           inspections = listOf(AngularMissingRequiredDirectiveInputBindingInspection::class.java, HtmlUnknownBooleanAttributeInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8),
           files = listOf("missing-required-directive-input-bindings.html", "missing-required-directive-input-bindings-module.ts", "foo-bar.directive.ts"))
  }

  fun testMissingRequiredInputBinding3() {
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspection::class.java)
    doTest(3, "<d<caret>iv appFooBar=\"foo\"", "Create 'appFooBar2' attribute",
           inspections = listOf(AngularMissingRequiredDirectiveInputBindingInspection::class.java, HtmlUnknownBooleanAttributeInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8),
           files = listOf("missing-required-directive-input-bindings.html", "missing-required-directive-input-bindings-module.ts", "foo-bar.directive.ts"))
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

  fun testInaccessibleSymbolHtmlAot() {
    doTest(inspections = listOf(AngularInaccessibleSymbolInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_13_3_5),
           configurators = listOf(Angular2TsConfigFile(strict = false)),
           files = listOf("private-html.html", "private-html.ts"))
  }

  fun testInaccessibleSymbolInlineAot() {
    doTest(inspections = listOf(AngularInaccessibleSymbolInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_13_3_5, TS_LIB),
           configurators = listOf(Angular2TsConfigFile(strict = false,)),
           files = listOf("private-inline.ts"))
  }

  fun testInaccessibleSymbolStrict() {
    doTest(inspections = listOf(AngularInaccessibleSymbolInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0, TS_LIB),
           configurators = listOf(Angular2TsConfigFile()),
           files = listOf("inaccessibleSymbolStrict.ts"))
  }

  fun testUnknownBlock1() {
    doTest(1,
           "foo@b<caret>ar.com",
           "Escape '@' with '&#",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("unknownBlock.html"))
  }

  fun testUnknownBlock2() {
    doTest(2,
           "something@f<caret>or.change.com",
           "Escape '@' with '&commat;'",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("unknownBlock.html"))
  }

  fun testUnknownBlock3() {
    doTest(3,
           "to @foo o<caret>n",
           "Escape '@' with '&#",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("unknownBlock.html"))
  }

  fun testUnknownBlock4() {
    doTest(4,
           "no @<caret> block name",
           "Escape '@' with '&#",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("unknownBlock.html"))
  }

  fun testUnknownBlockInjected() {
    doTest(1,
           "the @<caret>unknown block",
           "Escape '@' with '&#",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("unknownBlockInjected.ts"))
  }

  fun testBraceEscape() {
    doTest(1,
           "e {<caret> a",
           "Escape '{' with '&#123;'",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_16_2_8),
           files = listOf("braceEscape.html"))
  }

  fun testBraceEscapeNg17_1() {
    doTest(1,
           "e {<caret> a",
           "Escape '{' with '&#123;'",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("braceEscapeNg17.html"))
  }

  fun testBraceEscapeNg17_2() {
    doTest(2,
           "closing }<caret>",
           "Escape '",
           inspections = listOf(AngularIncorrectBlockUsageInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("braceEscapeNg17.html"))
  }

  fun testDeferBlockOnParameter() {
    doTest(inspections = listOf(AngularDeferBlockOnTriggerInspection::class.java),
           dependencies = listOf(ANGULAR_CORE_17_3_0),
           files = listOf("defer-block-on-trigger.html"))
  }

  private fun doTestNoFix(
    location: String,
    inspection: Class<out LocalInspectionTool?>?,
    quickFixName: String,
  ) {
    if (inspection != null) {
      myFixture.enableInspections(inspection)
    }
    myFixture.configureDependencies()
    myFixture.configureByFiles(location)
    myFixture.checkHighlighting()
    assertNull(myFixture.getAvailableIntention(quickFixName))
  }

  private fun doTest(
    inspection: Class<out LocalInspectionTool?>,
    vararg files: String,
  ) {
    doTest(1, null, null, inspection, *files)
  }

  private fun doTest(
    testNr: Int = 1,
    location: String? = null,
    quickFixName: String? = null,
    inspection: Class<out LocalInspectionTool?>,
    vararg files: String,
  ) {
    doTest(testNr, location, quickFixName, listOf(inspection), files = files.toList())
  }

  private fun doTest(
    testNr: Int = 1,
    location: String? = null,
    quickFixName: String? = null,
    inspections: List<Class<out LocalInspectionTool?>>,
    dependencies: List<Angular2TestModule> = listOf(ANGULAR_CORE_8_2_14),
    configurators: List<WebFrameworkTestConfigurator> = emptyList(),
    files: List<String>,
  ) {
    myFixture.enableInspections(inspections)
    doConfiguredTest(
      *dependencies.toTypedArray(),
      additionalFiles = files.drop(1),
      configureFileName = files[0],
      configurators = configurators,
    ) {
      myFixture.checkHighlighting()
      if (location == null || quickFixName == null) {
        return@doConfiguredTest
      }
      myFixture.moveToOffsetBySignature(location)
      myFixture.launchAction(myFixture.findSingleIntention(quickFixName))
      val lastDot = files[0].lastIndexOf('.')
      myFixture.checkResultByFile(files[0].substring(0, lastDot) + ".after" + testNr + files[0].substring(lastDot))
    }
  }
}
