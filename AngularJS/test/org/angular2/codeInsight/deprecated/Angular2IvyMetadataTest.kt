// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.webSymbolAtCaret
import com.intellij.webSymbols.webSymbolSourceAtCaret
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureCopy
import org.angular2.Angular2TestModule.Companion.configureLink
import org.angular2.inspections.AngularAmbiguousComponentTagInspection
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.inspections.AngularUndefinedTagInspection
import org.angularjs.AngularTestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2IvyMetadataTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "deprecated/metadata-ivy"
  }

  fun testInterModuleExtends() {
    configureCopy(myFixture, Angular2TestModule.NG_ZORRO_ANTD_8_5_0_IVY)
    myFixture.copyDirectoryToProject("ng-zorro", ".")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.configureFromTempProjectFile("inter_module_props.html")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testMixedMetadataResolution() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    //Test component matching, abstract class in hierarchy and indirect node module indexing
    myFixture.copyDirectoryToProject("material", ".")
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_9_1_1_MIXED, Angular2TestModule.ANGULAR_MATERIAL_8_2_3_MIXED)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureFromTempProjectFile("module.ts")
    myFixture.checkHighlighting()
    myFixture.moveToOffsetBySignature("mat-form<caret>-field")
    assertEquals("form-field.d.ts",
                 myFixture.webSymbolAtCaret()!!.psiContext!!.getContainingFile().getName())
    myFixture.moveToOffsetBySignature("mat-tab<caret>-group")
    assertEquals("tab-group.d.ts",
                 myFixture.webSymbolAtCaret()!!.psiContext!!.getContainingFile().getName())
  }

  fun testIonicMetadataResolution() {
    myFixture.copyDirectoryToProject("@ionic", ".")
    configureCopy(myFixture, Angular2TestModule.IONIC_ANGULAR_4_11_4_IVY)
    myFixture.enableInspections(AngularAmbiguousComponentTagInspection::class.java,
                                AngularUndefinedTagInspection::class.java,
                                AngularUndefinedBindingInspection::class.java,
                                HtmlUnknownTagInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java)
    myFixture.configureFromTempProjectFile("tab1.page.html")
    myFixture.checkHighlighting()
    myFixture.moveToOffsetBySignature("ion-card-<caret>subtitle")
    assertEquals("proxies.d.ts",
                 myFixture.webSymbolAtCaret()!!.psiContext!!.getContainingFile().getName())
  }

  fun testFunctionPropertyMetadata() {
    myFixture.copyDirectoryToProject("function_property", ".")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureFromTempProjectFile("template.html")
    myFixture.checkHighlighting()
    assertEquals("my-lib.component.d.ts",
                 myFixture.webSymbolSourceAtCaret()!!.getContainingFile().getName())
  }

  fun testPriority() {
    myFixture.copyDirectoryToProject("priority", ".")
    myFixture.configureFromTempProjectFile("template.html")
    myFixture.completeBasic()
    assertEquals(listOf("comp-ivy-bar", "comp-ivy-foo", "comp-meta-bar"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
  }

  fun testTransloco() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.copyDirectoryToProject("transloco", ".")
    configureLink(myFixture, Angular2TestModule.NGNEAT_TRANSLOCO_2_6_0_IVY)
    myFixture.configureFromTempProjectFile("transloco.html")
    myFixture.checkHighlighting()
  }

  fun testPureIvyConstructorAttribute() {
    myFixture.copyDirectoryToProject("pure-attr-support", ".")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureFromTempProjectFile("template.html")
    myFixture.checkHighlighting()
  }

  fun testStandaloneDeclarables() {
    myFixture.copyDirectoryToProject("standalone-declarables", ".")
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureFromTempProjectFile("app.component.ts")
    myFixture.checkHighlighting()
  }
}
