package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.util.Consumer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author ksafonov
 */
public class FlexSdkPanel implements Disposable {

  private ComboboxWithBrowseButton myCombo;
  private JButton myEditButton;
  private JLabel myInfoLabel;
  private JPanel myContentPane;
  private JLabel mySdkLabel;

  private final EventDispatcher<ChangeListener> myEventDispatcher;
  private boolean myMute;
  private final FlexProjectConfigurationEditor myConfigEditor;

  public FlexSdkPanel(FlexProjectConfigurationEditor configEditor) {
    myConfigEditor = configEditor;
    myEventDispatcher = EventDispatcher.create(ChangeListener.class);
    mySdkLabel.setLabelFor(myCombo.getComboBox());
    myInfoLabel.setIcon(UIUtil.getBalloonWarningIcon());

    myConfigEditor.addSdkListListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        rebuildComboModel();
      }
    }, this);

    //myCombo.addActionListener(new ActionListener() {
    //  @Override
    //  public void actionPerformed(ActionEvent e) {
    //    SdkConfigurationUtil.selectSdkHome(FlexIdeUtils.getSdkType(), new Consumer<String>() {
    //      @Override
    //      public void consume(String homePath) {
    //        FlexSdk sdk = myConfigEditor.findOrCreateSdk(homePath);// will update the model through listener
    //        setCurrentSdk(Factory.createSdkEntry(sdk.(), sdk.getHomePath()));
    //        myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(FlexSdkPanel.this));
    //      }
    //    });
    //  }
    //});

    myCombo.getChildComponent().addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        Object selectedItem = myCombo.getComboBox().getSelectedItem();
        //myConfigEditor.setSdkLibraryUsed(FlexSdkPanel.this, selectedItem instanceof LibraryEx ? (LibraryEx)selectedItem : null);
        //if (myMute) {
        //  return;
        //}
        comboItemChanged();
      }
    });

    myCombo.getChildComponent().setRenderer(new ListCellRendererWrapper(myCombo.getChildComponent().getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
          String homePath = value instanceof Sdk ? ((Sdk)value).getName() : (String)value;
          setText(FileUtil.toSystemDependentName(homePath));
        }
        else {
          setText("(none)");
        }
      }
    });

    myEditButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        editSdk();
      }
    });
  }

  public JPanel getContentPane() {
    return myContentPane;
  }

  public ComboboxWithBrowseButton getCombo() {
    return myCombo;
  }

  private void comboItemChanged() {
    updateControls();
    myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
  }

  private void updateControls() {
    Object selection = myCombo.getComboBox().getSelectedItem();
    if (selection instanceof Sdk) {
      myEditButton.setEnabled(true);
      myInfoLabel.setVisible(false);
    }
    else if (selection instanceof String) {
      myEditButton.setEnabled(false);
      myInfoLabel.setVisible(true);
      myInfoLabel.setText("Unknown Flex SDK");
    }
    else {
      myEditButton.setEnabled(false);
      myInfoLabel.setVisible(true);
      myInfoLabel.setText("Flex SDK not specified");
    }
  }

  private void rebuildComboModel() {
    Sdk matchingSdk = null;
    myMute = true;
    try {
      Object selection = myCombo.getComboBox().getSelectedItem();
      Sdk[] sdks = myConfigEditor.getFlexSdks();
      DefaultComboBoxModel model = new DefaultComboBoxModel(sdks);
      myCombo.getComboBox().setModel(model);
      if (selection instanceof String) {
        final String name = (String)selection;
        matchingSdk = ContainerUtil.find(sdks, new Condition<Sdk>() {
          @Override
          public boolean value(Sdk sdk) {
            return name.equals(sdk.getName());
          }
        });
        if (matchingSdk != null) {
          selection = matchingSdk; // if one has added matching SDK, switch to it (configurable will be modified anyway, since SDK was added)
        }
      }
      if (selection != null && !(selection instanceof Library)) {
        model.addElement(selection);
      }
      myCombo.getComboBox().setSelectedItem(selection);
    }
    finally {
      myMute = false;
    }

    comboItemChanged();
    if (matchingSdk != null) {
      //myConfigEditor.setSdkLibraryUsed(this, matchingSdk);
    }
  }

  private void editSdk() {
    throw new UnsupportedOperationException("TODO navigate to SDKs page");
  }

  public void addListener(ChangeListener listener, Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  public void setCurrentSdk(@Nullable SdkEntry sdkEntry) {
    myMute = true;
    try {
      if (sdkEntry != null) {
        Sdk sdk = myConfigEditor.findSdk(sdkEntry.getName());
        if (sdk != null) {
          myCombo.getComboBox().setSelectedItem(sdk);
        }
        else {
          DefaultComboBoxModel model = (DefaultComboBoxModel)myCombo.getComboBox().getModel();
          boolean found = false;
          String name  = sdkEntry.getName();
          for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(name)) {
              found = true;
              break;
            }
          }
          if (!found) {
            model.addElement(name);
          }
          myCombo.getComboBox().setSelectedItem(name);
        }
      }
      updateControls(); // force update
    }
    finally {
      myMute = false;
    }
  }

  @Nullable
  public Sdk getCurrentSdk() {
    Object selectedItem = myCombo.getComboBox().getSelectedItem();
    return selectedItem instanceof Sdk ? (Sdk)selectedItem : null;
  }

  @Nullable
  public String getCurrentSdkName() {
    Object selectedItem = myCombo.getComboBox().getSelectedItem();
    if (selectedItem instanceof Library) {
      return FlexProjectRootsUtil.getSdkLibraryId((Library)selectedItem);
    }
    else if (selectedItem instanceof String) {
      return (String)selectedItem;
    }
    return null;
  }

  @Override
  public void dispose() {
    //myConfigEditor.setSdkLibraryUsed(this, null);
  }

  public void reset() {
    rebuildComboModel();
  }

  public void setSdkLabelVisible(final boolean visible) {
    mySdkLabel.setVisible(visible);
  }

  public void setEditButtonVisible(final boolean visible) {
    myEditButton.setVisible(visible);
  }

  public void setNotNullCurrentSdkIfPossible() {
    final JComboBox comboBox = myCombo.getComboBox();
    if (comboBox.getSelectedItem() == null && comboBox.getModel().getSize() > 0) {
      comboBox.setSelectedItem(comboBox.getModel().getElementAt(0));
    }
  }

  private void createUIComponents() {
    myCombo = new ComboboxWithBrowseButton(new ComboBox(10));
  }
}
