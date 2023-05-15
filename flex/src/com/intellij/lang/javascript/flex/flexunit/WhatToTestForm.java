// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.BrowseModuleValueActionListener;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexMethodChooserDialog;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.ActionScriptPackageChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WhatToTestForm {
  private JPanel myMainPanel; // needed for form reuse

  private JRadioButton myPackageRadioButton;
  private JRadioButton myClassRadioButton;
  private JRadioButton myMethodRadioButton;

  private JLabel myPackageOrClassLabel;
  private JSReferenceEditor myPackageField;
  private JSReferenceEditor myClassField;
  private JPanel myMethodPanel;
  private TextFieldWithBrowseButton.NoPathCompletion myMethodField;

  private final Project myProject;
  private final ThrowableComputable<? extends Module, ? extends RuntimeConfigurationError> myModuleComputable;
  private final ThrowableComputable<? extends FlexUnitSupport, ? extends RuntimeConfigurationError> myFlexUnitSupportComputable;
  private TestClassFilter myMainClassFilter;

  public WhatToTestForm(final Project project,
                        final ThrowableComputable<? extends Module, ? extends RuntimeConfigurationError> moduleComputable,
                        final ThrowableComputable<? extends FlexUnitSupport, ? extends RuntimeConfigurationError> flexUnitSupportComputable) {
    myProject = project;
    myModuleComputable = moduleComputable;
    myFlexUnitSupportComputable = flexUnitSupportComputable;

    final ActionListener scopeChangeListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateOnScopeChange();
      }
    };

    myClassRadioButton.addActionListener(scopeChangeListener);
    myPackageRadioButton.addActionListener(scopeChangeListener);
    myMethodRadioButton.addActionListener(scopeChangeListener);

    new MethodChooserActionListener(project);
  }

  private void createUIComponents() {
    myMainClassFilter = new TestClassFilter(myProject);
    myClassField = JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null, myMainClassFilter,
                                                  ExecutionBundle.message("choose.test.class.dialog.title"));
    myPackageField = ActionScriptPackageChooserDialog.createPackageReferenceEditor("", myProject, null, GlobalSearchScope.EMPTY_SCOPE,
                                                                                   ExecutionBundle.message("choose.package.dialog.title"));
  }

  private void updateOnScopeChange() {
    if (myClassRadioButton.isSelected()) {
      setText(myPackageOrClassLabel, "Class:", 'C');
      myMethodPanel.setVisible(false);
      myPackageOrClassLabel.setLabelFor(myClassField.getChildComponent());
      myMainClassFilter.setAllowSuite(true);
      myClassField.setVisible(true);
      myClassField.invalidateHighlight();
      myPackageField.setVisible(false);
    }
    else if (myPackageRadioButton.isSelected()) {
      setText(myPackageOrClassLabel, "Package:", 'g');
      myPackageOrClassLabel.setLabelFor(myPackageField.getChildComponent());
      myMethodPanel.setVisible(false);
      myClassField.setVisible(false);
      myPackageField.setVisible(true);
    }
    else if (myMethodRadioButton.isSelected()) {
      setText(myPackageOrClassLabel, "Class:", 'C');
      myPackageOrClassLabel.setLabelFor(myClassField.getChildComponent());
      myMethodPanel.setVisible(true);
      myMainClassFilter.setAllowSuite(false);
      myClassField.setVisible(true);
      myClassField.invalidateHighlight();
      myPackageField.setVisible(false);
    }
  }

  private static void setText(JLabel label, String text, char mnemonic) {
    label.setText(text);
    label.setDisplayedMnemonic(mnemonic);
  }

  public void updateOnBCChange(final @Nullable FlexBuildConfiguration bc, final Module module) {
    if (bc == null) {
      updateOnError(FlexBundle.message("bc.not.specified"));
      return;
    }
    final FlexUnitSupport support = FlexUnitSupport.getSupport(bc, module);
    if (support == null) {
      updateOnError(FlexBundle.message("flexunit.not.found.for.bc", bc.getName()));
      return;
    }

    updateControls(GlobalSearchScope.moduleScope(module), support);
  }

  private void updateControls(final @NotNull GlobalSearchScope scope, final @NotNull FlexUnitSupport support) {
    myClassField.setScope(scope);
    myMainClassFilter.setSupport(support);
    myClassField.setChooserBlockingMessage(null);
    myPackageField.setScope(scope);
    myPackageField.setChooserBlockingMessage(null);
  }

  public void updateOnError(final String message) {
    myClassField.setScope(GlobalSearchScope.EMPTY_SCOPE);
    myMainClassFilter.setSupport(null);
    myClassField.setChooserBlockingMessage(message);
    myPackageField.setScope(GlobalSearchScope.EMPTY_SCOPE);
    myPackageField.setChooserBlockingMessage(message);
  }

  public void resetFrom(final @Nullable Module module,
                        final @Nullable FlexBuildConfiguration bc,
                        final FlexUnitRunnerParameters params) {
    switch (params.getScope()) {
      case Class -> {
        myClassRadioButton.setSelected(true);
        myClassField.setText(params.getClassName());
        myPackageField.setText("");
      }
      case Method -> {
        myMethodRadioButton.setSelected(true);
        myClassField.setText(params.getClassName());
        myPackageField.setText("");
        myMethodField.setText(params.getMethodName());
      }
      case Package -> {
        myPackageRadioButton.setSelected(true);
        myClassField.setText("");
        myPackageField.setText(params.getPackageName());
      }
      default -> {
        assert false : "Unknown scope: " + params.getScope();
      }
    }

    updateOnBCChange(bc, module);
    updateOnScopeChange();
  }

  public void applyTo(final FlexUnitRunnerParameters params) {
    if (myClassRadioButton.isSelected()) {
      params.setScope(FlexUnitRunnerParameters.Scope.Class);
      params.setClassName(myClassField.getText());
    }
    else if (myPackageRadioButton.isSelected()) {
      params.setScope(FlexUnitRunnerParameters.Scope.Package);
      params.setPackageName(myPackageField.getText());
    }
    else if (myMethodRadioButton.isSelected()) {
      params.setScope(FlexUnitRunnerParameters.Scope.Method);
      params.setClassName(myClassField.getText());
      params.setMethodName(myMethodField.getText());
    }
  }

  @SuppressWarnings("BoundFieldAssignment")
  public void dispose() {
    myPackageRadioButton = null;
    myClassRadioButton = null;
    myMethodRadioButton = null;
    myPackageOrClassLabel = null;
    myClassField = null;
    myPackageField = null;
    myMethodPanel = null;
    myMethodField = null;
  }

  private class MethodChooserActionListener extends BrowseModuleValueActionListener<JTextField> {
    protected MethodChooserActionListener(final Project project) {
      super(project);
      setField(myMethodField);
    }

    @Override
    protected String showDialog() {
      if (StringUtil.isEmpty(myClassField.getText())) {
        Messages.showInfoMessage(getProject(), ExecutionBundle.message("set.class.name.message"),
                                 ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      final Module module;
      final FlexUnitSupport support;
      try {
        module = myModuleComputable.compute();
        support = myFlexUnitSupportComputable.compute();
      }
      catch (RuntimeConfigurationError e) {
        Messages.showErrorDialog(getProject(), e.getMessage(), ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      final PsiElement clazz =
        ActionScriptClassResolver.findClassByQNameStatic(myClassField.getText(), GlobalSearchScope.moduleScope(module));
      if (!(clazz instanceof JSClass)) {
        Messages.showErrorDialog(getProject(), FlexBundle.message("class.not.found", myClassField.getText()),
                                 ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      FlexMethodChooserDialog dialog = new FlexMethodChooserDialog((JSClass)clazz, jsFunction -> support.isTestMethod(jsFunction), myMainPanel, myMethodField.getText());

      if (dialog.showAndGet()) {
        final JSFunction method = dialog.getSelectedMethod();
        return method != null ? method.getName() : null;
      }
      else {
        return null;
      }
    }
  }

  private static class TestClassFilter implements Condition<JSClass> {

    @NotNull private final Project myProject;
    @Nullable private FlexUnitSupport mySupport;
    private boolean myAllowSuite;

    private Condition<JSClass> myCondition;

    TestClassFilter(@NotNull Project project) {
      myProject = project;
      setSupport(null);
      setAllowSuite(false);
    }

    private synchronized Condition<JSClass> getCondition() {
      if (DumbService.getInstance(myProject).isDumb() || mySupport == null) {
        return Conditions.alwaysFalse();
      }

      if (myCondition == null) {
        myCondition = Conditions.cached(jsClass -> {
          assert mySupport != null;
          return mySupport.isTestClass(jsClass, myAllowSuite);
        });
      }
      return myCondition;
    }

    public synchronized void setSupport(@Nullable FlexUnitSupport support) {
      mySupport = support;
      myCondition = null;
    }

    public synchronized void setAllowSuite(boolean allowSuite) {
      myAllowSuite = allowSuite;
      myCondition = null;
    }

    @Override
    public boolean value(JSClass jsClass) {
      return getCondition().value(jsClass);
    }
  }
}
