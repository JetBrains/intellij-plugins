package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.Function;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunConfigurationEditor extends SettingsEditor<KarmaRunConfiguration> {

  private final Project myProject;
  private final JComponent myRootComponent;
  private final TextFieldWithHistoryWithBrowseButton myNodeInterpreterPathTextFieldWithBrowseButton;
  private final TextFieldWithHistoryWithBrowseButton myKarmaPackageDirPathTextFieldWithBrowseButton;
  private final TextFieldWithHistoryWithBrowseButton myConfigPathTextFieldWithBrowseButton;
  private final EnvironmentVariablesComponent myEnvironmentVariablesComponent;

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myProject = project;
    int verticalSpace = 7;
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel(NodeUIUtil.NODE_INTERPRETER_PATH_LABEL), new GridBagConstraints(
      0, 0,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.NONE,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    myNodeInterpreterPathTextFieldWithBrowseButton = NodeUIUtil.createNodeInterpreterTextField(project);
    panel.add(myNodeInterpreterPathTextFieldWithBrowseButton, new GridBagConstraints(
      0, 1,
      1, 1,
      1.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      new Insets(0, 0, verticalSpace, 0),
      0, 0
    ));

    myKarmaPackageDirPathTextFieldWithBrowseButton = createKarmaPackageDirPathTextField(project);
    panel.add(new JLabel("Karma node package:"), new GridBagConstraints(
      0, 2,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    panel.add(myKarmaPackageDirPathTextFieldWithBrowseButton, new GridBagConstraints(
      0, 3,
      1, 1,
      0, 0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      new Insets(0, 0, verticalSpace, 0),
      0, 0
    ));

    panel.add(new JLabel("Configuration file (usually *.conf.js):"), new GridBagConstraints(
      0, 4,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.NONE,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    myConfigPathTextFieldWithBrowseButton = createConfigurationFileTextField(project);
    panel.add(myConfigPathTextFieldWithBrowseButton, new GridBagConstraints(
      0, 5,
      1, 1,
      1.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      new Insets(0, 0, verticalSpace, 0),
      0, 0
    ));
    myEnvironmentVariablesComponent = new EnvironmentVariablesComponent();
    myEnvironmentVariablesComponent.setLabelLocation(BorderLayout.NORTH);
    panel.add(myEnvironmentVariablesComponent, new GridBagConstraints(
      0, 6,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    myRootComponent = panel;
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

    myEnvironmentVariablesComponent.setEnvs(runSettings.getEnvVars());
    myEnvironmentVariablesComponent.setPassParentEnvs(runSettings.isPassParentEnvVars());
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
    builder.setEnvVars(myEnvironmentVariablesComponent.getEnvs());
    builder.setPassParentEnvVars(myEnvironmentVariablesComponent.isPassParentEnvs());
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
