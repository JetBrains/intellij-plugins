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
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.ide.TitledHandler;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.ui.NameSuggestionsField;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.intellij.xml.util.XmlStringUtil;
import com.intellij.xml.util.XmlTagUtilBase;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.actions.AbstractDartFileProcessingAction;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class DartRenameHandler implements RenameHandler, TitledHandler {
  @Override
  public String getActionTitle() {
    return "Dart Rename Refactoring";
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext context) {
    showRenameDialog(project, editor, context);
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext context) {
    showRenameDialog(project, null, context);
  }

  @Override
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    return true;
  }

  @Override
  public boolean isRenaming(DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  private static void showRenameDialog(@NotNull Project project, @Nullable Editor editor, DataContext context) {
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
    final VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(context);
    if (psiFile == null || virtualFile == null) {
      return;
    }
    // Prepare the offset in the editor or of the selected element.
    final int offset;
    {
      final Caret caret = CommonDataKeys.CARET.getData(context);
      final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(context);
      if (caret != null) {
        offset = caret.getOffset();
      }
      else if (element != null) {
        offset = element.getTextOffset();
      }
      else {
        return;
      }
    }
    // Create the refactoring.
    final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
    final ServerRenameRefactoring refactoring = new ServerRenameRefactoring(path, offset, 0);
    // Validate initial status.
    {
      final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
      if (initialStatus == null) {
        return;
      }
      if (initialStatus.hasError()) {
        if (editor != null) {
          final String message = initialStatus.getMessage();
          assert message != null;
          AbstractDartFileProcessingAction.showHintLater(editor, message, true);
        }
        return;
      }
    }
    // Show the rename dialog.
    new DartRenameDialog(project, editor, refactoring).show();
  }
}

class DartRenameDialog extends RefactoringDialog {
  @Nullable private final Editor myEditor;
  @NotNull private final ServerRenameRefactoring myRefactoring;
  @NotNull private final String myOldName;

  private final JLabel myNewNamePrefix = new JLabel("");
  private NameSuggestionsField myNameSuggestionsField;

  private boolean myHasPendingRequests;
  private RefactoringStatus myOptionsStatus;

  public DartRenameDialog(@NotNull Project project, @Nullable Editor editor, @NotNull ServerRenameRefactoring refactoring) {
    super(project, true);
    myEditor = editor;
    myRefactoring = refactoring;
    myOldName = myRefactoring.getOldName();
    setTitle("Rename " + myRefactoring.getElementKindName());
    createNewNameComponent();
    init();
    // Listen for responses.
    myRefactoring.setListener(new ServerRefactoring.ServerRefactoringListener() {
      @Override
      public void requestStateChanged(final boolean hasPendingRequests, @NotNull final RefactoringStatus optionsStatus) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            myHasPendingRequests = hasPendingRequests;
            myOptionsStatus = optionsStatus;
            validateButtons();
          }
        });
      }
    });
  }

  @Override
  protected void canRun() throws ConfigurationException {
    // the same name
    if (Comparing.strEqual(getNewName(), myOldName)) {
      throw new ConfigurationException(null);
    }
    // has pending requests
    if (myHasPendingRequests || myOptionsStatus == null) {
      throw new ConfigurationException(null);
    }
    // has a fatal error
    if (myOptionsStatus.hasFatalError()) {
      throw new ConfigurationException(myOptionsStatus.getMessage());
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return null;
  }

  @Override
  protected JComponent createNorthPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();

    gbConstraints.insets = new Insets(0, 0, 4, 0);
    gbConstraints.weighty = 0;
    gbConstraints.weightx = 1;
    gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gbConstraints.fill = GridBagConstraints.BOTH;
    JLabel nameLabel = new JLabel();
    panel.add(nameLabel, gbConstraints);
    nameLabel.setText(XmlStringUtil.wrapInHtml(XmlTagUtilBase.escapeString(getLabelText(), false)));

    gbConstraints.insets = new Insets(0, 0, 4, 0);
    gbConstraints.gridwidth = 1;
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.weightx = 0;
    gbConstraints.gridx = 0;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(myNewNamePrefix, gbConstraints);

    gbConstraints.insets = new Insets(0, 0, 8, 0);
    gbConstraints.gridwidth = 2;
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.weightx = 1;
    gbConstraints.gridx = 0;
    gbConstraints.weighty = 1;
    panel.add(myNameSuggestionsField.getComponent(), gbConstraints);

    return panel;
  }

  @Override
  protected void doAction() {
    // Validate final status.
    {
      final RefactoringStatus finalStatus = myRefactoring.checkFinalConditions();
      if (finalStatus.hasError()) {
        Messages.showErrorDialog(myProject, finalStatus.getMessage(), "Error");
        return;
      }
    }
    // Apply the change.
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final SourceChange change = myRefactoring.getChange();
        assert change != null;
        final Set<String> excludedIds = myRefactoring.getPotentialEdits();
        AssistUtils.applySourceChange(myProject, change, excludedIds);
        close(DialogWrapper.OK_EXIT_CODE);
      }
    });
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myNameSuggestionsField.getFocusableComponent();
  }

  @Override
  protected boolean hasPreviewButton() {
    return false;
  }

  private void createNewNameComponent() {
    String[] suggestedNames = getSuggestedNames();
    myNameSuggestionsField = new NameSuggestionsField(suggestedNames, myProject, FileTypes.PLAIN_TEXT, myEditor) {
      @Override
      protected boolean shouldSelectAll() {
        return myEditor == null || myEditor.getSettings().isPreselectRename();
      }
    };
    myNameSuggestionsField.addDataChangedListener(new NameSuggestionsField.DataChanged() {
      @Override
      public void dataChanged() {
        processNewNameChanged();
      }
    });
  }

  @NotNull
  private String getLabelText() {
    @NonNls final String kindName = myRefactoring.getElementKindName();
    final String oldName = myOldName.isEmpty() ? "<empty>" : myOldName;
    return "Rename " + kindName.toLowerCase() + " '" + oldName + "' and its usages to:";
  }

  private String getNewName() {
    return myNameSuggestionsField.getEnteredName().trim();
  }

  @NotNull
  private String[] getSuggestedNames() {
    return new String[]{myOldName};
  }

  private void processNewNameChanged() {
    final String newName = getNewName();
    myRefactoring.setNewName(newName);
  }
}
