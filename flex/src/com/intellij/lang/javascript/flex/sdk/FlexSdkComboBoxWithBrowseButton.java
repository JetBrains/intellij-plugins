package com.intellij.lang.javascript.flex.sdk;

import com.intellij.ide.DataManager;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class FlexSdkComboBoxWithBrowseButton extends ComboboxWithBrowseButton {
  public interface Listener {
    void stateChanged();
  }

  // special combobox item that stands for sdk specified by module
  private static class ModuleSdk {
    private Sdk mySdk;
  }

  public static final Condition<Sdk> FLEX_RELATED_EXCEPT_FLEXMOJOS = new Condition<Sdk>() {
    public boolean value(final Sdk sdk) {
      return sdk != null &&
             sdk.getSdkType() instanceof IFlexSdkType &&
             ((IFlexSdkType)sdk.getSdkType()).getSubtype() != IFlexSdkType.Subtype.Flexmojos;
    }
  };

  public static final Condition<Sdk> FLEX_RELATED_SDK = new Condition<Sdk>() {
    public boolean value(final Sdk sdk) {
      return sdk != null && (sdk.getSdkType() instanceof IFlexSdkType);
    }
  };

  public static final String MODULE_SDK_KEY = "Module SDK";

  private final Condition<Sdk> mySdkEvaluator;

  private ModuleSdk myModuleSdk = new ModuleSdk();
  private boolean myShowModuleSdk = false;

  public FlexSdkComboBoxWithBrowseButton() {
    this(FLEX_RELATED_EXCEPT_FLEXMOJOS);
  }

  public FlexSdkComboBoxWithBrowseButton(final Condition<Sdk> sdkEvaluator) {
    this.mySdkEvaluator = sdkEvaluator;
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
        final ProjectJdksEditor editor = new ProjectJdksEditor(null, project, FlexSdkComboBoxWithBrowseButton.this);
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

    final Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
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
}
