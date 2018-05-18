package com.intellij.flex.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle;
import org.junit.Assert;

public class ActionScriptIntentionActionTest extends LightCodeInsightFixtureTestCase {

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
    PsiTestUtil.disablePsiTextConsistencyChecks(getTestRootDisposable());
    final String dirName = "mergeDeclarationAndInitialization/";

    final String intentionActionName = JSIntentionBundle.message("initialization.merge-declaration-and-initialization.display-name");

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
