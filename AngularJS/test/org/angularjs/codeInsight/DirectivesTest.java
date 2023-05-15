// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.lang.javascript.inspections.JSUndeclaredVariableInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;

public class DirectivesTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "directives";
  }

  public void testBindToControllerAttrs() {
    enableInspections();
    myFixture.configureByFiles("bind-to-controller.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testBindToControllerProps1() {
    enableInspections();
    myFixture.configureByFiles("bind-to-controller-1.html", "bind-to-controller.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testBindToControllerProps2() {
    enableInspections();
    myFixture.configureByFiles("bind-to-controller-2.html", "bind-to-controller.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testHighlighting() {
    enableInspections();
    myFixture.configureByFiles("create-titles-ids-injected.html", "create-titles-ids.js", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  private void enableInspections() {
    myFixture.enableInspections(JSUndeclaredVariableInspection.class,
                                JSUnresolvedReferenceInspection.class,
                                JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
  }
}
