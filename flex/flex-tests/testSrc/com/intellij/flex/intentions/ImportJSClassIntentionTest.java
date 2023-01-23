// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.intentions;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import com.intellij.lang.javascript.flex.ActionScriptAutoImportOptionsProvider;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class ImportJSClassIntentionTest extends BaseJSIntentionTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new JSUnresolvedReferenceInspection());
  }

  public void testImportClass() {
    doTestFor("Test1.as", "FooClass.as");
  }

  public void testImportComponent() {
    doTestForAndCheckFirstFile("Test2.as", "aPackage/FooComponent.mxml");
  }

  public void testPackageLocal() {
    doTestFor(getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testUnambiguousImportsOnTheFly() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    boolean oldHintsEnabled = CodeInsightSettings.getInstance().ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY;

    CodeInsightSettings.getInstance().ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = false;
    boolean old = ActionScriptAutoImportOptionsProvider.isAddUnambiguousImportsOnTheFly();
    ActionScriptAutoImportOptionsProvider.setAddUnambiguousImportsOnTheFly(true);
    try {
      DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(true);

      final String testName = getTestName(false);
      myFixture.configureByFiles(testName + ".as", testName + "_2.as");
      myFixture.type(" ");
      myFixture.type("\b");
      myFixture.doHighlighting();
      CodeInsightTestFixtureImpl.waitForUnresolvedReferencesQuickFixesUnderCaret(myFixture.getFile(), myFixture.getEditor());
      UIUtil.dispatchAllInvocationEvents();
      myFixture.checkResultByFile(testName + "_after.as");
    }
    finally {
      ActionScriptAutoImportOptionsProvider.setAddUnambiguousImportsOnTheFly(old);
      CodeInsightSettings.getInstance().ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = oldHintsEnabled;
    }
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/importclass";
  }
}