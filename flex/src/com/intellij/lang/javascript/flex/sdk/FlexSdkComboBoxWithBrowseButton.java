package com.intellij.lang.javascript.flex.sdk;

import com.intellij.ide.DataManager;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectJdksConfigurable;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlexSdkComboBoxWithBrowseButton extends ComboboxWithBrowseButton {
  public interface Listener {
    void stateChanged();
  }

  // special combobox item that stands for sdk specified by module
  private static class ModuleSdk {
    private Sdk mySdk;
  }

  public static final Condition<Sdk> FLEX_SDK = new Condition<Sdk>() {
    public boolean value(final Sdk sdk) {
      return sdk != null && sdk.getSdkType() instanceof FlexSdkType2;
    }
  };

  public static final Condition<Sdk> FLEX_OR_FLEXMOJOS_SDK = new Condition<Sdk>() {
    public boolean value(final Sdk sdk) {
      return sdk != null && (sdk.getSdkType() instanceof FlexSdkType2 || sdk.getSdkType() instanceof FlexmojosSdkType);
    }
  };

  public static final String MODULE_SDK_KEY = "Module SDK";

  private final Condition<Sdk> mySdkEvaluator;

  private ModuleSdk myModuleSdk = new ModuleSdk();
  private boolean myShowModuleSdk = false;

  public FlexSdkComboBoxWithBrowseButton() {
    this(FLEX_SDK);
  }

  public FlexSdkComboBoxWithBrowseButton(final Condition<Sdk> sdkEvaluator) {
    mySdkEvaluator = sdkEvaluator;
    rebuildSdkListAndSelectSdk(null); // if SDKs exist first will be selected automatically

    final JComboBox sdkCombo = getComboBox();
    sdkCombo.setRenderer(new ListCellRendererWrapper(sdkCombo.getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof ModuleSdk) {
          final Sdk sdk = ((ModuleSdk)value).mySdk;
          if (sdk == null) {
            if (sdkCombo.isEnabled()) {
              setText("<html><font color='red'>Module SDK [not set]</font></html>");
              setIcon(PlatformIcons.ERROR_INTRODUCTION_ICON);
            }
            else {
              setText("Module SDK [not set]");
              setIcon(IconLoader.getDisabledIcon(PlatformIcons.ERROR_INTRODUCTION_ICON));
            }
          }
          else {
            setText("Module SDK [" + sdk.getName() + "]");
            setIcon(((ModuleSdk)value).mySdk.getSdkType().getIcon());
          }
        }
        else if (value instanceof String) {
          if (sdkCombo.isEnabled()) {
            setText("<html><font color='red'>" + value + " [Invalid]</font></html>");
            setIcon(PlatformIcons.ERROR_INTRODUCTION_ICON);
          }
          else {
            setText(value + " [Invalid]");
            setIcon(IconLoader.getDisabledIcon(PlatformIcons.ERROR_INTRODUCTION_ICON));
          }
        }
        else if (value instanceof Sdk) {
          setText(((Sdk)value).getName());
          setIcon(((Sdk)value).getSdkType().getIcon());
        }
        else {
          if (sdkCombo.isEnabled()) {
            setText("<html><font color='red'>[none]</font></html>");
          }
          else {
            setText("[none]");
          }
        }
      }
    });

    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
        if (project == null) {
          project = ProjectManager.getInstance().getDefaultProject();
        }

        final FlexProjectConfigurationEditor currentFlexEditor =
          FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();

        ProjectSdksModel sdksModel = ProjectStructureConfigurable.getInstance(project).getProjectJdksModel();
        if (currentFlexEditor != null) {
          // project structure is open, don't directly commit model
          sdksModel = new NonCommittingWrapper(sdksModel, JdkListConfigurable.getInstance(project));
        }

        final ProjectJdksEditor editor =
          new ProjectJdksEditor(null, FlexSdkComboBoxWithBrowseButton.this, new ProjectJdksConfigurable(project, sdksModel));
        editor.show();
        if (editor.isOK()) {
          final Sdk selectedSdk = editor.getSelectedJdk();
          if (mySdkEvaluator.value(selectedSdk)) {
            rebuildSdkListAndSelectSdk(selectedSdk);
          }
          else {
            rebuildSdkListAndSelectSdk(null);
            if (selectedSdk != null) {
              Messages
                .showErrorDialog(FlexSdkComboBoxWithBrowseButton.this, FlexBundle.message("sdk.can.not.be.selected", selectedSdk.getName()),
                                 FlexBundle.message("select.flex.sdk"));
            }
          }
        }
      }
    });
  }

  private void rebuildSdkListAndSelectSdk(@Nullable final Sdk selectedSdk) {
    final String previousSelectedSdkName = getSelectedSdkRaw();
    final List<Object> sdkList = new ArrayList<Object>();

    if (myShowModuleSdk) {
      sdkList.add(myModuleSdk);
    }

    final Sdk[] sdks = FlexSdkUtils.getAllSdks();
    for (final Sdk sdk : sdks) {
      if (mySdkEvaluator.value(sdk)) {
        sdkList.add(sdk);
      }
    }

    if (!sdkList.isEmpty()) {
      getComboBox().setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(sdkList)));
      if (selectedSdk != null) {
        setSelectedSdkRaw(selectedSdk.getName(), false);
      }
      else if (previousSelectedSdkName != null) {
        setSelectedSdkRaw(previousSelectedSdkName, false);
      }
    }
    else {
      getComboBox().setModel(new DefaultComboBoxModel(new Object[]{null}));
    }
  }

  public void addComboboxListener(final Listener listener) {
    getComboBox().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        listener.stateChanged();
      }
    });

    getComboBox().addPropertyChangeListener("model", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        listener.stateChanged();
      }
    });
  }

  @Nullable
  public Sdk getSelectedSdk() {
    final Object selectedItem = getComboBox().getSelectedItem();

    if (selectedItem instanceof ModuleSdk) {
      return ((ModuleSdk)selectedItem).mySdk;
    }
    else if (selectedItem instanceof Sdk) {
      return (Sdk)selectedItem;
    }
    else {
      return null;
    }
  }

  public String getSelectedSdkRaw() {
    final Object selectedItem = getComboBox().getSelectedItem();

    if (selectedItem instanceof ModuleSdk) {
      return MODULE_SDK_KEY;
    }
    else if (selectedItem instanceof Sdk) {
      return ((Sdk)selectedItem).getName();
    }
    else if (selectedItem instanceof String) {
      return (String)selectedItem;
    }
    else {
      return "";
    }
  }

  public void setSelectedSdkRaw(final String sdkName) {
    setSelectedSdkRaw(sdkName, true);
  }

  private void setSelectedSdkRaw(final String sdkName, final boolean addErrorItemIfSdkNotFound) {
    final JComboBox combo = getComboBox();

    if (MODULE_SDK_KEY.equals(sdkName)) {
      combo.setSelectedItem(myModuleSdk);
      return;
    }
    else {
      for (int i = 0; i < combo.getItemCount(); i++) {
        final Object item = combo.getItemAt(i);
        if (item instanceof Sdk && ((Sdk)item).getName().equals(sdkName)) {
          combo.setSelectedItem(item);
          return;
        }
      }
    }

    // sdk not found
    if (addErrorItemIfSdkNotFound) {
      final List<Object> items = new ArrayList<Object>();
      items.add(sdkName);
      for (int i = 0; i < combo.getItemCount(); i++) {
        final Object item = combo.getItemAt(i);
        if (!(item instanceof String)) {
          items.add(item);
        }
      }
      combo.setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(items)));
    }
  }

  public void showModuleSdk(final boolean showModuleSdk) {
    if (myShowModuleSdk != showModuleSdk) {
      myShowModuleSdk = showModuleSdk;
      final Object selectedItem = getComboBox().getSelectedItem();
      rebuildSdkListAndSelectSdk(null);
      if (selectedItem instanceof String) {
        setSelectedSdkRaw((String)selectedItem, true);
      }
    }
  }

  public void setModuleSdk(final Sdk sdk) {
    if (sdk != myModuleSdk.mySdk) {
      myModuleSdk.mySdk = sdk;
    }
  }

  private static class NonCommittingWrapper extends ProjectSdksModel {
    private final ProjectSdksModel myOriginal;
    private JdkListConfigurable myConfigurable;

    public NonCommittingWrapper(final ProjectSdksModel original, JdkListConfigurable configurable) {
      myOriginal = original;
      myConfigurable = configurable;
    }

    public void apply() throws ConfigurationException {
      apply(null);
    }

    public void apply(@Nullable final MasterDetailsComponent configurable) throws ConfigurationException {
      myConfigurable.reset(); // just update the view
    }

    public void reset(@Nullable final Project project) {
      // ignore
    }

    public void addListener(final Listener listener) {
      myOriginal.addListener(listener);
    }

    public void removeListener(final Listener listener) {
      myOriginal.removeListener(listener);
    }

    public Listener getMulticaster() {
      return myOriginal.getMulticaster();
    }

    public Sdk[] getSdks() {
      return myOriginal.getSdks();
    }

    public Sdk findSdk(final String sdkName) {
      return myOriginal.findSdk(sdkName);
    }

    public void disposeUIResources() {
      // ignore
    }

    public HashMap<Sdk, Sdk> getProjectSdks() {
      return myOriginal.getProjectSdks();
    }

    public boolean isModified() {
      return myOriginal.isModified();
    }

    public void removeSdk(final Sdk editableObject) {
      myOriginal.removeSdk(editableObject);
    }

    public void createAddActions(final DefaultActionGroup group,
                                 final JComponent parent,
                                 final Consumer<Sdk> updateTree,
                                 @Nullable final Condition<SdkType> filter) {
      myOriginal.createAddActions(group, parent, updateTree, filter);
    }

    public void doAdd(final SdkType type, final Consumer<Sdk> updateTree) {
      myOriginal.doAdd(type, updateTree);
    }

    public void addSdk(final Sdk sdk) {
      myOriginal.addSdk(sdk);
    }

    public void doAdd(final ProjectJdkImpl newSdk, @Nullable final Consumer<Sdk> updateTree) {
      myOriginal.doAdd(newSdk, updateTree);
    }

    public Sdk findSdk(@Nullable final Sdk modelJdk) {
      return myOriginal.findSdk(modelJdk);
    }

    public Sdk getProjectSdk() {
      return myOriginal.getProjectSdk();
    }

    public void setProjectSdk(final Sdk projectSdk) {
      myOriginal.setProjectSdk(projectSdk);
    }

    public boolean isInitialized() {
      return myOriginal.isInitialized();
    }
  }
}
