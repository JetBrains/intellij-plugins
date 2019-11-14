// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.metadata;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.AngularAmbiguousComponentTagInspection;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angular2.inspections.AngularUndefinedTagInspection;
import org.angularjs.AngularTestUtil;

public class IvyMetadataTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) +"/ivy";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AngularTestUtil.enableIvyMetadataSupport(this);
  }

  public void testIonicMetadataResolution() {
    myFixture.copyDirectoryToProject("@ionic", ".");
    myFixture.enableInspections(AngularAmbiguousComponentTagInspection.class,
                                AngularUndefinedTagInspection.class,
                                AngularUndefinedBindingInspection.class,
                                HtmlUnknownTagInspection.class,
                                HtmlUnknownAttributeInspection.class);
    myFixture.configureFromTempProjectFile("tab1.page.html");
    myFixture.checkHighlighting();
    AngularTestUtil.moveToOffsetBySignature("ion-card-<caret>subtitle", myFixture);
    assertEquals("proxies.d.ts",
                 myFixture.getElementAtCaret().getContainingFile().getName());
  }
}
