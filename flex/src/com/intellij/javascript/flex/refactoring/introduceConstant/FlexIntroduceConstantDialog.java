// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.introduceConstant;

import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.introduce.BasicIntroducedEntityInfoProvider;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseClassBasedIntroduceDialog;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceHandler;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.ui.JSVisibilityPanel;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.ui.NameSuggestionsField;
import com.intellij.ui.EditorComboBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Maxim.Mossienko
 */
public class FlexIntroduceConstantDialog extends JSBaseClassBasedIntroduceDialog<BasicIntroducedEntityInfoProvider>
  implements FlexIntroduceConstantSettings {
  public static final String RECENT_KEY = "FlexIntroduceConstantDialog";
  private NameSuggestionsField myNameField;
  private JCheckBox myReplaceAllCheckBox;
  private JPanel myPanel;
  private EditorComboBox myVarType;
  private JLabel myNameLabel;
  private JLabel myTargetClassLabel;
  private JSVisibilityPanel myVisibilityPanel;
  private JPanel myTargetClassPanel;
  private final JSReferenceEditor myTargetClassField;

  protected FlexIntroduceConstantDialog(final Project project,
                                        final JSExpression[] occurrences,
                                        final JSExpression mainOccurrence,
                                        PsiElement scope) {
    super(
      project,
      new IntroduceConstantInfoProvider(mainOccurrence, occurrences, scope),
      "javascript.introduce.constant.title"
    );

    JSElement classAnchor = JSBaseIntroduceHandler.findClassAnchor(mainOccurrence);
    String initialText = classAnchor instanceof JSClass ? ((JSClass)classAnchor).getQualifiedName() : "";
    Module module = ModuleUtilCore.findModuleForPsiElement(mainOccurrence);
    GlobalSearchScope targetClassScope =
      module != null ? GlobalSearchScope.moduleWithDependenciesScope(module) : GlobalSearchScope.projectScope(project);
    PsiDirectory dir = PlatformPackageUtil.getDirectory(mainOccurrence);
    targetClassScope = PlatformPackageUtil.adjustScope(dir, targetClassScope, false, true);
    myTargetClassField = createTargetClassField(project, initialText, targetClassScope);
    myTargetClassPanel.add(myTargetClassField, BorderLayout.CENTER);
    myTargetClassLabel.setLabelFor(myTargetClassField.getChildComponent());
    myTargetClassField.addDocumentListener(new DocumentListener() {
      @Override
      public void documentChanged(@NotNull DocumentEvent e) {
        initiateValidation();
      }
    });
    doInit();
  }

  public static JSReferenceEditor createTargetClassField(Project project, String initialText, GlobalSearchScope scope) {
    return JSReferenceEditor.forClassName(initialText, project, RECENT_KEY, scope, null, null,
                                          RefactoringBundle.message("choose.destination.class"));
  }

  @Override
  protected void checkIsValid() {
    super.checkIsValid();
    Action action = getOKAction();
    String className = getClassName();
    PsiElement clazz = null;
    action.setEnabled(action.isEnabled() &&
                      (StringUtil.isEmpty(className) ||
                       (clazz = JSResolveUtil.findType(className, entityInfoProvider.myMainOccurrence, true)) instanceof JSClass));
    if (clazz == null) {
      JSElement element = JSBaseIntroduceHandler.findClassAnchor(entityInfoProvider.myMainOccurrence);
      clazz = element != null ? JSResolveUtil.findParent(element) : null;
    }

    if (clazz instanceof JSClass) {
      checkUniqueness(clazz);
    }
  }

  @Override
  protected JSVisibilityPanel getVisibilityPanel() {
    return myVisibilityPanel;
  }

  @Override
  protected NameSuggestionsField getNameField() {
    return myNameField;
  }

  @Override
  protected JLabel getNameLabel() {
    return myNameLabel;
  }

  @Override
  protected JPanel getPanel() {
    return myPanel;
  }

  @Override
  protected JCheckBox getReplaceAllCheckBox() {
    return myReplaceAllCheckBox;
  }

  @Override
  public JComboBox getVarTypeField() {
    return myVarType;
  }

  private void createUIComponents() {
    myNameField = configureNameField();
    myVarType = configureTypeField();
  }

  @Override
  public String getClassName() {
    return myTargetClassField.getText();
  }

  @Override
  protected void doOKAction() {
    myTargetClassField.updateRecents();
    super.doOKAction();
  }

  @Override
  protected String getHelpId() {
    return "refactoring.introduceConstant.ActionScript";
  }

}
