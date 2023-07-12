// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.hints.settings.ParameterNameHintsSettings;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class Angular2ParameterHintsTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected void tearDown() throws Exception {
    try {
      ParameterNameHintsSettings def = new ParameterNameHintsSettings();
      ParameterNameHintsSettings.getInstance().loadState(def.getState());
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  void doTest() {
    String testName = getTestName(false);
    myFixture.configureByFiles(testName + ".html", testName + ".ts", "package.json");
    myFixture.testInlays();
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testParameterHintsInHtml() {
    doTest();
  }
}
