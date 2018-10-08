// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.plugins.markdown.ui.actions.styling;

import com.intellij.codeInsight.daemon.impl.quickfix.EmptyExpression;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestinationImpl;
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static org.intellij.plugins.markdown.lang.MarkdownElementTypes.FULL_REFERENCE_LINK;
import static org.intellij.plugins.markdown.lang.MarkdownElementTypes.INLINE_LINK;

public class MarkdownIntroduceLinkReferenceAction extends AnAction implements DumbAware {
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

    PsiElement parentLink =
      MarkdownActionUtil.getCommonParentOfTypes(elements.getFirst(), elements.getSecond(), MarkdownTokenTypeSets.LINKS);

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

    PsiElement link =
      MarkdownActionUtil.getCommonTopmostParentOfTypes(elements.getFirst(), elements.getSecond(), MarkdownTokenTypeSets.LINKS);

    if (link == null) {
      return;
    }

    Project project = link.getProject();
    WriteCommandAction.runWriteCommandAction(file.getProject(), () -> {
      if (!file.isValid()) {
        return;
      }

      Pair<PsiElement, PsiElement> referencePair = MarkdownActionUtil.createLinkDeclarationAndReference(project, link, "reference");

      insertLastNewLine(file);
      insertLastNewLine(file);
      PsiElement declaration = file.addAfter(referencePair.getSecond(), file.getLastChild());
      PsiElement reference = link.replace(referencePair.getFirst());

      String url = Objects.requireNonNull(PsiTreeUtil.getChildOfType(declaration, MarkdownLinkDestinationImpl.class)).getText();

      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

      TemplateBuilderImpl builder = (TemplateBuilderImpl)TemplateBuilderFactory.getInstance().createTemplateBuilder(file);
      PsiElement declarationLabel = declaration.getFirstChild();
      PsiElement referenceLabel = reference.getFirstChild().getLastChild();

      Expression expression = ApplicationManager.getApplication().isUnitTestMode() ? new TextExpression("reference") : new EmptyExpression();
      builder
        .replaceElement(declarationLabel, TextRange.create(1, declarationLabel.getTextLength() - 1), VAR_NAME, expression, true);
      builder
        .replaceElement(referenceLabel, TextRange.create(1, referenceLabel.getTextLength() - 1), VAR_NAME, expression, true);

      editor.getCaretModel().moveToOffset(0);
      Template template = builder.buildInlineTemplate();

      TemplateManager.getInstance(project).startTemplate(editor, template, new DuplicatesFinder(file, editor, url));
    });
  }

  private static void insertLastNewLine(@NotNull PsiFile psiFile) {
    psiFile.addAfter(MarkdownPsiElementFactory.createNewLine(psiFile.getProject()), psiFile.getLastChild());
  }

  private static class DuplicatesFinder extends TemplateEditingAdapter {
    @NotNull private final String myUrl;
    @NotNull private final PsiFile myFile;
    @NotNull private final Editor myEditor;

    private DuplicatesFinder(@NotNull PsiFile file, @NotNull Editor editor, @NotNull String url) {
      myUrl = url;
      myFile = file;
      myEditor = editor;
    }

    @Override
    public void currentVariableChanged(@NotNull TemplateState templateState, Template template, int oldIndex, int newIndex) {
      if (!ApplicationManager.getApplication().isUnitTestMode() && (oldIndex != 0 || newIndex != 1) && (oldIndex != -1 || newIndex != -1)) {
        return;
      }

      TextResult reference = templateState.getVariableValue(VAR_NAME);
      if (reference == null) {
        return;
      }

      processDuplicates(reference.getText());
    }

    public void processDuplicates(@NotNull String referenceText) {
      PsiElement[] duplicatedLinks =
        PsiTreeUtil.collectElements(myFile, new PsiElementFilter() {
          @Override
          public boolean isAccepted(PsiElement element) {
            return MarkdownTokenTypeSets.AUTO_LINKS.contains(PsiUtilCore.getElementType(element))
                   && myUrl.equals(MarkdownActionUtil.getUrl(element))
                   //inside inline links
                   && PsiTreeUtil.findFirstParent(element, true, element1 ->  PsiUtilCore.getElementType(element1) == INLINE_LINK) == null
                   //generated link
                   && PsiTreeUtil.findFirstParent(element, element1 -> PsiUtilCore.getElementType(element1) == FULL_REFERENCE_LINK) == null;
          }
        });

      if (duplicatedLinks.length > 0) {
        List<SmartPsiElementPointer<PsiElement>> duplicates =
          ContainerUtil.map(duplicatedLinks, link -> SmartPointerManager.createPointer(link));

        if (ApplicationManager.getApplication().isUnitTestMode()) {
          MarkdownActionUtil.replaceDuplicates(myFile, myEditor, duplicates, referenceText);
        }
        else {
          ApplicationManager.getApplication().invokeLater(
            () -> MarkdownActionUtil.replaceDuplicates(myFile, myEditor, duplicates, referenceText));
        }
      }
    }
  }
}