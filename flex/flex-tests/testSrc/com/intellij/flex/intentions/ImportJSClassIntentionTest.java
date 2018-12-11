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

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.jetbrains.annotations.NotNull;

public class ImportJSClassIntentionTest extends BaseJSIntentionTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new JSUnresolvedVariableInspection());
  }

  public void testImportClass() {
    doTestFor("Test1.as", "FooClass.as");
  }

  public void testImportComponent() {

    String[] paths = {getBasePath() + "/Test2.as", getBasePath() + "/aPackage/FooComponent.mxml"};
    doTestForAndCheckFirstFile(paths);
  }

  public void testPackageLocal() {
    doTestFor(getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testUnambiguousImportsOnTheFly() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    boolean oldHintsEnabled = DaemonCodeAnalyzerSettings.getInstance().isImportHintEnabled();

    try {
      PropertiesComponent.getInstance().setValue("ActionScript.add.unambiguous.imports.on.the.fly", true);
      DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(true);

      final String testName = getTestName(false);
      myFixture.configureByFiles(getBasePath() + "/" + testName + ".as", getBasePath() + "/" + testName + "_2.as");
      myFixture.doHighlighting();
      myFixture.checkResultByFile(getBasePath() + "/" + testName + "_after.as");
    }
    finally {
      PropertiesComponent.getInstance().unsetValue("ActionScript.add.unambiguous.imports.on.the.fly");
      DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(oldHintsEnabled);
    }
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/importclass";
  }
}
