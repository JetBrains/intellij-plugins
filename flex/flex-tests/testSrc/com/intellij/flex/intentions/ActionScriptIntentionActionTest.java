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
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.inspections.JSJoinVariableDeclarationAndAssignmentInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSDaemonAnalyzerFixture.jsDaemonAnalyzerFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestApplication
public class ActionScriptIntentionActionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSDaemonAnalyzerFixture> js =
    jsDaemonAnalyzerFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("/js2_intentions/"), "js2"));

  @Test
  public void testSplitDeclarationAndInitialization() {
    final String dirName = "splitDeclarationAndInitialization";

    final String intentionActionName = JSIntentionBundle.message("initialization.split-declaration-and-initialization.display-name");

    EdtTestUtil.runInEdtAndWait(() -> doIntentionTest(dirName, "", intentionActionName));
    doIntentionTest(dirName, "2", intentionActionName);
    doTestNoIntention(dirName, "3", intentionActionName);
    doIntentionTest(dirName, "4", intentionActionName);
  }

  @Test
  public void testMergeDeclarationAndInitialization() {
    js.get().getFixture().enableInspections(new JSJoinVariableDeclarationAndAssignmentInspection());
    final String dirName = "mergeDeclarationAndInitialization/";
    final String intentionActionName = JavaScriptBundle.message("js.join.declaration.assignment.inspection.fix");
    EdtTestUtil.runInEdtAndWait(() -> doIntentionTest(dirName, "", intentionActionName));
  }

  @Test
  public void testJoinConcatenatedStringLiterals() {
    String actionName = JSIntentionBundle.message("string.join-concatenated-string-literals.display-name");
    String directory = "JoinConcatenatedStringLiterals/";
    EdtTestUtil.runInEdtAndWait(() -> doIntentionTest(directory, "", actionName));
  }

  @SuppressWarnings("SameParameterValue")
  private void doTestNoIntention(String directory, String fileSuffix, String intentionActionName) {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    String before = String.format("%s/before%s.js2", directory, fileSuffix);
    myFixture.configureByFile(before);
    IntentionAction intentionAction = CodeInsightTestUtil.findIntentionByText(myFixture.getAvailableIntentions(), intentionActionName);
    assertNull(intentionAction, String.format("Expected no intention by text %s but was %s", intentionActionName, intentionAction));
  }

  private void doIntentionTest(String baseDir, String fileSuffix, String intentionActionName) {
    String before = String.format("%s/before%s.js2", baseDir, fileSuffix);
    String after = String.format("%s/after%s.js2", baseDir, fileSuffix);
    EdtTestUtil.runInEdtAndWait(() -> CodeInsightTestUtil.doIntentionTest(js.get().getFixture(), intentionActionName, before, after));
  }
}
