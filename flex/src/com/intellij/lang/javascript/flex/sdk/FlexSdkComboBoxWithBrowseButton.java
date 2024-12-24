// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.sdk;

import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectJdksConfigurable;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public final class FlexSdkComboBoxWithBrowseButton extends ComboboxWithBrowseButton {
  public interface Listener {
    void stateChanged();
  }

  // special combobox item that stands for sdk specified by the build configuration
  private static class BCSdk {
    private Sdk mySdk;
  }

  public static final Condition<Sdk> FLEX_SDK = sdk -> sdk != null && sdk.getSdkType() instanceof FlexSdkType2;

  public static final Condition<Sdk> FLEX_OR_FLEXMOJOS_SDK =
    sdk -> sdk != null && (sdk.getSdkType() instanceof FlexSdkType2 || sdk.getSdkType() instanceof FlexmojosSdkType);

  public static final String BC_SDK_KEY = "BC SDK";

  private final Condition<? super Sdk> mySdkFilter;

  private final BCSdk myBCSdk = new BCSdk();
  private boolean myShowBCSdk = false;

  public FlexSdkComboBoxWithBrowseButton() {
    this(FLEX_SDK);
  }

  public FlexSdkComboBoxWithBrowseButton(final Condition<? super Sdk> sdkFilter) {
    mySdkFilter = sdkFilter;
    rebuildSdkListAndSelectSdk(null); // if SDKs exist first will be selected automatically

    final JComboBox sdkCombo = getComboBox();
    sdkCombo.setRenderer(SimpleListCellRenderer.create((label, value, index) -> {
      if (value instanceof BCSdk) {
        final Sdk sdk = ((BCSdk)value).mySdk;
        if (sdk == null) {
          if (sdkCombo.isEnabled()) {
            label.setText("<html>SDK set for the build configuration <font color='red'>[not set]</font></html>");
          }
          else {
            label.setText("SDK set for the build configuration [not set]");
          }
        }
        else {
          label.setText("SDK set for the build configuration [" + sdk.getName() + "]");
          label.setIcon(((SdkType)((BCSdk)value).mySdk.getSdkType()).getIcon());
        }
      }
      else if (value instanceof String) {
        if (sdkCombo.isEnabled()) {
          label.setText("<html><font color='red'>" + value + " [Invalid]</font></html>");
          label.setIcon(null);
        }
        else {
          label.setText(value + " [Invalid]");
        }
      }
      else if (value instanceof Sdk) {
        label.setText(((Sdk)value).getName());
        label.setIcon(((SdkType)((Sdk)value).getSdkType()).getIcon());
      }
      else {
        if (sdkCombo.isEnabled()) {
          label.setText("<html><font color='red'>[none]</font></html>");
        }
        else {
          label.setText("[none]");
        }
      }
    }));

    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
        if (project == null) {
          project = ProjectManager.getInstance().getDefaultProject();
        }

        final FlexProjectConfigurationEditor currentFlexEditor =
          FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();

        ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(project);
        ProjectSdksModel sdksModel = configurable.getProjectJdksModel();
        if (currentFlexEditor != null) {
          // project structure is open, don't directly commit model
          sdksModel = new NonCommittingWrapper(sdksModel, configurable.getJdkConfig());
        }

        final ProjectJdksEditor editor =
          new ProjectJdksEditor(null, FlexSdkComboBoxWithBrowseButton.this, new ProjectJdksConfigurable(project, sdksModel));
        if (editor.showAndGet()) {
          final Sdk selectedSdk = editor.getSelectedJdk();
          if (mySdkFilter.value(selectedSdk)) {
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

  private void rebuildSdkListAndSelectSdk(final @Nullable Sdk selectedSdk) {
    final String previousSelectedSdkName = getSelectedSdkRaw();
    final List<Object> sdkList = new ArrayList<>();

    if (myShowBCSdk) {
      sdkList.add(myBCSdk);
    }

    final Sdk[] sdks = FlexSdkUtils.getAllSdks();
    for (final Sdk sdk : sdks) {
      if (mySdkFilter.value(sdk)) {
        sdkList.add(sdk);
      }
    }

    if (!sdkList.isEmpty()) {
      // sort by version descending, Flexmojos SDKs - to the end of the list
      sdkList.sort((sdk1, sdk2) -> {
        if (sdk1 == myBCSdk && sdk2 != myBCSdk) return -1;
        if (sdk1 != myBCSdk && sdk2 == myBCSdk) return 1;

        if (sdk1 instanceof Sdk && sdk2 instanceof Sdk) {
          final SdkTypeId type1 = ((Sdk)sdk1).getSdkType();
          final SdkTypeId type2 = ((Sdk)sdk2).getSdkType();

          if (type1 == type2) return -StringUtil.compareVersionNumbers(((Sdk)sdk1).getVersionString(), ((Sdk)sdk2).getVersionString());
          if (type1 == FlexSdkType2.getInstance()) return -1;
          if (type2 == FlexSdkType2.getInstance()) return 1;
        }

        return 0;
      });

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
      @Override
      public void actionPerformed(ActionEvent e) {
        listener.stateChanged();
      }
    });

    getComboBox().addPropertyChangeListener("model", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        listener.stateChanged();
      }
    });
  }

  public @Nullable Sdk getSelectedSdk() {
    final Object selectedItem = getComboBox().getSelectedItem();

    if (selectedItem instanceof BCSdk) {
      return ((BCSdk)selectedItem).mySdk;
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

    if (selectedItem instanceof BCSdk) {
      return BC_SDK_KEY;
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

    if (BC_SDK_KEY.equals(sdkName)) {
      combo.setSelectedItem(myBCSdk);
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
      final List<Object> items = new ArrayList<>();
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

  public void showBCSdk(final boolean showBCSdk) {
    if (myShowBCSdk != showBCSdk) {
      myShowBCSdk = showBCSdk;
      final Object selectedItem = getComboBox().getSelectedItem();
      rebuildSdkListAndSelectSdk(null);
      if (selectedItem instanceof String) {
        setSelectedSdkRaw((String)selectedItem, true);
      }
    }
  }

  public void setBCSdk(final Sdk sdk) {
    if (sdk != myBCSdk.mySdk) {
      myBCSdk.mySdk = sdk;
    }
  }

  private static class NonCommittingWrapper extends ProjectSdksModel {
    private final ProjectSdksModel myOriginal;
    private final JdkListConfigurable myConfigurable;

    NonCommittingWrapper(final ProjectSdksModel original, JdkListConfigurable configurable) {
      myOriginal = original;
      myConfigurable = configurable;
    }

    @Override
    public void apply() throws ConfigurationException {
      apply(null);
    }

    @Override
    public void apply(final @Nullable MasterDetailsComponent configurable) throws ConfigurationException {
      myConfigurable.reset(); // just update the view
    }

    @Override
    public void reset(final @Nullable Project project) {
      // ignore
    }

    @Override
    public void addListener(final @NotNull Listener listener) {
      myOriginal.addListener(listener);
    }

    @Override
    public void removeListener(final @NotNull Listener listener) {
      myOriginal.removeListener(listener);
    }

    @Override
    public @NotNull Listener getMulticaster() {
      return myOriginal.getMulticaster();
    }

    @Override
    public Sdk @NotNull [] getSdks() {
      return myOriginal.getSdks();
    }

    @Override
    public Sdk findSdk(final @NotNull String sdkName) {
      return myOriginal.findSdk(sdkName);
    }

    @Override
    public void disposeUIResources() {
      // ignore
    }

    @Override
    public @NotNull HashMap<Sdk, Sdk> getProjectSdks() {
      return myOriginal.getProjectSdks();
    }

    @Override
    public boolean isModified() {
      return myOriginal.isModified();
    }

    @Override
    public void removeSdk(final @NotNull Sdk editableObject) {
      myOriginal.removeSdk(editableObject);
    }

    @Override
    public void createAddActions(@Nullable Project project,
                                 @NotNull DefaultActionGroup group,
                                 @NotNull JComponent parent,
                                 @NotNull java.util.function.Consumer<? super Sdk> updateTree,
                                 @Nullable Predicate<? super SdkTypeId> filter) {
      myOriginal.createAddActions(project, group, parent, updateTree, filter);
    }

    @Override
    public void doAdd(@NotNull JComponent parent, final @NotNull SdkType type, final @NotNull Consumer<? super Sdk> updateTree) {
      myOriginal.doAdd(parent, type, updateTree);
    }

    @Override
    public void addSdk(final @NotNull Sdk sdk) {
      myOriginal.addSdk(sdk);
    }

    @Override
    public void doAdd(final @NotNull Sdk newSdk, final @Nullable java.util.function.Consumer<? super Sdk> updateTree) {
      myOriginal.doAdd(newSdk, updateTree);
    }

    @Override
    public Sdk findSdk(final @Nullable Sdk modelJdk) {
      return myOriginal.findSdk(modelJdk);
    }

    @Override
    public Sdk getProjectSdk() {
      return myOriginal.getProjectSdk();
    }

    @Override
    public void setProjectSdk(final Sdk projectSdk) {
      myOriginal.setProjectSdk(projectSdk);
    }

    @Override
    public boolean isInitialized() {
      return myOriginal.isInitialized();
    }
  }
}
