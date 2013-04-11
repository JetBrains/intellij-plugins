package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.file.HbFileViewProvider;
import com.dmarcotte.handlebars.psi.*;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for custom plugin actions on chars typed by the user.  See {@link HbEnterHandler} for custom actions
 * on Enter.
 */
public class HbTypedHandler extends TypedHandlerDelegate {
  @Override
  public Result beforeCharTyped(char c, Project project, Editor editor, PsiFile file, FileType fileType) {
    int offset = editor.getCaretModel().getOffset();

    if (offset == 0 || offset > editor.getDocument().getTextLength()) {
      return Result.CONTINUE;
    }

    String previousChar = editor.getDocument().getText(new TextRange(offset - 1, offset));

    if (file.getViewProvider() instanceof HbFileViewProvider) {
      PsiDocumentManager.getInstance(project).commitAllDocuments();

      // we suppress the built-in "}" auto-complete when we see "{{"
      if (c == '{' && previousChar.equals("{")) {
        // since the "}" autocomplete is built in to IDEA, we need to hack around it a bit by
        // intercepting it before it is inserted, doing the work of inserting for the user
        // by inserting the '{' the user just typed...
        editor.getDocument().insertString(offset, Character.toString(c));
        // ... and position their caret after it as they'd expect...
        editor.getCaretModel().moveToOffset(offset + 1);

        // ... then finally telling subsequent responses to this charTyped to do nothing
        return Result.STOP;
      }
    }

    return Result.CONTINUE;
  }

  @Override
  public Result charTyped(char c, Project project, Editor editor, @NotNull PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    FileViewProvider provider = file.getViewProvider();

    if (offset < 2 || offset > editor.getDocument().getTextLength()) {
      return Result.CONTINUE;
    }

    String previousChar = editor.getDocument().getText(new TextRange(offset - 2, offset - 1));

    if (file.getViewProvider() instanceof HbFileViewProvider) {
      // if we're looking at a close stache, we may have some business too attend to
      if (c == '}' && previousChar.equals("}")) {
        autoInsertCloseTag(project, offset, editor, provider);
        adjustMustacheFormatting(project, offset, editor, file, provider);
      }
    }

    return Result.CONTINUE;
  }

  /**
   * When appropriate, auto-inserts Handlebars close tags.  i.e.  When "{{#tagId}}" or "{{^tagId}} is typed,
   * {{/tagId}} is automatically inserted
   */
  private void autoInsertCloseTag(Project project, int offset, Editor editor, FileViewProvider provider) {
    if (!HbConfig.isAutoGenerateCloseTagEnabled()) {
      return;
    }

    PsiDocumentManager.getInstance(project).commitAllDocuments();

    PsiElement elementAtCaret = provider.findElementAt(offset - 1, HbLanguage.class);

    PsiElement openTag = HbPsiUtil.findParentOpenTagElement(elementAtCaret);

    if (openTag != null && openTag.getChildren().length > 1) {
      // we've got an open block type stache... find its "name" (its first path element)
      HbPsiElement pathElem = (HbPsiElement)openTag.getChildren()[1];

      if (pathElem != null
          && pathElem instanceof HbPath) {
        // insert the corresponding close tag
        editor.getDocument().insertString(offset, "{{/" + pathElem.getText() + "}}");
      }
    }
  }

  /**
   * When appropriate, adjusts the formatting for some 'staches, particularily close 'staches
   * and simple inverses ("{{^}}" and "{{else}}")
   */
  private void adjustMustacheFormatting(Project project, int offset, Editor editor, PsiFile file, FileViewProvider provider) {
    if (!HbConfig.isFormattingEnabled()) {
      // formatting disabled; nothing to do
      return;
    }

    PsiElement elementAtCaret = provider.findElementAt(offset - 1, HbLanguage.class);
    PsiElement closeOrSimpleInverseParent = PsiTreeUtil.findFirstParent(elementAtCaret, true, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        return element != null
               && (element instanceof HbSimpleInverse
                   || element instanceof HbCloseBlockMustache);
      }
    });

    // run the formatter if the user just completed typing a SIMPLE_INVERSE or a CLOSE_BLOCK_STACHE
    if (closeOrSimpleInverseParent != null) {
      // grab the current caret position (AutoIndentLinesHandler is about to mess with it)
      PsiDocumentManager.getInstance(project).commitAllDocuments();
      CaretModel caretModel = editor.getCaretModel();
      CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
      codeStyleManager.adjustLineIndent(file, editor.getDocument().getLineStartOffset(caretModel.getLogicalPosition().line));
    }
  }
}
