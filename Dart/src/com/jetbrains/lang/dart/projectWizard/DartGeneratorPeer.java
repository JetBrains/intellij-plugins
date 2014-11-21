package com.jetbrains.lang.dart.projectWizard;

import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.Consumer;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DartGeneratorPeer implements WebProjectGenerator.GeneratorPeer<DartProjectWizardData> {
  private static final String DART_PROJECT_TEMPLATE = "DART_PROJECT_TEMPLATE";

  private JPanel myMainPanel;
  private TextFieldWithBrowseButton mySdkPathTextWithBrowse;
  private JBLabel myVersionLabel;

  private TextFieldWithBrowseButton myDartiumPathTextWithBrowse;
  private JButton myDartiumSettingsButton;
  private JBCheckBox myCheckedModeCheckBox;

  private JPanel myLoadingPanel;

  private JPanel myTemplatesPanel;
  private JBCheckBox myCreateSampleProjectCheckBox;
  private JBList myTemplatesList;

  private JBLabel myErrorLabel; // shown in IntelliJ IDEA only

  private ChromeSettings myDartiumSettingsCurrent;

  private boolean myIntellijLiveValidationEnabled = false;

  public DartGeneratorPeer() {
    // set initial values before initDartSdkAndDartiumControls() because listeners should not be triggered on initialization

    final Library[] libraries = ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel().getLibraries();
    final DartSdk sdkInitial = DartSdk.findDartSdkAmongGlobalLibs(libraries);
    mySdkPathTextWithBrowse.setText(sdkInitial == null ? "" : FileUtil.toSystemDependentName(sdkInitial.getHomePath()));

    final WebBrowser dartiumInitial = DartiumUtil.getDartiumBrowser();
    myDartiumSettingsCurrent = new ChromeSettings();
    if (dartiumInitial != null) {
      final BrowserSpecificSettings browserSpecificSettings = dartiumInitial.getSpecificSettings();
      if (browserSpecificSettings instanceof ChromeSettings) {
        myDartiumSettingsCurrent = (ChromeSettings)browserSpecificSettings.clone();
      }
    }

    myDartiumPathTextWithBrowse.setText(dartiumInitial == null
                                        ? ""
                                        : FileUtilRt.toSystemDependentName(StringUtil.notNullize(dartiumInitial.getPath())));


    // now setup controls
    DartSdkUtil.initDartSdkAndDartiumControls(null, mySdkPathTextWithBrowse, myVersionLabel, myDartiumPathTextWithBrowse,
                                              new Computable.PredefinedValueComputable<ChromeSettings>(myDartiumSettingsCurrent),
                                              myDartiumSettingsButton, myCheckedModeCheckBox,
                                              new Computable.PredefinedValueComputable<Boolean>(false));

    final boolean checkedMode = dartiumInitial == null || DartiumUtil.isCheckedMode(myDartiumSettingsCurrent.getEnvironmentVariables());
    myCheckedModeCheckBox.setSelected(checkedMode);

    myCreateSampleProjectCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        myTemplatesList.setEnabled(myCreateSampleProjectCheckBox.isSelected());
      }
    });

    myErrorLabel.setIcon(AllIcons.Actions.Lightning);
    myErrorLabel.setVisible(false);

    final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
    if (message == null) {
      startLoadingTemplates();
    }
    else {
      myLoadingPanel.setVisible(false);

      myCreateSampleProjectCheckBox.setEnabled(false);
      myTemplatesList.setEnabled(false);

      mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(final DocumentEvent e) {
          final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
          if (message == null) {
            mySdkPathTextWithBrowse.getTextField().getDocument().removeDocumentListener(this);
            startLoadingTemplates();
          }
        }
      });
    }
  }

  private void startLoadingTemplates() {
    myLoadingPanel.setVisible(true);
    myLoadingPanel.setPreferredSize(myTemplatesPanel.getPreferredSize());

    myTemplatesPanel.setVisible(false);

    myCreateSampleProjectCheckBox.setSelected(false); // until loaded

    final AsyncProcessIcon loadingIcon = new AsyncProcessIcon("Dart project templates loading");
    myLoadingPanel.add(loadingIcon, BorderLayout.WEST);
    loadingIcon.resume();

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        DartProjectTemplate.loadTemplatesAsync(mySdkPathTextWithBrowse.getText().trim(), new Consumer<List<DartProjectTemplate>>() {
          @Override
          public void consume(final List<DartProjectTemplate> templates) {
            loadingIcon.suspend();
            Disposer.dispose(loadingIcon);
            onTemplatesLoaded(templates);
          }
        });
      }
    });
  }

  private void onTemplatesLoaded(final List<DartProjectTemplate> templates) {
    myLoadingPanel.setVisible(false);
    myTemplatesPanel.setVisible(true);
    myCreateSampleProjectCheckBox.setEnabled(true);
    myTemplatesList.setEnabled(true);

    final String selectedTemplateName = PropertiesComponent.getInstance().getValue(DART_PROJECT_TEMPLATE);
    myCreateSampleProjectCheckBox.setSelected(selectedTemplateName != null);
    myTemplatesList.setEnabled(myCreateSampleProjectCheckBox.isSelected());

    DartProjectTemplate selectedTemplate = null;

    final DefaultListModel model = new DefaultListModel();
    for (DartProjectTemplate template : templates) {
      model.addElement(template);

      if (template.getName().equals(selectedTemplateName)) {
        selectedTemplate = template;
      }
    }

    myTemplatesList.setModel(model);

    if (selectedTemplate != null) {
      myTemplatesList.setSelectedValue(selectedTemplate, true);
    }
    else if (templates.size() > 0) {
      myTemplatesList.setSelectedIndex(0);
    }

    myTemplatesList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        final DartProjectTemplate template = (DartProjectTemplate)value;
        final String text = template.getDescription().isEmpty()
                            ? template.getName()
                            : template.getName() + " - " + StringUtil.decapitalize(template.getDescription());
        component.setText(text);
        return component;
      }
    });
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myMainPanel;
  }

  @Override
  public void buildUI(final @NotNull SettingsStep settingsStep) {
    assert false;
    //settingsStep.addSettingsField(DartBundle.message("dart.sdk.path.label"), mySdkPathTextWithBrowse);
    //settingsStep.addSettingsField(DartBundle.message("version.label"), myVersionLabel);
    //settingsStep.addSettingsField(DartBundle.message("dartium.path.label"), myDartiumSettingsPanel);
    //settingsStep.addSettingsField("", myCheckedModeCheckBox);
  }

  @NotNull
  @Override
  public DartProjectWizardData getSettings() {
    final String sdkPath = FileUtil.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
    final String dartiumPath = FileUtil.toSystemIndependentName(myDartiumPathTextWithBrowse.getText().trim());

    final DartProjectTemplate template = myCreateSampleProjectCheckBox.isSelected()
                                         ? (DartProjectTemplate)myTemplatesList.getSelectedValue() : null;
    if (template == null) {
      PropertiesComponent.getInstance().unsetValue(DART_PROJECT_TEMPLATE);
    }
    else {
      PropertiesComponent.getInstance().setValue(DART_PROJECT_TEMPLATE, template.getName());
    }

    return new DartProjectWizardData(sdkPath, dartiumPath, myDartiumSettingsCurrent, template);
  }

  @Nullable
  @Override
  public ValidationInfo validate() {
    // invalid Dartium path is not a blocking error
    final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
    if (message != null) {
      return new ValidationInfo(message, mySdkPathTextWithBrowse);
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
      myErrorLabel
        .setText(XmlStringUtil.wrapInHtml("<font color='#" + ColorUtil.toHex(JBColor.RED) + "'><left>" + info.message + "</left></font>"));

      if (!myIntellijLiveValidationEnabled) {
        myIntellijLiveValidationEnabled = true;
        enableIntellijLiveValidation();
      }

      return false;
    }
  }

  private void enableIntellijLiveValidation() {
    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent e) {
        validateInIntelliJ();
      }
    });

    myCreateSampleProjectCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        validateInIntelliJ();
      }
    });

    myTemplatesList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent e) {
        validateInIntelliJ();
      }
    });
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(final @NotNull WebProjectGenerator.SettingsStateListener stateListener) {
    // invalid Dartium path is not a blocking error
    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        stateListener.stateChanged(validate() == null);
      }
    });
  }
}
