// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring.introduce;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pass;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.IntroduceTargetChooser;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.refactoring.ui.NameSuggestionsField;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBUI;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.DartInlineHandler;
import com.jetbrains.lang.dart.ide.refactoring.ServerExtractLocalVariableRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.ServerRefactoringDialog;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DartServerExtractLocalVariableHandler implements RefactoringActionHandler {
  @Override
  public void invoke(@NotNull Project project, PsiElement @NotNull [] elements, DataContext dataContext) {
  }

  @Override
  public void invoke(final @NotNull Project project, final Editor editor, PsiFile file, DataContext dataContext) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), "1.14") < 0) {
      return;
    }

    if (editor == null || file == null) {
      return;
    }
    if (!CommonRefactoringUtil.checkReadOnlyStatus(file)) {
      return;
    }
    new ExtractLocalVariableProcessor(project, editor, file).perform();
  }
}


class ExtractLocalVariableProcessor {
  final @NotNull Project project;
  final @NotNull Editor editor;
  final @NotNull PsiFile file;

  ServerExtractLocalVariableRefactoring refactoring;

  ExtractLocalVariableProcessor(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    this.project = project;
    this.editor = editor;
    this.file = file;
  }

  public void perform() {
    final SelectionModel selectionModel = editor.getSelectionModel();
    final int offset = selectionModel.getSelectionStart();
    final int length = selectionModel.getSelectionEnd() - offset;
    // create refactoring
    createRefactoring(offset, length);
    if (refactoring == null) {
      return;
    }
    // prepare expressions
    final java.util.List<DartExpression> expressions;
    {
      final int[] offsets = refactoring.getCoveringExpressionOffsets();
      final int[] lengths = refactoring.getCoveringExpressionLengths();
      expressions = getDartExpressions(offsets, lengths);
      if (expressions == null) {
        return;
      }
    }
    // select the expression to extract
    if (expressions.size() == 1 || ApplicationManager.getApplication().isUnitTestMode()) {
      performOnExpression(expressions.get(0));
    }
    else if (expressions.size() > 1) {
      IntroduceTargetChooser.showChooser(editor, expressions, new Pass<>() {
        @Override
        public void pass(DartExpression expression) {
          performOnExpression(expression);
        }
      }, PsiElement::getText);
    }
  }

  private void createRefactoring(int offset, int length) {
    refactoring = new ServerExtractLocalVariableRefactoring(project, file.getVirtualFile(), offset, length);
    final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
    if (DartInlineHandler.showMessageIfError(project, editor, initialStatus)) {
      refactoring = null;
    }
  }

  private @Nullable DartExpression findExpressionWithRange(int offset, int length) {
    return PsiTreeUtil.findElementOfClassAtRange(file, offset, offset + length, DartExpression.class);
  }

  private @Nullable List<DartExpression> getDartExpressions(int[] offsets, int[] lengths) {
    final List<DartExpression> expressions = new ArrayList<>();
    for (int i = 0; i < offsets.length; i++) {
      final DartExpression expression = findExpressionWithRange(offsets[i], lengths[i]);
      if (expression == null) {
        return null;
      }
      expressions.add(expression);
    }
    return expressions;
  }

  private void performInPlace() {
    final String[] names = refactoring.getNames();
    if (names.length != 0) {
      refactoring.setName(names[0]);
    }
    // validate final status
    {
      final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
      if (DartInlineHandler.showMessageIfError(project, editor, finalConditions)) {
        return;
      }
    }
    // Apply the change.
    ApplicationManager.getApplication().runWriteAction(() -> {
      final SourceChange change = refactoring.getChange();
      assert change != null;
      try {
        AssistUtils.applySourceChange(project, change, true);
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
    });
  }

  private void performOnElementOccurrences() {
    if (editor.getSettings().isVariableInplaceRenameEnabled()) {
      performInPlace();
    }
    else {
      new DartServerExtractLocalVariableDialog(project, editor, refactoring).showAndGet();
    }
  }

  private void performOnExpression(DartExpression expression) {
    final int offset = expression.getTextOffset();
    final int length = expression.getTextLength();
    createRefactoring(offset, length);
    if (refactoring == null) {
      return;
    }
    // prepare occurrences
    final java.util.List<DartExpression> occurrences;
    {
      final int[] occurrencesOffsets = refactoring.getOccurrencesOffsets();
      final int[] occurrencesLengths = refactoring.getOccurrencesLengths();
      occurrences = getDartExpressions(occurrencesOffsets, occurrencesLengths);
      if (occurrences == null) {
        return;
      }
    }
    // handle occurrences
    OccurrencesChooser.<DartExpression>simpleChooser(editor).showChooser(expression, occurrences, new Pass<>() {
      @Override
      public void pass(OccurrencesChooser.ReplaceChoice replaceChoice) {
        refactoring.setExtractAll(replaceChoice == OccurrencesChooser.ReplaceChoice.ALL);
        performOnElementOccurrences();
      }
    });
  }
}

class DartServerExtractLocalVariableDialog extends ServerRefactoringDialog<ServerExtractLocalVariableRefactoring> {
  private final NameSuggestionsField myVariableNameField;

  DartServerExtractLocalVariableDialog(@NotNull Project project,
                                       @NotNull Editor editor,
                                       @NotNull ServerExtractLocalVariableRefactoring refactoring) {
    super(project, editor, refactoring);

    final String[] names = refactoring.getNames();
    myVariableNameField = new NameSuggestionsField(names, project, DartFileType.INSTANCE);

    setTitle(DartBundle.message("dialog.title.extract.local.variable"));
    init();

    final String name = StringUtil.notNullize(ArrayUtil.getFirstElement(names), "name");
    myRefactoring.setName(name);
    myVariableNameField.addDataChangedListener(() -> {
      final String name1 = myVariableNameField.getEnteredName();
      myRefactoring.setName(name1);
    });
  }

  @Override
  protected JComponent createCenterPanel() {
    return null;
  }

  @Override
  protected JComponent createNorthPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.gridx = 0;
    gbConstraints.gridy = 0;
    gbConstraints.gridwidth = 1;
    gbConstraints.weightx = 0;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.WEST;
    JLabel nameLabel = new JLabel();
    panel.add(nameLabel, gbConstraints);
    nameLabel.setText(DartBundle.message("label.text.name"));

    gbConstraints.insets = JBUI.insets(0, 4, 4, 0);
    gbConstraints.gridx = 1;
    gbConstraints.gridy = 0;
    gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gbConstraints.weightx = 1;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(myVariableNameField, gbConstraints);
    myVariableNameField.setPreferredSize(new Dimension(300, myVariableNameField.getPreferredSize().height));

    return panel;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return myVariableNameField;
  }
}
