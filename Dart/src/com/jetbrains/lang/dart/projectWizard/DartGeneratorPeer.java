// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.AsyncProcessIcon;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

public class DartGeneratorPeer implements ProjectGeneratorPeer<DartProjectWizardData> {
  private static final String DART_PROJECT_TEMPLATE = "DART_PROJECT_TEMPLATE";
  private static final String CREATE_SAMPLE_UNCHECKED = "CREATE_SAMPLE_UNCHECKED";

  private JPanel myMainPanel;
  private ComboboxWithBrowseButton mySdkPathComboWithBrowse;
  private JBLabel myVersionLabel;

  private JPanel myTemplatesPanel;
  private JPanel myLoadingTemplatesPanel;
  private JPanel myLoadedTemplatesPanel;
  private JBCheckBox myCreateSampleProjectCheckBox;
  private JBList<DartProjectTemplate> myTemplatesList;

  private JBLabel myErrorLabel; // shown in IntelliJ IDEA only

  private boolean myIntellijLiveValidationEnabled = false;

  private boolean myDartCreateCalcStarted;
  private boolean myStagehandCalcStarted;
  private List<DartProjectTemplate> myDartCreateTemplates;// not-null means that it's been already calculated
  private List<DartProjectTemplate> myStagehandTemplates;// not-null means that it's been already calculated

