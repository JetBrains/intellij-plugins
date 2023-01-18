/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion;

public class CfmlRenameTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
      return "/rename";
  }

  private void doTest(final String newName) {
    myFixture.testRename(Util.getInputDataFileName(getTestName(true)),
                         Util.getExpectedDataFileName(getTestName(true)), newName);
  }

  public void testSimpleVariableRename() { doTest("newName"); }
  public void testComponentVariableRename() { doTest("newName"); }
  public void testScriptFunctionRename() { doTest("newName"); }
  public void testTagFunctionRename() { doTest("newName"); }
  public void testLeaveScopeUnchanged() { doTest("newName"); }
  public void testLeaveScopeUnchanged2() { doTest("newName"); }
}
