package com.intellij.javascript.karma.execution;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.Function;
import com.intellij.util.Producer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.nodejs.NodeDetectionUtil;
import com.intellij.webcore.nodejs.NodeUIUtil;
import com.intellij.webcore.ui.SwingHelper;
import com.jetbrains.nodejs.NodeModuleManager;
import com.jetbrains.nodejs.util.CompletionModuleInfo;
import com.jetbrains.nodejs.util.ResolvedModuleInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myProject = project;
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Node.js interpreter:"), new GridBagConstraints(
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
      new Insets(0, 0, 7, 0),
      0, 0
    ));

    myKarmaPackageDirPathTextFieldWithBrowseButton = createKarmaPackageDirPathTextField(project);
    panel.add(new JLabel("Directory of Karma Node.js package:"), new GridBagConstraints(
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
      new Insets(0, 0, 7, 0),
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
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    myRootComponent = panel;
  }

  private static TextFieldWithHistoryWithBrowseButton createKarmaPackageDirPathTextField(@NotNull final Project project) {
    TextFieldWithHistoryWithBrowseButton karmaPackageDirPathComponent = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = karmaPackageDirPathComponent.getChildComponent();
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new Producer<List<String>>() {
      @Nullable
      @Override
      public List<String> produce() {
        Collection<CompletionModuleInfo> modules = NodeModuleManager.getInstance(project).collectVisibleNodeModules(project.getBaseDir());
        List<String> moduleDirs = ContainerUtil.newArrayListWithExpectedSize(modules.size());
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
      KarmaBundle.message("runConfiguration.karma_package_dir.browse_dialog.title")
    );
    return karmaPackageDirPathComponent;
  }

  @NotNull
  private static TextFieldWithHistoryWithBrowseButton createConfigurationFileTextField(@NotNull final Project project) {
    TextFieldWithHistoryWithBrowseButton textFieldWithHistoryWithBrowseButton = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = textFieldWithHistoryWithBrowseButton.getChildComponent();
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new Producer<List<String>>() {
      @Nullable
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
      KarmaBundle.message("runConfiguration.config_file.browse_dialog.title")
    );
    return textFieldWithHistoryWithBrowseButton;
  }

  @NotNull
  private static List<VirtualFile> listPossibleConfigFilesInProject(@NotNull Project project) {
    GlobalSearchScope scope = ProjectScope.getContentScope(project);
    Collection<VirtualFile> files = FileTypeIndex.getFiles(JavaScriptFileType.INSTANCE, scope);
    List<VirtualFile> result = ContainerUtil.newArrayList();
    for (VirtualFile file : files) {
      if (file.getName().endsWith(".conf.js")) {
        result.add(file);
      }
    }
    return result;
  }

  @Override
  protected void resetEditorFrom(@NotNull KarmaRunConfiguration runConfiguration) {
    String nodeInterpreterPath = KarmaGlobalSettingsUtil.loadNodeInterpreterPath();
    if (StringUtil.isEmpty(nodeInterpreterPath)) {
      File nodeInterpreterFile = PathEnvironmentVariableUtil.findInPath(NodeDetectionUtil.NODE_INTERPRETER_BASE_NAME);
      if (nodeInterpreterFile != null) {
        nodeInterpreterPath = nodeInterpreterFile.getAbsolutePath();
      }
    }
    setTextAndAddToHistory(myNodeInterpreterPathTextFieldWithBrowseButton.getChildComponent(), nodeInterpreterPath);

    String karmaNodePackageDir = KarmaGlobalSettingsUtil.loadKarmaNodePackageDir();
    if (StringUtil.isEmpty(karmaNodePackageDir)) {
      NodeModuleManager manager = NodeModuleManager.getInstance(myProject);
      ResolvedModuleInfo moduleInfo = manager.resolveModule("karma", myProject.getBaseDir());
      if (moduleInfo != null) {
        VirtualFile dir = moduleInfo.getModuleSourceRoot();
        if (dir.isDirectory()) {
          karmaNodePackageDir = FileUtil.toSystemDependentName(dir.getPath());
        }
      }
    }
    setTextAndAddToHistory(myKarmaPackageDirPathTextFieldWithBrowseButton.getChildComponent(), karmaNodePackageDir);
    KarmaRunSettings runSettings = runConfiguration.getRunSetting();
    myConfigPathTextFieldWithBrowseButton.getChildComponent().setText(runSettings.getConfigPath());
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
    runConfiguration.setRunSettings(builder.build());
    String karmaNodePackageDir = myKarmaPackageDirPathTextFieldWithBrowseButton.getChildComponent().getText();
    KarmaGlobalSettingsUtil.storeKarmaNodePackageDir(karmaNodePackageDir);
    String nodeInterpreterPath = myNodeInterpreterPathTextFieldWithBrowseButton.getChildComponent().getText();
    KarmaGlobalSettingsUtil.storeNodeInterpreterPath(nodeInterpreterPath);
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myRootComponent;
  }

  @Override
  protected void disposeEditor() {
  }
}
