package com.jetbrains.lang.dart.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckboxTreeBase;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.DefaultTreeModel;
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

  private JPanel myModulesPanel;
  private CheckboxTree myModulesCheckboxTree;

  private JBLabel myErrorLabel;

  private final @NotNull Project myProject;

  private boolean myDartSupportEnabledInitial;
  private @Nullable DartSdk mySdkInitial;
  private final @NotNull Collection<Module> myModulesWithDartSdkLibAttachedInitial = new THashSet<Module>();

  public DartConfigurable(final @NotNull Project project) {
    myProject = project;

    initEnableDartSupportCheckBox();
    initSdkPathTextWithBrowse();
    initModulesPanel();

    myErrorLabel.setIcon(AllIcons.Actions.Lightning);
  }

  private void initEnableDartSupportCheckBox() {
    myEnableDartSupportCheckBox.setText(DartBundle.message("enable.dart.support.for.project.0", myProject.getName()));
    myEnableDartSupportCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControlsEnabledState();
        updateErrorLabel();
      }
    });
  }

  private void initSdkPathTextWithBrowse() {
    final TextComponentAccessor<JTextField> textComponentAccessor = new TextComponentAccessor<JTextField>() {
      public String getText(final JTextField component) {
        return component.getText();
      }

      public void setText(final JTextField component, final String text) {
        if (!text.isEmpty() && !DartSdkUtil.isDartSdkHome(text)) {
          final String probablySdkPath = text + "/dart-sdk";
          if (DartSdkUtil.isDartSdkHome(probablySdkPath)) {
            component.setText(FileUtilRt.toSystemDependentName(probablySdkPath));
            return;
          }
        }

        component.setText(FileUtilRt.toSystemDependentName(text));
      }
    };

    final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderListener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Select Dart SDK path", null, mySdkPathTextWithBrowse, myProject,
                                                                           FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                                                           textComponentAccessor);
    mySdkPathTextWithBrowse.addBrowseFolderListener(myProject, browseFolderListener);

    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        updateSdkVersionLabel();
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
    Arrays.sort(modules, new Comparator<Module>() {
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

  @NotNull
  public String getId() {
    return "dart.settings";
  }

  @Nullable
  public Runnable enableSearch(final String option) {
    return null;
  }

  @Nls
  public String getDisplayName() {
    return DART_SETTINGS_PAGE_NAME;
  }

  @Nullable
  public String getHelpTopic() {
    return "settings.dart.settings";
  }

  @Nullable
  public JComponent createComponent() {
    return myMainPanel;
  }

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

  public void reset() {
    // remember initial state
    mySdkInitial = DartSdk.getGlobalDartSdk();
    myModulesWithDartSdkLibAttachedInitial.clear();

    if (mySdkInitial != null) {
      myModulesWithDartSdkLibAttachedInitial.addAll(
        DartSdkGlobalLibUtil.getModulesWithDartSdkGlobalLibAttached(myProject, mySdkInitial.getGlobalLibName()));
    }

    myDartSupportEnabledInitial = !myModulesWithDartSdkLibAttachedInitial.isEmpty();

    // reset UI
    myEnableDartSupportCheckBox.setSelected(myDartSupportEnabledInitial);
    mySdkPathTextWithBrowse.setText(mySdkInitial == null ? "" : FileUtilRt.toSystemDependentName(mySdkInitial.getHomePath()));

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
    updateSdkVersionLabel();
    updateErrorLabel();
  }

  public void apply() throws ConfigurationException {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        if (myEnableDartSupportCheckBox.isSelected()) {
          final String sdkHomePath = FileUtilRt.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
          final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();

          if (DartSdkUtil.isDartSdkHome(sdkHomePath)) {
            final String dartSdkGlobalLibName;

            if (mySdkInitial == null) {
              dartSdkGlobalLibName = DartSdkGlobalLibUtil.createDartSdkGlobalLib(sdkHomePath);
            }
            else {
              dartSdkGlobalLibName = mySdkInitial.getGlobalLibName();

              if (!sdkHomePath.equals(initialSdkHomePath)) {
                DartSdkGlobalLibUtil.updateDartSdkGlobalLib(dartSdkGlobalLibName, sdkHomePath);
              }
            }

            final Module[] modules = DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                                     ? myModulesCheckboxTree.getCheckedNodes(Module.class, null)
                                     : ModuleManager.getInstance(myProject).getModules();
            DartSdkGlobalLibUtil.updateDependencyOnDartSdkGlobalLib(myProject, modules, dartSdkGlobalLibName);
          }
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

  public void disposeUIResources() {
    mySdkInitial = null;
    myModulesWithDartSdkLibAttachedInitial.clear();
  }

  private void updateControlsEnabledState() {
    UIUtil.setEnabled(mySettingsPanel, myEnableDartSupportCheckBox.isSelected(), true);
  }

  private void updateSdkVersionLabel() {
    final String sdkHomePath = mySdkPathTextWithBrowse.getText().trim();
    myVersionLabel.setText(sdkHomePath.isEmpty() ? "" : DartSdkUtil.getSdkVersion(sdkHomePath));
  }

  private void updateErrorLabel() {
    final String message = getErrorMessage();
    myErrorLabel.setText(message);
    myErrorLabel.setVisible(message != null);
  }

  @Nullable
  private String getErrorMessage() {
    if (!myEnableDartSupportCheckBox.isSelected()) {
      return null;
    }

    final String sdkRootPath = mySdkPathTextWithBrowse.getText().trim();
    if (sdkRootPath.isEmpty()) return "Error: path to the Dart SDK is not specified.";

    final File sdkRoot = new File(sdkRootPath);
    if (!sdkRoot.isDirectory()) return "Error: the folder specified as the Dart SDK home does not exist.";

    if (!DartSdkUtil.isDartSdkHome(sdkRootPath)) return "Error: Dart SDK is not found in the specified location.";

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport() && myModulesCheckboxTree.getCheckedNodes(Module.class, null).length == 0) {
      return "Warning: no modules selected. Dart support will be disabled for the project.";
    }

    return null;
  }

  private void createUIComponents() {
    final CheckboxTree.CheckboxTreeCellRenderer renderer = new CheckboxTree.CheckboxTreeCellRenderer() {
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
      protected void checkNode(final CheckedTreeNode node, final boolean checked) {
        super.checkNode(node, checked);
        updateErrorLabel();
      }
    };
  }
}
