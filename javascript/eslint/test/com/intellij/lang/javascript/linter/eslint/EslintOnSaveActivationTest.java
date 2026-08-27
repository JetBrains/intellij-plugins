package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfiguration;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil;

/**
 * The Actions on Save path runs on EDT under a write-intent read action, so asking an action whether it is enabled
 * must not initialize a {@link com.intellij.openapi.components.PersistentStateComponent}: loading its state is a
 * blocking file read, which is an IJent RPC on WSL-backed projects and freezes the EDT (WEB-78877).
 */
public class EslintOnSaveActivationTest extends BasePlatformTestCase {

  /**
   * {@link com.intellij.testFramework.LightPlatformTestCase} reuses its light project unless the descriptor differs by
   * identity, so a dedicated instance is needed here: sharing the project with a test that already asked for a
   * configuration would defeat the assertions below.
   */
  private static final LightProjectDescriptor ISOLATED_PROJECT = new LightProjectDescriptor();

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return ISOLATED_PROJECT;
  }

  public void testSaveDoesNotCreateConfig() {
    assertNull("precondition: the ESLint configuration was already created during test setup",
               getProject().getServiceIfCreated(EslintConfiguration.class));
    assertNull("precondition: the StandardJS configuration was already created during test setup",
               getProject().getServiceIfCreated(StandardJSConfiguration.class));

    myFixture.configureByText("a.js", "var x = 1;");
    WriteCommandAction.runWriteCommandAction(
      getProject(), () -> myFixture.getEditor().getDocument().insertString(0, "// dirty\n"));

    FileDocumentManager.getInstance().saveAllDocuments();
    ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(getProject());

    assertNull("the save path must not create the ESLint configuration service (WEB-78877)",
               getProject().getServiceIfCreated(EslintConfiguration.class));
    assertNull("the save path must not create the StandardJS configuration service (WEB-78877)",
               getProject().getServiceIfCreated(StandardJSConfiguration.class));
  }
}
