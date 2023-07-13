// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection
import com.intellij.testFramework.UsefulTestCase
import com.intellij.webSymbols.resolveWebSymbolReference
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angularjs.AngularTestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2TagsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "deprecated/tags"
  }

  fun testCustomTagsCompletion20TypeScript() {
    myFixture.testCompletion("custom.html", "custom.after.html", "package.json", "custom.ts")
  }

  fun testCustomTagsCompletion20NoNormalize() {
    myFixture.testCompletion(
      "custom_no_normalize.html", "custom_no_normalize.after.html", "package.json",
      "custom_no_normalize.ts")
  }

  fun testCustomTagsResolve20TypeScriptComponent() {
    myFixture.configureByFiles("custom.after.html", "package.json", "custom.ts")
    val resolve = myFixture.resolveWebSymbolReference("my-cus<caret>tomer").psiContext
    assertNotNull(resolve)
    assertEquals("custom.ts", resolve!!.getContainingFile().getName())
    assertEquals("""
                   @Component({
                       selector: 'my-customer',
                       properties: {
                           'id':'dependency'
                       }
                   })
                   """.trimIndent(), AngularTestUtil.getDirectiveDefinitionText(resolve))
  }

  fun testCustomTagsResolve20TypeScriptDirective() {
    myFixture.configureByFiles("custom.after.html", "package.json", "custom_directive.ts")
    val resolve = myFixture.resolveWebSymbolReference("my-cus<caret>tomer").psiContext
    assertNotNull(resolve)
    assertEquals("custom_directive.ts", resolve!!.getContainingFile().getName())
    assertEquals("""
                   @Directive({
                       selector: 'my-customer',
                       properties: {
                           'id':'dependency'
                       }
                   })
                   """.trimIndent(), AngularTestUtil.getDirectiveDefinitionText(resolve))
  }

  fun testDeepPseudoSelector() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection::class.java)
    myFixture.testHighlighting("deepPseudoSelector.html", "package.json")
  }

  fun testCustomSpaceSeparated() {
    val variants = myFixture.getCompletionVariants(
      "customSpaceSeparated.html", "customSpaceSeparated.ts", "package.json")!!
    UsefulTestCase.assertContainsElements(variants, "dummy-list", "dummy-nav-list")
  }

  fun testInlineTemplateHtmlTags() {
    val variants = myFixture.getCompletionVariants("inline_template.ts", "package.json")!!
    UsefulTestCase.assertContainsElements(variants, "a", "img", "my-customer")
  }

  fun testNgContainerCompletion20() {
    myFixture.testCompletion("ngContainer.html", "ngContainer.after.html", "package.json")
  }

  fun testNgContainerResolve20() {
    myFixture.configureByFiles("ngContainer.after.html", "package.json")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>container", myFixture.getFile())
    val ref = myFixture.getFile().findReferenceAt(offsetBySignature)
    assertNotNull(ref)
    val resolve = ref!!.resolve()
    assertNotNull(resolve)
    assertEquals("ngContainer.after.html", resolve!!.getContainingFile().getName())
    assertEquals("<ng-container", AngularTestUtil.getDirectiveDefinitionText(resolve))
  }

  fun testNgTemplateCompletion20() {
    myFixture.testCompletion("ngTemplate.html", "ngTemplate.after.html", "package.json")
  }

  fun testNgTemplateResolve20() {
    myFixture.configureByFiles("ngTemplate.after.html", "package.json")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>template", myFixture.getFile())
    val ref = myFixture.getFile().findReferenceAt(offsetBySignature)
    assertNotNull(ref)
    val resolve = ref!!.resolve()
    assertNotNull(resolve)
    assertEquals("ngTemplate.after.html", resolve!!.getContainingFile().getName())
    assertEquals("<ng-template", AngularTestUtil.getDirectiveDefinitionText(resolve))
  }

  fun testNgContentCompletion20() {
    myFixture.testCompletion("ngContent.html", "ngContent.after.html", "package.json")
  }

  fun testNgContentResolve20() {
    myFixture.configureByFiles("ngContent.after.html", "package.json")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-<caret>content", myFixture.getFile())
    val ref = myFixture.getFile().findReferenceAt(offsetBySignature)
    assertNotNull(ref)
    val resolve = ref!!.resolve()
    assertNotNull(resolve)
    assertEquals("ngContent.after.html", resolve!!.getContainingFile().getName())
    assertEquals("<ng-content", AngularTestUtil.getDirectiveDefinitionText(resolve))
  }

  fun testNoNormalizedResolve20() {
    myFixture.configureByFiles("noNormalized.ts", "package.json")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("app_<caret>hello", myFixture.getFile())
    val ref = myFixture.getFile().findReferenceAt(offsetBySignature)
    assertNotNull(ref)
    val resolve = ref!!.resolve()
    assertNull(resolve)
  }

  fun testTagClassTypes() {
    myFixture.enableInspections(TypeScriptValidateTypesInspection::class.java)
    myFixture.configureByFiles("tagClassTypes.ts", "package.json")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testHtmlWithDoctype() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java,
                                HtmlUnknownTagInspection::class.java)
    myFixture.configureByFiles("withDoctype.html", "package.json")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testComponentNavigation() {
    myFixture.copyDirectoryToProject("component-navigation", ".")
    for (i in 1..4) {
      myFixture.configureFromTempProjectFile("app$i/app/app.component.html")
      val filePath = myFixture.resolveWebSymbolReference("<app-<caret>test>").psiContext!!.getContainingFile()
        .getVirtualFile()
        .getPath()
      assertTrue("$filePath should have $i", filePath.endsWith("/app$i/app/test.component.ts"))
    }
  }

  fun testSelfClosedTags() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.copyDirectoryToProject("selfClosedTags", ".")
    myFixture.configureFromTempProjectFile("app.component.html")
    myFixture.checkHighlighting()
  }
}
