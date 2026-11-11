// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.LinterHighlightingFixture;
import com.intellij.lang.javascript.linter.tslint.codestyle.TsLintImportCodeStyleAction;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.intellij.lang.javascript.linter.LinterHighlightingFixture.linterHighlightingFixture;
import static com.intellij.lang.javascript.linter.tslint.TsLintTestUtil.BASE_TEST_DATA_PATH;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class TsLintCodeStyleImportIntegrationTest {
  // Pinned to 5.x: typescript@latest is now 7.x (the native rewrite), whose runtime API no longer
  // exposes members such as ts.ScriptTarget, so tslint@6.x crashes ("Cannot read properties of undefined").
  private final TestFixture<LinterHighlightingFixture> tslint = linterHighlightingFixture(
    new JSTempDirFixtureOptions(BASE_TEST_DATA_PATH + "/config/import")
      .withMainPackage("tslint")
      .withGlobalPackages(Map.of("tslint", "latest",
                                 "typescript", "5.9.3")),
    () -> new TsLintInspection());

  @Test
  public void testFromConfigFileWithExtends() {
    CodeInsightTestFixture myFixture = tslint.get().getFixture();
    myFixture.configureByFile(tslint.get().getTestName(true) + "/" + TslintUtil.TSLINT_JSON);
    EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
    boolean ensureEofPrevious = editorSettings.isEnsureNewLineAtEOF();
    try {
      JSTestUtils.testWithTempCodeStyleSettings(myFixture.getProject(), (settings) -> {
        // The action needs the EDT.
        EdtTestUtil.runInEdtAndWait(() -> myFixture.testAction(new TsLintImportCodeStyleAction()));
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
