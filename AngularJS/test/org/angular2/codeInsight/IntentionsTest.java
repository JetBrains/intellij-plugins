// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.AngularTestUtil;
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle;

public class IntentionsTest extends LightPlatformCodeInsightFixtureTestCase {
  
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "intentions";
  }
  
  public void testComputeConstantInTemplate() {
    doTestForFile(getTestName(true), JSIntentionBundle.message("constant.constant-expression.display-name"));
  }

  public void testFlipConditionalInTemplate() {
    doTestForFile(getTestName(true), JSIntentionBundle.message("conditional.flip-conditional.display-name"));
  }

  public void testDeMorgansLawInTemplate() {
    doTestForFile(getTestName(true), JSIntentionBundle.message("bool.de-morgans-law.display-name.ANDAND"));
  }

  private void doTestForFile(String name, String intentionHint) {
    JSTestUtils.testES6(myFixture.getProject(), () -> {
      myFixture.setCaresAboutInjection(false);
      myFixture.configureByFiles(name + ".html", "angular2.js");
      IntentionAction action = ContainerUtil.find(myFixture.getAvailableIntentions(),
                                                  t -> StringUtil.equals(t.getText(), intentionHint));
      if (action == null) {
        throw new RuntimeException("Could not find intention by text " + intentionHint);
      }
      myFixture.launchAction(action);
      myFixture.checkResultByFile(getTestName(true) + "_after.html");
    });
  }
}
