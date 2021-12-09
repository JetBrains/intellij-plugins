// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.injections;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage;

public class GherkinInjectionTest extends BasePlatformTestCase {
  public void testJsonInjection() {
    doTest(
      "Feature: test\n" +
      "  Scenario: test\n" +
      "    Given test step\n" +
      "    \"\"\"json\n" +
      "      {\"abc\":<caret>  \"def\"}\n" +
      "    \"\"\"",
      true
    );
  }

  public void testYamlInjection() {
    doTest(
      "Feature: test\n" +
      "  Scenario: test\n" +
      "    Given test step\n" +
      "    \"\"\"xml\n" +
      "      <tag><caret></tag>\n" +
      "    \"\"\"",
      true
    );
  }

  public void testSimplePystring() {
    doTest(
      "Feature: test\n" +
      "  Scenario: test\n" +
      "    Given test step\n" +
      "    \"\"\"\n" +
      "      <tag><caret></tag>\n" +
      "    \"\"\"",
      false
    );
  }

  private void doTest(@NotNull String text, boolean injectionExpected) {
    myFixture.configureByText("test.feature", text);
    PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

    assertEquals(!injectionExpected, element.getLanguage().equals(GherkinLanguage.INSTANCE));
  }
}
