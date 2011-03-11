package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.BrowseModuleValueActionListener;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexMethodChooserDialog;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ModulesComboboxWrapper;
import com.intellij.lang.javascript.flex.run.FlexLauncherDialog;
import com.intellij.lang.javascript.flex.run.FlexRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EnumComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexUnitRunConfigurationForm extends SettingsEditor<FlexUnitRunConfiguration> {
  private final Project myProject;

  private JComboBox myModuleCombo;
  private JRadioButton myPackageRadioButton;
  private JRadioButton myClassRadioButton;
  private JLabel myPackageOrClassLabel;
  private JPanel myPanel;
  private JRadioButton myMethodRadioButton;
  private TextFieldWithBrowseButton.NoPathCompletion myMethodField;
  private JPanel myMethodPanel;
  private JComboBox myLogLevelCombo;
  private JCheckBox myShowLogCheckBox;
  private JCheckBox myRunTrustedCheckBox;
  private JLabel myLaunchWithLabel;
  private TextFieldWithBrowseButton myLaunchWithTextWithBrowse;
  private FlexSdkComboBoxWithBrowseButton myDebuggerSdkCombo;
  private JSReferenceEditor myPackageField;
  private JSReferenceEditor myClassField;
  private ModulesComboboxWrapper myModulesComboboxWrapper;

  private FlexRunnerParameters.LauncherType myLauncherType;
  private BrowsersConfiguration.BrowserFamily myBrowserFamily;
  private String myPlayerPath;

  private TestClassFilter myMainClassFilter;

  public FlexUnitRunConfigurationForm(final Project project) {
    myProject = project;

    new MethodChooserActionListener().setField(myMethodField);

    myModulesComboboxWrapper = new ModulesComboboxWrapper(myModuleCombo);
    myModulesComboboxWrapper.addActionListener(new ModulesComboboxWrapper.Listener() {
      public void moduleChanged() {
        updateOnModuleChange();
        updateRunTrustedOptionVisibility();
        updateLauncherTextWithBrowse();
        final Module module = myModulesComboboxWrapper.getSelectedModule();
        myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
      }
    });

    ChangeListener scopeChangeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateOnScopeChange();
      }
    };

    myClassRadioButton.addChangeListener(scopeChangeListener);
    myPackageRadioButton.addChangeListener(scopeChangeListener);
    myMethodRadioButton.addChangeListener(scopeChangeListener);

    myShowLogCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myShowLogCheckBox.isSelected()) {
          myLogLevelCombo.setEnabled(true);
          if (myLogLevelCombo.getSelectedItem() == null) {
            myLogLevelCombo.setSelectedItem(FlexUnitRunnerParameters.OutputLogLevel.values()[0]);
          }
          IdeFocusManager.getInstance(project).requestFocus(myLogLevelCombo, false);
        }
        else {
          myLogLevelCombo.setEnabled(false);
        }
      }
    });

    myLogLevelCombo.setModel(new EnumComboBoxModel<FlexUnitRunnerParameters.OutputLogLevel>(FlexUnitRunnerParameters.OutputLogLevel.class));

    myLaunchWithTextWithBrowse.getTextField().setEditable(false);
    myLaunchWithTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final FlexLauncherDialog dialog = new FlexLauncherDialog(myProject, myLauncherType, myBrowserFamily, myPlayerPath);
        dialog.show();
        if (dialog.isOK()) {
          myLauncherType = dialog.getLauncherType();
          final BrowsersConfiguration.BrowserFamily browser = dialog.getBrowserFamily();
          if (browser != null) {
            myBrowserFamily = browser;
          }
          myPlayerPath = dialog.getPlayerPath();

          updateLauncherTextWithBrowse();
        }
      }
    });

    myDebuggerSdkCombo.showModuleSdk(true);
  }

  private void updateOnModuleChange() {
    try {
      final Pair<Module, FlexUnitSupport> moduleAndSupport =
        FlexUnitRunConfiguration.getFlexUnitSupport(myProject, getSelectedModuleName());
      final GlobalSearchScope scope = GlobalSearchScope.moduleScope(moduleAndSupport.first);
      myClassField.setScope(scope);
      myMainClassFilter.setSupport(moduleAndSupport.second);
      myClassField.setChooserBlockingMessage(null);
      myPackageField.setScope(scope);
      myPackageField.setChooserBlockingMessage(null);
    }
    catch (RuntimeConfigurationError error) {
      myClassField.setScope(GlobalSearchScope.EMPTY_SCOPE);
      myMainClassFilter.setSupport(null);
      myClassField.setChooserBlockingMessage(error.getMessage());
      myPackageField.setScope(GlobalSearchScope.EMPTY_SCOPE);
      myPackageField.setChooserBlockingMessage(error.getMessage());
    }
  }

  protected void resetEditorFrom(FlexUnitRunConfiguration s) {
    FlexUnitRunnerParameters params = s.getRunnerParameters();
    if (params == null) {
      params = new FlexUnitRunnerParameters();
    }

    switch (params.getScope()) {
      case Class:
        myClassRadioButton.setSelected(true);
        myClassField.setText(params.getClassName());
        myPackageField.setText(null);
        break;
      case Method:
        myMethodRadioButton.setSelected(true);
        myClassField.setText(params.getClassName());
        myPackageField.setText(null);
        myMethodField.setText(params.getMethodName());
        break;
      case Package:
        myPackageRadioButton.setSelected(true);
        myClassField.setText(null);
        myPackageField.setText(params.getPackageName());
        break;
      default:
        assert false : "Unknown scope: " + params.getScope();
    }

    myModulesComboboxWrapper.configure(myProject, params.getModuleName());

    if (params.getOutputLogLevel() != null) {
      myShowLogCheckBox.setSelected(true);
      myLogLevelCombo.setEnabled(true);
      myLogLevelCombo.setSelectedItem(params.getOutputLogLevel());
    }
    else {
      myShowLogCheckBox.setSelected(false);
      myLogLevelCombo.setEnabled(false);
      myLogLevelCombo.setSelectedItem(null);
    }

    myRunTrustedCheckBox.setSelected(params.isRunTrusted());

    myLauncherType = params.getLauncherType();
    myBrowserFamily = params.getBrowserFamily();
    myPlayerPath = params.getPlayerPath();

    final Module module = myModulesComboboxWrapper.getSelectedModule();
    myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
    myDebuggerSdkCombo.setSelectedSdkRaw(params.getDebuggerSdkRaw());

    updateOnScopeChange();
    updateLauncherTextWithBrowse();
    updateRunTrustedOptionVisibility();
    updateOnModuleChange();
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

  private void updateLauncherTextWithBrowse() {
    if (myLauncherType == null) {
      // not initialized yet
      return;
    }

    final Module module = myModulesComboboxWrapper.getSelectedModule();
    if (module != null && FlexSdkUtils.hasDependencyOnAir(module)) {
      myLaunchWithTextWithBrowse.setText("AIR");
      myLaunchWithLabel.setEnabled(false);
      myLaunchWithTextWithBrowse.setEnabled(false);
    }
    else {
      myLaunchWithTextWithBrowse.setText(FlexRunConfiguration.getLauncherDescription(myLauncherType, myBrowserFamily, myPlayerPath));
      myLaunchWithLabel.setEnabled(true);
      myLaunchWithTextWithBrowse.setEnabled(true);
    }
  }

  private void updateRunTrustedOptionVisibility() {
    final Module module = myModulesComboboxWrapper.getSelectedModule();
    myRunTrustedCheckBox.setEnabled(module != null && !FlexSdkUtils.hasDependencyOnAir(module));
  }

  protected void applyEditorTo(FlexUnitRunConfiguration s) throws ConfigurationException {
    final FlexUnitRunnerParameters params = s.getRunnerParameters();

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

    params.setModuleName(myModulesComboboxWrapper.getSelectedText());

    if (myShowLogCheckBox.isSelected()) {
      params.setOutputLogLevel((FlexUnitRunnerParameters.OutputLogLevel)myLogLevelCombo.getSelectedItem());
    }
    else {
      params.setOutputLogLevel(null);
    }

    params.setRunTrusted(myRunTrustedCheckBox.isSelected());

    params.setLauncherType(myLauncherType);
    params.setBrowserFamily(myBrowserFamily);
    params.setPlayerPath(myPlayerPath);
    params.setDebuggerSdkRaw(myDebuggerSdkCombo.getSelectedSdkRaw());
  }

  @NotNull
  protected JComponent createEditor() {
    return myPanel;
  }

  @SuppressWarnings({"BoundFieldAssignment"})
  protected void disposeEditor() {
    myModuleCombo = null;
    myPackageRadioButton = null;
    myClassRadioButton = null;
    myMethodRadioButton = null;
    myPackageOrClassLabel = null;
    myClassField = null;
    myPackageField = null;
    myMethodPanel = null;
    myMethodField = null;
    myPanel = null;
    myModulesComboboxWrapper = null;
  }

  private static void setText(JLabel label, String text, char mnemonic) {
    label.setText(text);
    label.setDisplayedMnemonic(mnemonic);
  }

  public String getSelectedModuleName() {
    return myModulesComboboxWrapper.getSelectedText();
  }

  private void createUIComponents() {
    myDebuggerSdkCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_RELATED_SDK);
    myMainClassFilter = new TestClassFilter(myProject);
    myClassField = JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null, myMainClassFilter,
                                                  ExecutionBundle.message("choose.test.class.dialog.title"));
    myPackageField = JSReferenceEditor.forPackageName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE,
                                                      ExecutionBundle.message("choose.package.dialog.title"));
  }

  private class MethodChooserActionListener extends BrowseModuleValueActionListener {
    protected MethodChooserActionListener() {
      super(myProject);
    }

    protected String showDialog() {
      if (StringUtil.isEmpty(myClassField.getText())) {
        Messages.showInfoMessage(getProject(), ExecutionBundle.message("set.class.name.message"),
                                 ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      final Pair<Module, FlexUnitSupport> supportForModule;
      try {
        supportForModule = FlexUnitRunConfiguration.getFlexUnitSupport(myProject, getSelectedModuleName());
      }
      catch (RuntimeConfigurationError e) {
        Messages.showErrorDialog(getProject(), e.getMessage(), ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      final PsiElement clazz =
        JSResolveUtil.findClassByQName(myClassField.getText(), GlobalSearchScope.moduleScope(supportForModule.first));
      if (!(clazz instanceof JSClass)) {
        Messages.showErrorDialog(getProject(), FlexBundle.message("class.not.found", myClassField.getText()),
                                 ExecutionBundle.message("choose.test.method.dialog.title"));
        return null;
      }

      FlexMethodChooserDialog dialog = new FlexMethodChooserDialog((JSClass)clazz, new Condition<JSFunction>() {
        public boolean value(JSFunction jsFunction) {
          return supportForModule.second.isTestMethod(jsFunction);
        }
      }, getComponent(), myMethodField.getText());

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

  public static class TestClassFilter implements Condition<JSClass> {

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
