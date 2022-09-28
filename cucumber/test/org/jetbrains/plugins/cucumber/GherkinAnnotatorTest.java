// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public class GherkinAnnotatorTest extends BasePlatformTestCase {
  public void testTextInTheMiddleOfScenario() {
    myFixture.configureByText(GherkinFileType.INSTANCE,
                              """
                                Feature: test 1
                                  Scenario: test
                                    Given test
                                    <error descr="Unexpected element">Given</error>""");
    myFixture.testHighlighting();
  }

  public void testTextAsScenarioDescription() {
    myFixture.configureByText(GherkinFileType.INSTANCE,
                              """
                                Feature: test 1
                                  Scenario: test
                                    Given
                                    Given test""");
    myFixture.testHighlighting();
  }
}
