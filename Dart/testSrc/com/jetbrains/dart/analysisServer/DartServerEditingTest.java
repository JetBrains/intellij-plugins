// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartServerEditingTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  // fails because of https://github.com/dart-lang/sdk/issues/31456
  public void testInsertImportsOnPaste() {
    myFixture.configureByText("foo.dart", "import 'dart:math';\n" +
                                          "main() {\n" +
                                          "  new Random();<caret>\n" +
                                          "}");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY);
    myFixture.configureByText("bar.dart", "main() {\n" +
                                          "<caret>}");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    myFixture.checkResult("import 'dart:math';\n\n" +
                          "main() {\n" +
                          "  new Random();\n" +
                          "<caret>}");
  }
}
