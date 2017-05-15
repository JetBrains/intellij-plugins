package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackageField;
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
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class KarmaRunConfigurationEditor extends SettingsEditor<KarmaRunConfiguration> {

  private final NodeJsInterpreterField myNodeInterpreterField;
  private final NodePackageField myKarmaPackageField;
  private final TextFieldWithHistoryWithBrowseButton myConfigPathField;
  private final EnvironmentVariablesTextFieldWithBrowseButton myEnvVarsComponent;
  private final JTextField myBrowsers;
  private final JPanel myRootComponent;

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myNodeInterpreterField = new NodeJsInterpreterField(project, false);
    myKarmaPackageField = new NodePackageField(project, KarmaUtil.NODE_PACKAGE_NAME, myNodeInterpreterField::getInterpreter);
    myConfigPathField = createConfigurationFileTextField(project);
    myEnvVarsComponent = new EnvironmentVariablesTextFieldWithBrowseButton();
    myBrowsers = createBrowsersTextField();
    JComponent browsersDescription = createBrowsersDescription();
    myRootComponent = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.config_file.label"), myConfigPathField)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.browsers.label"), myBrowsers)
      .addLabeledComponent("", browsersDescription, 0, false)
      .addComponent(new JSeparator(), 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.node_interpreter.label"), myNodeInterpreterField, 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.karma_package_dir.label"), myKarmaPackageField)
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

  @NotNull
  private static TextFieldWithHistoryWithBrowseButton createConfigurationFileTextField(@NotNull final Project project) {
    TextFieldWithHistoryWithBrowseButton textFieldWithHistoryWithBrowseButton = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = textFieldWithHistoryWithBrowseButton.getChildComponent();
    textFieldWithHistory.setHistorySize(-1);
    textFieldWithHistory.setMinimumAndPreferredWidth(0);
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, () -> {
      List<VirtualFile> newFiles = KarmaUtil.listPossibleConfigFilesInProject(project);
      List<String> newFilePaths = ContainerUtil.map(newFiles, file -> FileUtil.toSystemDependentName(file.getPath()));
      Collections.sort(newFilePaths);
      return newFilePaths;
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
    myKarmaPackageField.setSelected(runConfiguration.getKarmaPackage());
    myConfigPathField.setTextAndAddToHistory(FileUtil.toSystemDependentName(runSettings.getConfigPath()));
    myBrowsers.setText(runSettings.getBrowsers());
    myEnvVarsComponent.setData(runSettings.getEnvData());

    updatePreferredWidth();
  }

  private void updatePreferredWidth() {
    DialogWrapper dialogWrapper = DialogWrapper.findInstance(myRootComponent);
    if (dialogWrapper instanceof SingleConfigurableEditor) {
      // dialog for single run configuration
      myNodeInterpreterField.setPreferredWidthToFitText();
      myKarmaPackageField.setPreferredWidthToFitText();
      SwingHelper.setPreferredWidthToFitText(myConfigPathField);
      ApplicationManager.getApplication().invokeLater(() -> SwingHelper.adjustDialogSizeToFitPreferredSize(dialogWrapper), ModalityState.any());
    }
  }

  @Override
  protected void applyEditorTo(@NotNull KarmaRunConfiguration runConfiguration) throws ConfigurationException {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();
    builder.setConfigPath(myConfigPathField.getChildComponent().getText());
    builder.setBrowsers(StringUtil.notNullize(myBrowsers.getText()));
    builder.setInterpreterRef(myNodeInterpreterField.getInterpreterRef());
    builder.setEnvData(myEnvVarsComponent.getData());
    builder.setKarmaPackage(myKarmaPackageField.getSelected());
    runConfiguration.setRunSettings(builder.build());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myRootComponent;
  }
}
