// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.ide.util.gotoByName.LanguageRef;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSGotoByNameFixture;
import com.intellij.lang.javascript.JSGotoByNameFixture.IndexingModesTest;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.NeedsIndex;
import com.intellij.testFramework.TestIndexingModeSupporter.IndexingMode;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.JSGotoByNameFixture.jsGotoByNameFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptGotoMemberTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSGotoByNameFixture> gotoFixture =
    jsGotoByNameFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("/goto")));

  @NeedsIndex.SmartMode(reason = "MXML is in a maintenance mode")
  @IndexingModesTest
  public void testMxmlMembers(IndexingMode mode) {
    gotoFixture.get().inMode(mode).configureByFile(gotoFixture.get().getTestName(false) + ".mxml");
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
    JSGotoByNameFixture goTo = gotoFixture.get();
    GotoSymbolModel2 model = new GotoSymbolModel2(goTo.getProject(), goTo.getTestRootDisposable());
    model.setFilterItems(LanguageRef.forAllLanguages());
    return goTo.doCheckGoto(model, name, shouldExist);
  }
}
