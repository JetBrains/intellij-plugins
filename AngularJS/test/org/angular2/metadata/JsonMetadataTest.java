// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.metadata;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.json.psi.impl.JsonFileImpl;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.webSymbols.WebTestUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.angular2.entities.metadata.psi.Angular2MetadataReference;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.inspections.AngularAmbiguousComponentTagInspection;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angular2.inspections.AngularUndefinedTagInspection;
import org.angular2.lang.metadata.MetadataJsonFileViewProviderFactory;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.webSymbols.WebTestUtil.webSymbolAtCaret;
import static com.intellij.openapi.util.Pair.pair;
import static java.util.Arrays.asList;
import static org.angular2.modules.Angular2TestModule.*;
import static org.angularjs.AngularTestUtil.*;

public class JsonMetadataTest extends Angular2CodeInsightFixtureTestCase {

  @NotNull
  private PsiElement resolveToWebSymbolSourceContext(@NotNull String signature) {
    return WebTestUtil.resolveWebSymbolReference(myFixture, signature).getPsiContext();
  }

  @Override
  protected String getTestDataPath() {
    return getBaseTestDataPath(getClass()) + "/json";
  }

  public void testMetadataJsonFileTypeBinary() {
    VirtualFile vFile = myFixture.copyFileToProject("common.metadata.json");
    myFixture.configureByFiles("package.json", "common.d.ts");
    PsiFile file = myFixture.getPsiManager().findFile(vFile);
    assert file != null;
    assert file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider;
    assert file.getViewProvider().getAllFiles().get(0) instanceof MetadataFileImpl;
  }

  public void testMetadataJsonFileTypeNormal() {
    PsiFile file = myFixture.configureByFiles("common.metadata.json", "package.json")[0];
    assert file instanceof JsonFileImpl;
    assert file.getViewProvider() instanceof SingleRootFileViewProvider;
    assert !(file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider);
    assert file.getViewProvider().getAllFiles().get(0) instanceof JsonFileImpl;
  }

  public void testMetadataStubBuilding() {
    myFixture.configureByFiles("package.json", "ng-zorro-antd.d.ts");
    testMetadataStubBuilding("ng-zorro-antd.metadata.json");
  }

  public void testMetadataStubBuildingWithResolution() {
    configureWithMetadataFiles("ant-design-icons-angular");
    myFixture.configureByFiles("ng-zorro-antd.d.ts", "nz-icon.directive.d.ts", "icon.directive.d.ts",
                               "nz-col.component.d.ts", "nz-form-control.component.d.ts");
    testMetadataStubBuilding("ng-zorro-antd.metadata.json", "ng-zorro-antd.metadata.resolved.psi.txt");
  }

  public void testAgmCoreModuleStubBuilding() {
    myFixture.configureByFiles("@agm-core/core.module.d.ts", "package.json");
    testMetadataStubBuilding("@agm-core/core.module.metadata.json");
  }

  public void testFormsMetadataStubBuilding() {
    myFixture.configureByFiles("package.json", "forms.d.ts");
    testMetadataStubBuilding("forms.metadata.json");
  }

  public void testSyncFusionDropdownsMetadataStubBuilding() {
    myFixture.configureByFiles("@syncfusion-ej2-angular-dropdowns/ej2-angular-dropdowns.d.ts", "package.json");
    testMetadataStubBuilding("@syncfusion-ej2-angular-dropdowns/ej2-angular-dropdowns.metadata.json");
  }

  public void testNgxsLabsDispatchMetadataStubBuilding() {
    myFixture.configureByFiles("ngxs-labs-dispatch.d.ts", "package.json");
    testMetadataStubBuilding("ngxs-labs-dispatch.metadata.json");
  }

  public void testDirectiveAttributesMetadataStubBuilding() {
    myFixture.configureByFiles("test-ng-attr.d.ts", "package.json");
    testMetadataStubBuilding("test-ng-attr.metadata.json");
  }

  public void testSpreadOperatorMetadataStubBuilding() {
    myFixture.configureByFiles("spread-operator/evo-ui-kit.d.ts", "package.json");
    testMetadataStubBuilding("spread-operator/evo-ui-kit.metadata.json");
  }

  public void testTranslocoDirectiveMetadataStubBuilding() {
    myFixture.configureByFiles("transloco/ngneat-transloco.d.ts", "package.json");
    testMetadataStubBuilding("transloco/ngneat-transloco.metadata.json");
  }

  public void testJsonFileType() {
    PsiFile file = myFixture.configureByFile("package.json");
    assert file != null;
    assert file.getViewProvider() instanceof SingleRootFileViewProvider;
    assert !(file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider);
    assert file.getViewProvider().getAllFiles().get(0) instanceof JsonFileImpl;
  }

