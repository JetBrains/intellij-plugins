// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class Angular2InaccessibleMemberAotQuickFixesTest extends Angular2MultiFileFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "aot";
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/";
  }

  public void testPrivateFieldFix() {
    doMultiFileTest("private.html", "private<caret>Used");
  }

  public void testPrivateFieldInlineFix() {
    doMultiFileTest("private-inline.ts", "private<caret>Used");
  }

  public void testPrivateGetterFix() {
    doMultiFileTest("private.html", "private<caret>UsedGet");
  }

  public void testProtectedMethodFix() {
    doMultiFileTest("private.html", "protected<caret>UsedFun");
  }

  public void testProtectedSetterFix() {
    doMultiFileTest("private.html", "protected<caret>UsedSet");
  }

  public void testPrivateConstructorFieldFix() {
    doMultiFileTest("private.ts", "private<caret>Field");
  }

  private void doMultiFileTest(String fileName, @NotNull String signature) {
    doTest((rootDir, rootAfter) -> {
      myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection.class);
      myFixture.configureFromTempProjectFile(fileName);
      myFixture.setCaresAboutInjection(false);
      AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
      myFixture.launchAction(myFixture.findSingleIntention("Make public"));
    });
  }
}
