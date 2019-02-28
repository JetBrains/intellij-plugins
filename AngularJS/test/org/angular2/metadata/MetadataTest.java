// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.metadata;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.json.psi.impl.JsonFileImpl;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.UsefulTestCase;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2BindingsInspection;
import org.angular2.lang.metadata.MetadataJsonFileViewProviderFactory;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.angularjs.AngularTestUtil;

import java.io.File;

public class MetadataTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
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
    VirtualFile vFile = myFixture.copyFileToProject("ng-zorro-antd.metadata.json");
    PsiFile file = myFixture.getPsiManager().findFile(vFile);
    assert file instanceof MetadataFileImpl;
    String result = DebugUtil.psiToString(file, false, false);
    UsefulTestCase.assertSameLinesWithFile(new File(getTestDataPath(), "ng-zorro-antd.metadata.psi.txt").toString(), result);
  }

  public void testMetadataStubBuildingWithResolution() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "ant-design-icons-angular");
      myFixture.configureByFiles("ng-zorro-antd.d.ts", "nz-icon.directive.d.ts", "icon.directive.ts",
                                 "nz-col.component.d.ts", "nz-form-control.component.d.ts");
      VirtualFile vFile = myFixture.copyFileToProject("ng-zorro-antd.metadata.json");
      PsiFile file = myFixture.getPsiManager().findFile(vFile);
      assert file instanceof MetadataFileImpl;
      String result = DebugUtil.psiToString(file, false, false);
      UsefulTestCase.assertSameLinesWithFile(new File(getTestDataPath(), "ng-zorro-antd.metadata.resolved.psi.txt").toString(), result);
    });
  }

  public void testJsonFileType() {
    PsiFile file = myFixture.configureByFile("package.json");
    assert file != null;
    assert file.getViewProvider() instanceof SingleRootFileViewProvider;
    assert !(file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider);
    assert file.getViewProvider().getAllFiles().get(0) instanceof JsonFileImpl;
  }

  public void testExtendsObfuscatedName() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "ng-zorro-antd");
      myFixture.configureByFiles("inherited_properties.html", "nz-col.component.d.ts", "nz-form-control.component.d.ts");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                  Angular2BindingsInspection.class);
      myFixture.checkHighlighting(true, false, true);
    });
  }

  public void testInterModuleExtends() {
    JSTestUtils.testES6(getProject(), () -> {
      AngularTestUtil.configureWithMetadataFiles(myFixture, "ng-zorro-antd", "ant-design-icons-angular");
      myFixture.configureByFiles("inter_module_props.html", "nz-icon.directive.d.ts", "icon.directive.ts");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                  Angular2BindingsInspection.class);
      myFixture.checkHighlighting(true, false, true);
    });
  }
}
