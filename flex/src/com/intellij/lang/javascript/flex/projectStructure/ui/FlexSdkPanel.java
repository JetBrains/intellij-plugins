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
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author ksafonov
 */
public class FlexSdkPanel implements Disposable {

  private ComboboxWithBrowseButton myCombo;
  private JButton myEditButton;
  private JLabel myInfoLabel;
  @SuppressWarnings("UnusedDeclaration") private JPanel myContentPane;
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

    myCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SdkConfigurationUtil.selectSdkHome(FlexIdeUtils.getSdkType(), new Consumer<String>() {
          @Override
          public void consume(String homePath) {
            FlexSdk sdk = myConfigEditor.findOrCreateSdk(homePath);// will update the model through listener
            setCurrentSdk(Factory.createSdkEntry(sdk.getLibraryId(), sdk.getHomePath()));
            myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(FlexSdkPanel.this));
          }
        });
      }
    });

    myCombo.getChildComponent().addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        Object selectedItem = myCombo.getComboBox().getSelectedItem();
        myConfigEditor.setSdkLibraryUsed(FlexSdkPanel.this, selectedItem instanceof LibraryEx ? (LibraryEx)selectedItem : null);
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
          String homePath = value instanceof Library ? FlexSdk.getHomePath((Library)value) : ((Pair<String, String>)value).second;
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
    if (selection instanceof Library) {
      myEditButton.setEnabled(true);
      myInfoLabel.setVisible(false);
    }
    else if (selection instanceof Pair) {
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
    LibraryEx matchingLibrary = null;
    myMute = true;
    try {
      Object selection = myCombo.getComboBox().getSelectedItem();
      LibraryEx[] libraries = myConfigEditor.getSdksLibraries();
      DefaultComboBoxModel model = new DefaultComboBoxModel(libraries);
      myCombo.getComboBox().setModel(model);
      if (selection instanceof Pair) {
        final String homePath = ((Pair<String, String>)selection).second;
        matchingLibrary = ContainerUtil.find(libraries, new Condition<LibraryEx>() {
          @Override
          public boolean value(LibraryEx library) {
            return FlexSdk.getHomePath(library).equals(homePath);
          }
        });
        if (matchingLibrary != null) {
          selection = matchingLibrary; // if one has added matching SDK, switch to it (configurable will be modified anyway, since SDK was added)
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
    if (matchingLibrary != null) {
      comboItemChanged();
      myConfigEditor.setSdkLibraryUsed(this, matchingLibrary);
    }
  }

  private void editSdk() {
    FlexSdk currentSdk = getCurrentSdk();
    if (currentSdk == null) {
      return;
    }

    Library library = currentSdk.getLibrary();
    LibraryEditor libraryEditor = myConfigEditor.getSdkLibraryEditor(library);
    Project project = myConfigEditor.getProject();
    new EditFlexSdkDialog(project, libraryEditor, myContentPane).show();
  }

  public void addListener(ChangeListener listener, Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  public void setCurrentSdk(@Nullable SdkEntry sdkEntry) {
    myMute = true;
    try {
      if (sdkEntry != null) {
        FlexSdk sdk = myConfigEditor.findSdk(sdkEntry.getLibraryId());
        if (sdk != null) {
          myCombo.getComboBox().setSelectedItem(sdk.getLibrary());
        }
        else {
          DefaultComboBoxModel model = (DefaultComboBoxModel)myCombo.getComboBox().getModel();
          boolean found = false;
          Pair<String, String> idAndHome = Pair.create(sdkEntry.getLibraryId(), sdkEntry.getHomePath());
          for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(idAndHome)) {
              found = true;
              break;
            }
          }
          if (!found) {
            model.addElement(idAndHome);
          }
          myCombo.getComboBox().setSelectedItem(idAndHome);
        }
      }
      updateControls(); // force update
    }
    finally {
      myMute = false;
    }
  }

  @Nullable
  public FlexSdk getCurrentSdk() {
    Object selectedItem = myCombo.getComboBox().getSelectedItem();
    if (selectedItem instanceof Library) {
      return new FlexSdk((Library)selectedItem);
    }
    return null;
  }

  @Nullable
  public String getCurrentSdkId() {
    Object selectedItem = myCombo.getComboBox().getSelectedItem();
    if (selectedItem instanceof Library) {
      return FlexProjectRootsUtil.getSdkLibraryId((Library)selectedItem);
    }
    else if (selectedItem instanceof Pair) {
      return ((Pair<String, String>)selectedItem).first;
    }
    return null;
  }

  @Override
  public void dispose() {
    myConfigEditor.setSdkLibraryUsed(this, null);
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
}
