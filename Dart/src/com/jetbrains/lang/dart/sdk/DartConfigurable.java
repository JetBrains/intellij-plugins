package com.jetbrains.lang.dart.sdk;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.Configurable.NoScroll;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class DartConfigurable implements SearchableConfigurable, NoScroll {

  private static final String DART_SETTINGS_PAGE_ID = "dart.settings";
  private static final String DART_SETTINGS_PAGE_NAME = DartBundle.message("dart.title");

  private JPanel myMainPanel;
  private JBCheckBox myEnableDartSupportCheckBox;

  private JPanel mySettingsPanel;
  private ComboboxWithBrowseButton mySdkPathComboWithBrowse;
  private JBLabel myVersionLabel;
  private JBCheckBox myCheckSdkUpdateCheckBox;
  // disabled and unchecked, shown in UI instead of myCheckSdkUpdateCheckBox if selected Dart SDK is a part of a Flutter SDK
  private JBCheckBox myCheckSdkUpdateCheckBoxFake;
  private ComboBox mySdkUpdateChannelCombo;
  private JButton myCheckSdkUpdateButton;

  private ComboboxWithBrowseButton myDartiumPathComboWithBrowse;
  private JButton myDartiumSettingsButton;
  private JBCheckBox myCheckedModeCheckBox;

  private JPanel myModulesPanel;

  private CheckboxTreeTable myModulesCheckboxTreeTable;
  private JBLabel myErrorLabel;

  private final @NotNull Project myProject;

  private boolean myInReset = false;

  private boolean myDartSupportEnabledInitial;
  private @Nullable DartSdk mySdkInitial;
  private final @NotNull Collection<Module> myModulesWithDartSdkLibAttachedInitial = new THashSet<>();

  private @Nullable WebBrowser myDartiumInitial;
  private ChromeSettings myDartiumSettingsCurrent;

  public DartConfigurable(final @NotNull Project project) {
    myProject = project;
    initEnableDartSupportCheckBox();
    initDartSdkAndDartiumControls();
    initModulesPanel();
    myErrorLabel.setIcon(AllIcons.Actions.Lightning);
  }

  private void initEnableDartSupportCheckBox() {
    myEnableDartSupportCheckBox.setText(DartBundle.message("enable.dart.support.for.project.0", myProject.getName()));
    myEnableDartSupportCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControlsEnabledState();
        updateErrorLabel();
      }
    });
  }

  private void initDartSdkAndDartiumControls() {
    final Computable<ChromeSettings> currentDartiumSettingsRetriever = () -> myDartiumSettingsCurrent;

    final Computable<Boolean> isResettingControlsComputable = () -> myInReset;

    DartSdkUtil.initDartSdkAndDartiumControls(myProject, mySdkPathComboWithBrowse, myVersionLabel, myDartiumPathComboWithBrowse,
                                              currentDartiumSettingsRetriever, myDartiumSettingsButton,
                                              isResettingControlsComputable);

    final JTextComponent sdkEditor = (JTextComponent)mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    sdkEditor.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
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

    final JTextComponent dartiumEditor = (JTextComponent)myDartiumPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    dartiumEditor.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        updateErrorLabel();
      }
    });

    myCheckSdkUpdateCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final boolean enabled = myCheckSdkUpdateCheckBox.isSelected() && myCheckSdkUpdateCheckBox.isEnabled();
        mySdkUpdateChannelCombo.setEnabled(enabled);
        myCheckSdkUpdateButton.setEnabled(enabled);

        if (enabled) {
          IdeFocusManager.getInstance(myProject).requestFocus(mySdkUpdateChannelCombo, true);
        }
      }
    });

    mySdkUpdateChannelCombo.setModel(new DefaultComboBoxModel(DartSdkUpdateOption.OPTIONS_TO_SHOW_IN_COMBO));
    mySdkUpdateChannelCombo.setRenderer(new ListCellRendererWrapper<DartSdkUpdateOption>() {
      @Override
      public void customize(JList list, DartSdkUpdateOption value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableName());
      }
    });

    myCheckSdkUpdateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final Runnable runnable = () -> checkSdkUpdate();
        ApplicationManagerEx.getApplicationEx()
          .runProcessWithProgressSynchronously(runnable, DartBundle.message("checking.dart.sdk.update"), true, myProject, myMainPanel);
      }
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
    if (!DartSdkLibUtil.isIdeWithMultipleModuleSupport()) {
      myModulesPanel.setVisible(false);
      return;
    }

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    Arrays.sort(modules, Comparator.comparing(module -> module.getName().toLowerCase(Locale.US)));

    final CheckedTreeNode rootNode = new CheckedTreeNode(myProject);
    ((DefaultTreeModel)myModulesCheckboxTreeTable.getTree().getModel()).setRoot(rootNode);
    myModulesCheckboxTreeTable.getTree().setRootVisible(true);
    myModulesCheckboxTreeTable.getTree().setShowsRootHandles(false);

    for (final Module module : modules) {
      rootNode.add(new CheckedTreeNode(module));
    }

    ((DefaultTreeModel)myModulesCheckboxTreeTable.getTree().getModel()).reload(rootNode);
  }

  @Override
  @NotNull
  public String getId() {
    return "dart.settings";
  }

  @Override
  @Nls
  public String getDisplayName() {
    return DART_SETTINGS_PAGE_NAME;
  }

  @Override
  @Nullable
  public String getHelpTopic() {
    return "settings.dart.settings";
  }

  @Override
  @Nullable
  public JComponent createComponent() {
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

    final String dartiumPath = getTextFromCombo(myDartiumPathComboWithBrowse);
    final String dartiumPathInitial = myDartiumInitial == null ? null : myDartiumInitial.getPath();
    if (!dartiumPath.isEmpty() && new File(dartiumPath).exists() && !dartiumPath.equals(dartiumPathInitial)) return true;

    if (myDartiumInitial != null && !myDartiumSettingsCurrent.equals(myDartiumInitial.getSpecificSettings())) return true;

    if (DartSdkLibUtil.isIdeWithMultipleModuleSupport()) {
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

  @NotNull
  private static String getTextFromCombo(@NotNull final ComboboxWithBrowseButton combo) {
    return FileUtilRt.toSystemIndependentName(combo.getComboBox().getEditor().getItem().toString().trim());
  }

  @Override
  public void reset() {
    myInReset = true;

    // remember initial state
    mySdkInitial = DartSdk.getDartSdk(myProject);
    myModulesWithDartSdkLibAttachedInitial.clear();

    if (mySdkInitial != null) {
      myModulesWithDartSdkLibAttachedInitial.addAll(DartSdkLibUtil.getModulesWithDartSdkEnabled(myProject));
    }

    myDartSupportEnabledInitial = !myModulesWithDartSdkLibAttachedInitial.isEmpty();

    myDartiumInitial = DartiumUtil.getDartiumBrowser();
    myDartiumSettingsCurrent = new ChromeSettings();
    if (myDartiumInitial != null) {
      final BrowserSpecificSettings browserSpecificSettings = myDartiumInitial.getSpecificSettings();
      if (browserSpecificSettings instanceof ChromeSettings) {
        myDartiumSettingsCurrent = (ChromeSettings)browserSpecificSettings.clone();
      }
    }

    // reset UI
    myEnableDartSupportCheckBox.setSelected(myDartSupportEnabledInitial);
    final String sdkInitialPath = mySdkInitial == null ? "" : FileUtilRt.toSystemDependentName(mySdkInitial.getHomePath());
    mySdkPathComboWithBrowse.getComboBox().getEditor().setItem(sdkInitialPath);
    if (!sdkInitialPath.isEmpty()) {
      ensureComboModelContainsCurrentItem(mySdkPathComboWithBrowse.getComboBox());
    }

    final DartSdkUpdateOption sdkUpdateOption = DartSdkUpdateOption.getDartSdkUpdateOption();
    myCheckSdkUpdateCheckBox.setSelected(sdkUpdateOption != DartSdkUpdateOption.DoNotCheck);
    mySdkUpdateChannelCombo.setSelectedItem(sdkUpdateOption);

    final String dartiumInitialPath =
      myDartiumInitial == null ? "" : FileUtilRt.toSystemDependentName(StringUtil.notNullize(myDartiumInitial.getPath()));
    myDartiumPathComboWithBrowse.getComboBox().getEditor().setItem(dartiumInitialPath);
    if (!dartiumInitialPath.isEmpty()) {
      ensureComboModelContainsCurrentItem(myDartiumPathComboWithBrowse.getComboBox());
    }

    // we decided to save one line in settings and always use Dartium in checked mode
    myCheckedModeCheckBox.setVisible(false);
    DartiumUtil.setCheckedMode(myDartiumSettingsCurrent.getEnvironmentVariables(), true);
    //final boolean checkedMode = myDartiumInitial == null || DartiumUtil.isCheckedMode(myDartiumSettingsCurrent.getEnvironmentVariables());
    //myCheckedModeCheckBox.setSelected(checkedMode);

    if (DartSdkLibUtil.isIdeWithMultipleModuleSupport()) {
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

    myInReset = false;
  }

  private static void ensureComboModelContainsCurrentItem(@NotNull final JComboBox comboBox) {
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
  public void apply() throws ConfigurationException {
    // similar to DartModuleBuilder.setupSdkAndDartium()
    final Runnable runnable = () -> {
      if (myEnableDartSupportCheckBox.isSelected()) {
        final String sdkHomePath = getTextFromCombo(mySdkPathComboWithBrowse);

        if (DartSdkUtil.isDartSdkHome(sdkHomePath)) {
          DartSdkUtil.updateKnownSdkPaths(myProject, sdkHomePath);

          DartSdkLibUtil.ensureDartSdkConfigured(sdkHomePath);
          DaemonCodeAnalyzer.getInstance(myProject).restart();

          final Module[] modules = DartSdkLibUtil.isIdeWithMultipleModuleSupport()
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

        final String dartiumPath = getTextFromCombo(myDartiumPathComboWithBrowse);
        DartiumUtil.applyDartiumSettings(dartiumPath, myDartiumSettingsCurrent);
      }
      else {
        if (myModulesWithDartSdkLibAttachedInitial.size() > 0 && mySdkInitial != null) {
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
    myDartiumInitial = null;
    myDartiumSettingsCurrent = null;
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
    myErrorLabel
      .setText(XmlStringUtil.wrapInHtml("<font color='#" + ColorUtil.toHex(JBColor.RED) + "'><left>" + message + "</left></font>"));
    myErrorLabel.setVisible(message != null);
  }

  @Nullable
  private String getErrorMessage() {
    if (!myEnableDartSupportCheckBox.isSelected()) {
      return null;
    }

    String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(getTextFromCombo(mySdkPathComboWithBrowse));
    if (message != null) return message;

    message = DartiumUtil.getErrorMessageIfWrongDartiumPath(getTextFromCombo(myDartiumPathComboWithBrowse));
    if (message != null) return message;

    if (DartSdkLibUtil.isIdeWithMultipleModuleSupport()) {
      final Module[] modules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (modules.length == 0) {
        return DartBundle.message("warning.no.modules.selected.dart.support.will.be.disabled");
      }
    }

    return null;
  }

  private void createUIComponents() {
    mySdkPathComboWithBrowse = new ComboboxWithBrowseButton(new ComboBox<>());
    myDartiumPathComboWithBrowse = new ComboboxWithBrowseButton(new ComboBox<>());

    final CheckboxTree.CheckboxTreeCellRenderer checkboxTreeCellRenderer = new CheckboxTree.CheckboxTreeCellRenderer() {
      @Override
      public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof CheckedTreeNode)) return;

        final boolean dartSupportEnabled = myEnableDartSupportCheckBox.isSelected();

        final CheckedTreeNode node = (CheckedTreeNode)value;
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
    myModulesCheckboxTreeTable.addCheckboxTreeListener(new CheckboxTreeAdapter() {
      @Override
      public void nodeStateChanged(@NotNull CheckedTreeNode node) {
        updateErrorLabel();
      }
    });
    //myModulesCheckboxTreeTable.setRowHeight(myModulesCheckboxTreeTable.getRowHeight() + 2);

    myModulesCheckboxTreeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {

      }

      public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
        throw new ExpandVetoException(event);
      }
    });
  }

  public static void openDartSettings(@NotNull final Project project) {
    ShowSettingsUtilImpl.showSettingsDialog(project, DART_SETTINGS_PAGE_ID, "");
  }
}
