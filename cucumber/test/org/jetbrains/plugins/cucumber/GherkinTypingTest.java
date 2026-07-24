// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.plugins.cucumber;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class GherkinTypingTest extends BasePlatformTestCase {
  public void testEnterAfterComment() {
    doTest("\n");
  }

  public void testAutoIndentTableCellSingleColumn() {
    doTest("|");
  }

  public void testAutoIndentTableCellMultipleColumns() {
    doTest("|");
  }

  public void testEnterKeepsClosedPyStringUnchanged() {
    doTest("\n");
  }

  public void testEnterAddsClosingQuotesForUnclosedPyString() {
    doTest("\n");
  }

  public void testEnterCreatesIndentInsideFeature() {
    doTest("\n");
  }

  public void testEnterCreatesIndentAfterFeatureLine() {
    doTest("\n");
  }

  public void testEnterInsertsClosingTripleQuotesAfterStep() {
    doTest("\n");
  }

  @Override
  protected @NotNull String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/typing";
  }

  private void doTest(final @NotNull String toType) {
    myFixture.configureByFile(getTestName(true) + ".feature");
    myFixture.type(toType);
    myFixture.checkResultByFile(getTestName(true) + "_after.feature");
  }
}
