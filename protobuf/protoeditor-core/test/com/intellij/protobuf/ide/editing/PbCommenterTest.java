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

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.common.EditorCaretTestUtil.CaretAndSelectionState;
import com.intellij.testFramework.common.EditorCaretTestUtil.CaretInfo;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

/** Tests for {@link PbCommenter} */
@TestApplication
public class PbCommenterTest {
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

  // Test comment by line action.
  @Test
  public void testCommentByLine() {
    myFixture.configureByFiles("CommenterTestCommentByLineBefore.proto.testdata");
    CommentByLineCommentAction action = new CommentByLineCommentAction();
    EdtTestUtil.runInEdtAndWait(() -> action.actionPerformedImpl(myFixture.getProject(), myFixture.getEditor()));
    myFixture.checkResultByFile("CommenterTestCommentByLineAfter.proto.testdata");
  }

  // Tests that hitting enter in the middle of a comment will keep the tail part in a comment.
  @Test
  public void testInternalNewline() {
    myFixture.configureByFiles("CommenterTestInternalNewlineBefore.proto.testdata");
    Editor editor = openFileInEditor(myFixture.getFile().getVirtualFile());
    setCaretPosition(editor, 3, "// Documentation for some".length());
    performTypingAction(editor, '\n');
    myFixture.checkResultByFile("CommenterTestInternalNewlineAfter.proto.testdata");
  }

  private Editor openFileInEditor(VirtualFile file) {
    return EdtTestUtil.runInEdtAndGet(() -> {
      myFixture.openFileInEditor(file);
      return myFixture.getEditor();
    });
  }

  private static void setCaretPosition(Editor editor, int lineNumber, int columnNumber) {
    final CaretInfo info = new CaretInfo(new LogicalPosition(lineNumber, columnNumber), null);
    EdtTestUtil.runInEdtAndWait(
        () ->
            EditorTestUtil.setCaretsAndSelection(
                editor, new CaretAndSelectionState(List.of(info), null)));
  }

  private void performTypingAction(Editor editor, char typedChar) {
    EdtTestUtil.runInEdtAndWait(() -> {
      EditorTestUtil.performTypingAction(editor, typedChar);
      PostprocessReformattingAspect.getInstance(myFixture.getProject()).doPostponedFormatting();
      PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();
    });
  }
}
