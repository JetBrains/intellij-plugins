// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.sdk;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.Configurable.NoScroll;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckboxTreeListener;
import com.intellij.ui.CheckboxTreeTable;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.PortField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.dsl.listCellRenderer.BuilderKt;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;

public final class DartConfigurable implements SearchableConfigurable, NoScroll {
  public static final int WEBDEV_PORT_DEFAULT = 53322;
  private static final String WEBDEV_PORT_PROPERTY_NAME = "dart.webdev.port";

  private static final String DART_SETTINGS_PAGE_ID = "dart.settings";

  private final JPanel myMainPanel;
  private final JBCheckBox myEnableDartSupportCheckBox;

  private final JPanel mySettingsPanel;
  private final ComboboxWithBrowseButton mySdkPathComboWithBrowse;
  private final JBLabel myVersionLabel;
  private final JBCheckBox myCheckSdkUpdateCheckBox;
  // disabled and unchecked, shown in UI instead of myCheckSdkUpdateCheckBox if selected Dart SDK is a part of a Flutter SDK
  private final JBCheckBox myCheckSdkUpdateCheckBoxFake;
  private final ComboBox<DartSdkUpdateOption> mySdkUpdateChannelCombo;
  private final JButton myCheckSdkUpdateButton;

  private final PortField myPortField;

  private final JBLabel myModulesPanelLabel;
  private final JPanel myModulesPanel;

  private final CheckboxTreeTable myModulesCheckboxTreeTable;
  private final JBLabel myErrorLabel;

  private final @NotNull Project myProject;
  private final boolean myShowModulesPanel;

  private boolean myDartSupportEnabledInitial;
  private @Nullable DartSdk mySdkInitial;
  private final @NotNull Collection<Module> myModulesWithDartSdkLibAttachedInitial = new HashSet<>();