  public DartGeneratorPeer() {
    // set initial values before initDartSdkControls() because listeners should not be triggered on initialization
    mySdkPathComboWithBrowse.getComboBox().setEditable(true);
    //mySdkPathComboWithBrowse.getComboBox().getEditor().setItem(...); initial sdk path will be correctly taken from known paths history

    // now setup controls
    DartSdkUtil.initDartSdkControls(null, mySdkPathComboWithBrowse, myVersionLabel);

    myCreateSampleProjectCheckBox.addActionListener(e -> myTemplatesList.setEnabled(myCreateSampleProjectCheckBox.isSelected()));
    String selectedTemplateName = PropertiesComponent.getInstance().getValue(DART_PROJECT_TEMPLATE);
    myCreateSampleProjectCheckBox.setSelected(!CREATE_SAMPLE_UNCHECKED.equals(selectedTemplateName));

    myTemplatesList.setEmptyText(DartBundle.message("set.sdk.to.see.sample.content.options"));

    myTemplatesList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        DartProjectTemplate template = (DartProjectTemplate)value;
        String text = template.getDescription().isEmpty()
                      ? template.getName()
                      : template.getName() + " - " + StringUtil.decapitalize(template.getDescription());
        component.setText(text);
        return component;
      }
    });

    myErrorLabel.setIcon(AllIcons.Actions.Lightning);
    myErrorLabel.setVisible(false);

    myLoadingTemplatesPanel.setVisible(false);
    myCreateSampleProjectCheckBox.setEnabled(false);
    myTemplatesList.setEnabled(false);

    final JTextComponent editorComponent = (JTextComponent)mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    editorComponent.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final @NotNull DocumentEvent e) {
        final String sdkPath = mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
        final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(sdkPath);
        if (message == null) {
          onSdkPathChanged();
        }
      }
    });

    onSdkPathChanged();
  }

  private void onSdkPathChanged() {
    String sdkPath = mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
    String errorMessage = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(sdkPath);
    if (errorMessage != null) {
      myLoadingTemplatesPanel.setVisible(false);
      myTemplatesPanel.setVisible(false);
      return;
    }

    boolean useDartCreate = Stagehand.isUseDartCreate(sdkPath);
    if (useDartCreate) {
      if (myDartCreateCalcStarted) {
        if (myDartCreateTemplates != null) {
          showTemplates(myDartCreateTemplates);
        }
        else {
          // Calculation in progress, just wait.
          myLoadingTemplatesPanel.setVisible(true);
          myLoadedTemplatesPanel.setVisible(false);
        }
      }
      else {
        myDartCreateCalcStarted = true;
        startLoadingTemplates(useDartCreate);
      }
    }
    else {
      if (myStagehandCalcStarted) {
        if (myStagehandTemplates != null) {
          showTemplates(myStagehandTemplates);
        }
        else {
          // Calculation in progress, just wait.
          myLoadingTemplatesPanel.setVisible(true);
          myLoadedTemplatesPanel.setVisible(false);
        }
      }
      else {
        myStagehandCalcStarted = true;
        startLoadingTemplates(useDartCreate);
      }
    }
  }

  private void startLoadingTemplates(boolean useDartCreate) {
    myLoadingTemplatesPanel.setVisible(true);
    myLoadingTemplatesPanel.setPreferredSize(myLoadedTemplatesPanel.getPreferredSize());

    myLoadedTemplatesPanel.setVisible(false);

    final AsyncProcessIcon asyncProcessIcon = new AsyncProcessIcon("Dart project templates loading");
    myLoadingTemplatesPanel.add(asyncProcessIcon, new GridConstraints());  // defaults are ok: row = 0, column = 0
    asyncProcessIcon.resume();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      final String sdkPath =
        FileUtil.toSystemIndependentName(mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
      DartProjectTemplate.loadTemplatesAsync(sdkPath, templates -> {
        asyncProcessIcon.suspend();
        myLoadingTemplatesPanel.remove(asyncProcessIcon);
        Disposer.dispose(asyncProcessIcon);

        if (useDartCreate) {
          myDartCreateTemplates = templates;
        }
        else {
          myStagehandTemplates = templates;
        }

        // it's better to call onSdkPathChanged() but not showTemplates() directly as sdk path could have been changed during this long calculation
        onSdkPathChanged();
      });
    });
  }

  private void showTemplates(final List<DartProjectTemplate> templates) {
    ListModel<DartProjectTemplate> currentModel = myTemplatesList.getModel();

    if (myLoadedTemplatesPanel.isVisible() &&
        currentModel.getSize() == templates.size() &&
        IntStream.range(0, templates.size()).allMatch(i -> templates.get(i).getName().equals(currentModel.getElementAt(i).getName()))) {
      // already showing the right list
      return;
    }

    boolean rightAfterLoading = myLoadingTemplatesPanel.isVisible();

    myLoadingTemplatesPanel.setVisible(false);
    myLoadedTemplatesPanel.setVisible(true);
    myCreateSampleProjectCheckBox.setEnabled(true);

    final String selectedTemplateName = PropertiesComponent.getInstance().getValue(DART_PROJECT_TEMPLATE);

    myTemplatesList.setVisibleRowCount(Math.min(8, templates.size()));
    myTemplatesList.setEnabled(myCreateSampleProjectCheckBox.isSelected());

    DartProjectTemplate selectedTemplate = null;

    final DefaultListModel<DartProjectTemplate> model = new DefaultListModel<>();
    for (DartProjectTemplate template : templates) {
      model.addElement(template);

      if (rightAfterLoading && template.getName().equals(selectedTemplateName)) {
        selectedTemplate = template;
      }
    }

    myTemplatesList.setModel(model);

    if (selectedTemplate != null) {
      myTemplatesList.setSelectedValue(selectedTemplate, true);
    }
    else if (!templates.isEmpty()) {
      myTemplatesList.setSelectedIndex(0);
    }
  }

  @Override
  public @NotNull JComponent getComponent(@NotNull TextFieldWithBrowseButton myLocationField, @NotNull Runnable checkValid) {
    return myMainPanel;
  }

  @NotNull JPanel getMainPanel() {
    return myMainPanel;
  }

  @Override
  public void buildUI(final @NotNull SettingsStep settingsStep) {
    settingsStep.addSettingsField(DartBundle.message("dart.sdk.path.label"), mySdkPathComboWithBrowse);
    settingsStep.addSettingsField(DartBundle.message("version.label"), myVersionLabel);
    settingsStep.addSettingsComponent(myTemplatesPanel);
  }

  @Override
  public @NotNull DartProjectWizardData getSettings() {
    final String sdkPath = FileUtil.toSystemIndependentName(mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
    final DartProjectTemplate template = myCreateSampleProjectCheckBox.isSelected() ? myTemplatesList.getSelectedValue() : null;
    PropertiesComponent.getInstance().setValue(DART_PROJECT_TEMPLATE, template == null ? CREATE_SAMPLE_UNCHECKED : template.getName());

    return new DartProjectWizardData(sdkPath, template);
  }

  @Override
  public @Nullable ValidationInfo validate() {
    final String sdkPath = mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
    final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(sdkPath);
    if (message != null) {
      return new ValidationInfo(message, mySdkPathComboWithBrowse);
    }

    if (myCreateSampleProjectCheckBox.isSelected()) {
      if (myTemplatesList.getSelectedValue() == null) {
        return new ValidationInfo(DartBundle.message("project.template.not.selected"), myCreateSampleProjectCheckBox);
      }
    }

    return null;
  }

  public boolean validateInIntelliJ() {
    final ValidationInfo info = validate();

    if (info == null) {
      myErrorLabel.setVisible(false);
      return true;
    }
    else {
      myErrorLabel.setVisible(true);
      HtmlChunk.Element html = new HtmlBuilder().append(info.message)
        .wrapWith("font").attr("color", "#" + ColorUtil.toHex(JBColor.RED))
        .wrapWith("html");
      myErrorLabel.setText(html.toString());

      if (!myIntellijLiveValidationEnabled) {
        myIntellijLiveValidationEnabled = true;
        enableIntellijLiveValidation();
      }

      return false;
    }
  }

  private void enableIntellijLiveValidation() {
    final JTextComponent editorComponent = (JTextComponent)mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    editorComponent.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final @NotNull DocumentEvent e) {
        validateInIntelliJ();
      }
    });

    myCreateSampleProjectCheckBox.addActionListener(e -> validateInIntelliJ());

    myTemplatesList.addListSelectionListener(e -> validateInIntelliJ());
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(final @NotNull WebProjectGenerator.SettingsStateListener stateListener) {
    final JTextComponent editorComponent = (JTextComponent)mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    editorComponent.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final @NotNull DocumentEvent e) {
        stateListener.stateChanged(validate() == null);
      }
    });

    myCreateSampleProjectCheckBox.addActionListener(e -> stateListener.stateChanged(validate() == null));

    myTemplatesList.addListSelectionListener(e -> stateListener.stateChanged(validate() == null));
  }

  private void createUIComponents() {
    mySdkPathComboWithBrowse = new ComboboxWithBrowseButton(new ComboBox<>());
  }
}
