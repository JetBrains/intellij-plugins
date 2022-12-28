// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.scope.KarmaScopeView;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.scale.JBUIScale;
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
import java.util.HashMap;
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
  private final RawCommandLineEditor myKarmaOptionsEditor;
  private final Map<KarmaScopeKind, JRadioButton> myRadioButtonMap = new HashMap<>();
  private final Map<KarmaScopeKind, KarmaScopeView> myScopeKindViewMap = new HashMap<>();
  private final JPanel mySelectedScopeKindPanel;
  private final JPanel myRootComponent;
  private final int myLongestLabelWidth = new JLabel(UIUtil.removeMnemonic(KarmaBundle.message("runConfiguration.environment.label"))).getPreferredSize().width;

  public KarmaRunConfigurationEditor(@NotNull Project project) {
    myProject = project;
    myNodeInterpreterField = new NodeJsInterpreterField(project, true);
    myNodeOptionsEditor = createOptionsEditor(null);
    myKarmaPackageField = new NodePackageField(myNodeInterpreterField, KarmaUtil.PKG_DESCRIPTOR, null);
    myWorkingDirComponent = createWorkingDirComponent(project);
    myConfigPathField = createConfigurationFileTextField(project);
    myEnvVarsComponent = new EnvironmentVariablesTextFieldWithBrowseButton();
    myKarmaOptionsEditor = createOptionsEditor(KarmaBundle.message("run_config.karma_options.placeholder.text"));
    JPanel scopeKindPanel = createScopeKindRadioButtonPanel();
    mySelectedScopeKindPanel = new JPanel(new BorderLayout());
    myRootComponent = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.config_file.label"), myConfigPathField)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.karmaOptions.label"), myKarmaOptionsEditor)
      .addComponent(new JSeparator(), 8)
      .addLabeledComponent(NodeJsInterpreterField.getLabelTextForComponent(), myNodeInterpreterField, 8)
      .addLabeledComponent(JavaScriptBundle.message("rc.nodeOptions.label"), myNodeOptionsEditor)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.karma_package_dir.label"), myKarmaPackageField)
      .addLabeledComponent(JavaScriptBundle.message("rc.workingDirectory.label"), myWorkingDirComponent)
      .addLabeledComponent(KarmaBundle.message("runConfiguration.environment.label"), myEnvVarsComponent)
      .addSeparator(8)
      .addComponent(scopeKindPanel)
      .addComponent(mySelectedScopeKindPanel)
      .getPanel();
  }

  @NotNull
  private static RawCommandLineEditor createOptionsEditor(@Nullable @NlsContexts.StatusText String emptyText) {
    RawCommandLineEditor editor = new RawCommandLineEditor();
    JTextField field = editor.getTextField();
    if (field instanceof ExpandableTextField) {
      ((ExpandableTextField)field).setMonospaced(false);
    }
    if (field instanceof ComponentWithEmptyText && emptyText != null) {
      ((ComponentWithEmptyText)field).getEmptyText().setText(emptyText);
    }
    return editor;
  }

  @NotNull
  private static TextFieldWithBrowseButton createWorkingDirComponent(@NotNull Project project) {
    TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      textFieldWithBrowseButton,
      JavaScriptBundle.message("rc.workingDirectory.browseDialogTitle"),
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
    PathShortener.enablePathShortening(textFieldWithBrowseButton.getTextField(), null);
    return textFieldWithBrowseButton;
  }

  @NotNull
  private JPanel createScopeKindRadioButtonPanel() {
    JPanel testKindPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, JBUIScale.scale(40), 0));
    testKindPanel.setBorder(JBUI.Borders.emptyLeft(10));
    ButtonGroup buttonGroup = new ButtonGroup();
    for (KarmaScopeKind scopeKind : KarmaScopeKind.values()) {
      JRadioButton radioButton = new JRadioButton(UIUtil.replaceMnemonicAmpersand(scopeKind.getName()));
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
  private static TextFieldWithHistoryWithBrowseButton createConfigurationFileTextField(@NotNull final Project project) {
    TextFieldWithHistoryWithBrowseButton textFieldWithHistoryWithBrowseButton = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = textFieldWithHistoryWithBrowseButton.getChildComponent();
    textFieldWithHistory.setHistorySize(-1);
    textFieldWithHistory.setMinimumAndPreferredWidth(0);
    // add a fake empty element as 'Down' key doesn't show popup if the combobox model is empty
    textFieldWithHistory.setHistory(Collections.singletonList(""));
    PathShortener.enablePathShortening(textFieldWithHistory.getTextEditor(), null);
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, () -> {
      textFieldWithHistory.setHistory(Collections.emptyList());
      List<VirtualFile> newFiles = KarmaUtil.listPossibleConfigFilesInProject(project);
      List<String> newFilePaths = ContainerUtil.sorted(ContainerUtil.map(newFiles, file -> {
        String path = FileUtil.toSystemDependentName(file.getPath());
        return FileUtil.getLocationRelativeToUserHome(path, false);
      }));
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
    myConfigPathField.setTextAndAddToHistory(runSettings.getConfigPathSystemDependent());
    myKarmaOptionsEditor.setText(runSettings.getKarmaOptions());
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
    builder.setKarmaOptions(StringUtil.notNullize(myKarmaOptionsEditor.getText()));
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