  public DartConfigurable(final @NotNull Project project) {
    myProject = project;
    {
      mySdkPathComboWithBrowse = new ComboboxWithBrowseButton(new ComboBox<>());

      final CheckboxTree.CheckboxTreeCellRenderer checkboxTreeCellRenderer = new CheckboxTree.CheckboxTreeCellRenderer() {
        @Override
        public void customizeRenderer(JTree tree,
                                      Object value,
                                      boolean selected,
                                      boolean expanded,
                                      boolean leaf,
                                      int row,
                                      boolean hasFocus) {
          if (!(value instanceof CheckedTreeNode node)) return;

          final boolean dartSupportEnabled = myEnableDartSupportCheckBox.isSelected();

          final Object userObject = node.getUserObject();

          if (userObject instanceof Project) {
            if (!dartSupportEnabled) {
              //disabled state is also used as partially selected, that's why we do not change 'enabled' state if dartSupportEnabled
              getCheckbox().setEnabled(false);
            }
            getTextRenderer().setEnabled(dartSupportEnabled);

            getTextRenderer().append(DartBundle.message("project.0", ((Project)userObject).getName()));
          }
          else if (userObject instanceof Module) {
            getCheckbox().setEnabled(dartSupportEnabled);
            getTextRenderer().setEnabled(dartSupportEnabled);

            final Icon moduleIcon = ModuleType.get((Module)userObject).getIcon();
            getTextRenderer().setIcon(dartSupportEnabled ? moduleIcon : IconLoader.getDisabledIcon(moduleIcon));
            getTextRenderer().append(((Module)userObject).getName());
          }
        }
      };

      myModulesCheckboxTreeTable = new CheckboxTreeTable(null, checkboxTreeCellRenderer, new ColumnInfo[]{new TreeColumnInfo("")});
      myModulesCheckboxTreeTable.addCheckboxTreeListener(new CheckboxTreeListener() {
        @Override
        public void nodeStateChanged(@NotNull CheckedTreeNode node) {
          updateErrorLabel();
        }
      });
      //myModulesCheckboxTreeTable.setRowHeight(myModulesCheckboxTreeTable.getRowHeight() + 2);

      myModulesCheckboxTreeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
        @Override
        public void treeWillExpand(final TreeExpansionEvent event) { }

        @Override
        public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
          throw new ExpandVetoException(event);
        }
      });
    }
    {
      // GUI initializer generated by IntelliJ IDEA GUI Designer
      // >>> IMPORTANT!! <<<
      // DO NOT EDIT OR ADD ANY CODE HERE!
      myMainPanel = new JPanel();
      myMainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
      myEnableDartSupportCheckBox = new JBCheckBox();
      myEnableDartSupportCheckBox.setMargin(new Insets(2, 0, 2, 3));
      this.$$$loadButtonText$$$(myEnableDartSupportCheckBox, this.$$$getMessageFromBundle$$$("messages/DartBundle",
                                                                                             "settings.page.checkbox.enable.dart.support.for.project.0"));
      myMainPanel.add(myEnableDartSupportCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                       GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                                       null, null, null, 0, false));
      mySettingsPanel = new JPanel();
      mySettingsPanel.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
      myMainPanel.add(mySettingsPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                           GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                           GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                           null, null, null, 0, false));
      mySettingsPanel.add(mySdkPathComboWithBrowse,
                          new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                              GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myVersionLabel = new JBLabel();
      myVersionLabel.setText("1.0");
      mySettingsPanel.add(myVersionLabel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                              GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                              null, null, 0, false));
      final JPanel panel1 = new JPanel();
      panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 2, 0, 0), -1, -1));
      mySettingsPanel.add(panel1, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                      null, null, 0, false));
      myModulesPanel = new JPanel();
      myModulesPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
      panel1.add(myModulesPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                     null, null, 0, false));
      myModulesPanelLabel = new JBLabel();
      this.$$$loadLabelText$$$(myModulesPanelLabel, this.$$$getMessageFromBundle$$$("messages/DartBundle",
                                                                                    "settings.page.label.enable.dart.support.for.following.modules"));
      myModulesPanel.add(myModulesPanelLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                  null, null, 0, false));
      final JBScrollPane jBScrollPane1 = new JBScrollPane();
      myModulesPanel.add(jBScrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                            null, null, null, 0, false));
      myModulesCheckboxTreeTable.setPreferredScrollableViewportSize(new Dimension(-1, -1));
      jBScrollPane1.setViewportView(myModulesCheckboxTreeTable);
      final Spacer spacer1 = new Spacer();
      panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
      final Spacer spacer2 = new Spacer();
      mySettingsPanel.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                       GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 15), new Dimension(-1, 15),
                                                       new Dimension(-1, 15), 0, false));
      final JPanel panel2 = new JPanel();
      panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 2, 0, 0), -1, -1));
      mySettingsPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final JBLabel jBLabel1 = new JBLabel();
      this.$$$loadLabelText$$$(jBLabel1, this.$$$getMessageFromBundle$$$("messages/DartBundle", "dart.sdk.path.label"));
      panel2.add(jBLabel1,
                 new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final JPanel panel3 = new JPanel();
      panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 2, 0, 0), -1, -1));
      mySettingsPanel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final JBLabel jBLabel2 = new JBLabel();
      this.$$$loadLabelText$$$(jBLabel2, this.$$$getMessageFromBundle$$$("messages/DartBundle", "version.label"));
      panel3.add(jBLabel2,
                 new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      mySdkUpdateChannelCombo = new ComboBox();
      final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
      mySdkUpdateChannelCombo.setModel(defaultComboBoxModel1);
      mySettingsPanel.add(mySdkUpdateChannelCombo,
                          new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                              false));
      myCheckSdkUpdateButton = new JButton();
      this.$$$loadButtonText$$$(myCheckSdkUpdateButton,
                                this.$$$getMessageFromBundle$$$("messages/DartBundle", "settings.page.button.check.updates.now"));
      mySettingsPanel.add(myCheckSdkUpdateButton,
                          new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                              GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final JPanel panel4 = new JPanel();
      panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
      mySettingsPanel.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null,
                                                      null, null, 0, false));
      myCheckSdkUpdateCheckBox = new JBCheckBox();
      myCheckSdkUpdateCheckBox.setMargin(new Insets(2, 0, 2, 3));
      this.$$$loadButtonText$$$(myCheckSdkUpdateCheckBox,
                                this.$$$getMessageFromBundle$$$("messages/DartBundle", "settings.page.checkbox.check.sdk.update"));
      panel4.add(myCheckSdkUpdateCheckBox,
                 new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myCheckSdkUpdateCheckBoxFake = new JBCheckBox();
      myCheckSdkUpdateCheckBoxFake.setEnabled(false);
      myCheckSdkUpdateCheckBoxFake.setMargin(new Insets(2, 0, 2, 3));
      this.$$$loadButtonText$$$(myCheckSdkUpdateCheckBoxFake,
                                this.$$$getMessageFromBundle$$$("messages/DartBundle", "settings.page.checkbox.check.sdk.update"));
      panel4.add(myCheckSdkUpdateCheckBoxFake,
                 new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final JBLabel jBLabel3 = new JBLabel();
      this.$$$loadLabelText$$$(jBLabel3, this.$$$getMessageFromBundle$$$("messages/DartBundle", "settings.page.label.webdev.server.port"));
      mySettingsPanel.add(jBLabel3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      myPortField = new PortField();
      mySettingsPanel.add(myPortField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                           GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                           null, 0, false));
      final Spacer spacer3 = new Spacer();
      myMainPanel.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                   GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 15), new Dimension(-1, 15),
                                                   new Dimension(-1, 15), 0, false));
      myErrorLabel = new JBLabel();
      this.$$$loadLabelText$$$(myErrorLabel, this.$$$getMessageFromBundle$$$("messages/DartBundle", "error.path.to.sdk.not.specified"));
      myMainPanel.add(myErrorLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      myModulesPanelLabel.setLabelFor(myModulesCheckboxTreeTable);
      jBLabel1.setLabelFor(mySdkPathComboWithBrowse);
      jBLabel3.setLabelFor(myPortField);
    }
    myShowModulesPanel = DartSdkLibUtil.isIdeWithMultipleModuleSupport() || ModuleManager.getInstance(project).getModules().length > 1;
    initEnableDartSupportCheckBox();
    initDartSdkControls();
    initModulesPanel();
    myErrorLabel.setIcon(AllIcons.Actions.Lightning);
  }

  private static Method $$$cachedGetBundleMethod$$$ = null;

  /** @noinspection ALL */
  private String $$$getMessageFromBundle$$$(String path, String key) {
    ResourceBundle bundle;
    try {
      Class<?> thisClass = this.getClass();
      if ($$$cachedGetBundleMethod$$$ == null) {
        Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
        $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
      }
      bundle = (ResourceBundle)$$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
    }
    catch (Exception e) {
      bundle = ResourceBundle.getBundle(path);
    }
    return bundle.getString(key);
  }

  /** @noinspection ALL */
  private void $$$loadLabelText$$$(JLabel component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) break;
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /** @noinspection ALL */
  private void $$$loadButtonText$$$(AbstractButton component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) break;
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /** @noinspection ALL */
  public JComponent $$$getRootComponent$$$() { return myMainPanel; }

  private void initEnableDartSupportCheckBox() {
    myEnableDartSupportCheckBox.setText(DartSdkLibUtil.isIdeWithMultipleModuleSupport()
                                        ? DartBundle
                                          .message("settings.page.checkbox.enable.dart.support.for.project.0", myProject.getName())
                                        : DartBundle.message("settings.page.checkbox.enable.dart.support.check.box"));
    myEnableDartSupportCheckBox.addActionListener(e -> {
      updateControlsEnabledState();
      updateErrorLabel();
    });
  }

  private void initDartSdkControls() {
    DartSdkUtil.initDartSdkControls(myProject, mySdkPathComboWithBrowse, myVersionLabel);

    final JTextComponent sdkEditor = (JTextComponent)mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    sdkEditor.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final @NotNull DocumentEvent e) {
        final String sdkHomePath = getTextFromCombo(mySdkPathComboWithBrowse);
        if (!sdkHomePath.isEmpty()) {
          final String version = DartSdkUtil.getSdkVersion(sdkHomePath);
          if (version != null && (version.contains("-dev.") || version.contains("-edge."))) {
            mySdkUpdateChannelCombo.setSelectedItem(DartSdkUpdateOption.StableAndDev);
          }
        }

        updateControlsEnabledState();
        updateErrorLabel();
      }
    });

    myCheckSdkUpdateCheckBox.addActionListener(e -> {
      final boolean enabled = myCheckSdkUpdateCheckBox.isSelected() && myCheckSdkUpdateCheckBox.isEnabled();
      mySdkUpdateChannelCombo.setEnabled(enabled);
      myCheckSdkUpdateButton.setEnabled(enabled);

      if (enabled) {
        IdeFocusManager.getInstance(myProject).requestFocus(mySdkUpdateChannelCombo, true);
      }
    });

    mySdkUpdateChannelCombo.setModel(new DefaultComboBoxModel<>(DartSdkUpdateOption.OPTIONS_TO_SHOW_IN_COMBO));
    mySdkUpdateChannelCombo.setRenderer(BuilderKt.textListCellRenderer("", DartSdkUpdateOption::getPresentableName));

    myCheckSdkUpdateButton.addActionListener(e -> {
      final Runnable runnable = this::checkSdkUpdate;
      ApplicationManagerEx.getApplicationEx()
        .runProcessWithProgressSynchronously(runnable, DartBundle.message("checking.dart.sdk.update"), true, true, myProject,
                                             myMainPanel, null);
    });
  }

  private void checkSdkUpdate() {
    final String currentSdkVersion = myVersionLabel.getText();

    final DartSdkUpdateChecker.SdkUpdateInfo sdkUpdateInfo =
      DartSdkUpdateChecker.getSdkUpdateInfo((DartSdkUpdateOption)mySdkUpdateChannelCombo.getSelectedItem());

    ApplicationManager.getApplication().invokeLater(() -> {
      if (sdkUpdateInfo == null) {
        Messages.showErrorDialog(myProject,
                                 DartBundle.message("dart.sdk.update.check.failed"),
                                 DartBundle.message("dart.sdk.update.title"));
      }
      else {
        final String message;
        if (currentSdkVersion == null || currentSdkVersion.isEmpty()) {
          message = DartBundle.message("dart.sdk.0.available.for.download", sdkUpdateInfo.myVersion, sdkUpdateInfo.myDownloadUrl);
        }
        else if (DartSdkUpdateChecker.compareDartSdkVersions(currentSdkVersion, sdkUpdateInfo.myVersion) >= 0) {
          message = DartBundle.message("dart.sdk.0.is.up.to.date", currentSdkVersion);
        }
        else {
          message =
            DartBundle.message("new.dart.sdk.0.available.for.download..dialog", sdkUpdateInfo.myVersion, sdkUpdateInfo.myDownloadUrl);
        }

        Messages.showInfoMessage(myProject, message, DartBundle.message("dart.sdk.update.title"));
      }
    }, ModalityState.defaultModalityState(), myProject.getDisposed());
  }

  private void initModulesPanel() {
    if (!myShowModulesPanel) {
      myModulesPanel.setVisible(false);
      return;
    }

    myModulesPanelLabel.setText(DartSdkLibUtil.isIdeWithMultipleModuleSupport()
                                ? DartBundle.message("settings.page.label.enable.dart.support.for.following.modules")
                                : DartBundle.message("settings.page.label.enable.dart.support.for.following.projects"));

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    Arrays.sort(modules, Comparator.comparing(module -> StringUtil.toLowerCase(module.getName())));

    final CheckedTreeNode rootNode = new CheckedTreeNode(myProject);
    ((DefaultTreeModel)myModulesCheckboxTreeTable.getTree().getModel()).setRoot(rootNode);
    myModulesCheckboxTreeTable.getTree().setRootVisible(DartSdkLibUtil.isIdeWithMultipleModuleSupport());
    myModulesCheckboxTreeTable.getTree().setShowsRootHandles(false);
    myModulesCheckboxTreeTable.getTree().setBorder(JBUI.Borders.emptyLeft(5));

    for (final Module module : modules) {
      rootNode.add(new CheckedTreeNode(module));
    }

    ((DefaultTreeModel)myModulesCheckboxTreeTable.getTree().getModel()).reload(rootNode);
  }

  @Override
  public @NotNull String getId() {
    return "dart.settings";
  }

  @Override
  public @Nls String getDisplayName() {
    return DartBundle.message("dart.title");
  }

  @Override
  public @Nullable String getHelpTopic() {
    return "settings.dart.settings";
  }

  @Override
  public @Nullable JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    final String sdkHomePath = getTextFromCombo(mySdkPathComboWithBrowse);
    final boolean sdkSelected = DartSdkUtil.isDartSdkHome(sdkHomePath);

    // was disabled, now disabled (or no sdk selected) => not modified, do not care about other controls
    if (!myDartSupportEnabledInitial && (!myEnableDartSupportCheckBox.isSelected() || !sdkSelected)) return false;
    // was enabled, now disabled => modified
    if (myDartSupportEnabledInitial && !myEnableDartSupportCheckBox.isSelected()) return true;
    // was disabled, now enabled or was enabled, now enabled => need to check further

    final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();
    if (sdkSelected && !sdkHomePath.equals(initialSdkHomePath)) return true;

    final boolean flutter = FlutterUtil.getFlutterRoot(sdkHomePath) != null;
    if (!flutter) {
      final DartSdkUpdateOption sdkUpdateOption = myCheckSdkUpdateCheckBox.isSelected()
                                                  ? (DartSdkUpdateOption)mySdkUpdateChannelCombo.getSelectedItem()
                                                  : DartSdkUpdateOption.DoNotCheck;
      if (sdkUpdateOption != DartSdkUpdateOption.getDartSdkUpdateOption()) return true;
    }

    if (myPortField.getNumber() != getWebdevPort(myProject)) return true;

    if (myShowModulesPanel) {
      final Module[] selectedModules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (selectedModules.length != myModulesWithDartSdkLibAttachedInitial.size()) return true;

      for (final Module module : selectedModules) {
        if (!myModulesWithDartSdkLibAttachedInitial.contains(module)) return true;
      }
    }
    else {
      if (myDartSupportEnabledInitial != myEnableDartSupportCheckBox.isSelected()) return true;
    }

    return false;
  }

  private static @NotNull String getTextFromCombo(final @NotNull ComboboxWithBrowseButton combo) {
    return FileUtilRt.toSystemIndependentName(combo.getComboBox().getEditor().getItem().toString().trim());
  }

  @Override
  public void reset() {
    // remember initial state
    mySdkInitial = DartSdk.getDartSdk(myProject);
    myModulesWithDartSdkLibAttachedInitial.clear();

    if (mySdkInitial != null) {
      myModulesWithDartSdkLibAttachedInitial.addAll(DartSdkLibUtil.getModulesWithDartSdkEnabled(myProject));
    }

    myDartSupportEnabledInitial = !myModulesWithDartSdkLibAttachedInitial.isEmpty();

    // reset UI
    myEnableDartSupportCheckBox.setSelected(myDartSupportEnabledInitial);
    @NlsSafe String sdkInitialPath = mySdkInitial == null ? "" : FileUtilRt.toSystemDependentName(mySdkInitial.getHomePath());
    mySdkPathComboWithBrowse.getComboBox().getEditor().setItem(sdkInitialPath);
    if (!sdkInitialPath.isEmpty()) {
      ensureComboModelContainsCurrentItem(mySdkPathComboWithBrowse.getComboBox());
    }

    final DartSdkUpdateOption sdkUpdateOption = DartSdkUpdateOption.getDartSdkUpdateOption();
    myCheckSdkUpdateCheckBox.setSelected(sdkUpdateOption != DartSdkUpdateOption.DoNotCheck);
    mySdkUpdateChannelCombo.setSelectedItem(sdkUpdateOption);

    myPortField.setNumber(getWebdevPort(myProject));

    if (myShowModulesPanel) {
      final CheckedTreeNode rootNode = (CheckedTreeNode)myModulesCheckboxTreeTable.getTree().getModel().getRoot();
      rootNode.setChecked(false);
      final Enumeration children = rootNode.children();
      while (children.hasMoreElements()) {
        final CheckedTreeNode node = (CheckedTreeNode)children.nextElement();
        node.setChecked(myModulesWithDartSdkLibAttachedInitial.contains((Module)node.getUserObject()));
      }
    }

    updateControlsEnabledState();
    updateErrorLabel();
  }

  private static void ensureComboModelContainsCurrentItem(final @NotNull JComboBox comboBox) {
    final Object currentItem = comboBox.getEditor().getItem();

    boolean contains = false;
    for (int i = 0; i < comboBox.getModel().getSize(); i++) {
      if (currentItem.equals(comboBox.getModel().getElementAt(i))) {
        contains = true;
        break;
      }
    }

    if (!contains) {
      ((DefaultComboBoxModel)comboBox.getModel()).insertElementAt(currentItem, 0);
      comboBox.setSelectedItem(currentItem); // to set focus on current item in combo popup
      comboBox.getEditor().setItem(currentItem); // to set current item in combo itself
    }
  }

  @Override
  public void apply() {
    // similar to DartModuleBuilder.setupSdk()
    final Runnable runnable = () -> {
      if (myEnableDartSupportCheckBox.isSelected()) {
        final String sdkHomePath = getTextFromCombo(mySdkPathComboWithBrowse);

        if (DartSdkUtil.isDartSdkHome(sdkHomePath)) {
          DartSdkUtil.updateKnownSdkPaths(myProject, sdkHomePath);

          DartSdkLibUtil.ensureDartSdkConfigured(myProject, sdkHomePath);
          DaemonCodeAnalyzer.getInstance(myProject).restart(this);

          final Module[] modules = myShowModulesPanel
                                   ? myModulesCheckboxTreeTable.getCheckedNodes(Module.class)
                                   : ModuleManager.getInstance(myProject).getModules();
          DartSdkLibUtil.enableDartSdkForSpecifiedModulesAndDisableForOthers(myProject, modules);
        }

        final boolean flutter = FlutterUtil.getFlutterRoot(sdkHomePath) != null;
        if (!flutter) {
          final DartSdkUpdateOption sdkUpdateOption = myCheckSdkUpdateCheckBox.isSelected()
                                                      ? (DartSdkUpdateOption)mySdkUpdateChannelCombo.getSelectedItem()
                                                      : DartSdkUpdateOption.DoNotCheck;
          DartSdkUpdateOption.setDartSdkUpdateOption(sdkUpdateOption);
        }

        setWebdevPort(myProject, myPortField.getNumber());
      }
      else {
        if (!myModulesWithDartSdkLibAttachedInitial.isEmpty() && mySdkInitial != null) {
          DartSdkLibUtil.disableDartSdk(myModulesWithDartSdkLibAttachedInitial);
        }
      }
    };

    ApplicationManager.getApplication().runWriteAction(runnable);

    reset(); // because we rely on remembering initial state
  }

  @Override
  public void disposeUIResources() {
    mySdkInitial = null;
    myModulesWithDartSdkLibAttachedInitial.clear();
  }

  private void updateControlsEnabledState() {
    UIUtil.setEnabled(mySettingsPanel, myEnableDartSupportCheckBox.isSelected(), true);

    final boolean flutter = FlutterUtil.getFlutterRoot(getTextFromCombo(mySdkPathComboWithBrowse)) != null;
    myCheckSdkUpdateCheckBox.setVisible(!flutter);
    final boolean enabled = myCheckSdkUpdateCheckBox.isVisible() &&
                            myCheckSdkUpdateCheckBox.isEnabled() &&
                            myCheckSdkUpdateCheckBox.isSelected();
    mySdkUpdateChannelCombo.setEnabled(enabled);
    myCheckSdkUpdateButton.setEnabled(enabled);

    myCheckSdkUpdateCheckBoxFake.setVisible(flutter);
    myCheckSdkUpdateCheckBoxFake.setEnabled(false);
  }

  private void updateErrorLabel() {
    final String message = getErrorMessage();
    if (message == null) {
      myErrorLabel.setText("");
      myErrorLabel.setVisible(false);
      return;
    }

    HtmlChunk.Element html = new HtmlBuilder().append(message)
      .wrapWith("font").attr("color", "#" + ColorUtil.toHex(JBColor.RED))
      .wrapWith("html");
    myErrorLabel.setText(html.toString());
    myErrorLabel.setVisible(true);
  }

  private @Nullable @NlsContexts.Label String getErrorMessage() {
    if (!myEnableDartSupportCheckBox.isSelected()) {
      return null;
    }

    String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(getTextFromCombo(mySdkPathComboWithBrowse));
    if (message != null) return message;

    if (myShowModulesPanel) {
      final Module[] modules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (modules.length == 0) {
        return DartSdkLibUtil.isIdeWithMultipleModuleSupport()
               ? DartBundle.message("warning.no.modules.selected.dart.support.will.be.disabled")
               : DartBundle.message("warning.no.projects.selected.dart.support.will.be.disabled");
      }
    }

    return null;
  }

  public static void openDartSettings(final @NotNull Project project) {
    ShowSettingsUtilImpl.showSettingsDialog(project, DART_SETTINGS_PAGE_ID, "");
  }

  public static int getWebdevPort(@NotNull Project project) {
    return PropertiesComponent.getInstance(project).getInt(WEBDEV_PORT_PROPERTY_NAME, WEBDEV_PORT_DEFAULT);
  }

  private static void setWebdevPort(@NotNull Project project, int port) {
    PropertiesComponent.getInstance(project).setValue(WEBDEV_PORT_PROPERTY_NAME, port, WEBDEV_PORT_DEFAULT);
  }
}
