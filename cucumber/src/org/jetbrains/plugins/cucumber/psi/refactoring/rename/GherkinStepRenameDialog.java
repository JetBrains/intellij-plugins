// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ReadOnlyFragmentModificationException;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.ReadonlyFragmentModificationHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameProcessor;
import org.intellij.lang.regexp.RegExpCapability;
import org.intellij.lang.regexp.RegExpLexer;
import org.intellij.lang.regexp.RegExpTT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;
import java.util.List;

import static org.jetbrains.plugins.cucumber.CucumberUtil.getCucumberStepReference;

public final class GherkinStepRenameDialog extends RenameDialog {
  private AbstractStepDefinition myStepDefinition;

  public GherkinStepRenameDialog(@NotNull Project project,
                                 @NotNull PsiElement psiElement,
                                 @Nullable PsiElement nameSuggestionContext, Editor editor) {
    super(project, psiElement, nameSuggestionContext, editor);
  }

  @Override
  protected RenameProcessor createRenameProcessor(@NotNull String newName) {
    return new RenameProcessor(getProject(), getPsiElement(), newName,
                               getRefactoringScope(), isSearchInComments(), isSearchInNonJavaFiles());
  }

  @Override
  protected String getFullName() {
    return CucumberBundle.message("cucumber.step");
  }

  @Override
  protected boolean areButtonsValid() {
    final String newName = getNewName();
    if (newName.isEmpty()) return false;

    // Cucumber steps are natural language, so in theory – any text should be fine
    // TODO: IDEA-375196 Warn about renaming a Cucumber Expression if a new parameter is introduced
    return true;
  }

  @Override
  protected void createNewNameComponent() {
    super.createNewNameComponent();

    final Runnable guardRunnable = () -> {
      final Editor editor = getNameSuggestionsField().getEditor();
      if (editor != null) {
        editor.getSelectionModel().removeSelection();
        editor.getCaretModel().moveToOffset(0);
        final Document document = editor.getDocument();
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(document, new ReadonlyFragmentModificationHandler() {
          @Override
          public void handle(final ReadOnlyFragmentModificationException e) {
            //do nothing
          }
        });

        String expr = myStepDefinition.getExpression();
        if (expr == null) throw new IllegalStateException("expression in the step definition being renamed must not be null");
        if (CucumberUtil.isCucumberExpression(expr)) {
          guardCukexSpecialSymbols(editor);
        }
        else {
          guardRegexSpecialSymbols(editor);
        }
      }
    };

    SwingUtilities.invokeLater(guardRunnable);
  }

  private AbstractStepDefinition getStepDefinition() {
    if (myStepDefinition == null) {
      final CucumberStepReference ref = getCucumberStepReference(getPsiElement());
      if (ref != null) {
        myStepDefinition = ref.resolveToDefinition();
      }
    }
    return myStepDefinition;
  }

  private static void guardRegexSpecialSymbols(@NotNull Editor editor) {
    final String text = editor.getDocument().getText();
    final RegExpLexer lexer = new RegExpLexer(EnumSet.noneOf(RegExpCapability.class));

    lexer.start(text);
    while (lexer.getTokenType() != null) {
      if (lexer.getTokenType() != RegExpTT.CHARACTER) {
        editor.getDocument().createGuardedBlock(lexer.getTokenStart(), lexer.getTokenEnd());
      }
      lexer.advance();
    }
  }

  private static void guardCukexSpecialSymbols(@NotNull Editor editor) {
    final String text = editor.getDocument().getText();
    final List<TextRange> ranges = CucumberUtil.getCukexHighlightRanges(text);
    for (final TextRange range : ranges) {
      editor.getDocument().createGuardedBlock(range.getStartOffset(), range.getEndOffset());
    }
  }

  @Override
  public String[] getSuggestedNames() {
    AbstractStepDefinition stepDefinition = getStepDefinition();
    if (stepDefinition != null) {
      final String regexOrCukex = stepDefinition.getExpression();
      if (regexOrCukex != null) {
        if (CucumberUtil.isCucumberExpression(regexOrCukex)) return new String[]{regexOrCukex};
        String result = StringUtil.trimStart(regexOrCukex, "^");
        result = StringUtil.trimEnd(result, "$");
        return new String[]{result};
      }
    }

    return super.getSuggestedNames();
  }

  @Override
  protected void processNewNameChanged() {
    getPreviewAction().setEnabled(true);
    getRefactorAction().setEnabled(true);
  }

  @Override
  protected void createCheckboxes(JPanel panel, GridBagConstraints gbConstraints) {
    super.createCheckboxes(panel, gbConstraints);
    getCbSearchInComments().setVisible(false);
  }
}
