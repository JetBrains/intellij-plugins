package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.scope.KarmaScopeView;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.*;
import com.intellij.webcore.ui.PathShortener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KarmaRunConfigurationEditor extends SettingsEditor<KarmaRunConfiguration> {

  private final Project myProject;
  private final NodeJsInterpreterField myNodeInterpreterField;
  private final RawCommandLineEditor myNodeOptionsEditor;
  private final NodePackageField myKarmaPackageField;
  private final TextFieldWithBrowseButton myWorkingDirComponent;
  private final TextFieldWithHistoryWithBrowseButton myConfigPathField;
  private final EnvironmentVariablesTextFieldWithBrowseButton myEnvVarsComponent;
  private final JTextField myBrowsers;
  private final Map<KarmaScopeKind, JRadioButton> myRadioButtonMap = ContainerUtil.newHashMap();
  private final Map<KarmaScopeKind, KarmaScopeView> myScopeKindViewMap = ContainerUtil.newHashMap();
  private final JPanel mySelectedScopeKindPanel;
  private final JPanel myRootComponent;
  private final int myLongestLabelWidth = new JLabel("Environment variables:").getPreferredSize().width;

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myProject = project;
    myNodeInterpreterField = new NodeJsInterpreterField(project, false);
    myNodeOptionsEditor = createNodeOptionsEditor();
    myKarmaPackageField = new NodePackageField(myNodeInterpreterField, KarmaUtil.NODE_PACKAGE_NAME);
    myWorkingDirComponent = createWorkingDirComponent(project);
    myConfigPathField = createConfigurationFileTextField(project);
    PathShortener.enablePathShortening(myConfigPathField.getChildComponent().getTextEditor(), null);
    myEnvVarsComponent = new EnvironmentVariablesTextFieldWithBrowseButton();
    myBrowsers = createBrowsersTextField();
    JComponent browsersDescription = createBrowsersDescription();
    JPanel scopeKindPanel = createScopeKindRadioButtonPanel();
    mySelectedScopeKindPanel = new JPanel(new BorderLayout());
    myRootComponent = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.config_file.label"), myConfigPathField)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.browsers.label"), myBrowsers)
      .addLabeledComponent("", browsersDescription, 0, false)
      .addComponent(new JSeparator(), 8)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.node_interpreter.label"), myNodeInterpreterField, 8)
      .addLabeledComponent("Node o&ptions:", myNodeOptionsEditor)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.karma_package_dir.label"), myKarmaPackageField)
      .addLabeledComponent("Working directory:", myWorkingDirComponent)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.environment.label"), myEnvVarsComponent)
      .addSeparator(8)
      .addComponent(scopeKindPanel)
      .addComponent(mySelectedScopeKindPanel)
      .getPanel();
  }

  @NotNull
  private static RawCommandLineEditor createNodeOptionsEditor() {
    RawCommandLineEditor editor = new RawCommandLineEditor();
    editor.setDialogCaption("Node Options");
    JTextField field = editor.getTextField();
    if (field instanceof ExpandableTextField) {
      field.putClientProperty("monospaced", false);
    }
    return editor;
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
  private static TextFieldWithBrowseButton createWorkingDirComponent(@NotNull Project project) {
    TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      textFieldWithBrowseButton,
      "Select Working Directory",
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
    PathShortener.enablePathShortening(textFieldWithBrowseButton.getTextField(), null);
    return textFieldWithBrowseButton;
  }

  @NotNull
  private JPanel createScopeKindRadioButtonPanel() {
    JPanel testKindPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, JBUI.scale(40), 0));
    testKindPanel.setBorder(JBUI.Borders.emptyLeft(10));
    ButtonGroup buttonGroup = new ButtonGroup();
    for (KarmaScopeKind scopeKind : KarmaScopeKind.values()) {
      JRadioButton radioButton = new JRadioButton(UIUtil.removeMnemonic(scopeKind.getName()));
      final int index = UIUtil.getDisplayMnemonicIndex(scopeKind.getName());
      if (index != -1) {
        radioButton.setMnemonic(scopeKind.getName().charAt(index + 1));
        radioButton.setDisplayedMnemonicIndex(index);
      }
      radioButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setScopeKind(scopeKind);
        }
      });
      myRadioButtonMap.put(scopeKind, radioButton);
      testKindPanel.add(radioButton);
      buttonGroup.add(radioButton);
    }
    return testKindPanel;
  }

  private void setScopeKind(@NotNull KarmaScopeKind scopeKind) {
    KarmaScopeKind selectedScopeKind = getScopeKind();
    if (selectedScopeKind != scopeKind) {
      JRadioButton radioButton = myRadioButtonMap.get(scopeKind);
      radioButton.setSelected(true);
    }
    KarmaScopeView view = getScopeKindView(scopeKind);
    setCenterBorderLayoutComponent(mySelectedScopeKindPanel, view.getComponent());
  }

  @Nullable
  private KarmaScopeKind getScopeKind() {
    for (Map.Entry<KarmaScopeKind, JRadioButton> entry : myRadioButtonMap.entrySet()) {
      if (entry.getValue().isSelected()) {
        return entry.getKey();
      }
    }
    return null;
  }

  @NotNull
  private KarmaScopeView getScopeKindView(@NotNull KarmaScopeKind scopeKind) {
    KarmaScopeView view = myScopeKindViewMap.get(scopeKind);
    if (view == null) {
      view = scopeKind.createView(myProject);
      myScopeKindViewMap.put(scopeKind, view);
      JComponent component = view.getComponent();
      if (component.getLayout() instanceof GridBagLayout) {
        component.add(Box.createHorizontalStrut(myLongestLabelWidth), new GridBagConstraints(
          0, GridBagConstraints.RELATIVE,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          JBUI.insetsRight(UIUtil.DEFAULT_HGAP),
          0, 0
        ));
      }
    }
    return view;
  }

  private static void setCenterBorderLayoutComponent(@NotNull JPanel panel, @NotNull Component child) {
    Component prevChild = ((BorderLayout)panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
    if (prevChild != null) {
      panel.remove(prevChild);
    }
    panel.add(child, BorderLayout.CENTER);
    panel.revalidate();
    panel.repaint();
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
    // add a fake empty element as 'Down' key doesn't show popup if the combobox model is empty
    textFieldWithHistory.setHistory(Collections.singletonList(""));
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, () -> {
      textFieldWithHistory.setHistory(Collections.emptyList());
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
    myNodeOptionsEditor.setText(runSettings.getNodeOptions());
    myKarmaPackageField.setSelected(runConfiguration.getKarmaPackage());
    myConfigPathField.setTextAndAddToHistory(FileUtil.toSystemDependentName(runSettings.getConfigPath()));
    myBrowsers.setText(runSettings.getBrowsers());
    myWorkingDirComponent.setText(runSettings.getWorkingDirectorySystemDependent());
    myEnvVarsComponent.setData(runSettings.getEnvData());
    setScopeKind(runSettings.getScopeKind());
    KarmaScopeView view = getScopeKindView(runSettings.getScopeKind());
    view.resetFrom(runSettings);

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
  protected void applyEditorTo(@NotNull KarmaRunConfiguration runConfiguration) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();
    builder.setConfigPath(PathShortener.getAbsolutePath(myConfigPathField.getChildComponent().getTextEditor()));
    builder.setBrowsers(StringUtil.notNullize(myBrowsers.getText()));
    builder.setInterpreterRef(myNodeInterpreterField.getInterpreterRef());
    builder.setNodeOptions(myNodeOptionsEditor.getText());
    builder.setKarmaPackage(myKarmaPackageField.getSelected());
    builder.setWorkingDirectory(PathShortener.getAbsolutePath(myWorkingDirComponent.getTextField()));
    builder.setEnvData(myEnvVarsComponent.getData());
    KarmaScopeKind scopeKind = getScopeKind();
    if (scopeKind != null) {
      builder.setScopeKind(scopeKind);
      KarmaScopeView view = getScopeKindView(scopeKind);
      view.applyTo(builder);
    }
    runConfiguration.setRunSettings(builder.build());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myRootComponent;
  }
}
