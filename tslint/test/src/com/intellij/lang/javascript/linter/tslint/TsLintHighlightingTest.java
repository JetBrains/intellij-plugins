package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintHighlightingTest extends LinterHighlightingTest {

  @Override
  protected String getBasePath() {
    return "/highlighting/";
  }

  @Override
  protected String getTestDataPath() {
    return TsLintTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }

  @NotNull
  @Override
  protected InspectionProfileEntry getInspection() {
    return new TsLintInspection();
  }

  @NotNull
  @Override
  protected String getPackageName() {
    return "tslint";
  }

  public void testOne() throws Exception {
    doTest("one", "one/one.ts");
  }

  private void doTest(@NotNull String directoryToCopy, @NotNull String filePathToTest) throws IOException {
    if (!myNodeLinterPackagePaths.checkPaths()) return;

    final File tmp = FileUtil.createTempFile("tslint", ".json");
    tmp.deleteOnExit();
    FileUtil.copy(new File(getTestDataPath(), directoryToCopy + "/tslint.json"), tmp);

    final TsLintState state = new TsLintState.Builder()
      .setNodePath(NodeJsInterpreterRef.create(myNodeLinterPackagePaths.getNodePath().getAbsolutePath()))
      .setPackagePath(myNodeLinterPackagePaths.getPackagePath().getPath())
      .setCustomConfigFileUsed(true)
      .setCustomConfigFilePath(tmp.getAbsolutePath())
      .build();

    runTest(directoryToCopy, filePathToTest, state);
  }

  private void runTest(String directoryToCopy, String filePathToTest, TsLintState state) {
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);

    myFixture.copyDirectoryToProject(directoryToCopy, myFixture.getTempDirPath());
    myFixture.configureByFiles(filePathToTest);
    myFixture.testHighlighting(true, false, true);
  }
}
