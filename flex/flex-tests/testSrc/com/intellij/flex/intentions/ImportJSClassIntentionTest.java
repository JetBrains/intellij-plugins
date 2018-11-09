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

import com.intellij.codeInsight.daemon.DaemonAnalyzerTestCase;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.quickFix.ActionHint;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@DaemonAnalyzerTestCase.CanChangeDocumentDuringHighlighting
public class ImportJSClassIntentionTest extends BaseJSIntentionTestCase {

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[]{new JSUnresolvedVariableInspection()};
  }

  public void testImportClass() throws Exception {
    doTestFor("Test1.as", "FooClass.as");
  }

  public void testImportComponent() throws Exception {
    String[] paths = {getBasePath() + "/Test2.as", getBasePath() + "/aPackage/FooComponent.mxml"};
    configureByFiles(getBasePath(), paths);

    String contents = StringUtil.convertLineSeparators(FileUtil.loadFile(new File(getTestDataPath() + paths[0]), CharsetToolkit.UTF8_CHARSET));
    final ActionHint actionHint = parseActionHintImpl(getFile(), contents);

    doAction(actionHint, paths[0], "Test2.as");
  }

  public void testPackageLocal() throws Exception {
    doTestFor(getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testUnambiguousImportsOnTheFly() throws Exception {
    boolean oldHintsEnabled = DaemonCodeAnalyzerSettings.getInstance().isImportHintEnabled();

    try {
      PropertiesComponent.getInstance().setValue("ActionScript.add.unambiguous.imports.on.the.fly", true);
      DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(true);

      final String testName = getTestName(false);
      configureByFiles(null, getBasePath() + "/" + testName + ".as", getBasePath() + "/" + testName + "_2.as");
      doHighlighting();
      checkResultByFile(getBasePath() + "/" + testName + "_after.as");
    }
    finally {
      PropertiesComponent.getInstance().unsetValue("ActionScript.add.unambiguous.imports.on.the.fly");
      DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(oldHintsEnabled);
    }
  }


  @Override
  @NonNls
  public String getBasePath() {
    return "/importclass";
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }
}
