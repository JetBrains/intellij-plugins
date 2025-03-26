// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import org.jetbrains.annotations.NotNull;


public final class DartEnterInDocLineCommentHandler implements EnterHandlerDelegate {

  // EnterInLineCommentHandler doesn't work well enough for Dart doc comments
  @Override
  public Result preprocessEnter(final @NotNull PsiFile file,
                                final @NotNull Editor editor,
                                final @NotNull Ref<Integer> caretOffsetRef,
                                final @NotNull Ref<Integer> caretAdvance,
                                final @NotNull DataContext dataContext,
                                final EditorActionHandler originalHandler) {
    if (file.getLanguage() != DartLanguage.INSTANCE && !HtmlUtil.isHtmlFile(file)) return Result.Continue;

    final int caretOffset = caretOffsetRef.get().intValue();
    if (caretOffset == 0) return Result.Continue;

    final Document document = editor.getDocument();
    PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
    final PsiElement psiAtOffset = file.findElementAt(caretOffset - 1);

    if (psiAtOffset != null && psiAtOffset.getNode().getElementType() == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {
      final CharSequence text = document.getCharsSequence();
      final int offset = CharArrayUtil.shiftForward(text, caretOffset, " \t");

      if (StringUtil.startsWith(text, offset, DartDocUtil.SINGLE_LINE_DOC_COMMENT)) {
        caretOffsetRef.set(offset);
        caretAdvance.set(DartDocUtil.SINGLE_LINE_DOC_COMMENT.length());
      }
      else {
        final String docText = StringUtil.trimStart(psiAtOffset.getText(), DartDocUtil.SINGLE_LINE_DOC_COMMENT);
        final int spacesBeforeText = StringUtil.isEmptyOrSpaces(docText) ? 1 : StringUtil.countChars(docText, ' ', 0, true);
        final int spacesToAdd = Math.max(0, spacesBeforeText - StringUtil.countChars(text, ' ', caretOffset, true));
        document.insertString(caretOffset, DartDocUtil.SINGLE_LINE_DOC_COMMENT + StringUtil.repeatSymbol(' ', spacesToAdd));
        caretAdvance.set(DartDocUtil.SINGLE_LINE_DOC_COMMENT.length() + spacesBeforeText);
      }
      return Result.Default;
    }

    return Result.Continue;
  }
}
