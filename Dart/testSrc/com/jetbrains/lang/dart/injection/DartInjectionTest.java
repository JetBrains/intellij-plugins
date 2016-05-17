package com.jetbrains.lang.dart.injection;

import com.intellij.codeInsight.daemon.quickFix.LightQuickFixTestCase;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assume;

public class DartInjectionTest extends LightQuickFixTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + "/injection/";
  }

  private void doTest() throws Exception {
    configureByFile(getTestName(false) + ".dart");
    ParsingTestCase.doCheckResult(getTestDataPath(), getTestName(false) + "." + "txt", toParseTreeText(myFile));
  }

  private static String toParseTreeText(PsiFile file) {
    return DebugUtil.psiToString(file, false, false, (psiElement, consumer) -> InjectedLanguageUtil
      .enumerate(psiElement, (PsiLanguageInjectionHost.InjectedPsiVisitor)(injectedPsi, places) -> consumer.consume(injectedPsi)));
  }

  public void testHtmlInStrings() throws Exception {
    doTest();
  }

  public void testRegExp() throws Exception {
    Assume.assumeTrue("This test is not applicable in current environment because JavaScript plugin is not available",
                      Language.findLanguageByID("JSRegexp") != null);
    doTest();
  }

  public void testJsonWithComment() throws Exception {
    doTest();
  }
}
