package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdServerSettings;
import com.google.jstestdriver.idea.server.JstdServerSettingsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PortField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

public class JstdServerSettingsTab {

  private static final int GAP = 8;

  private final PortField myPortField;
  private final JSpinner myBrowserTimeoutSpinner;
  private final ComboBox myRunnerModeComboBox;
  private final TabInfo myTabInfo;
  private boolean myTrackChanges = true;

  public JstdServerSettingsTab(@NotNull Disposable parentDisposable) {
    myPortField = new PortField();
    myBrowserTimeoutSpinner = createBrowserTimeoutSpinner();
    myRunnerModeComboBox = new ComboBox(JstdServerSettings.RunnerMode.values());
    JPanel form = FormBuilder.createFormBuilder()
      .setAlignLabelOnRight(true)
      .addLabeledComponent("&Port:", myPortField)
      .addLabeledComponent("&Browser timeout:", addMillisDescription(myBrowserTimeoutSpinner))
      .addLabeledComponent("&Runner mode:", myRunnerModeComboBox)
      .getPanel();
    JPanel result = createResultPanel(form);
    result.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
    myTabInfo = new TabInfo(new JBScrollPane(result));
    myTabInfo.setText("Settings");
    setSettings(JstdServerSettingsManager.loadSettings());
    JstdServerSettingsManager.addListener(new JstdServerSettingsManager.Listener() {
      @Override
      public void onChanged(@NotNull JstdServerSettings settings) {
        setSettings(settings);
      }
    }, parentDisposable);
    Disposer.register(parentDisposable, new Disposable() {
      @Override
      public void dispose() {
        saveSettings();
      }
    });
    listenForChanges();
  }

  @NotNull
  public TabInfo getTabInfo() {
    return myTabInfo;
  }

  private void listenForChanges() {
    myPortField.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        update();
      }
    });
    myBrowserTimeoutSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        update();
      }
    });
    myRunnerModeComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        update();
      }
    });
  }

  private void update() {
    if (myTrackChanges) {
      JstdServerSettings settings = getSettings();
      JstdServerSettingsManager.saveSettings(settings);
    }
  }

  @NotNull
  private static JPanel createResultPanel(@NotNull JPanel form) {
    JPanel p = new JPanel(new BorderLayout(0, 0));
    p.add(form, BorderLayout.NORTH);
    p.add(new JPanel(), BorderLayout.CENTER);
    p.add(createHyperlink(), BorderLayout.SOUTH);
    return p;
  }

  @NotNull
  private static JComponent addMillisDescription(@NotNull JSpinner spinner) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel.add(spinner);
    panel.add(Box.createHorizontalStrut(10));
    panel.add(new JLabel("ms"));
    return panel;
  }

  @NotNull
  private static JSpinner createBrowserTimeoutSpinner() {
    JSpinner spinner = new JSpinner();
    spinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
    JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(spinner, "#");
    spinner.setEditor(numberEditor);
    numberEditor.getTextField().setColumns(5);
    return spinner;
  }

  @NotNull
  private static JComponent createHyperlink() {
    return SwingHelper.createWebHyperlink(
      "JsTestDriver Server Options Help",
      "https://code.google.com/p/js-test-driver/wiki/CommandLineFlags"
    );
  }

  @NotNull
  private JstdServerSettings getSettings() {
    try {
      myPortField.commitEdit();
      myBrowserTimeoutSpinner.commitEdit();
    }
    catch (ParseException ignored) {
    }
    int browserTimeout = ((SpinnerNumberModel)myBrowserTimeoutSpinner.getModel()).getNumber().intValue();
    JstdServerSettings.RunnerMode runnerMode = ObjectUtils.tryCast(myRunnerModeComboBox.getSelectedItem(), JstdServerSettings.RunnerMode.class);
    runnerMode = ObjectUtils.notNull(runnerMode, JstdServerSettings.RunnerMode.QUIET);
    return new JstdServerSettings.Builder()
      .setPort(myPortField.getNumber())
      .setBrowserTimeoutMillis(browserTimeout)
      .setRunnerMode(runnerMode)
      .build();
  }

  private void setSettings(@NotNull JstdServerSettings settings) {
    myTrackChanges = false;
    try {
      myPortField.setNumber(settings.getPort());
      myBrowserTimeoutSpinner.setValue(settings.getBrowserTimeoutMillis());
      myRunnerModeComboBox.setSelectedItem(settings.getRunnerMode());
    }
    finally {
      myTrackChanges = true;
    }
  }

  public void saveSettings() {
    JstdServerSettings settings = getSettings();
    JstdServerSettingsManager.saveSettings(settings);
  }
}
