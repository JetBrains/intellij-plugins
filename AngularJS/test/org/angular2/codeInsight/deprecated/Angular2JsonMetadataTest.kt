// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.json.psi.impl.JsonFileImpl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.*
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.resolveWebSymbolReference
import com.intellij.webSymbols.webSymbolAtCaret
import com.intellij.webSymbols.webSymbolSourceAtCaret
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureCopy
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule
import org.angular2.entities.metadata.psi.Angular2MetadataReference
import org.angular2.inspections.AngularAmbiguousComponentTagInspection
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.inspections.AngularUndefinedTagInspection
import org.angular2.lang.metadata.MetadataJsonFileViewProviderFactory.MetadataFileViewProvider
import org.angular2.lang.metadata.psi.MetadataFileImpl
import org.angularjs.AngularTestUtil
import java.io.File

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2JsonMetadataTest : Angular2CodeInsightFixtureTestCase() {
  private fun resolveToWebSymbolSourceContext(signature: String): PsiElement {
    return myFixture.resolveWebSymbolReference(signature).psiContext!!
  }

  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "deprecated/metadata-json"
  }

  fun testMetadataJsonFileTypeBinary() {
    val vFile = myFixture.copyFileToProject("common.metadata.json")
    myFixture.configureByFiles("package.json", "common.d.ts")
    val file = myFixture.getPsiManager().findFile(vFile)!!
    assert(file.getViewProvider() is MetadataFileViewProvider)
    assert(file.getViewProvider().getAllFiles()[0] is MetadataFileImpl)
  }

  fun testMetadataJsonFileTypeNormal() {
    val file = myFixture.configureByFiles("common.metadata.json", "package.json")[0]
    assert(file is JsonFileImpl)
    assert(file.getViewProvider() is SingleRootFileViewProvider)
    assert(file.getViewProvider() !is MetadataFileViewProvider)
    assert(file.getViewProvider().getAllFiles()[0] is JsonFileImpl)
  }

  fun testMetadataStubBuilding() {
    myFixture.configureByFiles("package.json", "ng-zorro-antd.d.ts")
    testMetadataStubBuilding("ng-zorro-antd.metadata.json")
  }

  fun testMetadataStubBuildingWithResolution() {
    configureWithMetadataFiles("ant-design-icons-angular")
    myFixture.configureByFiles("ng-zorro-antd.d.ts", "nz-icon.directive.d.ts", "icon.directive.d.ts",
                               "nz-col.component.d.ts", "nz-form-control.component.d.ts")
    testMetadataStubBuilding("ng-zorro-antd.metadata.json", "ng-zorro-antd.metadata.resolved.psi.txt")
  }

  fun testAgmCoreModuleStubBuilding() {
    myFixture.configureByFiles("@agm-core/core.module.d.ts", "package.json")
    testMetadataStubBuilding("@agm-core/core.module.metadata.json")
  }

  fun testFormsMetadataStubBuilding() {
    myFixture.configureByFiles("package.json", "forms.d.ts")
    testMetadataStubBuilding("forms.metadata.json")
  }

  fun testSyncFusionDropdownsMetadataStubBuilding() {
    myFixture.configureByFiles("@syncfusion-ej2-angular-dropdowns/ej2-angular-dropdowns.d.ts", "package.json")
    testMetadataStubBuilding("@syncfusion-ej2-angular-dropdowns/ej2-angular-dropdowns.metadata.json")
  }

  fun testNgxsLabsDispatchMetadataStubBuilding() {
    myFixture.configureByFiles("ngxs-labs-dispatch.d.ts", "package.json")
    testMetadataStubBuilding("ngxs-labs-dispatch.metadata.json")
  }

  fun testDirectiveAttributesMetadataStubBuilding() {
    myFixture.configureByFiles("test-ng-attr.d.ts", "package.json")
    testMetadataStubBuilding("test-ng-attr.metadata.json")
  }

  fun testSpreadOperatorMetadataStubBuilding() {
    myFixture.configureByFiles("spread-operator/evo-ui-kit.d.ts", "package.json")
    testMetadataStubBuilding("spread-operator/evo-ui-kit.metadata.json")
  }

  fun testTranslocoDirectiveMetadataStubBuilding() {
    myFixture.configureByFiles("transloco/ngneat-transloco.d.ts", "package.json")
    testMetadataStubBuilding("transloco/ngneat-transloco.metadata.json")
  }

  fun testJsonFileType() {
    val file = myFixture.configureByFile("package.json")!!
    assert(file.getViewProvider() is SingleRootFileViewProvider)
    assert(file.getViewProvider() !is MetadataFileViewProvider)
    assert(file.getViewProvider().getAllFiles()[0] is JsonFileImpl)
  }

  fun testExtendsObfuscatedName() {
    configureWithMetadataFiles("ng-zorro-antd")
    myFixture.configureByFiles("inherited_properties.html", "nz-col.component.d.ts", "nz-form-control.component.d.ts")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting(true, false, true)
  }

  fun testInterModuleExtends() {
    configureWithMetadataFiles("ng-zorro-antd", "ant-design-icons-angular")
    myFixture.configureByFiles("inter_module_props.html", "inter_module_props.ts", "extends-comp.ts", "nz-icon.directive.d.ts",
                               "icon.directive.d.ts")
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting(true, false, true)
  }

  fun testMetadataWithExportAliases() {
    myFixture.copyDirectoryToProject("node_modules/export-aliases", ".")
    myFixture.configureByFile("package.json")
    val vFile = myFixture.getTempDirFixture().getFile("export.test.metadata.json")
    val file = myFixture.getPsiManager().findFile(vFile!!)
    assert(file is MetadataFileImpl)
    val result = DebugUtil.psiToString(file!!, true, false)
    UsefulTestCase
      .assertSameLinesWithFile(File(testDataPath, "node_modules/export-aliases/export.test.metadata.json.txt").toString(), result)
  }

  fun testMaterialMetadataResolution() {
    //Test component matching and indirect node module indexing
    configureCopy(myFixture, Angular2TestModule.ANGULAR_MATERIAL_7_2_1, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.enableInspections(AngularAmbiguousComponentTagInspection::class.java,
                                AngularUndefinedTagInspection::class.java)
    myFixture.configureByFile("material/module.ts")
    myFixture.checkHighlighting()
    myFixture.moveToOffsetBySignature("mat-form<caret>-field")
    assertEquals("form-field.d.ts",
                 myFixture.webSymbolAtCaret()!!.psiContext!!.getContainingFile().getName())
  }

  fun testMaterialMetadataStubGeneration() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_MATERIAL_7_2_1, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    val materialDir = myFixture.getTempDirFixture().getFile("node_modules/@angular/material")
    val pathPrefix = materialDir!!.getPath()
    val material = myFixture.getPsiManager().findDirectory(materialDir)
    material!!.acceptChildren(object : PsiElementVisitor() {
      override fun visitFile(file: PsiFile) {
        if (file.getName().endsWith(".metadata.json")) {
          val relativeFile = FileUtil.getRelativePath(pathPrefix, file.getVirtualFile().getPath(), '/')
          assert(file is MetadataFileImpl) { relativeFile!! }
          val result = DebugUtil.psiToString(file, true, false)
          UsefulTestCase.assertSameLinesWithFile(File(testDataPath, "material-stubs/$relativeFile.txt").toString(), result)
        }
      }

      override fun visitDirectory(dir: PsiDirectory) {
        val relativeFile = FileUtil.getRelativePath(
          pathPrefix, dir.getVirtualFile().getPath(), '/')
        if ("typings" != relativeFile) {
          dir.acceptChildren(this)
        }
      }
    })
  }

  fun testIonicMetadataResolution() {
    configureCopy(myFixture, Angular2TestModule.IONIC_ANGULAR_4_1_1)
    myFixture.copyDirectoryToProject("ionic", ".")
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

  fun testMultipleNodeModulesResolution() {
    myFixture.copyDirectoryToProject("multiple_node_modules", ".")
    val file = myFixture.getPsiManager().findFile(
      myFixture.getTempDirFixture().getFile("foo/node_modules/modules-test/test.metadata.json")!!)!!
    val nodeModule = (file.getFirstChild() as Angular2MetadataNodeModule)
    for (check in listOf(Pair("Test1", "foo1.metadata.json"),
                         Pair("Test2", "root1.metadata.json"),
                         Pair("Test3", "bar1.metadata.json"))) {
      val reference1 = nodeModule.findMember("Test1") as Angular2MetadataReference?
      assert(reference1!!.resolve() != null) { check }
      assertEquals(check.toString(), "foo1.metadata.json", reference1.resolve()!!.getContainingFile().getName())
    }
    // Should resolve to lexically first file
    assertEquals("bar1.metadata.json",
                 (nodeModule.findMember("Test4") as Angular2MetadataReference?)!!.resolve()!!.getContainingFile().getName())
  }

  fun testRouterLink() {
    configureWithMetadataFiles("routerLink")
    myFixture.configureByFiles("routerLink.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[routerLink]", "routerLink2")
  }

  fun testOneTimeBindingAttributeCompletion2JavaScriptPrimeButton() {
    configureWithMetadataFiles("primeButton")
    myFixture.configureByFiles("primeButton.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "icon", "iconPos", "label")
  }

  fun testTemplate20Metadata() {
    configureWithMetadataFiles("template")
    myFixture.configureByFiles("template.html")
    val resolve = resolveToWebSymbolSourceContext("*myHover<caret>List")
    assertEquals("template.metadata.json", resolve.getContainingFile().getName())
    AngularTestUtil.assertUnresolvedReference("myHover<caret>List", myFixture)
  }

  fun testNoTemplate20Metadata() {
    configureWithMetadataFiles("noTemplate")
    myFixture.configureByFiles("noTemplate.html")
    val resolve = resolveToWebSymbolSourceContext("myHover<caret>List")
    assertEquals("noTemplate.metadata.json", resolve.getContainingFile().getName())
    AngularTestUtil.assertUnresolvedReference("*myHover<caret>List", myFixture)
  }

  fun testTemplate20NoMetadata() {
    myFixture.configureByFiles("template.html", "package.json", "template.ts")
    val resolve = resolveToWebSymbolSourceContext("*myHover<caret>List")
    assertEquals("template.ts", resolve.getContainingFile().getName())
    AngularTestUtil.assertUnresolvedReference("myHover<caret>List", myFixture)
  }

  fun testNoTemplate20NoMetadata() {
    myFixture.configureByFiles("noTemplate.html", "package.json", "noTemplate.ts")
    val resolve = resolveToWebSymbolSourceContext("myHover<caret>List")
    assertEquals("noTemplate.ts", resolve.getContainingFile().getName())
    AngularTestUtil.assertUnresolvedReference("*myHover<caret>List", myFixture)
  }

  fun testSelectorListSpacesCompiled() {
    configureWithMetadataFiles("flexOrder")
    myFixture.configureByFiles("flexOrder.html")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[fxFlexOrder]")
  }

  fun testNoStandardJSEventsUnknownTag() {
    myFixture.configureByFiles("flexOrder.html", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "onclick", "onkeyup")
  }

  fun testVirtualInOuts() {
    configureWithMetadataFiles("ionic")
    myFixture.configureByFiles("div.html")
    myFixture.type("ion-item ")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "fakeInput", "[fakeInput]", "(fakeOutput)")
  }

  fun testTranslocoDirective() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.copyDirectoryToProject("transloco", ".")
    myFixture.configureByFile("package.json")
    myFixture.configureFromTempProjectFile("transloco.html")
    myFixture.checkHighlighting()
  }

  private fun testMetadataStubBuilding(metadataJson: String, psiOutput: String = metadataJson.removeSuffix(".json") + ".psi.txt") {
    val vFile = myFixture.copyFileToProject(metadataJson)
    val file = myFixture.getPsiManager().findFile(vFile)
    assert(file is MetadataFileImpl)
    val result = DebugUtil.psiToString(file!!, true, false)
    UsefulTestCase.assertSameLinesWithFile(File(testDataPath, psiOutput).toString(), result)
  }

  private fun configureWithMetadataFiles(vararg names: String) {
    myFixture.configureByFiles("package.json")
    for (name in names) {
      myFixture.configureByFiles("$name.d.ts")
      myFixture.copyFileToProject("$name.metadata.json")
    }
  }
}
