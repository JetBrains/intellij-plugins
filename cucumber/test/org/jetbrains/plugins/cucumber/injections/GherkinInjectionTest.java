// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.injections;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage;

public class GherkinInjectionTest extends BasePlatformTestCase {
  public void testJsonInjection() {
    doTest(
      """
        Feature: test
          Scenario: test
            Given test step
            ""\"json
              {"abc":<caret>  "def"}
            ""\"""",
      true
    );
  }

  public void testYamlInjection() {
    doTest(
      """
        Feature: test
          Scenario: test
            Given test step
            ""\"xml
              <tag><caret></tag>
            ""\"""",
      true
    );
  }

  public void testSimplePystring() {
    doTest(
      """
        Feature: test
          Scenario: test
            Given test step
            ""\"
              <tag><caret></tag>
            ""\"""",
      false
    );
  }

  private void doTest(@NotNull String text, boolean injectionExpected) {
    myFixture.configureByText("test.feature", text);
    PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

    assertEquals(!injectionExpected, element.getLanguage().equals(GherkinLanguage.INSTANCE));
  }
}
