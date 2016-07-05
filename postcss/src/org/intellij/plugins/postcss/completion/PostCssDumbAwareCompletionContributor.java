package org.intellij.plugins.postcss.completion;

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
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.util.CssCompletionUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssDumbAwareCompletionContributor extends CompletionContributor implements DumbAware {

  private static final AddSpaceWithBracesInsertHandler ADD_SPACE_WITH_BRACES_INSERT_HANDLER = new AddSpaceWithBracesInsertHandler(false);

  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
    result = fixPrefixForVendorPrefixes(parameters, result, CssElementTypes.CSS_ATKEYWORD);
    super.fillCompletionVariants(parameters, result);
    if (!result.isStopped()) {
      PsiElement position = parameters.getPosition();
      if (CssPsiUtil.getStylesheetLanguage(position) == PostCssLanguage.INSTANCE &&
          nestKeyword().accepts(position)) {
        result.addElement(CssCompletionUtil.lookupForKeyword("@nest", ADD_SPACE_WITH_BRACES_INSERT_HANDLER));
      }
    }
  }

  private static ElementPattern<? extends PsiElement> nestKeyword() {
    return inPostCssFile(CssElementTypes.CSS_ATKEYWORD).with(
      new PatternCondition<PsiElement>("INSIDE_RULESET") {
        @Override
        public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
          if (PsiTreeUtil.getParentOfType(element, CssSelectorList.class, CssDeclaration.class) != null) return false;
          PsiElement parent = element.getParent();
          return PostCssPsiUtil.isChildOfRuleset(parent instanceof CssAtRule ? parent : element);
        }
      });
  }

  private static PsiElementPattern.Capture<PsiElement> inPostCssFile(IElementType type) {
    return psiElement(type).inside(psiElement(PostCssElementTypes.POST_CSS_STYLESHEET));
  }

  /*TODO use CssDumbAwareCompletionContributor#fixPrefixForVendorPrefixes instead when PostCSS module will be part of API*/
  public static CompletionResultSet fixPrefixForVendorPrefixes(@NotNull CompletionParameters parameters,
                                                               @NotNull CompletionResultSet result,
                                                               @NotNull IElementType... typesToFix) {
    final PsiElement position = parameters.getPosition();
    IElementType type = position.getNode().getElementType();
    if (CssElementTypes.NAME_TOKEN_TYPES.contains(type) || type == CssElementTypes.CSS_ATKEYWORD
        || TokenSet.create(typesToFix).contains(type)) {
      final String positionText = position.getText();
      final int prefixShift = parameters.getOffset() - position.getTextRange().getStartOffset();
      if (0 < prefixShift && prefixShift < positionText.length()) {
        return result.withPrefixMatcher(result.getPrefixMatcher().cloneWithPrefix(positionText.substring(0, prefixShift)));
      }
    }
    return result;
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
          Template template = TemplateManager.getInstance(project).
            createTemplate("postcss_insert_handler_template", "postcss", " $VAR0$ {\n$END$\n}");
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