// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class MultiFileIntentionsTest extends Angular2MultiFileFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "intentions";
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/";
  }

  public void testBasicFieldCreation() {
    doMultiFileTest("Create Field 'foo'");
  }

  public void testThisQualifiedFieldCreation() {
    doMultiFileTest("Create Field 'foo'");
  }

  public void testQualifiedFieldCreation() {
    doMultiFileTest("Create Field 'foo'");
  }

  public void testBasicMethodCreation() {
    doMultiFileTest("Create Method 'foo'");
  }

  public void testThisQualifiedMethodCreation() {
    doMultiFileTest("Create Method 'foo'");
  }

  public void testQualifiedMethodCreation() {
    doMultiFileTest("Create Method 'foo'");
  }

  private void doMultiFileTest(String intentionHint) {
    doTest((rootDir, rootAfter) -> {
      myFixture.enableInspections(TypeScriptUnresolvedVariableInspection.class, TypeScriptUnresolvedFunctionInspection.class);
      myFixture.configureFromTempProjectFile("template.html");
      myFixture.setCaresAboutInjection(false);
      AngularTestUtil.moveToOffsetBySignature("f<caret>oo", myFixture);
      myFixture.launchAction(myFixture.findSingleIntention(intentionHint));
    });
  }
}
