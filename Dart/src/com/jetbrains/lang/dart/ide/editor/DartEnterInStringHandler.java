package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypes;
import org.jetbrains.annotations.NotNull;

public class DartEnterInStringHandler extends EnterHandlerDelegateAdapter {

  @Override
  public Result preprocessEnter(@NotNull final PsiFile file,
                                @NotNull final Editor editor,
                                @NotNull final Ref<Integer> caretOffsetRef,
                                @NotNull final Ref<Integer> caretAdvanceRef,
                                @NotNull final DataContext dataContext,
                                final EditorActionHandler originalHandler) {
    int caretOffset = caretOffsetRef.get().intValue();
    int caretAdvance = caretAdvanceRef.get().intValue();
    PsiElement psiAtOffset = file.findElementAt(caretOffset);
    if (psiAtOffset == null || psiAtOffset.getTextOffset() > caretOffset) {
      return Result.Continue;
    }
    Document document = editor.getDocument();
    ASTNode token = psiAtOffset.getNode();
    IElementType type = token.getElementType();
    if (type == DartTokenTypes.REGULAR_STRING_PART) {
      token = token.getTreeNext();
      type = token.getElementType();
      // At this point we might have type == TokenType.ERROR_ELEMENT if the string is unterminated.
      // We're ignoring that case since we don't have a rich enough set of signals to deduce where or
      // even if an additional quote should be added. Since strings are by default terminated it should be rare.
    }
    if (type == DartTokenTypes.CLOSING_QUOTE) {
      // The final effect is to insert matching close-quote, newline, indent, matching open-quote.
      char literalStart = token.getText().charAt(0);
      StringBuilder quotes = new StringBuilder();
      quotes.append(literalStart);
      quotes.append(literalStart);
      document.insertString(caretOffset, quotes.toString());
      caretOffset++;
      caretAdvance++;
      caretOffsetRef.set(caretOffset);
      caretAdvanceRef.set(caretAdvance);
      return Result.Default;
    }
    return Result.Continue;
  }
}