  public void testExtendsObfuscatedName() {
    configureWithMetadataFiles("ng-zorro-antd");
    myFixture.configureByFiles("inherited_properties.html", "nz-col.component.d.ts", "nz-form-control.component.d.ts");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testInterModuleExtends() {
    configureWithMetadataFiles("ng-zorro-antd", "ant-design-icons-angular");
    myFixture.configureByFiles("inter_module_props.html", "inter_module_props.ts", "extends-comp.ts", "nz-icon.directive.d.ts",
                               "icon.directive.d.ts");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testMetadataWithExportAliases() {
    myFixture.copyDirectoryToProject("node_modules/export-aliases", ".");
    myFixture.configureByFile("package.json");
    VirtualFile vFile = myFixture.getTempDirFixture().getFile("export.test.metadata.json");
    PsiFile file = myFixture.getPsiManager().findFile(vFile);
    assert file instanceof MetadataFileImpl;
    String result = DebugUtil.psiToString(file, true, false);
    UsefulTestCase
      .assertSameLinesWithFile(new File(getTestDataPath(), "node_modules/export-aliases/export.test.metadata.json.txt").toString(), result);
  }

  public void testMaterialMetadataResolution() {
    //Test component matching and indirect node module indexing
    configureCopy(myFixture, ANGULAR_MATERIAL_7_2_1, ANGULAR_COMMON_4_0_0);
    myFixture.enableInspections(AngularAmbiguousComponentTagInspection.class,
                                AngularUndefinedTagInspection.class);
    myFixture.configureByFile("material/module.ts");
    myFixture.checkHighlighting();
    moveToOffsetBySignature("mat-form<caret>-field", myFixture);
    assertEquals("form-field.d.ts",
                 webSymbolAtCaret(myFixture).getPsiContext().getContainingFile().getName());
  }

  public void testMaterialMetadataStubGeneration() {
    configureCopy(myFixture, ANGULAR_MATERIAL_7_2_1, ANGULAR_COMMON_4_0_0);
    VirtualFile materialDir = myFixture.getTempDirFixture().getFile("node_modules/@angular/material");
    String pathPrefix = materialDir.getPath();
    PsiDirectory material = myFixture.getPsiManager().findDirectory(materialDir);
    material.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile file) {
        if (file.getName().endsWith(".metadata.json")) {
          String relativeFile = FileUtil.getRelativePath(pathPrefix, file.getVirtualFile().getPath(), '/');
          assert file instanceof MetadataFileImpl : relativeFile;
          String result = DebugUtil.psiToString(file, true, false);
          UsefulTestCase.assertSameLinesWithFile(new File(getTestDataPath(), "material-stubs/" + relativeFile + ".txt").toString(), result);
        }
      }

      @Override
      public void visitDirectory(@NotNull PsiDirectory dir) {
        String relativeFile = FileUtil.getRelativePath(
          pathPrefix, dir.getVirtualFile().getPath(), '/');
        if (!"typings".equals(relativeFile)) {
          dir.acceptChildren(this);
        }
      }
    });
  }

  public void testIonicMetadataResolution() {
    configureCopy(myFixture, IONIC_ANGULAR_4_1_1);
    myFixture.copyDirectoryToProject("ionic", ".");
    myFixture.enableInspections(AngularAmbiguousComponentTagInspection.class,
                                AngularUndefinedTagInspection.class,
                                AngularUndefinedBindingInspection.class,
                                HtmlUnknownTagInspection.class,
                                HtmlUnknownAttributeInspection.class);
    myFixture.configureFromTempProjectFile("tab1.page.html");
    myFixture.checkHighlighting();
    moveToOffsetBySignature("ion-card-<caret>subtitle", myFixture);
    assertEquals("proxies.d.ts",
                 webSymbolAtCaret(myFixture).getPsiContext().getContainingFile().getName());
  }

  public void testFunctionPropertyMetadata() {
    myFixture.copyDirectoryToProject("function_property", ".");
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureFromTempProjectFile("template.html");
    myFixture.checkHighlighting();
    assertEquals("my-lib.component.d.ts",
                 WebTestUtil.webSymbolSourceAtCaret(myFixture).getContainingFile().getName());
  }

  public void testMultipleNodeModulesResolution() {
    myFixture.copyDirectoryToProject("multiple_node_modules", ".");
    PsiFile file =
      myFixture.getPsiManager().findFile(myFixture.getTempDirFixture().getFile("foo/node_modules/modules-test/test.metadata.json"));
    assert file != null;
    Angular2MetadataNodeModule nodeModule = (Angular2MetadataNodeModule)file.getFirstChild();
    assert nodeModule != null;

    for (Pair<String, String> check : asList(pair("Test1", "foo1.metadata.json"),
                                             pair("Test2", "root1.metadata.json"),
                                             pair("Test3", "bar1.metadata.json"))) {
      Angular2MetadataReference reference1 = (Angular2MetadataReference)nodeModule.findMember("Test1");
      assert reference1.resolve() != null : check;
      assertEquals(check.toString(), "foo1.metadata.json", reference1.resolve().getContainingFile().getName());
    }
    // Should resolve to lexically first file
    assertEquals("bar1.metadata.json", ((Angular2MetadataReference)nodeModule.findMember("Test4")).resolve().getContainingFile().getName());
  }

  public void testRouterLink() {
    configureWithMetadataFiles("routerLink");
    myFixture.configureByFiles("routerLink.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[routerLink]", "routerLink2");
  }

  public void testOneTimeBindingAttributeCompletion2JavaScriptPrimeButton() {
    configureWithMetadataFiles("primeButton");
    myFixture.configureByFiles("primeButton.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "icon", "iconPos", "label");
  }

  public void testTemplate20Metadata() {
    configureWithMetadataFiles("template");
    myFixture.configureByFiles("template.html");
    PsiElement resolve = resolveToWebSymbolSourceContext("*myHover<caret>List");
    assertEquals("template.metadata.json", resolve.getContainingFile().getName());
    assertUnresolvedReference("myHover<caret>List", myFixture);
  }

  public void testNoTemplate20Metadata() {
    configureWithMetadataFiles("noTemplate");
    myFixture.configureByFiles("noTemplate.html");
    PsiElement resolve = resolveToWebSymbolSourceContext("myHover<caret>List");
    assertEquals("noTemplate.metadata.json", resolve.getContainingFile().getName());
    assertUnresolvedReference("*myHover<caret>List", myFixture);
  }

  public void testTemplate20NoMetadata() {
    myFixture.configureByFiles("template.html", "package.json", "template.ts");
    PsiElement resolve = resolveToWebSymbolSourceContext("*myHover<caret>List");
    assertEquals("template.ts", resolve.getContainingFile().getName());
    assertUnresolvedReference("myHover<caret>List", myFixture);
  }

  public void testNoTemplate20NoMetadata() {
    myFixture.configureByFiles("noTemplate.html", "package.json", "noTemplate.ts");
    PsiElement resolve = resolveToWebSymbolSourceContext("myHover<caret>List");
    assertEquals("noTemplate.ts", resolve.getContainingFile().getName());
    assertUnresolvedReference("*myHover<caret>List", myFixture);
  }

  public void testSelectorListSpacesCompiled() {
    configureWithMetadataFiles("flexOrder");
    myFixture.configureByFiles("flexOrder.html");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[fxFlexOrder]");
  }

  public void testNoStandardJSEventsUnknownTag() {
    myFixture.configureByFiles("flexOrder.html", "package.json");
    myFixture.completeBasic();
    assertDoesntContain(myFixture.getLookupElementStrings(), "onclick", "onkeyup");
  }

  public void testVirtualInOuts() {
    configureWithMetadataFiles("ionic");
    myFixture.configureByFiles("div.html");
    myFixture.type("ion-item ");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "fakeInput", "[fakeInput]", "(fakeOutput)");
  }

  public void testTranslocoDirective() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.copyDirectoryToProject("transloco", ".");
    myFixture.configureByFile("package.json");
    myFixture.configureFromTempProjectFile("transloco.html");
    myFixture.checkHighlighting();
  }

  private void testMetadataStubBuilding(String metadataJson) {
    testMetadataStubBuilding(metadataJson, StringUtil.trimEnd(metadataJson, ".json") + ".psi.txt");
  }

  private void testMetadataStubBuilding(String metadataJson, String psiOutput) {
    VirtualFile vFile = myFixture.copyFileToProject(metadataJson);
    PsiFile file = myFixture.getPsiManager().findFile(vFile);
    assert file instanceof MetadataFileImpl;
    String result = DebugUtil.psiToString(file, true, false);
    UsefulTestCase.assertSameLinesWithFile(new File(getTestDataPath(), psiOutput).toString(), result);
  }

  private void configureWithMetadataFiles(String @NotNull ... names) {
    myFixture.configureByFiles("package.json");
    for (String name : names) {
      myFixture.configureByFiles(name + ".d.ts");
      myFixture.copyFileToProject(name + ".metadata.json");
    }
  }
}
