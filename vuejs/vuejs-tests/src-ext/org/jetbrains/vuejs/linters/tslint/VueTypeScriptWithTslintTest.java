// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.tslint.TypeScriptServiceWithTslintTestBase;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.LineSeparator;

import java.io.IOException;

import static org.jetbrains.vuejs.lang.VueTestUtilKt.vueRelativeTestDataPath;

public class VueTypeScriptWithTslintTest extends TypeScriptServiceWithTslintTestBase {
  @Override
  protected String getBasePath() {
    return vueRelativeTestDataPath() + "/linters/tslint/";
  }

  public void testFilterWhitespaceErrorsByScriptTag() {
    doHighlightingTest("main", "vue");
  }

  public void testFixAllErrorsWithWhitespaceRules() {
    doFixAllTest();
  }

  public void testMatchingLineEndingsNotHighlighted() {
    doHighlightingTest("main", "vue", () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
  }

  public void testMismatchedLineEndingsHighlighted() {
    doHighlightingTest("main", "vue", () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.LF));
  }

  public void testFixAllWithUpdatingLineSeparators() throws IOException {
    doFixAllTest();
    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
  }

  public void testFixAllWithCrlfLineSeparator() throws IOException {
    doFixTest("main", "vue", "Fix all auto-fixable tslint failures",
              () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
  }

  private void doFixAllTest() {
    doFixTest("main", "vue", "Fix all auto-fixable tslint failures");
  }
}
