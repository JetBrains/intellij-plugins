package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.text.CharArrayUtil;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import org.jetbrains.annotations.NotNull;


public class DartEnterInDocLineCommentHandler extends EnterHandlerDelegateAdapter {

  // EnterInLineCommentHandler doesn't work well enough for Dart doc comments
  @Override
  public Result preprocessEnter(@NotNull final PsiFile file,
                                @NotNull final Editor editor,
                                @NotNull final Ref<Integer> caretOffsetRef,
                                @NotNull final Ref<Integer> caretAdvance,
                                @NotNull final DataContext dataContext,
                                final EditorActionHandler originalHandler) {
    final int caretOffset = caretOffsetRef.get().intValue();
    final Document document = editor.getDocument();
    final PsiElement psiAtOffset = file.findElementAt(caretOffset);
    final PsiElement probablyDocComment = psiAtOffset instanceof PsiWhiteSpace && psiAtOffset.getText().startsWith("\n")
                                          ? psiAtOffset.getPrevSibling()
                                          : psiAtOffset == null && caretOffset > 0 && caretOffset == document.getTextLength()
                                            ? file.findElementAt(caretOffset - 1)
                                            : psiAtOffset;

    if (probablyDocComment != null &&
        probablyDocComment.getTextOffset() < caretOffset &&
        probablyDocComment.getNode().getElementType() == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {
      final CharSequence text = document.getCharsSequence();
      final int offset = CharArrayUtil.shiftForward(text, caretOffset, " \t");

      if (StringUtil.startsWith(text, offset, DartDocUtil.SINGLE_LINE_DOC_COMMENT)) {
        caretOffsetRef.set(offset);
      }
      else {
        final String docText = StringUtil.trimStart(probablyDocComment.getText(), DartDocUtil.SINGLE_LINE_DOC_COMMENT);
        final int spacesBeforeText = StringUtil.isEmptyOrSpaces(docText) ? 1 : StringUtil.countChars(docText, ' ', 0, true);
        final int spacesToAdd = Math.max(0, spacesBeforeText - StringUtil.countChars(text, ' ', caretOffset, true));
        document.insertString(caretOffset, DartDocUtil.SINGLE_LINE_DOC_COMMENT + StringUtil.repeatSymbol(' ', spacesToAdd));
        caretAdvance.set(spacesBeforeText);
      }
      return Result.Default;
    }

    return Result.Continue;
  }
}
