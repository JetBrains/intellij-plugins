// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

public class NgForTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngFor";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testNgFor() {
    JSTestUtils.testES6(getProject(), () -> {
      final List<String> variants = myFixture.getCompletionVariants("NgFor.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json");
      assertNotNull(variants);
      assertTrue(variants.size() >= 2);
      assertEquals("created_at", variants.get(0));
      assertEquals("email", variants.get(1));
    });
  }

  public void testNgForWithinAttribute() {
    JSTestUtils.testES6(getProject(), () -> {
      final List<String> variants = myFixture.getCompletionVariants("NgForWithinAttribute.ts", "ng_for_of.ts","iterable_differs.ts", "package.json");
      assertNotNull(variants);
      assertTrue(variants.size() >= 2);
      assertEquals("created_at", variants.get(0));
      assertEquals("email", variants.get(1));
    });
  }

  public void testNgForWithinAttributeHTML() {
    JSTestUtils.testES6(getProject(), () -> {
      final List<String> variants =
        myFixture.getCompletionVariants("NgForWithinAttributeHTML.html", "NgForWithinAttributeHTML.ts", "ng_for_of.ts","iterable_differs.ts", "package.json");
      assertNotNull(variants);
      assertTrue(variants.size() >= 2);
      assertEquals("created_at", variants.get(0));
      assertEquals("email", variants.get(1));
    });
  }

}
