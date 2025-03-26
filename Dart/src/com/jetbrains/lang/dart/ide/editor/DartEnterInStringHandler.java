// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.DartTokenTypes.*;

public final class DartEnterInStringHandler implements EnterHandlerDelegate {

  @Override
  public Result preprocessEnter(final @NotNull PsiFile file,
                                final @NotNull Editor editor,
                                final @NotNull Ref<Integer> caretOffsetRef,
                                final @NotNull Ref<Integer> caretAdvanceRef,
                                final @NotNull DataContext dataContext,
                                final EditorActionHandler originalHandler) {
    if (file.getLanguage() != DartLanguage.INSTANCE && !HtmlUtil.isHtmlFile(file)) return Result.Continue;

    int caretOffset = caretOffsetRef.get().intValue();
    PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
    PsiElement psiAtOffset = file.findElementAt(caretOffset);
    int psiOffset;
    if (psiAtOffset == null || (psiOffset = psiAtOffset.getTextRange().getStartOffset()) > caretOffset) {
      return Result.Continue;
    }

    ASTNode node = psiAtOffset.getNode();
    IElementType nodeType = node.getElementType();

    if ((nodeType == SHORT_TEMPLATE_ENTRY_START || nodeType == LONG_TEMPLATE_ENTRY_START) && caretOffset == psiOffset) {
      node = node.getTreeParent();
      nodeType = node.getElementType();
    }

    if (nodeType == RAW_TRIPLE_QUOTED_STRING && caretOffset >= psiOffset + "r'''".length()) {
      return Result.DefaultSkipIndent; // Multiline string gets no indent
    }

    if (nodeType == RAW_SINGLE_QUOTED_STRING && caretOffset >= psiOffset + "r'".length()) {
      char quote = node.getText().charAt(1);
      breakString("r" + quote, String.valueOf(quote), caretOffsetRef, caretAdvanceRef, editor.getDocument());
      return Result.Default;
    }

    if (nodeType == REGULAR_STRING_PART ||
        nodeType == CLOSING_QUOTE ||
        nodeType == SHORT_TEMPLATE_ENTRY ||
        nodeType == LONG_TEMPLATE_ENTRY) {
      final String openingQuoteText = getOpeningQuoteText(node);
      if (openingQuoteText != null) {
        if (openingQuoteText.length() == 1) {
          breakString(openingQuoteText, openingQuoteText, caretOffsetRef, caretAdvanceRef, editor.getDocument());
          return Result.Default;
        }
        else {
          return Result.DefaultSkipIndent; // Multiline string gets no indent
        }
      }
    }

    return Result.Continue;
  }

  private static @Nullable String getOpeningQuoteText(final @NotNull ASTNode node) {
    ASTNode prev = node.getTreePrev();
    while (prev != null) {
      if (prev.getElementType() == OPEN_QUOTE) {
        return prev.getText();
      }
      prev = prev.getTreePrev();
    }
    return null;
  }

  private static void breakString(final @NotNull String startQuote,
                                  final @NotNull String endQuote,
                                  final @NotNull Ref<Integer> caretOffsetRef,
                                  final @NotNull Ref<Integer> caretAdvanceRef,
                                  final @NotNull Document document) {
    // The final effect is to insert matching close-quote, newline, indent, matching open-quote.
    int caretOffset = caretOffsetRef.get().intValue();
    int caretAdvance = caretAdvanceRef.get().intValue();
    document.insertString(caretOffset, endQuote + startQuote);
    caretOffset += endQuote.length();
    caretAdvance += startQuote.length();
    caretOffsetRef.set(caretOffset);
    caretAdvanceRef.set(caretAdvance);
  }
}
