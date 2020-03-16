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
package com.intellij.flex.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.inspections.JSJoinVariableDeclarationAndAssignmentInspection;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle;
import org.junit.Assert;

public class ActionScriptIntentionActionTest extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("js2_intentions"));
  }

  @Override
  protected String getBasePath() {
    return super.getBasePath();
  }

  public void testSplitDeclarationAndInitialization() {
    final String dirName = "splitDeclarationAndInitialization";

    final String intentionActionName = JSIntentionBundle.message("initialization.split-declaration-and-initialization.display-name");

    doIntentionTest(dirName, "", intentionActionName);
    doIntentionTest(dirName, "2", intentionActionName);
    doTestNoIntention(dirName, "3", intentionActionName);
    doIntentionTest(dirName, "4", intentionActionName);
  }

  public void testMergeDeclarationAndInitialization() {
    myFixture.enableInspections(new JSJoinVariableDeclarationAndAssignmentInspection());
    final String dirName = "mergeDeclarationAndInitialization/";
    final String intentionActionName = JavaScriptBundle.message("js.join.declaration.assignment.inspection.fix");
    doIntentionTest(dirName, "", intentionActionName);
  }

  public void testJoinConcatenatedStringLiterals() {
    String actionName = JSIntentionBundle.message("string.join-concatenated-string-literals.display-name");
    String directory = "JoinConcatenatedStringLiterals/";
    doIntentionTest(directory, "", actionName);
  }

  @SuppressWarnings("SameParameterValue")
  private void doTestNoIntention(String directory, String fileSuffix, String intentionActionName) {
    String before = String.format("%s/before%s.js2", directory, fileSuffix);
    myFixture.configureByFile(before);
    IntentionAction intentionAction = CodeInsightTestUtil.findIntentionByText(myFixture.getAvailableIntentions(), intentionActionName);
    Assert.assertNull(String.format("Expected no intention by text %s but was %s", intentionActionName, intentionAction), intentionAction);
  }

  private void doIntentionTest(String baseDir, String fileSuffix, String intentionActionName) {
    String before = String.format("%s/before%s.js2", baseDir, fileSuffix);
    String after = String.format("%s/after%s.js2", baseDir, fileSuffix);
    CodeInsightTestUtil.doIntentionTest(myFixture, intentionActionName, before, after);
  }
}
