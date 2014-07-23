package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PhoneGapProjectConfigurable.java
 *
 * Created by Masahiro Suzuka on 2014/05/30.
 */
public class PhoneGapProjectConfigurable implements Configurable {

  private JPanel component;
  private JTextField plugmanExecutableField;
  private JButton installPhoneGapCordovaPluginButton;
  private JTable pluginsTable;
  private MyTableModel myTableModel;

  public PhoneGapProjectConfigurable() {
    myTableModel = new MyTableModel(new String[] {"Installed", "plugin_id"}, 0);
    pluginsTable.setModel(myTableModel);
    new PlugmanSearchThread().run();
    installPhoneGapCordovaPluginButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        // TODO
        // Kick 'plugman --install plugin_id'
      }
    });
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "PhoneGap/Cordova";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return component;
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {

  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {

  }

  private class PlugmanInstallThread implements Runnable {
    @Override
    public void run() {

    }
  }

  private class PlugmanSearchThread implements Runnable {
    @Override
    public void run() {
      try {
        StringBuilder out = new StringBuilder();
        StringBuilder error = new StringBuilder();
        //final GeneralCommandLine generalCommandLine = new GeneralCommandLine(plugmanExecutableField.getText(), "search");
        final GeneralCommandLine generalCommandLine = new GeneralCommandLine("/usr/local/bin/plugman", "search");
        OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine);
        OutputListener outputListener = new OutputListener(out, error);
        osProcessHandler.addProcessListener(outputListener);
        osProcessHandler.startNotify();
        generalCommandLine.createProcess();
        osProcessHandler.waitFor();

        String plugmanSearchResult = outputListener.getOutput().getStdout();

        String[] splitedPlugmanSearchResult = plugmanSearchResult.split("\n");
        for (String string : splitedPlugmanSearchResult) {
          // Parse plugin-id form string
          // ^[0-9a-z\.]+" matches reverce-domain like "org.apache.cordova.media"
          Matcher matcher = Pattern.compile("^[0-9a-z\\.]+").matcher(string);
          if (matcher.find()) {

            String phonegapPluginId = matcher.group();
            boolean installed = false;

            // TODO
            // Parse project_root/plugins.xml and get id of already-installed-plugin
            // if phonegap-plugins was already installed set true in installed
            // if plugins.xml is missing it means 'no plugin was installed'

            myTableModel.addRow(new Object[]{installed, phonegapPluginId});
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private class MyTableModel extends DefaultTableModel {
    public MyTableModel(String[] columnNames, int rowNum) {
      super(columnNames, rowNum);
    }

    @Override
    public Class getColumnClass(int col) {
      switch (col) {
        case 0:
          return Boolean.class;
        case 1:
          return String.class;
      }
      return Object.class;
    }
  }
}
