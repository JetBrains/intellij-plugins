// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.modules.Angular2TestModule;
import org.angularjs.AngularTestUtil;

import java.util.List;

public class NgTemplateLetTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngTemplateLet";
  }

  public void testNgFor() {
    Angular2TestModule.configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14);
    final List<String> variants = myFixture.getCompletionVariants("NgFor.ts");
    assertNotNull(variants);
    assertSameElements(variants, "isPrototypeOf", "propertyIsEnumerable", "valueOf", "is_hidden", "constructor", "created_at",
                       "hasOwnProperty", "updated_at", "toString", "email", "username", "toLocaleString");
  }

  public void testNgForInspections() {
    Angular2TestModule.configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14);
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("NgForInspections.ts");
    myFixture.checkHighlighting();
  }
}
