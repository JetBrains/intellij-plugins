// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.codestyle.TsLintImportCodeStyleAction;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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

  @Override
  protected @NotNull Map<String, String> getGlobalPackageVersionsToInstall() {
    return Map.of("tslint", "latest",
                  "typescript", "latest");
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
