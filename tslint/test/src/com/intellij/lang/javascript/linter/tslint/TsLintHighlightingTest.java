package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
    doTest("one", "one/one.ts", true, true, null);
  }

  public void testNoAdditionalDirectory() throws Exception {
    doTest("noAdditionalDirectory", "noAdditionalDirectory/data.ts", true, true, null);
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("Could not find custom rule directory:", false, true);
  }

  public void testNoConfig() throws Exception {
    doTest("noConfig", "noConfig/data.ts", false, true, null);
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("TSLint: Config file was not found.", false, true);
  }

  public void testBadConfig() throws Exception {
    doTest("badConfig", "badConfig/data.ts", false, true, null);
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("TSLint: Config file was not found.", false, true);
  }

  public void testLineSeparatorsWin() throws Exception {
    if (!SystemInfo.isWindows) return;
    doTest("lineSeparators", "lineSeparators/data.ts", true, true, LineSeparator.CRLF);
  }

  private void doTest(@NotNull String directoryToCopy, @NotNull String filePathToTest, boolean copyConfig,
                      @SuppressWarnings("SameParameterValue") boolean useConfig, LineSeparator lineSeparator) throws IOException {
    runTest(copyConfig, useConfig, lineSeparator, filePathToTest, directoryToCopy + "/tslint.json");
  }

  private void runTest(boolean copyConfig, boolean useConfig, @Nullable LineSeparator lineSeparator,
                       String... filePathToTest) {
    final String[] paths = copyConfig ? filePathToTest : new String[]{filePathToTest[0]};
    final PsiFile[] files = myFixture.configureByFiles(paths);
    if (lineSeparator != null) {
      Arrays.stream(files).forEach(file -> ensureLineSeparators(file.getVirtualFile(), lineSeparator.getSeparatorString()));
    }
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(getProject());
    final TsLintState.Builder builder = new TsLintState.Builder(configuration.getExtendedState().getState());
    if (useConfig) {
      final String configPath = copyConfig ? FileUtil.toSystemDependentName(files[files.length - 1].getVirtualFile().getPath()) : "aaa";
      builder.setCustomConfigFileUsed(true).setCustomConfigFilePath(configPath);
    }
    configuration.setExtendedState(true, builder.build());

    myFixture.testHighlighting(true, false, true);
  }
}
