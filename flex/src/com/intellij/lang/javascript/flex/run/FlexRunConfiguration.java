package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Maxim.Mossienko
 *         Date: Dec 27, 2007
 *         Time: 11:54:47 PM
 */
public class FlexRunConfiguration extends RunConfigurationBase
  implements RunProfileWithCompileBeforeLaunchOption, LocatableConfiguration {

  private @NotNull FlexRunnerParameters myRunnerParameters;

  public FlexRunConfiguration(final Project project, final ConfigurationFactory configurationFactory, final String name) {
    super(project, configurationFactory, name);
    myRunnerParameters = createRunnerParametersInstance();
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlexRunConfigurationOptions(getProject());
  }

  public JDOMExternalizable createRunnerSettings(ConfigurationInfoProvider provider) {
    return null;
  }

  public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(ProgramRunner runner) {
    return null;
  }

  @Override
  public FlexRunConfiguration clone() {
    final FlexRunConfiguration clone = (FlexRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    return FlexBaseRunner.EMPTY_RUN_STATE;
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    final Module module = getAndValidateModule(getProject(), myRunnerParameters.getModuleName());

    switch (myRunnerParameters.getRunMode()) {
      case HtmlOrSwfFile:
        final String path = myRunnerParameters.getHtmlOrSwfFilePath().trim();
        if (path.length() == 0) {
          throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.no.html.or.swf.specified"));
        }
        if (!path.toLowerCase().endsWith(".swf")) {
          if (myRunnerParameters.getLauncherType() == LauncherParameters.LauncherType.Player) {
            throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.only.swf.can.be.run.with.flash.player"));
          }
        }
        break;
      case Url:
        try {
          new URL(myRunnerParameters.getUrlToLaunch());
        }
        catch (MalformedURLException e) {
          throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.incorrect.url"));
        }
        if (myRunnerParameters.getLauncherType() == LauncherParameters.LauncherType.Player) {
          throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.url.can.not.be.run.with.flash.player"));
        }
        break;
      case MainClass:
        checkMainClassBasedConfiguration(module, myRunnerParameters);
        break;
      case ConnectToRunningFlashPlayer:
        break;
    }

    checkDebuggerSdk(myRunnerParameters);
  }

  protected static void checkDebuggerSdk(final FlexRunnerParameters params) throws RuntimeConfigurationError {
    final String debuggerSdkRaw = params.getDebuggerSdkRaw();
    if (!debuggerSdkRaw.equals(FlexSdkComboBoxWithBrowseButton.MODULE_SDK_KEY)) {
      final Sdk sdk = ProjectJdkTable.getInstance().findJdk(debuggerSdkRaw);
      if (sdk == null || !(sdk.getSdkType() instanceof IFlexSdkType)) {
        throw new RuntimeConfigurationError(FlexBundle.message("debugger.sdk.not.found", debuggerSdkRaw));
      }
    }
  }

  protected static void checkMainClassBasedConfiguration(final Module module, final FlexRunnerParameters params)
    throws RuntimeConfigurationError {
    final String mainClassName = params.getMainClassName();
    if (StringUtil.isEmpty(mainClassName)) {
      throw new RuntimeConfigurationError(FlexBundle.message("class.not.specified"));
    }
    if (!DumbService.getInstance(module.getProject()).isDumb()) {
      final PsiElement psiElement = JSResolveUtil.findClassByQName(mainClassName, GlobalSearchScope.moduleScope(module));
      if (!(psiElement instanceof JSClass)) {
        throw new RuntimeConfigurationError(FlexBundle.message("class.not.found", mainClassName));
      }
      final JSClass jsClass = (JSClass)psiElement;
      final JSAttributeList attributeList = jsClass.getAttributeList();
      if (attributeList == null || attributeList.getAccessType() != JSAttributeList.AccessType.PUBLIC) {
        throw new RuntimeConfigurationError(FlexBundle.message("class.not.public", mainClassName));
      }
    }
  }

  public static boolean isApplicableExtension(final VirtualFile file) {
    final String extension = file.getExtension();
    return FlexUtils.isSwfExtension(extension) || FlexUtils.isHtmlExtension(extension);
  }

  public static Module findModuleFromFile(VirtualFile file, final Project project) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    Module moduleForFile = fileIndex.getModuleForFile(file);
    while (moduleForFile == null && fileIndex.isIgnored(file)) {
      file = file.getParent();
      if (file == null) break;
      moduleForFile = fileIndex.getModuleForFile(file);
    }

    return moduleForFile;
  }

  public boolean isGeneratedName() {
    return Comparing.equal(getName(), suggestedName());
  }

  public String suggestedName() {
    final String path = myRunnerParameters.getHtmlOrSwfFilePath();
    return myRunnerParameters.getRunMode() == FlexRunnerParameters.RunMode.MainClass
           ? StringUtil.getShortName(myRunnerParameters.getMainClassName())
           : myRunnerParameters.getRunMode() == FlexRunnerParameters.RunMode.HtmlOrSwfFile
             ? path.substring(path.lastIndexOf('/') + 1)
             : "unnamed";
  }

  @NotNull
  public FlexRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = createRunnerParametersInstance();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  protected FlexRunnerParameters createRunnerParametersInstance() {
    return new FlexRunnerParameters();
  }

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  @NotNull
  public Module[] getModules() {
    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());
    if (module != null) {
      return new Module[]{module};
    }
    else {
      return Module.EMPTY_ARRAY;
    }
  }



  public static Module getAndValidateModule(Project project, String moduleName) throws RuntimeConfigurationError {
    if (StringUtil.isEmpty(moduleName)) {
      throw new RuntimeConfigurationError(FlexBundle.message("module.not.specified"));
    }
    final Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
    if (module == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("module.not.found", moduleName));
    }
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      throw new RuntimeConfigurationError(FlexBundle.message("not.flex.module.no.flex.facet", module.getName()));
    }

    if (FlexUtils.getSdkForActiveBC(module) == null) {
      final String s = (ModuleType.get(module) instanceof FlexModuleType ? "module " : "Flex facet(s) of module ") + module.getName();
      throw new RuntimeConfigurationError(FlexBundle.message("flex.sdk.not.set.for", s));
    }
    return module;
  }

  public static String getLauncherDescription(final LauncherParameters.LauncherType launcherType,
                                              final BrowsersConfiguration.BrowserFamily browserFamily,
                                              final String playerPath) {
    switch (launcherType) {
      case OSDefault:
        return FlexBundle.message("system.default.application");
      case Browser:
        return browserFamily.getName();
      case Player:
        return FileUtil.toSystemDependentName(playerPath);
      default:
        return "";
    }
  }

  static class FlexRunConfigurationOptions extends SettingsEditor<FlexRunConfiguration> implements PanelWithAnchor {
    private JPanel myPanel;
    private JComboBox myModuleComboBox;
    private JBRadioButton myUseHtmlOrSwfRadioButton;
    private JRadioButton myUseUrlRadioButton;
    private JRadioButton myUseMainClassRadioButton;
    private JRadioButton myConnectToRunningPlayerRadioButton;
    private ComboboxWithBrowseButton myHtmlOrSwfComboWithBrowse;
    private JTextField myUrlTextField;
    private JSReferenceEditor myMainClassTextWithBrowse;
    private JCheckBox myRunTrustedCheckBox;
    private TextFieldWithBrowseButton myLaunchWithTextWithBrowse;
    private JBLabel myLaunchWithLabel;
    private FlexSdkComboBoxWithBrowseButton myDebuggerSdkCombo;
    private final Project myProject;
    private ModulesComboboxWrapper myModuleComboboxWrapper;
    private LauncherParameters.LauncherType myLauncherType;
    private BrowsersConfiguration.BrowserFamily myBrowserFamily;
    private String myPlayerPath;
    private JSClassChooserDialog.PublicInheritor myMainClassFilter;
    private JComponent anchor;

    FlexRunConfigurationOptions(final Project project) {
      myProject = project;

      final ActionListener radioButtonListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          updateControls();
          updateFocus();
        }
      };

      myUseHtmlOrSwfRadioButton.addActionListener(radioButtonListener);
      myUseUrlRadioButton.addActionListener(radioButtonListener);
      myUseMainClassRadioButton.addActionListener(radioButtonListener);
      myConnectToRunningPlayerRadioButton.addActionListener(radioButtonListener);

      myHtmlOrSwfComboWithBrowse.getComboBox().setPrototypeDisplayValue("12345678901234567890123546789012345678901234567890");
      myHtmlOrSwfComboWithBrowse.getComboBox().setEditable(true);
      myHtmlOrSwfComboWithBrowse.addBrowseFolderListener(project, new FileChooserDescriptor(true, false, false, false, false, false) {
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || isApplicableExtension(file));
        }
      });

      myModuleComboboxWrapper = new ModulesComboboxWrapper(myModuleComboBox);
      myModuleComboboxWrapper.addActionListener(new ModulesComboboxWrapper.Listener() {
        public void moduleChanged() {
          fillHtmlOrSwfCombo();
          final Module module = myModuleComboboxWrapper.getSelectedModule();
          myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getSdkForActiveBC(module));
          updateMainClassField();
        }
      });

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

            updateControls();
          }
        }
      });

      myDebuggerSdkCombo.showModuleSdk(true);

      updateMainClassField();

      setAnchor(myUseHtmlOrSwfRadioButton);
    }

    private void updateMainClassField() {
      try {
        Module module = getAndValidateModule(myProject, myModuleComboboxWrapper.getSelectedText());
        final GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
        myMainClassTextWithBrowse.setScope(scope);
        myMainClassFilter.setScope(scope);
        myMainClassTextWithBrowse.setChooserBlockingMessage(null);
      }
      catch (RuntimeConfigurationError error) {
        myMainClassTextWithBrowse.setScope(GlobalSearchScope.EMPTY_SCOPE);
        myMainClassFilter.setScope(null);
        myMainClassTextWithBrowse.setChooserBlockingMessage(error.getMessage());
      }
    }

    private void fillHtmlOrSwfCombo() {
      final Object currentSelection = myHtmlOrSwfComboWithBrowse.getComboBox().getEditor().getItem();
      final Module selectedModule = myModuleComboboxWrapper.getSelectedModule();
      //myHtmlOrSwfComboWithBrowse.getComboBox().setModel(new DefaultComboBoxModel(
      //  selectedModule != null ? FlexUtils.suggestHtmlAndSwfFilesToLaunch(selectedModule) : ArrayUtil.EMPTY_STRING_ARRAY));
      myHtmlOrSwfComboWithBrowse.getComboBox().getEditor().setItem(currentSelection);
    }

    private void updateControls() {
      myHtmlOrSwfComboWithBrowse.setEnabled(myUseHtmlOrSwfRadioButton.isSelected());
      myUrlTextField.setEnabled(myUseUrlRadioButton.isSelected());
      myMainClassTextWithBrowse.setEnabled(myUseMainClassRadioButton.isSelected());
      myRunTrustedCheckBox.setEnabled(myUseHtmlOrSwfRadioButton.isSelected() || myUseMainClassRadioButton.isSelected());
      myLaunchWithTextWithBrowse.getTextField().setText(getLauncherDescription(myLauncherType, myBrowserFamily, myPlayerPath));
      myLaunchWithLabel.setEnabled(!myConnectToRunningPlayerRadioButton.isSelected());
      myLaunchWithTextWithBrowse.setEnabled(!myConnectToRunningPlayerRadioButton.isSelected());
    }

    private void updateFocus() {
      final Component toFocus = myUseHtmlOrSwfRadioButton.isSelected()
                                ? myHtmlOrSwfComboWithBrowse.getComboBox().getEditor().getEditorComponent()
                                : myUseUrlRadioButton.isSelected()
                                  ? myUrlTextField
                                  : myUseMainClassRadioButton.isSelected() ? myMainClassTextWithBrowse.getChildComponent() : null;
      if (toFocus != null) {
        IdeFocusManager.getInstance(myProject).requestFocus(toFocus, true);
      }
    }

    protected void resetEditorFrom(final FlexRunConfiguration runConfiguration) {
      final FlexRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

      myModuleComboboxWrapper.configure(myProject, runnerParameters.getModuleName());
      myUseHtmlOrSwfRadioButton.setSelected(runnerParameters.getRunMode() == FlexRunnerParameters.RunMode.HtmlOrSwfFile);
      myUseUrlRadioButton.setSelected(runnerParameters.getRunMode() == FlexRunnerParameters.RunMode.Url);
      myUseMainClassRadioButton.setSelected(runnerParameters.getRunMode() == FlexRunnerParameters.RunMode.MainClass);
      myConnectToRunningPlayerRadioButton
        .setSelected(runnerParameters.getRunMode() == FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer);
      myHtmlOrSwfComboWithBrowse.getComboBox().getEditor().setItem(FileUtil.toSystemDependentName(runnerParameters.getHtmlOrSwfFilePath()));
      myUrlTextField.setText(runnerParameters.getUrlToLaunch());
      myMainClassTextWithBrowse.setText(runnerParameters.getMainClassName());
      myRunTrustedCheckBox.setSelected(runnerParameters.isRunTrusted());
      myLauncherType = runnerParameters.getLauncherType();
      myBrowserFamily = runnerParameters.getBrowserFamily();
      myPlayerPath = runnerParameters.getPlayerPath();

      final Module module = myModuleComboboxWrapper.getSelectedModule();
      myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getSdkForActiveBC(module));
      myDebuggerSdkCombo.setSelectedSdkRaw(runnerParameters.getDebuggerSdkRaw());

      updateControls();
    }

    protected void applyEditorTo(final FlexRunConfiguration runConfiguration) throws ConfigurationException {
      final FlexRunnerParameters flexRunnerParameters = runConfiguration.getRunnerParameters();

      flexRunnerParameters.setModuleName(myModuleComboboxWrapper.getSelectedText());
      FlexRunnerParameters.RunMode runMode = myUseHtmlOrSwfRadioButton.isSelected()
                                             ? FlexRunnerParameters.RunMode.HtmlOrSwfFile
                                             : myUseUrlRadioButton.isSelected()
                                               ? FlexRunnerParameters.RunMode.Url
                                               : myUseMainClassRadioButton.isSelected()
                                                 ? FlexRunnerParameters.RunMode.MainClass
                                                 : FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer;

      flexRunnerParameters.setRunMode(runMode);
      final String htmlOrSwfFilePath = (String)myHtmlOrSwfComboWithBrowse.getComboBox().getEditor().getItem();
      if (htmlOrSwfFilePath != null) {
        flexRunnerParameters.setHtmlOrSwfFilePath(FileUtil.toSystemIndependentName(htmlOrSwfFilePath.trim()));
      }
      flexRunnerParameters.setUrlToLaunch(myUrlTextField.getText().trim());
      flexRunnerParameters.setMainClassName(myMainClassTextWithBrowse.getText().trim());
      flexRunnerParameters.setRunTrusted(myRunTrustedCheckBox.isSelected());
      flexRunnerParameters.setLauncherType(myLauncherType);
      flexRunnerParameters.setBrowserFamily(myBrowserFamily);
      flexRunnerParameters.setPlayerPath(myPlayerPath);
      flexRunnerParameters.setDebuggerSdkRaw(myDebuggerSdkCombo.getSelectedSdkRaw());
    }

    @NotNull
    protected JComponent createEditor() {
      return myPanel;
    }

    @SuppressWarnings({"BoundFieldAssignment"})
    protected void disposeEditor() {
      myPanel = null;
      myHtmlOrSwfComboWithBrowse = null;
      myModuleComboBox = null;
      myUseUrlRadioButton = null;
      myUseMainClassRadioButton = null;
      myModuleComboboxWrapper = null;
    }

    private void createUIComponents() {
      myDebuggerSdkCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_OR_FLEXMOJOS_SDK);
      myMainClassFilter = new JSClassChooserDialog.PublicInheritor(myProject, FlashRunConfigurationForm.SPRITE_CLASS_NAME, null, true);
      myMainClassTextWithBrowse = JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null,
                                                                 myMainClassFilter, ExecutionBundle.message(
        "choose.main.class.dialog.title"));
    }

    @Override
    public JComponent getAnchor() {
      return anchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
      this.anchor = anchor;
      myUseHtmlOrSwfRadioButton.setAnchor(anchor);
      myLaunchWithLabel.setAnchor(anchor);
    }
  }
}
