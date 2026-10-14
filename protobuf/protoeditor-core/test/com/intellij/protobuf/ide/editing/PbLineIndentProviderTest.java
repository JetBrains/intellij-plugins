/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.editing;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.EnterAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbLanguage;
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider;
import com.intellij.psi.impl.source.codeStyle.lineIndent.FormatterBasedLineIndentProvider;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.text.CharArrayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for {@link PbLineIndentProvider} */
@TestApplication
public class PbLineIndentProviderTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp() {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture, "/ide/editing/");
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
  }

  /**
   * Tests that a PbLineIndentProvider and FormatterBasedLineIndentProvider behave identically when
   * generating indents for new lines.
   */
  @Test
  public void testAgreementWithFormatter() {
    myFixture.configureByFile("LineIndentProvider.proto.testdata");

    LineIndentProvider customProvider = new PbLineIndentProvider();
    LineIndentProvider formatterProvider = new FormatterBasedLineIndentProvider();
    String customText = EdtTestUtil.runInEdtAndGet(() -> buildIndentedOutput(customProvider, formatterProvider));
    String formatterText = EdtTestUtil.runInEdtAndGet(() -> buildIndentedOutput(formatterProvider, formatterProvider));

    assertEquals(formatterText, customText);
  }

  // This method does the following:
  // - first, iterate over all lines and remove leading indents
  // - next, iterate over all lines and get the computed indent from the given provider
  // - update the line indent with the computed indent
  // - finally, return the document text
  // This mimics pressing return at each new line. The output should be a properly-indented file.
  private String buildIndentedOutput(
      LineIndentProvider provider, LineIndentProvider formatterProvider) {
    Project project = myFixture.getProject();
    Editor editor = myFixture.getEditor();
    Document document = myFixture.getEditor().getDocument();
    for (int line = 0; line < document.getLineCount(); line++) {
      int offset = document.getLineStartOffset(line);
      int endOffset = document.getLineEndOffset(line);
      String lineText = document.getText(TextRange.create(offset, endOffset));
      WriteCommandAction.runWriteCommandAction(
          project, () -> document.deleteString(offset, offset + leadingSpaces(lineText)));
    }
    for (int line = 0; line < document.getLineCount(); line++) {
      int offset = document.getLineStartOffset(line);

      convertNewlineToEnterAction(project, editor, document, offset);

      CharSequence docChars = document.getCharsSequence();
      int indentStart = CharArrayUtil.shiftBackwardUntil(docChars, offset - 1, "\n") + 1;
      int indentEnd = CharArrayUtil.shiftForward(docChars, indentStart, " \t");
      // Finally, compute the actual intended indent and replace the indent which was simply based
      // on the previous line.
      String newIndent = provider.getLineIndent(project, editor, PbLanguage.INSTANCE, indentEnd);
      // In IntelliJ 2017.1 the JavaLikeLangLineIndentProvider no longer directly delegates to
      // the formatter in the null case. Instead the enter handler adjusts the indent using the
      // formatting model asynchronously:
      // https://github.com/JetBrains/intellij-community/commit/1e3e879291388cbb1fd8f98d86911b391dfc467c
      // Simulate that here.
      if (newIndent == null) {
        newIndent = formatterProvider.getLineIndent(project, editor, PbLanguage.INSTANCE, offset);
      }
      if (newIndent != null) {
        String finalNewIndent = newIndent;
        WriteCommandAction.runWriteCommandAction(
            project, () -> document.replaceString(indentStart, indentEnd, finalNewIndent));
      }
    }
    return document.getText();
  }

  private static void convertNewlineToEnterAction(
      Project project, Editor editor, Document document, int offset) {
    // Delete the preceding newline, then run the enter action which will add back the newline
    // plus some amount of indent copied from the previous line.
    if (offset != 0) {
      WriteCommandAction.runWriteCommandAction(
          project,
          () -> {
            editor.getCaretModel().moveToOffset(offset);
            assertEquals('\n', document.getCharsSequence().charAt(offset - 1));
            document.deleteString(offset - 1, offset);
            EnterAction.insertNewLineAtCaret(editor);
          });
    }
  }

  private static int leadingSpaces(String string) {
    int count = 0;
    while (count < string.length() && string.charAt(count) == ' ') {
      count++;
    }
    return count;
  }
}
