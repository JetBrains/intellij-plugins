// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.metadata;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.lang.javascript.TypeScriptTestUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.inspections.AngularAmbiguousComponentTagInspection;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angular2.inspections.AngularUndefinedTagInspection;
import org.angularjs.AngularTestUtil;

import java.util.List;

import static com.intellij.webSymbols.WebTestUtil.webSymbolAtCaret;
import static com.intellij.webSymbols.WebTestUtil.webSymbolSourceAtCaret;
import static org.angular2.modules.Angular2TestModule.*;

public class IvyMetadataTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "/ivy";
  }

  public void testInterModuleExtends() {
    configureCopy(myFixture, NG_ZORRO_ANTD_8_5_0_IVY);
    myFixture.copyDirectoryToProject("ng-zorro", ".");
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.configureFromTempProjectFile("inter_module_props.html");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testMixedMetadataResolution() {
    TypeScriptTestUtil.forceDefaultTsConfig(getProject(), getTestRootDisposable());
    //Test component matching, abstract class in hierarchy and indirect node module indexing
    myFixture.copyDirectoryToProject("material", ".");
    configureCopy(myFixture, ANGULAR_CORE_9_1_1_MIXED, ANGULAR_MATERIAL_8_2_3_MIXED);
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureFromTempProjectFile("module.ts");
    myFixture.checkHighlighting();
    AngularTestUtil.moveToOffsetBySignature("mat-form<caret>-field", myFixture);
    assertEquals("form-field.d.ts",
                 webSymbolAtCaret(myFixture).getPsiContext().getContainingFile().getName());
    AngularTestUtil.moveToOffsetBySignature("mat-tab<caret>-group", myFixture);
    assertEquals("tab-group.d.ts",
                 webSymbolAtCaret(myFixture).getPsiContext().getContainingFile().getName());
  }


  public void testIonicMetadataResolution() {
    myFixture.copyDirectoryToProject("@ionic", ".");
    configureCopy(myFixture, IONIC_ANGULAR_4_11_4_IVY);
    myFixture.enableInspections(AngularAmbiguousComponentTagInspection.class,
                                AngularUndefinedTagInspection.class,
                                AngularUndefinedBindingInspection.class,
                                HtmlUnknownTagInspection.class,
                                HtmlUnknownAttributeInspection.class);
    myFixture.configureFromTempProjectFile("tab1.page.html");
    myFixture.checkHighlighting();
    AngularTestUtil.moveToOffsetBySignature("ion-card-<caret>subtitle", myFixture);
    assertEquals("proxies.d.ts",
                 webSymbolAtCaret(myFixture).getPsiContext().getContainingFile().getName());
  }

  public void testFunctionPropertyMetadata() {
    myFixture.copyDirectoryToProject("function_property", ".");
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureFromTempProjectFile("template.html");
    myFixture.checkHighlighting();
    assertEquals("my-lib.component.d.ts",
                 webSymbolSourceAtCaret(myFixture).getContainingFile().getName());
  }

  public void testPriority() {
    myFixture.copyDirectoryToProject("priority", ".");
    myFixture.configureFromTempProjectFile("template.html");
    myFixture.completeBasic();
    assertEquals(List.of("comp-ivy-bar", "comp-ivy-foo", "comp-meta-bar"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()));
  }

  public void testTransloco() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.copyDirectoryToProject("transloco", ".");
    configureLink(myFixture, NGNEAT_TRANSLOCO_2_6_0_IVY);
    myFixture.configureFromTempProjectFile("transloco.html");
    myFixture.checkHighlighting();
  }

  public void testPureIvyConstructorAttribute() {
    myFixture.copyDirectoryToProject("pure-attr-support", ".");
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureFromTempProjectFile("template.html");
    myFixture.checkHighlighting();
  }

  public void testStandaloneDeclarables() {
    myFixture.copyDirectoryToProject("standalone-declarables", ".");
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureFromTempProjectFile("app.component.ts");
    myFixture.checkHighlighting();
  }
}
