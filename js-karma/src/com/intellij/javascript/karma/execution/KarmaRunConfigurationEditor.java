package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesTextField;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.NodeSettings;
import com.intellij.javascript.nodejs.NodeUIUtil;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.Function;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KarmaRunConfigurationEditor extends SettingsEditor<KarmaRunConfiguration> {

  private final Project myProject;
  private final TextFieldWithHistoryWithBrowseButton myNodeInterpreterPathTextFieldWithBrowseButton;
  private final TextFieldWithHistoryWithBrowseButton myKarmaPackageDirPathTextFieldWithBrowseButton;
  private final TextFieldWithHistoryWithBrowseButton myConfigPathTextFieldWithBrowseButton;
  private final EnvironmentVariablesTextField myEnvironmentVariablesTextField;
  private final JBTextField myBrowsers;
  private final JPanel myRootComponent;

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myProject = project;
    myNodeInterpreterPathTextFieldWithBrowseButton = NodeUIUtil.createNodeInterpreterTextField(project);
    myKarmaPackageDirPathTextFieldWithBrowseButton = createKarmaPackageDirPathTextField(project);
    myConfigPathTextFieldWithBrowseButton = createConfigurationFileTextField(project);
    myEnvironmentVariablesTextField = new EnvironmentVariablesTextField();
    myBrowsers = createBrowsersTextField();
    JLabel browserLabel = new JLabel("comma-separated list of browsers (e.g. Chrome,ChromeCanary,Firefox)");
    browserLabel.setFont(UIUtil.getTitledBorderFont());
    myRootComponent = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.config_file.label"), myConfigPathTextFieldWithBrowseButton)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.browsers.label"), myBrowsers)
      .addLabeledComponent("", browserLabel, 0, false)
      .addComponent(new JSeparator(), 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.node_interpreter.label"), myNodeInterpreterPathTextFieldWithBrowseButton, 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.karma_package_dir.label"), myKarmaPackageDirPathTextFieldWithBrowseButton)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.environment.label"), myEnvironmentVariablesTextField.getComponent())
      .getPanel();
  }

  @NotNull
  private static JBTextField createBrowsersTextField() {
    JBTextField browsers = new JBTextField();
    // by default 'browsers' setting from the configuration file is used
    StatusText emptyStatusText = browsers.getEmptyText();
    emptyStatusText.clear();
    emptyStatusText.appendText("using ", SimpleTextAttributes.GRAYED_ATTRIBUTES);
    emptyStatusText.appendText("browsers", SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
    emptyStatusText.appendText(" setting from the configuration file", SimpleTextAttributes.GRAYED_ATTRIBUTES);
    return browsers;
  }

  private TextFieldWithHistoryWithBrowseButton createKarmaPackageDirPathTextField(@NotNull final Project project) {
    TextFieldWithHistoryWithBrowseButton karmaPackageDirPathComponent = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = karmaPackageDirPathComponent.getChildComponent();
    textFieldWithHistory.setHistorySize(-1);
    textFieldWithHistory.setMinimumAndPreferredWidth(0);
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
      @NotNull
      @Override
      public List<String> produce() {
        NodeSettings nodeSettings = KarmaGlobalSettingsUtil.getNodeSettings();
        if (nodeSettings != null) {
          nodeSettings.initGlobalNodeModulesDir();
        }
        List<CompletionModuleInfo> modules = ContainerUtil.newArrayList();
        VirtualFile requester = KarmaGlobalSettingsUtil.getRequester(
          myProject,
          myConfigPathTextFieldWithBrowseButton.getChildComponent().getText()
        );
        NodeModuleSearchUtil.findModulesWithName(modules,
                                                 KarmaGlobalSettingsUtil.NODE_PACKAGE_NAME,
                                                 requester,
                                                 nodeSettings,
                                                 true);
        List<String> moduleDirs = ContainerUtil.newArrayListWithCapacity(modules.size());
        for (CompletionModuleInfo module : modules) {
          VirtualFile dir = module.getVirtualFile();
          if (dir != null && dir.isDirectory()) {
            moduleDirs.add(FileUtil.toSystemDependentName(dir.getPath()));
          }
        }
        Collections.sort(moduleDirs);
        return moduleDirs;
      }
    });

    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      karmaPackageDirPathComponent,
      KarmaBundle.message("runConfiguration.karma_package_dir.browse_dialog.title"),
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
    return karmaPackageDirPathComponent;
  }

  @NotNull
  private static TextFieldWithHistoryWithBrowseButton createConfigurationFileTextField(@NotNull final Project project) {
    TextFieldWithHistoryWithBrowseButton textFieldWithHistoryWithBrowseButton = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = textFieldWithHistoryWithBrowseButton.getChildComponent();
    textFieldWithHistory.setHistorySize(-1);
    textFieldWithHistory.setMinimumAndPreferredWidth(0);
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
      @NotNull
      @Override
      public List<String> produce() {
        List<VirtualFile> newFiles = listPossibleConfigFilesInProject(project);
        List<String> newFilePaths = ContainerUtil.map(newFiles, new Function<VirtualFile, String>() {
          @Override
          public String fun(VirtualFile file) {
            return FileUtil.toSystemDependentName(file.getPath());
          }
        });
        Collections.sort(newFilePaths);
        return newFilePaths;
      }
    });

    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      textFieldWithHistoryWithBrowseButton,
      KarmaBundle.message("runConfiguration.config_file.browse_dialog.title"),
      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
    );
    return textFieldWithHistoryWithBrowseButton;
  }

  @NotNull
  private static List<VirtualFile> listPossibleConfigFilesInProject(@NotNull Project project) {
    GlobalSearchScope scope = ProjectScope.getContentScope(project);
    Collection<VirtualFile> files = FileTypeIndex.getFiles(JavaScriptFileType.INSTANCE, scope);
    List<VirtualFile> result = ContainerUtil.newArrayList();
    for (VirtualFile file : files) {
      if (file != null && file.isValid() && !file.isDirectory() && file.getName().endsWith(".conf.js")) {
        String path = file.getPath();
        if (!path.contains("/node_modules/") && !path.contains("/bower_components/")) {
          result.add(file);
        }
      }
    }
    return result;
  }

  @Override
  protected void resetEditorFrom(@NotNull KarmaRunConfiguration runConfiguration) {
    KarmaRunSettings runSettings = runConfiguration.getRunSettings();

    String nodeInterpreterPath = KarmaGlobalSettingsUtil.getNodeInterpreterPath();
    setTextAndAddToHistory(myNodeInterpreterPathTextFieldWithBrowseButton.getChildComponent(), nodeInterpreterPath);

    String karmaNodePackageDir = KarmaGlobalSettingsUtil.getKarmaNodePackageDir(myProject, runSettings.getConfigPath());
    setTextAndAddToHistory(myKarmaPackageDirPathTextFieldWithBrowseButton.getChildComponent(), karmaNodePackageDir);

    setTextAndAddToHistory(myConfigPathTextFieldWithBrowseButton.getChildComponent(), runSettings.getConfigPath());
    myBrowsers.setText(runSettings.getBrowsers());

    myEnvironmentVariablesTextField.setEnvs(runSettings.getEnvVars());
    myEnvironmentVariablesTextField.setPassParentEnvs(runSettings.isPassParentEnvVars());
  }

  private static void setTextAndAddToHistory(@NotNull TextFieldWithHistory textFieldWithHistory, @Nullable String text) {
    textFieldWithHistory.setText(text);
    textFieldWithHistory.addCurrentTextToHistory();
  }

  @Override
  protected void applyEditorTo(@NotNull KarmaRunConfiguration runConfiguration) throws ConfigurationException {
    String configPath = myConfigPathTextFieldWithBrowseButton.getChildComponent().getText();
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();
    builder.setConfigPath(configPath);
    builder.setBrowsers(StringUtil.notNullize(myBrowsers.getText()));
    builder.setEnvVars(myEnvironmentVariablesTextField.getEnvs());
    builder.setPassParentEnvVars(myEnvironmentVariablesTextField.isPassParentEnvs());
    runConfiguration.setRunSettings(builder.build());
    String karmaNodePackageDir = myKarmaPackageDirPathTextFieldWithBrowseButton.getChildComponent().getText();
    KarmaGlobalSettingsUtil.storeKarmaPackageDir(myProject, karmaNodePackageDir);
    String nodeInterpreterPath = myNodeInterpreterPathTextFieldWithBrowseButton.getChildComponent().getText();
    KarmaGlobalSettingsUtil.storeNodeInterpreterPath(nodeInterpreterPath);
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myRootComponent;
  }
}
