// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionActionDelegate;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angular2.inspections.quickfixes.AngularMakePublicQuickFix;
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

  public void testPrivateConstructorFieldFix() {
    doMultiFileTest("private.ts", "private<caret>Field");
  }

  public void testPrivateConstructorDecoratedFieldFix() {
    doMultiFileTest("private.ts", "private<caret>Field");
  }

  public void testPrivateConstructorDecoratedFieldFix2() {
    doMultiFileTest("private.html", "private<caret>Field");
  }

  private void doMultiFileTest(String fileName, @NotNull String signature) {
    doTest((rootDir, rootAfter) -> {
      myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection.class);
      myFixture.configureFromTempProjectFile(fileName);
      myFixture.setCaresAboutInjection(false);
      AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
      IntentionAction intentionAction = ContainerUtil.find(
        myFixture.filterAvailableIntentions("Make 'public'"),
        intention -> IntentionActionDelegate.unwrap(intention) instanceof AngularMakePublicQuickFix
      );
      assert intentionAction != null;
      myFixture.launchAction(intentionAction);
    });
  }
}
