// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.modules.Angular2TestModule;
import org.angularjs.AngularTestUtil;

import java.util.List;

import static org.angularjs.AngularTestUtil.renderLookupItems;

public class NgForTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngFor";
  }

  public void testNgFor() {
    final List<String> variants = myFixture.getCompletionVariants("NgFor.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json");
    assertNotNull(variants);
    assertSameElements(variants, "isPrototypeOf", "propertyIsEnumerable", "valueOf", "is_hidden", "constructor", "created_at",
                       "hasOwnProperty", "updated_at", "toString", "email", "username", "toLocaleString");
  }

  public void testNgForInspections() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("NgForInspections.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testNgForWithinAttribute() {
    final List<String> variants = myFixture.getCompletionVariants(
      "NgForWithinAttribute.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("created_at", variants.get(0));
    assertEquals("email", variants.get(1));
  }

  public void testNgForWithinAttributeHTML() {
    final List<String> variants = myFixture.getCompletionVariants(
      "NgForWithinAttributeHTML.html", "NgForWithinAttributeHTML.ts", "ng_for_of.ts", "iterable_differs.ts",
      "package.json");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("created_at", variants.get(0));
    assertEquals("email", variants.get(1));
  }

  public void testNgForWithPipe() { // WEB-51209
    myFixture.enableInspections(JSDaemonAnalyzerLightTestCase.configureDefaultLocalInspectionTools().toArray(LocalInspectionTool[]::new));
    myFixture.configureByFiles("NgForWithPipeHTML.html", "NgForWithPipe.ts", "package.json");
    Angular2TestModule.configure(myFixture, false, null, Angular2TestModule.ANGULAR_CORE_13_3_5, Angular2TestModule.ANGULAR_COMMON_13_3_5);
    myFixture.checkHighlighting();
    myFixture.type('.');
    myFixture.complete(CompletionType.BASIC);
    assertOrderedEquals(ContainerUtil.getFirstItems(renderLookupItems(myFixture, false, true, true), 2),
                        "key#string", "value#null");
  }
}
