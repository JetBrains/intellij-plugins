package org.intellij.plugins.markdown.ui.actions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MarkdownActionUtil {
  @Nullable
  public static SplitFileEditor findSplitEditor(AnActionEvent e) {
    final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
    if (editor instanceof SplitFileEditor) {
      return (SplitFileEditor)editor;
    }
    else {
      return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
    }
  }

  @Nullable
  public static Editor findMarkdownTextEditor(AnActionEvent e) {
    final SplitFileEditor splitEditor = findSplitEditor(e);
    if (splitEditor == null) {
      return null;
    }

    if (!(splitEditor.getMainEditor() instanceof TextEditor)) {
      return null;
    }
    final TextEditor mainEditor = (TextEditor)splitEditor.getMainEditor();
    if (!mainEditor.getComponent().isVisible()) {
      return null;
    }

    return mainEditor.getEditor();
  }

  @Nullable
  public static PsiElement getCommonParentOfType(@NotNull PsiFile file, @NotNull Caret caret, @NotNull final IElementType elementType) {
    final PsiElement base;
    if (caret.getSelectionStart() == caret.getSelectionEnd()) {
      base = file.findElementAt(caret.getOffset());
    }
    else {
      final PsiElement startElement = file.findElementAt(caret.getSelectionStart());
      final PsiElement endElement = file.findElementAt(caret.getSelectionEnd());
      if (startElement == null || endElement == null) {
        return null;
      }

      base = PsiTreeUtil.findCommonParent(startElement, endElement);
    }

    return PsiTreeUtil.findFirstParent(base, false, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        final ASTNode node = element.getNode();
        return node != null && node.getElementType() == elementType;
      }
    });
  }

  @NotNull
  public static SelectionState getCommonState(@NotNull PsiFile file, @NotNull Caret caret, @NotNull final IElementType stateInQuestion) {
    return getCommonParentOfType(file, caret, stateInQuestion) == null ? SelectionState.NO : SelectionState.YES;
  }

  public enum SelectionState {
    YES,
    NO,
    INCONSISTENT
  }
}
