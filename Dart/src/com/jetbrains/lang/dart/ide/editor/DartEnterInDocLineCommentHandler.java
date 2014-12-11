package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.text.CharArrayUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import org.jetbrains.annotations.NotNull;


public class DartEnterInDocLineCommentHandler extends EnterHandlerDelegateAdapter {

  // NOTE: derived from com.intellij.codeInsight.editorActions.enter.EnterInLineCommentHandler
  @Override
  public Result preprocessEnter(@NotNull final PsiFile file,
                                @NotNull final Editor editor,
                                @NotNull final Ref<Integer> caretOffsetRef,
                                @NotNull final Ref<Integer> caretAdvance,
                                @NotNull final DataContext dataContext,
                                final EditorActionHandler originalHandler) {
    final int caretOffset = caretOffsetRef.get().intValue();
    final PsiElement psiAtOffset = file.findElementAt(caretOffset);
    if (psiAtOffset != null && psiAtOffset.getTextOffset() < caretOffset) {
      final ASTNode token = psiAtOffset.getNode();
      final Document document = editor.getDocument();
      final CharSequence text = document.getText();
      if (psiAtOffset.getLanguage() instanceof DartLanguage && token.getElementType() == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {
        final int offset = CharArrayUtil.shiftForward(text, caretOffset, " \t");

        if (offset < document.getTextLength() && text.charAt(offset) != '\n') {
          String prefix = DartDocUtil.SINGLE_LINE_DOC_COMMENT;
          if (!StringUtil.startsWith(text, offset, prefix)) {
            if (text.charAt(caretOffset) != ' ' && !prefix.endsWith(" ")) {
              prefix += " ";
            }
            document.insertString(caretOffset, prefix);
            return Result.Default;
          }
          else {
            final int afterPrefix = offset + prefix.length();
            if (afterPrefix < document.getTextLength() && text.charAt(afterPrefix) != ' ') {
              document.insertString(afterPrefix, " ");
              //caretAdvance.set(0);
            }
            caretOffsetRef.set(offset);
          }
          return Result.Default;
        }
      }
    }
    return Result.Continue;
  }
}
