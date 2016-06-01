package com.jetbrains.lang.dart.sdk;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.SmartHashMap;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.LocalPathCellEditor;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class DartConfigurable implements SearchableConfigurable {

  private static final String DART_SETTINGS_PAGE_ID = "dart.settings";
  private static final String DART_SETTINGS_PAGE_NAME = DartBundle.message("dart.title");

  private static final String CUSTOM_PACKAGE_ROOT_LIB_NAME = "Dart custom package root";

  private JPanel myMainPanel;
  private JBCheckBox myEnableDartSupportCheckBox;

  private JPanel mySettingsPanel;
  private ComboboxWithBrowseButton mySdkPathComboWithBrowse;
  private JBLabel myVersionLabel;
  private JBCheckBox myCheckSdkUpdateCheckBox;
  private ComboBox mySdkUpdateChannelCombo;
  private JButton myCheckSdkUpdateButton;

  private ComboboxWithBrowseButton myDartiumPathComboWithBrowse;
  private JButton myDartiumSettingsButton;
  private JBCheckBox myCheckedModeCheckBox;

  private JBCheckBox myCustomPackageRootCheckBox;
  private TextFieldWithBrowseButton myCustomPackageRootTextWithBrowse;

  private JPanel myModulesPanel;

  private CheckboxTreeTable myModulesCheckboxTreeTable;
  private JBLabel myErrorLabel;

  private final @NotNull Project myProject;

  private boolean myInReset = false;

  private boolean myDartSupportEnabledInitial;
  private @Nullable DartSdk mySdkInitial;
  private final @NotNull Collection<Module> myModulesWithDartSdkLibAttachedInitial = new THashSet<Module>();

  private @Nullable WebBrowser myDartiumInitial;
  private ChromeSettings myDartiumSettingsCurrent;
  private final @NotNull Map<Module, String> myModuleToCustomPackageRootCurrent = new THashMap<Module, String>();

  public DartConfigurable(final @NotNull Project project) {
    myProject = project;
    initEnableDartSupportCheckBox();
    initDartSdkAndDartiumControls();
    initCustomPackageRootPanel();
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
                                              currentDartiumSettingsRetriever, myDartiumSettingsButton, myCheckedModeCheckBox,
                                              isResettingControlsComputable);

    final JTextComponent sdkEditor = (JTextComponent)mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
    sdkEditor.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        final String sdkHomePath = mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
        if (!sdkHomePath.isEmpty()) {
          final String version = DartSdkUtil.getSdkVersion(sdkHomePath);
          if (version != null && (version.contains("-dev.") || version.contains("-edge."))) {
            mySdkUpdateChannelCombo.setSelectedItem(DartSdkUpdateOption.StableAndDev);
          }
        }

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
        if (currentSdkVersion.isEmpty()) {
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

  private void initCustomPackageRootPanel() {
    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      myCustomPackageRootCheckBox.setVisible(false);
      myCustomPackageRootTextWithBrowse.setVisible(false);
      return;
    }

    myCustomPackageRootCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final boolean enabled = myCustomPackageRootCheckBox.isSelected() && myCustomPackageRootCheckBox.isEnabled();
        myCustomPackageRootTextWithBrowse.setEnabled(enabled);
        updateErrorLabel();

        if (enabled) {
          IdeFocusManager.getInstance(myProject).requestFocus(myCustomPackageRootTextWithBrowse.getTextField(), true);
        }
      }
    });

    final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> bfListener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(DartBundle.message("select.custom.package.root"), null,
                                                                           myCustomPackageRootTextWithBrowse, myProject,
                                                                           FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                                                           TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

    myCustomPackageRootTextWithBrowse.addBrowseFolderListener(myProject, bfListener);

    myCustomPackageRootTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        final Module module = myModuleToCustomPackageRootCurrent.keySet().iterator().next();
        final String customPackageRoot = FileUtil.toSystemIndependentName(myCustomPackageRootTextWithBrowse.getText().trim());
        myModuleToCustomPackageRootCurrent.put(module, StringUtil.nullize(customPackageRoot));
        updateErrorLabel();
      }
    });
  }

  private void initModulesPanel() {
    if (!DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      myModulesPanel.setVisible(false);
      return;
    }

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    Arrays.sort(modules, (module1, module2) -> module1.getName().toLowerCase(Locale.US).compareTo(module2.getName().toLowerCase(Locale.US)));

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
  @Nullable
  public Runnable enableSearch(final String option) {
    return null;
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
    final String sdkHomePath =
      FileUtilRt.toSystemIndependentName(mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
    final boolean sdkSelected = DartSdkUtil.isDartSdkHome(sdkHomePath);

    // was disabled, now disabled (or no sdk selected) => not modified, do not care about other controls
    if (!myDartSupportEnabledInitial && (!myEnableDartSupportCheckBox.isSelected() || !sdkSelected)) return false;
    // was enabled, now disabled => modified
    if (myDartSupportEnabledInitial && !myEnableDartSupportCheckBox.isSelected()) return true;
    // was disabled, now enabled or was enabled, now enabled => need to check further

    final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();
    if (sdkSelected && !sdkHomePath.equals(initialSdkHomePath)) return true;

    final DartSdkUpdateOption sdkUpdateOption = myCheckSdkUpdateCheckBox.isSelected()
                                                ? (DartSdkUpdateOption)mySdkUpdateChannelCombo.getSelectedItem()
                                                : DartSdkUpdateOption.DoNotCheck;
    if (sdkUpdateOption != DartSdkUpdateOption.getDartSdkUpdateOption()) return true;

    final String dartiumPath =
      FileUtilRt.toSystemIndependentName(myDartiumPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
    final String dartiumPathInitial = myDartiumInitial == null ? null : myDartiumInitial.getPath();
    if (!dartiumPath.isEmpty() && new File(dartiumPath).exists() && !dartiumPath.equals(dartiumPathInitial)) return true;

    if (myDartiumInitial != null && !myDartiumSettingsCurrent.equals(myDartiumInitial.getSpecificSettings())) return true;

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final Module[] selectedModules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (selectedModules.length != myModulesWithDartSdkLibAttachedInitial.size()) return true;

      for (final Module module : selectedModules) {
        if (!myModulesWithDartSdkLibAttachedInitial.contains(module)) return true;

        final String currentPackageRootPath = myModuleToCustomPackageRootCurrent.get(module);
        if (!Comparing.equal(getCustomPackageRootPath(module), currentPackageRootPath)) return true;
      }
    }
    else {
      if (myDartSupportEnabledInitial != myEnableDartSupportCheckBox.isSelected()) return true;

      final Map.Entry<Module, String> entry = myModuleToCustomPackageRootCurrent.entrySet().iterator().next();
      final String currentPackageRootPath = myCustomPackageRootCheckBox.isSelected() ? entry.getValue() : null;
      if (!Comparing.equal(currentPackageRootPath, getCustomPackageRootPath(entry.getKey()))) return true;
    }

    return false;
  }

  @Override
  public void reset() {
    myInReset = true;

    // remember initial state
    mySdkInitial = DartSdk.getDartSdk(myProject);
    myModulesWithDartSdkLibAttachedInitial.clear();

    if (mySdkInitial != null) {
      myModulesWithDartSdkLibAttachedInitial.addAll(DartSdkGlobalLibUtil.getModulesWithDartSdkEnabled(myProject));
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

    myModuleToCustomPackageRootCurrent.clear();
    for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
      myModuleToCustomPackageRootCurrent.put(module, getCustomPackageRootPath(module));
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

    final boolean checkedMode = myDartiumInitial == null || DartiumUtil.isCheckedMode(myDartiumSettingsCurrent.getEnvironmentVariables());
    myCheckedModeCheckBox.setSelected(checkedMode);

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final CheckedTreeNode rootNode = (CheckedTreeNode)myModulesCheckboxTreeTable.getTree().getModel().getRoot();
      rootNode.setChecked(false);
      final Enumeration children = rootNode.children();
      while (children.hasMoreElements()) {
        final CheckedTreeNode node = (CheckedTreeNode)children.nextElement();
        node.setChecked(myModulesWithDartSdkLibAttachedInitial.contains((Module)node.getUserObject()));
      }
    }
    else {
      final String path = myModuleToCustomPackageRootCurrent.entrySet().iterator().next().getValue();
      myCustomPackageRootTextWithBrowse.setText(path == null ? "" : FileUtil.toSystemDependentName(path));
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
        final String sdkHomePath =
          FileUtilRt.toSystemIndependentName(mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());

        if (DartSdkUtil.isDartSdkHome(sdkHomePath)) {
          DartSdkUtil.updateKnownSdkPaths(myProject, sdkHomePath);

          DartSdkGlobalLibUtil.ensureDartSdkConfigured(sdkHomePath);
          DaemonCodeAnalyzer.getInstance(myProject).restart();

          final Module[] modules = DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                                   ? myModulesCheckboxTreeTable.getCheckedNodes(Module.class)
                                   : ModuleManager.getInstance(myProject).getModules();
          DartSdkGlobalLibUtil.enableDartSdkForSpecifiedModulesAndDisableForOthers(myProject, modules);

          for (Module module : ModuleManager.getInstance(myProject).getModules()) {
            if (ArrayUtil.contains(module, modules)) {
              final String customPackageRoot =
                DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport() || myCustomPackageRootCheckBox.isSelected()
                ? myModuleToCustomPackageRootCurrent.get(module)
                : null;
              setCustomPackageRootPath(module, customPackageRoot);
            }
            else {
              setCustomPackageRootPath(module, null);
            }
          }
        }

        final DartSdkUpdateOption sdkUpdateOption = myCheckSdkUpdateCheckBox.isSelected()
                                                    ? (DartSdkUpdateOption)mySdkUpdateChannelCombo.getSelectedItem()
                                                    : DartSdkUpdateOption.DoNotCheck;
        DartSdkUpdateOption.setDartSdkUpdateOption(sdkUpdateOption);

        final String dartiumPath =
          FileUtilRt.toSystemIndependentName(myDartiumPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
        DartiumUtil.applyDartiumSettings(dartiumPath, myDartiumSettingsCurrent);
      }
      else {
        if (myModulesWithDartSdkLibAttachedInitial.size() > 0 && mySdkInitial != null) {
          DartSdkGlobalLibUtil.disableDartSdk(myModulesWithDartSdkLibAttachedInitial);
        }

        for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
          setCustomPackageRootPath(module, null);
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
    myModuleToCustomPackageRootCurrent.clear();
  }

  private void updateControlsEnabledState() {
    UIUtil.setEnabled(mySettingsPanel, myEnableDartSupportCheckBox.isSelected(), true);

    mySdkUpdateChannelCombo.setEnabled(myCheckSdkUpdateCheckBox.isSelected() && myCheckedModeCheckBox.isEnabled());

    if (!DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final String path = myModuleToCustomPackageRootCurrent.entrySet().iterator().next().getValue();
      myCustomPackageRootCheckBox.setSelected(path != null);
      myCustomPackageRootTextWithBrowse.setEnabled(myCustomPackageRootCheckBox.isSelected() && myCustomPackageRootCheckBox.isEnabled());
    }
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

    String message =
      DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
    if (message != null) return message;

    message =
      DartiumUtil.getErrorMessageIfWrongDartiumPath(myDartiumPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim());
    if (message != null) return message;

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final Module[] modules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (modules.length == 0) {
        return DartBundle.message("warning.no.modules.selected.dart.support.will.be.disabled");
      }

      for (final Module module : modules) {
        final String customPackageRoot = myModuleToCustomPackageRootCurrent.get(module);
        final String customPackagePathError = getErrorMessageForCustomPackageRoot(customPackageRoot);
        if (customPackagePathError != null) {
          return customPackagePathError;
        }
      }
    }
    else {
      if (myCustomPackageRootCheckBox.isSelected()) {
        final String customPackageRoot = myModuleToCustomPackageRootCurrent.entrySet().iterator().next().getValue();
        if (customPackageRoot == null) {
          return DartBundle.message("warning.custom.package.root.not.specified");
        }

        final String customPackagePathError = getErrorMessageForCustomPackageRoot(customPackageRoot);
        if (customPackagePathError != null) {
          return customPackagePathError;
        }
      }
    }

    return null;
  }

  @Nullable
  private static String getErrorMessageForCustomPackageRoot(@Nullable final String customPackageRootPath) {
    if (customPackageRootPath == null) return null;
    final VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(customPackageRootPath);
    if (folder == null || !folder.isDirectory()) {
      return DartBundle.message("warning.custom.package.root.not.found", FileUtil.toSystemDependentName(customPackageRootPath));
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

    final TableCellRenderer customPackageRootCellRenderer = new TableCellRenderer() {
      private final JBLabel myLabel = new JBLabel();
      private final TextFieldWithBrowseButton myTextWithBrowse = new TextFieldWithBrowseButton() {
        public void setOpaque(final boolean isOpaque) {
          // never make this renderer opaque in order not to have cell selection background between text field and browse button
        }
      };

      public Component getTableCellRendererComponent(final JTable table,
                                                     final Object value,
                                                     final boolean isSelected,
                                                     final boolean hasFocus,
                                                     final int row,
                                                     final int column) {

        if (value instanceof String) {
          final String text = FileUtil.toSystemDependentName((String)value);

          if (isSelected) {
            myTextWithBrowse.setText(text);
            return myTextWithBrowse;
          }
          else {
            myLabel.setText(text);
            return myLabel;
          }
        }
        else {
          myLabel.setText("");
          return myLabel;
        }
      }
    };

    final LocalPathCellEditor customPackageRootEditor = new LocalPathCellEditor(myProject) {
      public Object getCellEditorValue() {
        return FileUtil.toSystemIndependentName(myComponent.getChildComponent().getText().trim());
      }

      @Override
      public Component getTableCellEditorComponent(final JTable table,
                                                   @Nullable final Object value,
                                                   final boolean isSelected,
                                                   final int row,
                                                   final int column) {
        final TextFieldWithBrowseButton fieldWithBrowse = new TextFieldWithBrowseButton();
        fieldWithBrowse.addBrowseFolderListener(DartBundle.message("select.custom.package.root"), null, myProject,
                                                FileChooserDescriptorFactory.createSingleFolderDescriptor());
        myComponent = new CellEditorComponentWithBrowseButton<JTextField>(fieldWithBrowse, this);
        final String text = value != null ? FileUtil.toSystemDependentName((String)value) : "";

        myComponent.getChildComponent().setText(text);
        return myComponent;
      }
    }/*.normalizePath(true)*/;

    final String columnName = DartBundle.message("custom.package.root");
    final ColumnInfo<CheckedTreeNode, String> CUSTOM_PACKAGE_ROOT_COLUMN = new ColumnInfo<CheckedTreeNode, String>(columnName) {
      @Nullable
      public String valueOf(final CheckedTreeNode node) {
        final Object userObject = node.getUserObject();
        if (node.isChecked() && userObject instanceof Module) {
          return myModuleToCustomPackageRootCurrent.get(userObject);
        }
        return null;
      }

      public boolean isCellEditable(final CheckedTreeNode node) {
        return node.isChecked() && node.getUserObject() instanceof Module;
      }

      @Nullable
      public TableCellRenderer getRenderer(final CheckedTreeNode node) {
        return customPackageRootCellRenderer;
      }

      @Nullable
      public TableCellEditor getEditor(final CheckedTreeNode node) {
        return customPackageRootEditor;
      }

      public void setValue(final CheckedTreeNode node, final String value) {
        final Object userObject = node.getUserObject();
        if (userObject instanceof Module) {
          myModuleToCustomPackageRootCurrent.put((Module)userObject, StringUtil.nullize(value));
          updateErrorLabel();
        }
      }

      public int getWidth(final JTable table) {
        return new JLabel(getName()).getMinimumSize().width * 3 / 2;
      }
    };

    myModulesCheckboxTreeTable =
      new CheckboxTreeTable(null, checkboxTreeCellRenderer, new ColumnInfo[]{new TreeColumnInfo(""), CUSTOM_PACKAGE_ROOT_COLUMN});
    myModulesCheckboxTreeTable.addCheckboxTreeListener(new CheckboxTreeAdapter() {
      @Override
      public void nodeStateChanged(@NotNull CheckedTreeNode node) {
        updateErrorLabel();
      }
    });
    myModulesCheckboxTreeTable.setRowHeight(myModulesCheckboxTreeTable.getRowHeight() + 2);

    myModulesCheckboxTreeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {

      }

      public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
        throw new ExpandVetoException(event);
      }
    });

    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new RemoveCustomPackageRootAction(myModulesCheckboxTreeTable));
    PopupHandler.installPopupHandler(myModulesCheckboxTreeTable, group, ActionPlaces.UNKNOWN, ActionManager.getInstance());
  }

  private class RemoveCustomPackageRootAction extends AnAction {
    private final CheckboxTreeTable myTable;

    public RemoveCustomPackageRootAction(final CheckboxTreeTable table) {
      super(DartBundle.message("remove.custom.package.root"));
      myTable = table;
    }

    public void update(final AnActionEvent e) {
      TableUtil.stopEditing(myTable);

      boolean enabled = false;
      for (Object item : myTable.getSelection()) {
        if (item instanceof CheckedTreeNode && ((CheckedTreeNode)item).isChecked()) {
          final Object userObject = ((CheckedTreeNode)item).getUserObject();
          if (userObject instanceof Module && myModuleToCustomPackageRootCurrent.get(userObject) != null) {
            enabled = true;
            break;
          }
        }
      }

      e.getPresentation().setEnabled(enabled);
    }

    public void actionPerformed(final AnActionEvent e) {
      for (Object item : myTable.getSelection()) {
        final Object userObject = item instanceof CheckedTreeNode ? ((CheckedTreeNode)item).getUserObject() : null;
        if (userObject instanceof Module) {
          myModuleToCustomPackageRootCurrent.put((Module)userObject, null);
        }
      }

      final TableModel model = myTable.getModel();
      if (model instanceof AbstractTableModel) {
        ((AbstractTableModel)model).fireTableDataChanged();
      }

      updateErrorLabel();
    }
  }

  private static boolean isCustomPackageRootLibraryEntry(@NotNull final OrderEntry entry) {
    final String libName = entry instanceof LibraryOrderEntry ? ((LibraryOrderEntry)entry).getLibraryName() : null;
    // previously library name was plural "Dart custom package roots", so for compatibility we check startsWith() instead of equals()
    return libName != null && libName.startsWith(CUSTOM_PACKAGE_ROOT_LIB_NAME);
  }

  @Nullable
  private static String getCustomPackageRootPath(@NotNull final Module module) {
    for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (isCustomPackageRootLibraryEntry(entry)) {
        final String[] urls = ((LibraryOrderEntry)entry).getRootUrls(OrderRootType.CLASSES);
        if (urls.length > 0) {
          return VfsUtilCore.urlToPath(urls[0]);
        }
      }
    }
    return null;
  }

  private static void setCustomPackageRootPath(@NotNull final Module module, @Nullable final String path) {
    if (Comparing.equal(path, getCustomPackageRootPath(module))) return;

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      for (final OrderEntry entry : modifiableModel.getOrderEntries()) {
        if (isCustomPackageRootLibraryEntry(entry)) {
          modifiableModel.removeOrderEntry(entry);
        }
      }

      if (path != null) {
        final Library library = modifiableModel.getModuleLibraryTable().createLibrary(CUSTOM_PACKAGE_ROOT_LIB_NAME);
        final Library.ModifiableModel libModel = library.getModifiableModel();
        libModel.addRoot(VfsUtilCore.pathToUrl(path), OrderRootType.CLASSES);
        libModel.commit();
      }

      modifiableModel.commit();
    }
    catch (Exception e) {
      if (!modifiableModel.isDisposed()) modifiableModel.dispose();
    }
  }

  @Nullable
  public static VirtualFile getCustomPackageRoot(@NotNull final Module module) {
    for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (isCustomPackageRootLibraryEntry(entry)) {
        VirtualFile[] files = ((LibraryOrderEntry)entry).getRootFiles(OrderRootType.CLASSES);
        return files.length > 0 ? files[0] : null;
      }
    }
    return null;
  }

  @NotNull
  public static Map<String, String> getContentRootPathToCustomPackageRootMap(@NotNull final Module module) {
    final String customPackageRootPath = getCustomPackageRootPath(module);
    if (customPackageRootPath != null) {
      final Map<String, String> result = new SmartHashMap<String, String>();
      for (String contentRootUrl : ModuleRootManager.getInstance(module).getContentRootUrls()) {
        result.put(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentRootUrl)),
                   FileUtil.toSystemDependentName(customPackageRootPath));
      }
      return result;
    }

    return Collections.emptyMap();
  }

  public static void openDartSettings(@NotNull final Project project) {
    ShowSettingsUtilImpl.showSettingsDialog(project, DART_SETTINGS_PAGE_ID, "");
  }
}
