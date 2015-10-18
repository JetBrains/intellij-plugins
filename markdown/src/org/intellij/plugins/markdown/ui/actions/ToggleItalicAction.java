package org.intellij.plugins.markdown.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.jetbrains.annotations.NotNull;

public class ToggleItalicAction extends ToggleAction implements DumbAware {
  private static final Logger LOG = Logger.getInstance(ToggleItalicAction.class);

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(MarkdownActionUtil.findMarkdownTextEditor(e) != null);
    super.update(e);
  }

  @Override
  public boolean isSelected(AnActionEvent e) {
    final Editor editor = MarkdownActionUtil.findMarkdownTextEditor(e);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || psiFile == null) {
      return false;
    }

    MarkdownActionUtil.SelectionState lastState = null;
    for (Caret caret : editor.getCaretModel().getAllCarets()) {
      final MarkdownActionUtil.SelectionState state = MarkdownActionUtil.getCommonState(psiFile, caret, MarkdownElementTypes.EMPH);
      if (lastState == null) {
        lastState = state;
      }
      else if (lastState != state) {
        lastState = MarkdownActionUtil.SelectionState.INCONSISTENT;
        break;
      }
    }

    if (lastState == MarkdownActionUtil.SelectionState.INCONSISTENT) {
      e.getPresentation().setEnabled(false);
      return false;
    }
    else {
      e.getPresentation().setEnabled(true);
      return lastState == MarkdownActionUtil.SelectionState.YES;
    }
  }

  @Override
  public void setSelected(AnActionEvent e, final boolean state) {
    final Editor editor = MarkdownActionUtil.findMarkdownTextEditor(e);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || psiFile == null) {
      return;
    }


    WriteCommandAction.runWriteCommandAction(psiFile.getProject(), new Runnable() {
      @Override
      public void run() {
        if (!psiFile.isValid()) {
          return;
        }

        final Document document = editor.getDocument();
        for (Caret caret : ContainerUtil.reverse(editor.getCaretModel().getAllCarets())) {
          if (!state) {
            final PsiElement closestEmph = MarkdownActionUtil.getCommonParentOfType(psiFile, caret, MarkdownElementTypes.EMPH);
            if (closestEmph == null) {
              LOG.warn("Could not find enclosing element on its destruction");
              continue;
            }

            final TextRange range = closestEmph.getTextRange();
            removeEmphFromSelection(document, caret, range);
          }
          else {
            addEmphToSelection(document, caret);
          }
        }

        PsiDocumentManager.getInstance(psiFile.getProject()).commitDocument(document);
      }
    });

  }

  public void removeEmphFromSelection(@NotNull Document document, @NotNull Caret caret, @NotNull TextRange range) {

    // Easy case --- selection corresponds to some emph
    if (range.getStartOffset() + 1 == caret.getSelectionStart() && range.getEndOffset() - 1 == caret.getSelectionEnd()) {
      document.deleteString(range.getEndOffset() - 1, range.getEndOffset());
      document.deleteString(range.getStartOffset(), range.getStartOffset() + 1);
      return;
    }

    final CharSequence text = document.getCharsSequence();
    char emphChar = text.charAt(range.getStartOffset());

    int from = caret.getSelectionStart();
    int to = caret.getSelectionEnd();

    while (from > range.getStartOffset() && Character.isWhitespace(text.charAt(from - 1))) {
      from--;
    }
    while (to + 1 < range.getEndOffset() && Character.isWhitespace(text.charAt(to))) {
      to++;
    }

    if (to + 1 == range.getEndOffset()) {
      document.deleteString(range.getEndOffset() - 1, range.getEndOffset());
    }
    else {
      document.insertString(to, String.valueOf(emphChar));
    }

    if (from - 1 == range.getStartOffset()) {
      document.deleteString(range.getStartOffset(), range.getStartOffset() + 1);
    }
    else {
      document.insertString(from, String.valueOf(emphChar));
    }
  }

  public void addEmphToSelection(@NotNull Document document, @NotNull Caret caret) {
    int from = caret.getSelectionStart();
    int to = caret.getSelectionEnd();

    final CharSequence text = document.getCharsSequence();
    while (from < to && Character.isWhitespace(text.charAt(from))) {
      from++;
    }
    while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
      to--;
    }

    if (from == to) {
      from = caret.getSelectionStart();
      to = caret.getSelectionEnd();
    }

    final char emphChar = isWord(text, from, to) ? '_' : '*';
    document.insertString(to, String.valueOf(emphChar));
    document.insertString(from, String.valueOf(emphChar));

    if (caret.getSelectionStart() == caret.getSelectionEnd()) {
      caret.moveCaretRelatively(1, 0, false, false);
    }
  }

  private static boolean isWord(@NotNull CharSequence text, int from, int to) {
    return (from == 0 || !Character.isLetterOrDigit(text.charAt(from - 1)))
           && (to == text.length() || !Character.isLetterOrDigit(text.charAt(to)));
  }
}
