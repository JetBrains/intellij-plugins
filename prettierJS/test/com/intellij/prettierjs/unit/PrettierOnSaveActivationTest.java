package com.intellij.prettierjs.unit;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierConfiguration;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The Actions on Save path runs on EDT under a write-intent read action, so asking an action whether it is enabled
 * must not initialize a {@link com.intellij.openapi.components.PersistentStateComponent}: loading its state is a
 * blocking file read, which is an IJent RPC on WSL-backed projects and freezes the EDT (WEB-78877).
 */
@TestApplication
public class PrettierOnSaveActivationTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testSaveDoesNotCreateConfig() {
    CodeInsightTestFixture fixture = codeInsight.get();
    Project project = fixture.getProject();
    assertNull(project.getServiceIfCreated(PrettierConfiguration.class),
               "precondition: the Prettier configuration was already created during test setup");

    fixture.configureByText("a.js", "var x = 1;");
    runInEdtAndWait(() -> {
      WriteCommandAction.runWriteCommandAction(
        project, () -> fixture.getEditor().getDocument().insertString(0, "// dirty\n"));

      FileDocumentManager.getInstance().saveAllDocuments();
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(project);
    });

    assertNull(project.getServiceIfCreated(PrettierConfiguration.class),
               "the save path must not create the Prettier configuration service (WEB-78877)");
  }
}
