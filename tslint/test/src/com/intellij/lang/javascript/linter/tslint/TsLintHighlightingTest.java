package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintHighlightingTest extends LinterHighlightingTest {

  @Override
  protected String getBasePath() {
    final String homePath = isCommunity() ? PlatformTestUtil.getCommunityPath() : PathManager.getHomePath();
    final String path = TsLintTestUtil.BASE_TEST_DATA_PATH + "/highlighting/";
    return File.separator + FileUtil.getRelativePath(new File(homePath), new File(path));
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

    final TsLintState.Builder builder = new TsLintState.Builder()
      .setNodePath(NodeJsInterpreterRef.create(myNodeLinterPackagePaths.getNodePath().getAbsolutePath()))
      .setPackagePath(myNodeLinterPackagePaths.getPackagePath().getPath());

    runTest(builder, true, filePathToTest, directoryToCopy + "/tslint.json");
  }

  private void runTest(TsLintState.Builder builder, boolean withConfig, String... filePathToTest) {
    final PsiFile[] files = myFixture.configureByFiles(filePathToTest);
    if (withConfig) {
      builder.setCustomConfigFileUsed(true)
        .setCustomConfigFilePath(FileUtil.toSystemDependentName(files[files.length - 1].getVirtualFile().getPath()));
    }
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, builder.build());

    myFixture.testHighlighting(true, false, true);
  }
}
