package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.util.Consumer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author ksafonov
 */
public class FlexSdkPanel implements Disposable {

  private final FlexSdksModifiableModel myModifiableModel;

  private ComboboxWithBrowseButton myCombo;
  private JButton myEditButton;
  private JLabel myInfoLabel;
  @SuppressWarnings("UnusedDeclaration") private JPanel myContentPane;
  private JLabel mySdkLabel;

  private final EventDispatcher<ChangeListener> myEventDispatcher;
  private boolean myMute;

  public FlexSdkPanel(FlexSdksModifiableModel modifiableModel) {
    myModifiableModel = modifiableModel;
    myEventDispatcher = EventDispatcher.create(ChangeListener.class);
    mySdkLabel.setLabelFor(myCombo.getComboBox());
    myInfoLabel.setIcon(UIUtil.getBalloonWarningIcon());

    myModifiableModel.addSdkListListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        rebuildComboModel();
      }
    }, this);

    myCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SdkConfigurationUtil.selectSdkHome(FlexIdeUtils.getSdkType(), new Consumer<String>() {
          @Override
          public void consume(String homePath) {
            myModifiableModel.findOrCreateSdk(homePath); // will update the model through listener
            setCurrentHomePath(homePath);
            myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(FlexSdkPanel.this));
          }
        });
      }
    });

    myCombo.getChildComponent().addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        myModifiableModel.setUsed(FlexSdkPanel.this, (String)myCombo.getComboBox().getSelectedItem());
        if (myMute) {
          return;
        }
        comboItemChanged();
      }
    });

    myCombo.getChildComponent().setRenderer(new ListCellRendererWrapper(myCombo.getChildComponent().getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
          String homePath = (String)value;
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

  private void comboItemChanged() {
    updateControls();
    myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
  }

  private void updateControls() {
    Object selection = myCombo.getComboBox().getSelectedItem();
    if (selection != null) {
      String homePath = (String)selection;
      if (FlexIdeUtils.getSdkType().isValidSdkHome(homePath)) {
        myEditButton.setEnabled(true);
        myInfoLabel.setVisible(false);
      }
      else {
        myEditButton.setEnabled(false);
        myInfoLabel.setVisible(true);
        myInfoLabel.setText("Flex SDK not found or corrupted");
      }
    }
    else {
      myEditButton.setEnabled(false);
      myInfoLabel.setVisible(true);
      myInfoLabel.setText("Flex SDK not specified");
    }
  }

  private void rebuildComboModel() {
    myMute = true;
    try {
      Object selection = myCombo.getComboBox().getSelectedItem();
      myCombo.getComboBox().setModel(new DefaultComboBoxModel(myModifiableModel.getHomePaths()));
      myCombo.getComboBox().setSelectedItem(selection);
    }
    finally {
      myMute = false;
    }
  }

  private void editSdk() {
  }

  public void addListener(ChangeListener listener, Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  public void setCurrentHomePath(@Nullable String sdkHomePath) {
    myMute = true;
    try {
      myCombo.getComboBox().setSelectedItem(sdkHomePath);
      updateControls(); // force update
    }
    finally {
      myMute = false;
    }
  }

  @Nullable
  public FlexSdk getCurrentSdk() {
    String sdkHome = (String)myCombo.getComboBox().getSelectedItem();
    if (sdkHome != null) {
      FlexSdk sdk = myModifiableModel.findSdk(sdkHome);
      if (sdk != null && sdk.isValid()) {
        return sdk;
      }
    }
    return null;
  }

  @Override
  public void dispose() {
    myModifiableModel.setUsed(this, null);
  }

  public void reset() {
    rebuildComboModel();
  }
}
