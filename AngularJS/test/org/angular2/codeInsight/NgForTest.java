// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angularjs.AngularTestUtil;

import java.util.List;

public class NgForTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngFor";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
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
}
