// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.injection;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

// When no version of cucumber-jvm can be determined, ensure that injections are performed as if on the latest cucumber-jvm version. 
public class CucumberJavaDefaultVersionInjectionTest extends BasePlatformTestCase {
  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "injection/injection_7";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11);
  }

  private void doTest() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("Steps.java");
    ParsingTestCase.doCheckResult(getTestDataPath(), getTestName(true) + "/Steps.txt", toParseTreeText(myFixture.getFile()));
  }

  private @NotNull String toParseTreeText(PsiFile file) {
    return DebugUtil.psiToString(file, true, false, (psiElement, consumer) -> {
      InjectedLanguageManager.getInstance(getProject()).enumerate(psiElement, (injectedPsi, places) -> {
        consumer.consume(injectedPsi);
      });
    });
  }

  public void testRegexInjection() {
    doTest();
  }
}
