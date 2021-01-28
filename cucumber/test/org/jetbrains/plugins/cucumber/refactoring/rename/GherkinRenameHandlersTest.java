// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinInplaceRenameHandler;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinStepRenameHandler;

public class GherkinRenameHandlersTest extends BasePlatformTestCase {
  private static final String TEST_DATA_PATH = "/refactoring/renameHandlers";

  public void testUseStepParameterRenameHandlerInParameterAndCell() {
    doTest(false, true);
  }

  public void testUseStepRenameHandlerInStep() {
    doTest(true, false);
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + TEST_DATA_PATH;
  }

  private void doTest(boolean stepRenameHandlerAvailable, boolean stepParameterRenameHandlerAvailable) {
    myFixture.configureByFile(getTestName(true) + ".feature");
    DataContext context = ((EditorEx)myFixture.getEditor()).getDataContext();

    GherkinStepRenameHandler stepRenameHandler = new GherkinStepRenameHandler();
    GherkinInplaceRenameHandler gherkinInplaceRenameHandler = new GherkinInplaceRenameHandler();
    assertEquals(stepRenameHandlerAvailable, stepRenameHandler.isAvailableOnDataContext(context));
    assertEquals(stepParameterRenameHandlerAvailable, gherkinInplaceRenameHandler.isAvailableOnDataContext(context));
  }
}
