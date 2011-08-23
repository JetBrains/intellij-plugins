package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.RecentsManager;
import com.intellij.ui.TextAccessor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.EventDispatcher;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ksafonov
 */
public class SdkPathCombo extends ComboboxWithBrowseButton implements TextAccessor {

  private final Project myProject;
  private final String myHistoryKey;
  private final EventDispatcher<ChangeListener> myEventDispatcher = EventDispatcher.create(ChangeListener.class);

  public SdkPathCombo(Project project, final SdkType sdkType, String historyKey) {
    myProject = project;
    myHistoryKey = historyKey;
    getChildComponent().setEditable(true);
    setText("");
    getChildComponent().setEditor(new DelegatingComboBoxEditor(getChildComponent().getEditor()) {
      JTextField field = null;

      @Override
      public Component getEditorComponent() {
        final Component c = super.getEditorComponent();
        if (c instanceof JTextField && field != c) {
          field = (JTextField)c;
          // TODO until kirillk fixes path completion in combobox
          //FileChooserFactory.getInstance().installFileCompletion(field, sdkType.getHomeChooserDescriptor(), false, null);
          field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
              myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(SdkPathCombo.this));
            }
          });
        }
        return c;
      }
    });

    getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SdkConfigurationUtil.selectSdkHome(sdkType, new Consumer<String>() {
          @Override
          public void consume(String s) {
            setText(FileUtil.toSystemDependentName(s));
          }
        });
      }
    });

    final List<String> recentEntries = RecentsManager.getInstance(project).getRecentEntries(historyKey);
    if (recentEntries != null) {
      setHistory(ArrayUtil.toStringArray(recentEntries));
    }

    getChildComponent().addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(SdkPathCombo.this));
        }
      }
    });
  }

  public void addListener(ChangeListener listener) {
    myEventDispatcher.addListener(listener);
  }

  public String getText() {
    // TODO this is probably not the best way to get text if combo being edited
    if (ArrayUtil.contains(getChildComponent().getEditor().getEditorComponent(), getChildComponent().getComponents())) {
      return String.valueOf(getChildComponent().getEditor().getItem());
    }
    else {
      return String.valueOf(getChildComponent().getSelectedItem());
    }
  }

  public void setText(String text) {
    getChildComponent().setSelectedItem(text);
    prependHistoryItem(text);
  }

  private void setHistory(final String[] history) {
    getChildComponent().setModel(new DefaultComboBoxModel(history));
  }

  public void saveHistory() {
    RecentsManager.getInstance(myProject).registerRecentEntry(myHistoryKey, getText());
  }

  private void prependHistoryItem(String item) {
    ArrayList<Object> objects = new ArrayList<Object>();
    objects.add(item);
    int count = getChildComponent().getItemCount();
    for (int i = 0; i < count; i++) {
      final Object itemAt = getChildComponent().getItemAt(i);
      if (!item.equals(itemAt)) {
        objects.add(itemAt);
      }
    }
    getChildComponent().setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(objects)));
  }

  private static class DelegatingComboBoxEditor implements ComboBoxEditor {
    private final ComboBoxEditor myDelegate;

    public DelegatingComboBoxEditor(ComboBoxEditor delegate) {
      myDelegate = delegate;
    }

    @Override
    public Component getEditorComponent() {
      return myDelegate.getEditorComponent();
    }

    @Override
    public void setItem(Object anObject) {
      myDelegate.setItem(anObject);
    }

    @Override
    public Object getItem() {
      return myDelegate.getItem();
    }

    @Override
    public void selectAll() {
      myDelegate.selectAll();
    }

    @Override
    public void addActionListener(ActionListener l) {
      myDelegate.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
      myDelegate.removeActionListener(l);
    }
  }
}
