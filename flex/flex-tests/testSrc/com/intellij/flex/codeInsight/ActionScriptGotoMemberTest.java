// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.ide.util.gotoByName.LanguageRef;
import com.intellij.lang.javascript.JSGotoByNameTestBase;
import com.intellij.navigation.NavigationItem;
import com.intellij.testFramework.NeedsIndex;
import com.intellij.testFramework.TestFrameworkUtil;
import com.intellij.testFramework.TestIndexingModeSupporter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("NewClassNamingConvention")
public class ActionScriptGotoMemberTest extends JSGotoByNameTestBase {

  public static @NotNull Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ActionScriptGotoMemberTest.class);
    TestIndexingModeSupporter.addTest(ActionScriptGotoMemberTest.class, new TestIndexingModeSupporter.FullIndexSuite(), suite);
    return TestFrameworkUtil.flattenSuite(suite);
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/goto");
  }

  @NeedsIndex.SmartMode(reason = "MXML is in a maintenance mode")
  public void testMxmlMembers() {
    myFixture.configureByFile(getTestName(false) + ".mxml");
    checkGotoSymbol("myField1", true);
    checkGotoSymbol("myField2", true);
    checkGotoSymbol("field3", false);
    checkGotoSymbol("method1", true);
    checkGotoSymbol("myXml", true);
    checkGotoSymbol("myXmlItem", false);
    checkGotoSymbol("myXmlInnerItem", false);
    checkGotoSymbol("myXmlList", true);
    checkGotoSymbol("myXmlListItem", false);
    checkGotoSymbol("myModel", true);
    checkGotoSymbol("myModelItem", false);
    checkGotoSymbol("myWebService", true);
    checkGotoSymbol("myOperation", false);
    checkGotoSymbol("myHttpService", true);
    checkGotoSymbol("myGroup", true);
    checkGotoSymbol("myLabel", true);
    checkGotoSymbol("myPrivate", false);
    checkGotoSymbol("MxmlMembers", true);
    checkGotoSymbol("", false);
  }

  private @Nullable NavigationItem checkGotoSymbol(String name, boolean shouldExist) {
    GotoSymbolModel2 model = new GotoSymbolModel2(myFixture.getProject(), myFixture.getTestRootDisposable());
    model.setFilterItems(LanguageRef.forAllLanguages());
    return doCheckGoto(model, name, shouldExist);
  }
}
