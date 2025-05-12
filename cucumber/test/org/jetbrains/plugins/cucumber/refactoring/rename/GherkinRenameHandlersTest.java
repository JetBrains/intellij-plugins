// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinParameterRenameHandler;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinStepRenameHandler;

/**
 * Tests that appropriate rename handlers are available for different parts of the Gherkin file.
 */
public class GherkinRenameHandlersTest extends BasePlatformTestCase {
  private static final String TEST_DATA_PATH = "/refactoring/renameHandlers";

  public void testStepRenameHandler_1() {
    doTest(true, false);
  }

  public void testParameterRenameHandler_1() {
    doTest(false, true);
  }

  // Test for IDEA-372546
  public void testParameterRenameHandler_2() {
    doTest(false, true);
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + TEST_DATA_PATH;
  }

  private void doTest(boolean stepRenameHandlerAvailable, boolean parameterRenameHandlerAvailable) {
    myFixture.configureByFile(getTestName(true) + ".feature");
    DataContext context = ((EditorEx)myFixture.getEditor()).getDataContext();

    GherkinStepRenameHandler stepRenameHandler = new GherkinStepRenameHandler();
    GherkinParameterRenameHandler parameterRenameHandler = new GherkinParameterRenameHandler();
    assertEquals(stepRenameHandlerAvailable, stepRenameHandler.isAvailableOnDataContext(context));
    assertEquals(parameterRenameHandlerAvailable, parameterRenameHandler.isAvailableOnDataContext(context));
  }
}
