// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.lang.javascript.JSGotoByNameFixture;
import com.intellij.lang.javascript.JSGotoByNameFixture.IndexingModesTest;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestIndexingModeSupporter.IndexingMode;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import static com.intellij.lang.javascript.JSGotoByNameFixture.jsGotoByNameFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptGotoSymbolTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSGotoByNameFixture> gotoFixture = jsGotoByNameFixture(projectFixture);

  @BeforeEach
  void setUp() {
    JSTestUtils.initJSIndexes(gotoFixture.get().getProject());
  }

  @IndexingModesTest
  public void testAsSymbol(IndexingMode mode) {
    gotoFixture.get().inMode(mode)
      .addFileToProject("com/test/Foo.as", "package com.test { public class Foo { public var someVar:int; public function someFunc(){} } }");
    checkGotoSymbol("someVar", true);
    checkGotoSymbol("someFunc", true);
  }

  private NavigationItem checkGotoSymbol(@NotNull String symbolName, boolean exists) {
    GotoSymbolModel2 model = new GotoSymbolModel2(gotoFixture.get().getProject());
    return gotoFixture.get().doCheckGoto(model, symbolName, exists);
  }
}
