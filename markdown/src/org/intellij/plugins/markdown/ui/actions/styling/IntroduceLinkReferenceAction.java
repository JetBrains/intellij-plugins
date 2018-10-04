// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.plugins.markdown.ui.actions.styling;

import com.intellij.codeInsight.daemon.impl.quickfix.EmptyExpression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory;
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil;
import org.jetbrains.annotations.NotNull;

import static org.intellij.plugins.markdown.lang.MarkdownElementTypes.LINK_DESTINATION;
import static org.intellij.plugins.markdown.lang.MarkdownElementTypes.LINK_TEXT;

public class IntroduceLinkReferenceAction extends AnAction implements DumbAware {
  private static final String VAR_NAME = "reference";

  @SuppressWarnings("Duplicates")
  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    final Editor editor = MarkdownActionUtil.findMarkdownTextEditor(e);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || psiFile == null || !psiFile.isValid()) {
      return;
    }

    Caret caret = editor.getCaretModel().getCurrentCaret();
    final Couple<PsiElement> elements = MarkdownActionUtil.getElementsUnderCaretOrSelection(psiFile, caret);

    if (elements == null) {
      e.getPresentation().setEnabled(false);
      return;
    }

    PsiElement parentLink = MarkdownActionUtil.getCommonParentOfTypes(elements.getFirst(), elements.getSecond(),
                                                                      TokenSet.orSet(TokenSet.create(MarkdownElementTypes.INLINE_LINK),
                                                                                     MarkdownTokenTypeSets.AUTO_LINKS));

    if (parentLink == null) {
      e.getPresentation().setEnabled(false);
      return;
    }

    e.getPresentation().setEnabled(true);
  }

  @SuppressWarnings("Duplicates")
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Editor editor = MarkdownActionUtil.findMarkdownTextEditor(e);
    final PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || file == null) {
      return;
    }

    Caret caret = editor.getCaretModel().getCurrentCaret();
    Couple<PsiElement> elements = MarkdownActionUtil.getElementsUnderCaretOrSelection(file, caret);
    assert elements != null;


    PsiElement link = MarkdownActionUtil.getCommonTopmostParentOfTypes(elements.getFirst(), elements.getSecond(),
                                                                       TokenSet.orSet(TokenSet.create(MarkdownElementTypes.INLINE_LINK),
                                                                                      MarkdownTokenTypeSets.AUTO_LINKS));

    if (link == null) {
      return;
    }

    Project project = link.getProject();
    WriteCommandAction.runWriteCommandAction(file.getProject(), () -> {
      if (!file.isValid()) {
        return;
      }

      String text = null;
      String url = null;
      String title = null;
      IElementType type = PsiUtilCore.getElementType(link);
      if (type == MarkdownElementTypes.AUTOLINK) {
        url = link.getFirstChild().getNextSibling().getText();
      }
      else if (type == MarkdownTokenTypes.GFM_AUTOLINK) {
        url = link.getText();
      }
      else if (type == MarkdownTokenTypes.EMAIL_AUTOLINK) {
        url = link.getText();
      }
      else if (type == MarkdownElementTypes.INLINE_LINK) {
        SyntaxTraverser<PsiElement> syntaxTraverser = SyntaxTraverser.psiTraverser();
        PsiElement textElement = syntaxTraverser.children(link).find(child -> PsiUtilCore.getElementType(child) == LINK_TEXT);
        if (textElement != null) {
          text = textElement.getText();
          if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);
          }
        }

        url = syntaxTraverser.children(link).find(child -> PsiUtilCore.getElementType(child) == LINK_DESTINATION).getText();

        PsiElement titleElement =
          syntaxTraverser.children(link).find(child -> PsiUtilCore.getElementType(child) == MarkdownElementTypes.LINK_TITLE);
        if (titleElement != null) {
          title = titleElement.getText();
        }
      }

      assert url != null;

      Pair<PsiElement, PsiElement> referencePair = MarkdownPsiElementFactory.createLinkReference(project, url, text, title);

      int offset = link.getTextOffset();

      insertLastNewLine(file);
      insertLastNewLine(file);
      PsiElement declaration = file.addAfter(referencePair.getFirst(), file.getLastChild());
      PsiElement reference = link.replace(referencePair.getSecond());

      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

      TemplateBuilderImpl builder = (TemplateBuilderImpl)TemplateBuilderFactory.getInstance().createTemplateBuilder(file);
      PsiElement declarationRef = declaration.getFirstChild();

      if (text != null && (text.contains("[") || text.contains("]"))) {
        builder.replaceElement(declarationRef, "declaration", new TextExpression(declarationRef.getText()), false);
        builder.replaceElement(reference, "reference", new TextExpression(reference.getText()), false);
      }
      else if (text != null) {
        builder.replaceElement(declarationRef, TextRange.create(1, declarationRef.getTextLength() - 1), VAR_NAME, new TextExpression(text),
                               false);
        builder.replaceElement(reference, TextRange.create(1, reference.getTextLength() - 1), VAR_NAME, new TextExpression(text), false);
      }
      else {
        builder.replaceElement(declarationRef, TextRange.create(1, declarationRef.getTextLength() - 1), VAR_NAME, new EmptyExpression(),
                               false);
        builder.replaceElement(reference, TextRange.create(1, reference.getTextLength() - 1), VAR_NAME, new EmptyExpression(), false);
      }

      editor.getCaretModel().moveToOffset(0);
      Template template = builder.buildInlineTemplate();

      TemplateManager.getInstance(project).startTemplate(editor, template);
      if (text != null) {
        editor.getCaretModel().moveToOffset(offset + text.length() + 2);
      }
    });
  }

  private static void insertLastNewLine(@NotNull PsiFile psiFile) {
    psiFile.addAfter(MarkdownPsiElementFactory.createNewLine(psiFile.getProject()), psiFile.getLastChild());
  }
}