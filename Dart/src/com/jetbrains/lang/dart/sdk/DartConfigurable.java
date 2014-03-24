package com.jetbrains.lang.dart.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;

public class DartConfigurable implements SearchableConfigurable {

  public static final String DART_SETTINGS_PAGE_NAME = DartBundle.message("dart.title");

  private JPanel myMainPanel;
  private JBCheckBox myEnableDartSupportCheckBox;

  private JPanel mySettingsPanel;
  private TextFieldWithBrowseButton mySdkPathTextWithBrowse;
  private JBLabel myVersionLabel;

  private TextFieldWithBrowseButton myDartiumPathTextWithBrowse;
  private JButton myDartiumSettingsButton;

  private JPanel myModulesPanel;

  private CheckboxTree myModulesCheckboxTree;
  private JBLabel myErrorLabel;

  private final @NotNull Project myProject;

  private boolean myInReset = false;

  private boolean myDartSupportEnabledInitial;
  private @Nullable DartSdk mySdkInitial;
  private final @NotNull Collection<Module> myModulesWithDartSdkLibAttachedInitial = new THashSet<Module>();

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
    final Insets margin = myEnableDartSupportCheckBox.getMargin();
    myEnableDartSupportCheckBox.setMargin(new Insets(margin.top, 0, margin.bottom, margin.right));
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
    final Computable<Boolean> isResettingControlsComputable = new Computable<Boolean>() {
      public Boolean compute() {
        return myInReset;
      }
    };

    DartSdkUtil.initDartSdkAndDartiumControls(myProject, mySdkPathTextWithBrowse, myVersionLabel, myDartiumPathTextWithBrowse,
                                              isResettingControlsComputable);

