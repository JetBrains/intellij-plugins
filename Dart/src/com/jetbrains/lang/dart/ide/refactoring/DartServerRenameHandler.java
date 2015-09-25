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
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.ui.NameSuggestionsField;
import com.intellij.xml.util.XmlStringUtil;
import com.intellij.xml.util.XmlTagUtilBase;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.actions.AbstractDartFileProcessingAction;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

// todo implement ContextAwareActionHandler?
public class DartServerRenameHandler implements RenameHandler, TitledHandler {
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
    // Dart file rename is not handled using server yet
  }

  @Override
  public boolean isAvailableOnDataContext(@NotNull final DataContext dataContext) {
    if (!DartResolver.isServerDrivenResolution()) return false;

    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (editor == null) return false;

    final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (!DartAnalysisServerService.isLocalDartOrHtmlFile(file)) return false;

    final PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    if (psiElement != null) {
      return psiElement.getLanguage() == DartLanguage.INSTANCE;
    }

    // in case of comment (that also may contain reference that is valid to rename) psiElement is null
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
    final PsiElement elementAtOffset = psiFile == null ? null : psiFile.findElementAt(editor.getCaretModel().getOffset());

    return elementAtOffset != null && elementAtOffset.getLanguage() == DartLanguage.INSTANCE;
  }

  @Override
  public boolean isRenaming(DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  private static void showRenameDialog(@NotNull Project project, @NotNull Editor editor, DataContext context) {
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
    final ServerRenameRefactoring refactoring = new ServerRenameRefactoring(virtualFile.getPath(), offset, 0);
    // Validate initial status.
    {
      final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
      if (initialStatus == null) {
        return;
      }
      if (initialStatus.hasError()) {
        final String message = initialStatus.getMessage();
        assert message != null;
        AbstractDartFileProcessingAction.showHintLater(editor, message, true);
        return;
      }
    }
    // Show the rename dialog.
    new DartRenameDialog(project, editor, refactoring).show();
  }
}

class DartRenameDialog extends ServerRefactoringDialog {
  @NotNull ServerRenameRefactoring myRefactoring;
  @NotNull private final String myOldName;

  private final JLabel myNewNamePrefix = new JLabel("");
  private NameSuggestionsField myNameSuggestionsField;

  public DartRenameDialog(@NotNull Project project, @Nullable Editor editor, @NotNull ServerRenameRefactoring refactoring) {
    super(project, editor, refactoring);
    myRefactoring = refactoring;
    myOldName = myRefactoring.getOldName();
    setTitle("Rename " + myRefactoring.getElementKindName());
    createNewNameComponent();
    init();
  }

  @Override
  protected void canRun() throws ConfigurationException {
    if (Comparing.strEqual(getNewName(), myOldName)) {
      throw new ConfigurationException(null);
    }
    super.canRun();
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
  public JComponent getPreferredFocusedComponent() {
    return myNameSuggestionsField.getFocusableComponent();
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
