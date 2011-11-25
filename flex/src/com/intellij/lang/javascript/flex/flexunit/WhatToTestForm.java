package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.BrowseModuleValueActionListener;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexMethodChooserDialog;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PlatformUtils;
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
  private final ThrowableComputable<Module, RuntimeConfigurationError> myModuleComputable;
  private final ThrowableComputable<FlexUnitSupport, RuntimeConfigurationError> myFlexUnitSupportComputable;
  private TestClassFilter myMainClassFilter;

  public WhatToTestForm(final Project project,
                        final ThrowableComputable<Module, RuntimeConfigurationError> moduleComputable,
                        final ThrowableComputable<FlexUnitSupport, RuntimeConfigurationError> flexUnitSupportComputable) {
    myProject = project;
    myModuleComputable = moduleComputable;
    myFlexUnitSupportComputable = flexUnitSupportComputable;

    final ActionListener scopeChangeListener = new ActionListener() {
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
    myPackageField = JSReferenceEditor.forPackageName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE,
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

  // todo remove module param and fix scope
  public void updateOnBCChange(final @Nullable FlexIdeBuildConfiguration bc, final Module module) {
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

  public void updateOnModuleChange(final String moduleName) {
    assert !PlatformUtils.isFlexIde();
    try {
      final Pair<Module, FlexUnitSupport> moduleAndSupport = FlexUnitRunConfiguration.getFlexUnitSupport(myProject, moduleName);
      updateControls(GlobalSearchScope.moduleScope(moduleAndSupport.first), moduleAndSupport.second);
    }
    catch (RuntimeConfigurationError e) {
      updateOnError(e.getMessage());
    }
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

  public void resetFrom(final FlexUnitCommonParameters params) {
    switch (params.getScope()) {
      case Class:
        myClassRadioButton.setSelected(true);
        myClassField.setText(params.getClassName());
        myPackageField.setText("");
        break;
      case Method:
        myMethodRadioButton.setSelected(true);
        myClassField.setText(params.getClassName());
        myPackageField.setText("");
        myMethodField.setText(params.getMethodName());
        break;
      case Package:
        myPackageRadioButton.setSelected(true);
        myClassField.setText("");
        myPackageField.setText(params.getPackageName());
        break;
      default:
        assert false : "Unknown scope: " + params.getScope();
    }

    if (params instanceof FlexUnitRunnerParameters) {
      updateOnModuleChange(((FlexUnitRunnerParameters)params).getModuleName());
    }
    else {
      try {
        final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = ((NewFlexUnitRunnerParameters)params).checkAndGetModuleAndBC(myProject);
        updateOnBCChange(moduleAndBC.second, moduleAndBC.first);
      }
      catch (RuntimeConfigurationError e) {
        updateOnError(e.getMessage());
      }
    }

    updateOnScopeChange();
  }

  public void applyTo(final FlexUnitCommonParameters params) {
    if (myClassRadioButton.isSelected()) {
      params.setScope(FlexUnitCommonParameters.Scope.Class);
      params.setClassName(myClassField.getText());
    }
    else if (myPackageRadioButton.isSelected()) {
      params.setScope(FlexUnitCommonParameters.Scope.Package);
      params.setPackageName(myPackageField.getText());
    }
    else if (myMethodRadioButton.isSelected()) {
      params.setScope(FlexUnitCommonParameters.Scope.Method);
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

  private class MethodChooserActionListener extends BrowseModuleValueActionListener {
    protected MethodChooserActionListener(final Project project) {
      super(project);
      setField(myMethodField);
    }

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

      final PsiElement clazz = JSResolveUtil.findClassByQName(myClassField.getText(), GlobalSearchScope.moduleScope(module));
      if (!(clazz instanceof JSClass)) {
        Messages.showErrorDialog(getProject(), FlexBundle.message("class.not.found", myClassField.getText()),
                                 ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      FlexMethodChooserDialog dialog = new FlexMethodChooserDialog((JSClass)clazz, new Condition<JSFunction>() {
        public boolean value(JSFunction jsFunction) {
          return support.isTestMethod(jsFunction);
        }
      }, myMainPanel, myMethodField.getText());

      dialog.show();
      if (dialog.isOK()) {
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

    public TestClassFilter(@NotNull Project project) {
      myProject = project;
      setSupport(null);
      setAllowSuite(false);
    }

    private synchronized Condition<JSClass> getCondition() {
      if (DumbService.getInstance(myProject).isDumb() || mySupport == null) {
        return Conditions.alwaysFalse();
      }

      if (myCondition == null) {
        myCondition = Conditions.cached(new Condition<JSClass>() {
          @Override
          public boolean value(JSClass jsClass) {
            assert mySupport != null;
            return mySupport.isTestClass(jsClass, myAllowSuite);
          }
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
