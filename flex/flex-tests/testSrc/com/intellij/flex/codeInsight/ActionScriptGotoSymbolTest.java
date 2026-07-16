// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.lang.javascript.JSGotoByNameTestBase;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.navigation.NavigationItem;
import com.intellij.testFramework.TestFrameworkUtil;
import com.intellij.testFramework.TestIndexingModeSupporter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("NewClassNamingConvention")
public class ActionScriptGotoSymbolTest extends JSGotoByNameTestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
  }

  public static @NotNull Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ActionScriptGotoSymbolTest.class);
    TestIndexingModeSupporter.addTest(ActionScriptGotoSymbolTest.class, new TestIndexingModeSupporter.FullIndexSuite(), suite);
    return TestFrameworkUtil.flattenSuite(suite);
  }

  public void testAsSymbol() {
    myFixture.addFileToProject("com/test/Foo.as", "package com.test { public class Foo { public var someVar:int; public function someFunc(){} } }");
    checkGotoSymbol("someVar", true);
    checkGotoSymbol("someFunc", true);
  }

  private NavigationItem checkGotoSymbol(@NotNull String symbolName, boolean exists) {
    GotoSymbolModel2 model = new GotoSymbolModel2(myFixture.getProject());
    return doCheckGoto(model, symbolName, exists);
  }
}
