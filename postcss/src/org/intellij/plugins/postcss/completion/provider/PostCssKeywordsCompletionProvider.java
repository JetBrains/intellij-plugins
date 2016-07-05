package org.intellij.plugins.postcss.completion.provider;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.CompleteMacro;
import com.intellij.css.util.CssPsiUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.css.util.CssCompletionUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssKeywordsCompletionProvider extends CompletionProvider<CompletionParameters> {

  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                ProcessingContext context,
                                @NotNull CompletionResultSet result) {
    boolean indentBased = CssPsiUtil.isIndentBasedCssLanguage(parameters.getPosition());
    AddSpaceWithBracesInsertHandler addSpaceWithBracesInsertHandler = new AddSpaceWithBracesInsertHandler(indentBased);

    addAtKeyword("@nest", addSpaceWithBracesInsertHandler, result);
  }

  private static void addAtKeyword(@NotNull String lookupString,
                                   @Nullable InsertHandler<LookupElement> insertHandler,
                                   @NotNull CompletionResultSet result) {
    result.addElement(CssCompletionUtil.lookupForKeyword(lookupString, insertHandler));
  }

  /*TODO Use CssAtKeywordsCompletionProvider#AddSpaceWithBracesInsertHandler instead when PostCSS module will be part of API*/

  private static class AddSpaceWithBracesInsertHandler implements InsertHandler<LookupElement> {
    private final boolean myIndentBased;

    private AddSpaceWithBracesInsertHandler(boolean indentBased) {
      myIndentBased = indentBased;
    }

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
      context.setAddCompletionChar(false);
      Editor editor = context.getEditor();
      if (myIndentBased) {
        typeOrMove(editor, ' ');
        AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
      }
      else {
        int offset = skipWhiteSpaces(editor, editor.getCaretModel().getOffset());
        if (editor.getDocument().getCharsSequence().charAt(offset) != '{') {
          Project project = editor.getProject();
          Template
            template =
            TemplateManager.getInstance(project).createTemplate("postcss_insert_handler_template", "postcss", " $VAR0$ {\n$END$\n}");
          template.addVariable("VAR0", new MacroCallNode(new CompleteMacro()), true);
          template.setToReformat(true);
          TemplateManager.getInstance(project).startTemplate(editor, template);
        }
        else {
          typeOrMove(editor, ' ');
          AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
        }
      }
    }
  }

  private static int skipWhiteSpaces(Editor editor, int offset) {
    CharSequence sequence = editor.getDocument().getCharsSequence();
    while (offset < sequence.length() && StringUtil.isWhiteSpace(sequence.charAt(offset))) {
      offset += 1;
    }
    return Math.min(sequence.length() - 1, offset);
  }

  private static void typeOrMove(@NotNull Editor editor, char ch) {
    if (!isCaretAtChar(editor, ch)) {
      EditorModificationUtil.insertStringAtCaret(editor, String.valueOf(ch));
    }
    else {
      EditorModificationUtil.moveCaretRelatively(editor, 1);
    }
  }

  private static boolean isCaretAtChar(Editor editor, char ch) {
    final int startOffset = editor.getCaretModel().getOffset();
    final Document document = editor.getDocument();
    return document.getTextLength() > startOffset && document.getCharsSequence().charAt(startOffset) == ch;
  }
}