    final DocumentAdapter documentListener = new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        updateErrorLabel();
      }
    };

    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(documentListener);
    myDartiumPathTextWithBrowse.getTextField().getDocument().addDocumentListener(documentListener);

    myDartiumSettingsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(myProject, myDartiumSettingsCurrent.createConfigurable());
      }
    });
  }

  private void initModulesPanel() {
    if (!DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      myModulesPanel.setVisible(false);
      return;
    }

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    Arrays.sort(modules, new Comparator<Module>() {
      @Override
      public int compare(final Module module1, final Module module2) {
        return module1.getName().toLowerCase().compareTo(module2.getName().toLowerCase());
      }
    });

    myModulesCheckboxTree.setVisibleRowCount(5);

    final CheckedTreeNode rootNode = new CheckedTreeNode(myProject);
    myModulesCheckboxTree.setModel(new DefaultTreeModel(rootNode));

    for (final Module module : modules) {
      rootNode.add(new CheckedTreeNode(module));
    }

    ((DefaultTreeModel)myModulesCheckboxTree.getModel()).reload(rootNode);
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
    final String sdkHomePath = FileUtilRt.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
    final boolean sdkSelected = DartSdkUtil.isDartSdkHome(sdkHomePath);

    // was disabled, now disabled (or no sdk selected) => not modified, do not care about other controls
    if (!myDartSupportEnabledInitial && (!myEnableDartSupportCheckBox.isSelected() || !sdkSelected)) return false;
    // was enabled, now disabled => modified
    if (myDartSupportEnabledInitial && !myEnableDartSupportCheckBox.isSelected()) return true;
    // was disabled, now enabled or was enabled, now enabled => need to check further

    final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();
    if (sdkSelected && !sdkHomePath.equals(initialSdkHomePath)) return true;

    final String dartiumPath = FileUtilRt.toSystemIndependentName(myDartiumPathTextWithBrowse.getText().trim());
    final String dartiumPathInitial = myDartiumInitial == null ? null : myDartiumInitial.getPath();
    if (!dartiumPath.isEmpty() && new File(dartiumPath).exists() && !dartiumPath.equals(dartiumPathInitial)) return true;

    if (myDartiumInitial != null && !myDartiumSettingsCurrent.equals(myDartiumInitial.getSpecificSettings())) return true;

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final Module[] selectedModules = myModulesCheckboxTree.getCheckedNodes(Module.class, null);
      if (selectedModules.length != myModulesWithDartSdkLibAttachedInitial.size()) return true;

      for (final Module module : selectedModules) {
        if (!myModulesWithDartSdkLibAttachedInitial.contains(module)) return true;
      }
    }
    else {
      return myDartSupportEnabledInitial != myEnableDartSupportCheckBox.isSelected();
    }

    return false;
  }

  @Override
  public void reset() {
    myInReset = true;

    // remember initial state
    mySdkInitial = DartSdk.getGlobalDartSdk();
    myModulesWithDartSdkLibAttachedInitial.clear();

    if (mySdkInitial != null) {
      myModulesWithDartSdkLibAttachedInitial.addAll(
        DartSdkGlobalLibUtil.getModulesWithDartSdkGlobalLibAttached(myProject, mySdkInitial.getGlobalLibName()));
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
    mySdkPathTextWithBrowse.setText(mySdkInitial == null ? "" : FileUtilRt.toSystemDependentName(mySdkInitial.getHomePath()));
    myDartiumPathTextWithBrowse.setText(myDartiumInitial == null
                                        ? ""
                                        : FileUtilRt.toSystemDependentName(StringUtil.notNullize(myDartiumInitial.getPath())));

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final CheckedTreeNode rootNode = (CheckedTreeNode)myModulesCheckboxTree.getModel().getRoot();
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

  @Override
  public void apply() throws ConfigurationException {
    // similar to DartApplicationGenerator.setupSdkAndDartium()
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        if (myEnableDartSupportCheckBox.isSelected()) {
          final String sdkHomePath = FileUtilRt.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
          final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();

          if (DartSdkUtil.isDartSdkHome(sdkHomePath)) {
            final String dartSdkGlobalLibName;

            if (mySdkInitial == null) {
              dartSdkGlobalLibName = DartSdkGlobalLibUtil.createDartSdkGlobalLib(myProject, sdkHomePath);
            }
            else {
              dartSdkGlobalLibName = mySdkInitial.getGlobalLibName();

              if (!sdkHomePath.equals(initialSdkHomePath)) {
                DartSdkGlobalLibUtil.updateDartSdkGlobalLib(myProject, dartSdkGlobalLibName, sdkHomePath);
              }
            }

            final Module[] modules = DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                                     ? myModulesCheckboxTree.getCheckedNodes(Module.class, null)
                                     : ModuleManager.getInstance(myProject).getModules();
            DartSdkGlobalLibUtil.updateDependencyOnDartSdkGlobalLib(myProject, modules, dartSdkGlobalLibName);
          }

          final String dartiumPath = FileUtilRt.toSystemIndependentName(myDartiumPathTextWithBrowse.getText().trim());
          DartiumUtil.applyDartiumSettings(dartiumPath, myDartiumSettingsCurrent);
        }
        else {
          if (myModulesWithDartSdkLibAttachedInitial.size() > 0 && mySdkInitial != null) {
            DartSdkGlobalLibUtil.detachDartSdkGlobalLib(myModulesWithDartSdkLibAttachedInitial, mySdkInitial.getGlobalLibName());
          }
        }
      }
    });

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
  }

  private void updateErrorLabel() {
    final String message = getErrorMessage();
    myErrorLabel.setText(
      XmlStringUtil.wrapInHtml("<font color='#" + ColorUtil.toHex(JBColor.RED) + "'><left>" + message + "</left></font>"));
    myErrorLabel.setVisible(message != null);
  }

  @Nullable
  private String getErrorMessage() {
    if (!myEnableDartSupportCheckBox.isSelected()) {
      return null;
    }

    String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
    if (message != null) return message;

    message = DartiumUtil.getErrorMessageIfWrongDartiumPath(myDartiumPathTextWithBrowse.getText().trim());
    if (message != null) return message;

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport() && myModulesCheckboxTree.getCheckedNodes(Module.class, null).length == 0) {
      return DartBundle.message("warning.no.modules.selected.dart.support.will.be.disabled");
    }

    return null;
  }

  private void createUIComponents() {
    final CheckboxTree.CheckboxTreeCellRenderer renderer = new CheckboxTree.CheckboxTreeCellRenderer() {
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

    myModulesCheckboxTree = new CheckboxTree(renderer, null, new CheckboxTreeBase.CheckPolicy(true, true, true, true)) {
      @Override
      protected void checkNode(final CheckedTreeNode node, final boolean checked) {
        super.checkNode(node, checked);
        updateErrorLabel();
      }
    };
  }
}
