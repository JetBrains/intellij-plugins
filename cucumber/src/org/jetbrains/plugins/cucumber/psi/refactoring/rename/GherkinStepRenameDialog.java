// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ReadOnlyFragmentModificationException;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.ReadonlyFragmentModificationHandler;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import org.intellij.lang.regexp.RegExpCapability;
import org.intellij.lang.regexp.RegExpLexer;
import org.intellij.lang.regexp.RegExpTT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridBagConstraints;
import java.util.EnumSet;
import java.util.List;

/// A rename dialog for Cucumber step definitions.
///
/// Features:
/// - guarding regex/cukex parameters (because it is unclear how rename should proceed in such a case)
/// - showing a warning if the user introduces a new cukex parameter
///   (because a better solution – similar to "introduce parameter refactoring" in Java – is not implemented yet. See IDEA-376074)
///
/// It is not clear whether the equivalent functionality can be provided with inline rename, so it is a dialog.
public final class GherkinStepRenameDialog extends RenameDialog {
  private AbstractStepDefinition stepDefinition;
  private int oldParameterCount;

  public GherkinStepRenameDialog(@NotNull Project project,
                                 @NotNull PsiElement psiElement,
                                 @Nullable PsiElement nameSuggestionContext, Editor editor) {
    super(project, psiElement, nameSuggestionContext, editor);
  }

  @Override
  protected RenameProcessor createRenameProcessor(@NotNull String newName) {
    // Called when the user clicks the "Refactor" button (when the refactoring is accepted)
    return new RenameProcessor(getProject(), getPsiElement(), newName,
                               getRefactoringScope(), isSearchInComments(), isSearchInNonJavaFiles());
  }

  @Override
  protected String getFullName() {
    return CucumberBundle.message("cucumber.step");
  }

  @Override
  protected void canRun() throws ConfigurationException {
    if (getNewName().isEmpty()) {
      throw new ConfigurationException(CucumberBundle.message("cucumber.refactor.rename.cannot.be.empty"));
    }

    final int newParameterCount = CucumberUtil.getCukexHighlightRanges(getNewName()).size();
    if (oldParameterCount != newParameterCount) {
      final int delta = newParameterCount - oldParameterCount;
      throw new ConfigurationException(CucumberBundle.message("cucumber.refactor.rename.new.parameter.introduced", delta));
    }
  }

  @Override
  protected void createNewNameComponent() {
    super.createNewNameComponent();

    final Runnable guardRunnable = () -> {
      final Editor editor = getNameSuggestionsField().getEditor();
      // Bug: on the first show of the dialog, this editor is null. On the following shows, it works fine.
      if (editor != null) {
        // By default, the rename dialog starts with the name being selected. We don't want that, so we remove selection
        editor.getSelectionModel().removeSelection();

        // By default, the rename dialog starts with the caret at offset 0. It seems more logical to put the caret at the end.
        editor.getCaretModel().moveToOffset(editor.getDocument().getTextLength());

        final Document document = editor.getDocument();
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(document, new ReadonlyFragmentModificationHandler() {
          @Override
          public void handle(ReadOnlyFragmentModificationException e) {
            //do nothing
          }
        });

        String expr = stepDefinition.getExpression();
        if (expr == null) throw new IllegalStateException("expression in the step definition being renamed must not be null");
        if (CucumberUtil.isCucumberExpression(expr)) {
          guardCukexSpecialSymbols(editor);
        }
        else {
          guardRegexSpecialSymbols(editor);
        }
      }
    };

    // TODO: Migrate to ApplicationManager.getApplication().invokeLater with the right ModalityState
    SwingUtilities.invokeLater(guardRunnable);
  }

  @RequiresEdt
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

  @RequiresEdt
  private static void guardCukexSpecialSymbols(@NotNull Editor editor) {
    final String text = editor.getDocument().getText();
    final List<TextRange> ranges = CucumberUtil.getCukexHighlightRanges(text);
    for (final TextRange range : ranges) {
      editor.getDocument().createGuardedBlock(range.getStartOffset(), range.getEndOffset());
    }
  }

  @Override
  public String[] getSuggestedNames() {
    if (stepDefinition == null) {
      final CucumberStepReference ref = CucumberUtil.getCucumberStepReference(getPsiElement());
      if (ref != null) {
        stepDefinition = ref.resolveToDefinition();
      }
    }
    if (stepDefinition == null) {
      throw new IllegalStateException("step definition must not be null");
    }

    final String regexOrCukex = stepDefinition.getExpression();
    if (regexOrCukex != null) {
      if (CucumberUtil.isCucumberExpression(regexOrCukex)) {
        oldParameterCount = CucumberUtil.getCukexHighlightRanges(regexOrCukex).size();
        return new String[]{regexOrCukex};
      }
      String result = StringUtil.trimStart(regexOrCukex, "^");
      result = StringUtil.trimEnd(result, "$");
      return new String[]{result};
    }

    return super.getSuggestedNames();
  }

  @Override
  protected void processNewNameChanged() {
    super.processNewNameChanged();
    // The overridden method sets these to "false" in ConfigurationException was thrown. We don't want to be so restrictive. 
    getPreviewAction().setEnabled(true);
    getRefactorAction().setEnabled(true);
  }

  @Override
  protected void createCheckboxes(JPanel panel, GridBagConstraints gbConstraints) {
    super.createCheckboxes(panel, gbConstraints);
    getCbSearchInComments().setVisible(false);
  }
}
