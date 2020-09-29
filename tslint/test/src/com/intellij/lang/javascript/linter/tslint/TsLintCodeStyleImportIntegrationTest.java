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
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.codestyle.TsLintImportCodeStyleAction;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.linter.tslint.TsLintTestUtil.BASE_TEST_DATA_PATH;

public class TsLintCodeStyleImportIntegrationTest extends LinterHighlightingTest {
  @NotNull
  @Override
  protected InspectionProfileEntry getInspection() {
    return new TsLintInspection();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(BASE_TEST_DATA_PATH + "/config/import");
  }

  @NotNull
  @Override
  protected String getPackageName() {
    return "tslint";
  }

  public void testFromConfigFileWithExtends() {
    myFixture.configureByFile(getTestName(true) + "/" + TslintUtil.TSLINT_JSON);
    EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
    boolean ensureEofPrevious = editorSettings.isEnsureNewLineAtEOF();
    try {
      JSTestUtils.testWithTempCodeStyleSettings(getProject(), (settings) -> {
        myFixture.testAction(new TsLintImportCodeStyleAction());
        TypeScriptCodeStyleSettings customSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
        assertFalse(customSettings.USE_SEMICOLON_AFTER_STATEMENT);
        assertTrue(customSettings.SPACES_WITHIN_IMPORTS);
      });
    }
    finally {
      editorSettings.setEnsureNewLineAtEOF(ensureEofPrevious);
    }
  }
}
