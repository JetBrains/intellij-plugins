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

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandler;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.lang.psi.PbAggregateValue;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbTextFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TypedHandlerDelegate} that auto-inserts matching '>' characters, similar to
 * auto-insertion of matching '}'.
 */
public class ProtoTypedHandler extends TypedHandlerDelegate {
  private static final Logger logger = Logger.getInstance(ProtoTypedHandler.class);

  @Override
  public @NotNull Result beforeCharTyped(
    final char c,
    final @NotNull Project project,
    final @NotNull Editor editor,
    final @NotNull PsiFile file,
    @NotNull FileType fileType) {
    if (c == '>' && handleFile(file) && TypedHandler.handleRParen(editor, file.getFileType(), c)) {
      return Result.STOP;
    }
    return Result.CONTINUE;
  }

  @Override
  public @NotNull Result charTyped(
    final char c, final @NotNull Project project, final @NotNull Editor editor, final @NotNull PsiFile file) {

    if (handleFile(file)) {
      if (c == '<') {
        handleAfterLParen(editor, file.getFileType(), c);
        return Result.STOP;
      } else if (c == '>' && inTextFormat(file, editor)) {
        indentBrace(project, editor, c);
      }
    }

    return Result.CONTINUE;
  }

  private static boolean handleFile(PsiFile file) {
    return file instanceof PbFile || file instanceof PbTextFile;
  }

  // This is a copy of TypedHandler#handleAfterLParen(...), which is unfortunately private.
  private static void handleAfterLParen(Editor editor, FileType fileType, char lparenChar) {
    int offset = editor.getCaretModel().getOffset();
    HighlighterIterator iterator = editor.getHighlighter().createIterator(offset);
    boolean atEndOfDocument = offset == editor.getDocument().getTextLength();

    if (!atEndOfDocument) {
      iterator.retreat();
    }
    if (iterator.atEnd()) {
      return;
    }
    BraceMatcher braceMatcher = BraceMatchingUtil.getBraceMatcher(fileType, iterator);
    if (iterator.atEnd()) {
      return;
    }
    IElementType braceTokenType = iterator.getTokenType();
    final CharSequence fileText = editor.getDocument().getCharsSequence();
    if (!braceMatcher.isLBraceToken(iterator, fileText, fileType)) {
      return;
    }

    if (!iterator.atEnd()) {
      iterator.advance();

      if (!iterator.atEnd()
          && !BraceMatchingUtil.isPairedBracesAllowedBeforeTypeInFileType(
              braceTokenType, iterator.getTokenType(), fileType)) {
        return;
      }

      iterator.retreat();
    }

    int lparenOffset =
        BraceMatchingUtil.findLeftmostLParen(iterator, braceTokenType, fileText, fileType);
    if (lparenOffset < 0) {
      lparenOffset = 0;
    }

    iterator = editor.getHighlighter().createIterator(lparenOffset);
    boolean matched = BraceMatchingUtil.matchBrace(fileText, fileType, iterator, true, true);

    if (!matched) {
      String text;
      if (lparenChar == '(') {
        text = ")";
      } else if (lparenChar == '[') {
        text = "]";
      } else if (lparenChar == '<') {
        text = ">";
      } else if (lparenChar == '{') {
        text = "}";
      } else {
        throw new AssertionError("Unknown char " + lparenChar);
      }
      editor.getDocument().insertString(offset, text);
    }
  }

  /**
   * A copy of TypedHandler#indentBrace(...) which is not public.
   *
   * @see TypedHandler
   */
  private static void indentBrace(
    final @NotNull Project project, final @NotNull Editor editor, final char braceChar) {
    final int offset = editor.getCaretModel().getOffset() - 1;
    final Document document = editor.getDocument();
    CharSequence chars = document.getCharsSequence();
    if (offset < 0 || chars.charAt(offset) != braceChar) {
      return;
    }

    int spaceStart = CharArrayUtil.shiftBackward(chars, offset - 1, " \t");
    if (spaceStart < 0 || chars.charAt(spaceStart) == '\n' || chars.charAt(spaceStart) == '\r') {
      PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
      documentManager.commitDocument(document);

      final PsiFile file = documentManager.getPsiFile(document);
      if (file == null || !file.isWritable()) {
        return;
      }
      PsiElement element = file.findElementAt(offset);
      if (element == null) {
        return;
      }

      EditorHighlighter highlighter = editor.getHighlighter();
      HighlighterIterator iterator = highlighter.createIterator(offset);

      final FileType fileType = file.getFileType();
      BraceMatcher braceMatcher = BraceMatchingUtil.getBraceMatcher(fileType, iterator);
      IElementType oppositeTokenType = braceMatcher.getOppositeBraceTokenType(iterator.getTokenType());
      boolean rBraceToken = braceMatcher.isRBraceToken(iterator, chars, fileType);
      final boolean isBrace = braceMatcher.isLBraceToken(iterator, chars, fileType) || rBraceToken;
      int lBraceOffset = -1;

      if (CodeInsightSettings.getInstance().REFORMAT_BLOCK_ON_RBRACE
          && rBraceToken
          && braceMatcher.isStructuralBrace(iterator, chars, fileType)
          && offset > 0
          && oppositeTokenType != null) {
        lBraceOffset =
            BraceMatchingUtil.findLeftLParen(
                highlighter.createIterator(offset - 1),
                oppositeTokenType,
                editor.getDocument().getCharsSequence(),
                fileType);
      }
      if (element.getNode() != null && isBrace) {
        final int finalLBraceOffset = lBraceOffset;
        ApplicationManager.getApplication()
            .runWriteAction(
                () -> {
                  try {
                    int newOffset;
                    if (finalLBraceOffset != -1) {
                      RangeMarker marker = document.createRangeMarker(offset, offset + 1);
                      CodeStyleManager.getInstance(project)
                          .reformatRange(file, finalLBraceOffset, offset, true);
                      newOffset = marker.getStartOffset();
                      marker.dispose();
                    } else {
                      newOffset =
                          CodeStyleManager.getInstance(project).adjustLineIndent(file, offset);
                    }

                    editor.getCaretModel().moveToOffset(newOffset + 1);
                    editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
                    editor.getSelectionModel().removeSelection();
                  } catch (IncorrectOperationException e) {
                    logger.error(e);
                  }
                });
      }
    }
  }

  static boolean inTextFormat(PsiFile file, Editor editor) {
    if (file instanceof PbTextFile) {
      return true;
    }
    if (file instanceof PbFile) {
      PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
      if (element == null) {
        return false;
      }
      return PsiTreeUtil.getParentOfType(element, PbAggregateValue.class) != null;
    }
    return false;
  }
}
