/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.refactoring.introduce;

import com.google.common.collect.Lists;
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
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
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
import java.util.List;

public class DartServerExtractLocalVariableHandler implements RefactoringActionHandler {
  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

  @Override
  public void invoke(final @NotNull Project project, final Editor editor, PsiFile file, DataContext dataContext) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), "1.14") < 0) {
      new DartIntroduceVariableHandler().invoke(project, editor, file, dataContext);
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
      IntroduceTargetChooser.showChooser(editor, expressions, new Pass<DartExpression>() {
        @Override
        public void pass(DartExpression expression) {
          performOnExpression(expression);
        }
      }, expression -> expression.getText());
    }
  }

  private void createRefactoring(int offset, int length) {
    refactoring = new ServerExtractLocalVariableRefactoring(project, file.getVirtualFile(), offset, length);
    final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
    if (showMessageIfError(initialStatus)) {
      refactoring = null;
    }
  }

  @Nullable
  private DartExpression findExpressionWithRange(int offset, int length) {
    return PsiTreeUtil.findElementOfClassAtRange(file, offset, offset + length, DartExpression.class);
  }

  @Nullable
  private List<DartExpression> getDartExpressions(int[] offsets, int[] lengths) {
    final List<DartExpression> expressions = Lists.newArrayList();
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
      if (showMessageIfError(finalConditions)) {
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
    OccurrencesChooser.<DartExpression>simpleChooser(editor)
      .showChooser(expression, occurrences, new Pass<OccurrencesChooser.ReplaceChoice>() {
        @Override
        public void pass(OccurrencesChooser.ReplaceChoice replaceChoice) {
          refactoring.setExtractAll(replaceChoice == OccurrencesChooser.ReplaceChoice.ALL);
          performOnElementOccurrences();
        }
      });
  }

  private boolean showMessageIfError(@Nullable final RefactoringStatus status) {
    if (status == null) {
      return true;
    }
    if (status.hasError()) {
      final String message = status.getMessage();
      assert message != null;
      CommonRefactoringUtil.showErrorHint(project, editor, message, CommonBundle.getErrorTitle(), null);
      return true;
    }
    return false;
  }
}

class DartServerExtractLocalVariableDialog extends ServerRefactoringDialog {
  @NotNull final ServerExtractLocalVariableRefactoring myRefactoring;
  private final NameSuggestionsField myVariableNameField;

  public DartServerExtractLocalVariableDialog(@NotNull Project project,
                                              @NotNull Editor editor,
                                              @NotNull ServerExtractLocalVariableRefactoring refactoring) {
    super(project, editor, refactoring);
    myRefactoring = refactoring;

    final String[] names = refactoring.getNames();
    myVariableNameField = new NameSuggestionsField(names, project);

    setTitle("Extract Local Variable");
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
    nameLabel.setText("Name:");

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

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myVariableNameField;
  }
}
