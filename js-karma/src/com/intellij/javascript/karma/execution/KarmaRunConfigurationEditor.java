package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.NodeSettings;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.Function;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class KarmaRunConfigurationEditor extends SettingsEditor<KarmaRunConfiguration> {

  private final Project myProject;
  private final NodeJsInterpreterField myNodeInterpreterField;
  private final TextFieldWithHistoryWithBrowseButton myKarmaPackageDirPathTextFieldWithBrowseButton;
  private final TextFieldWithHistoryWithBrowseButton myConfigPathTextFieldWithBrowseButton;
  private final EnvironmentVariablesTextFieldWithBrowseButton myEnvVarsComponent;
  private final JTextField myBrowsers;
  private final JPanel myRootComponent;

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myProject = project;
    myNodeInterpreterField = new NodeJsInterpreterField(project, false);
    myKarmaPackageDirPathTextFieldWithBrowseButton = createKarmaPackageDirPathTextField(project);
    myConfigPathTextFieldWithBrowseButton = createConfigurationFileTextField(project);
    myEnvVarsComponent = new EnvironmentVariablesTextFieldWithBrowseButton();
    myBrowsers = createBrowsersTextField();
    JComponent browsersDescription = createBrowsersDescription();
    myRootComponent = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.config_file.label"), myConfigPathTextFieldWithBrowseButton)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.browsers.label"), myBrowsers)
      .addLabeledComponent("", browsersDescription, 0, false)
      .addComponent(new JSeparator(), 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.node_interpreter.label"), myNodeInterpreterField, 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.karma_package_dir.label"), myKarmaPackageDirPathTextFieldWithBrowseButton)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.environment.label"), myEnvVarsComponent)
      .getPanel();
  }

  @NotNull
  private static JComponent createBrowsersDescription() {
    Color fgColor = UIUtil.getLabelDisabledForeground();
    JEditorPane editorPane = SwingHelper.createHtmlViewer(true, UIUtil.getTitledBorderFont(), null, fgColor);
    SwingHelper.setHtml(editorPane, "overrides <i>browsers</i> setting from the configuration file", fgColor);
    JPanel panel = SwingHelper.wrapWithHorizontalStretch(editorPane);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    return panel;
  }

  @NotNull
  private static JTextField createBrowsersTextField() {
    JBTextField browsers = new JBTextField();
    StatusText emptyStatusText = browsers.getEmptyText();
    emptyStatusText.setText("comma-separated list of browsers (e.g. Chrome,ChromeCanary,Firefox)");
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
        NodeJsLocalInterpreter interpreter = NodeJsLocalInterpreter.tryCast(myNodeInterpreterField.getInterpreter());
        List<CompletionModuleInfo> modules = ContainerUtil.newArrayList();
        VirtualFile requester = KarmaUtil.getRequester(
          myProject,
          myConfigPathTextFieldWithBrowseButton.getChildComponent().getText()
        );
        NodeModuleSearchUtil.findModulesWithName(modules,
                                                 KarmaUtil.NODE_PACKAGE_NAME,
                                                 requester,
                                                 NodeSettings.create(interpreter),
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

    //noinspection DialogTitleCapitalization
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
        List<VirtualFile> newFiles = KarmaUtil.listPossibleConfigFilesInProject(project);
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

  @Override
  protected void resetEditorFrom(@NotNull KarmaRunConfiguration runConfiguration) {
    KarmaRunSettings runSettings = runConfiguration.getRunSettings();

    myNodeInterpreterField.setInterpreterRef(runSettings.getInterpreterRef());

    String karmaPackageDir = FileUtil.toSystemDependentName(runConfiguration.getKarmaPackageDir());
    setTextAndAddToHistory(myKarmaPackageDirPathTextFieldWithBrowseButton.getChildComponent(), karmaPackageDir);

    String configFilePath = FileUtil.toSystemDependentName(runSettings.getConfigPath());
    setTextAndAddToHistory(myConfigPathTextFieldWithBrowseButton.getChildComponent(), configFilePath);

    myBrowsers.setText(runSettings.getBrowsers());
    myEnvVarsComponent.setData(runSettings.getEnvData());

    updatePreferredWidth();
  }

  private void updatePreferredWidth() {
    DialogWrapper dialogWrapper = DialogWrapper.findInstance(myRootComponent);
    if (dialogWrapper instanceof SingleConfigurableEditor) {
      // dialog for single run configuration
      myNodeInterpreterField.setPreferredWidthToFitText();
      SwingHelper.setPreferredWidthToFitText(myKarmaPackageDirPathTextFieldWithBrowseButton);
      SwingHelper.setPreferredWidthToFitText(myConfigPathTextFieldWithBrowseButton);
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          SwingHelper.adjustDialogSizeToFitPreferredSize(dialogWrapper);
        }
      }, ModalityState.any());
    }
  }

  private static void setTextAndAddToHistory(@NotNull TextFieldWithHistory textFieldWithHistory, @Nullable String text) {
    textFieldWithHistory.setText(text);
    textFieldWithHistory.getModel().setSelectedItem(text);
    textFieldWithHistory.addCurrentTextToHistory();
  }

  @Override
  protected void applyEditorTo(@NotNull KarmaRunConfiguration runConfiguration) throws ConfigurationException {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();
    builder.setConfigPath(myConfigPathTextFieldWithBrowseButton.getChildComponent().getText());
    builder.setBrowsers(StringUtil.notNullize(myBrowsers.getText()));
    builder.setInterpreterRef(myNodeInterpreterField.getInterpreterRef());
    builder.setEnvData(myEnvVarsComponent.getData());
    builder.setKarmaPackageDir(myKarmaPackageDirPathTextFieldWithBrowseButton.getChildComponent().getText());
    runConfiguration.setRunSettings(builder.build());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myRootComponent;
  }
}
