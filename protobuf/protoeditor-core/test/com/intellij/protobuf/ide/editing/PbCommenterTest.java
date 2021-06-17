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

import com.google.common.collect.ImmutableList;
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.EditorTestUtil.CaretAndSelectionState;
import com.intellij.testFramework.EditorTestUtil.CaretInfo;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

/** Tests for {@link PbCommenter} */
public class PbCommenterTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.registerTestdataFileExtension();
  }

  @Override
  public String getTestDataPath() {
    return super.getTestDataPath() + "/ide/editing/";
  }

  // Test comment by line action.
  public void testCommentByLine() {
    myFixture.configureByFiles("CommenterTestCommentByLineBefore.proto.testdata");
    CommentByLineCommentAction action = new CommentByLineCommentAction();
    action.actionPerformedImpl(getProject(), getEditor());
    myFixture.checkResultByFile("CommenterTestCommentByLineAfter.proto.testdata");
  }

  // Tests that hitting enter in the middle of a comment will keep the tail part in a comment.
  public void testInternalNewline() {
    myFixture.configureByFiles("CommenterTestInternalNewlineBefore.proto.testdata");
    Editor editor = openFileInEditor(getFile().getVirtualFile());
    setCaretPosition(editor, 3, "// Documentation for some".length());
    performTypingAction(editor, '\n');
    myFixture.checkResultByFile("CommenterTestInternalNewlineAfter.proto.testdata");
  }

  private Editor openFileInEditor(VirtualFile file) {
    myFixture.openFileInEditor(file);
    return getEditor();
  }

  private static void setCaretPosition(Editor editor, int lineNumber, int columnNumber) {
    final CaretInfo info = new CaretInfo(new LogicalPosition(lineNumber, columnNumber), null);
    EdtTestUtil.runInEdtAndWait(
        () ->
            EditorTestUtil.setCaretsAndSelection(
                editor, new CaretAndSelectionState(ImmutableList.of(info), null)));
  }

  private void performTypingAction(Editor editor, char typedChar) {
    EditorTestUtil.performTypingAction(editor, typedChar);
    PostprocessReformattingAspect.getInstance(getProject()).doPostponedFormatting();
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
  }
}
