package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckBoxList;
import com.intellij.util.ui.UIUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.List;

public class LocalesDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JLabel myLabel;
  private CheckBoxList myCheckBoxList;

  private final Collection<String> mySdkLocales;

  public LocalesDialog(final Project project, final @NotNull Sdk sdk, final Collection<String> selectedLocales) {
    super(project);

    mySdkLocales = getAvailableLocales(sdk);

    setTitle(CompilerOptionInfo.getOptionInfo("compiler.locale").DISPLAY_NAME);
    myLabel.setText(FlexBundle.message("locales.dialog.label", sdk.getName()));

    final Map<String, Boolean> map = new TreeMap<String, Boolean>();
    for (String locale : mySdkLocales) {
      map.put(locale, false);
    }
    for (String locale : selectedLocales) {
      map.put(locale, true);
    }
    myCheckBoxList.setStringItems(map);

    init();
  }

  private static Collection<String> getAvailableLocales(final Sdk sdk) {
    final Collection<String> result = new THashSet<String>();
    final File localeDir = new File(sdk.getHomePath() + "/frameworks/locale");
    if (localeDir.isDirectory()) {
      //noinspection ConstantConditions
      for (File subdir : localeDir.listFiles()) {
        if (subdir.isDirectory() && containsSwc(subdir)) {
          result.add(subdir.getName());
        }
      }
    }
    return result;
  }

  private static boolean containsSwc(final File dir) {
    return dir.list(new FilenameFilter() {
      public boolean accept(final File dir, final String name) {
        return name.toLowerCase().endsWith(".swc");
      }
    }).length > 0;
  }

  private void createUIComponents() {
    myCheckBoxList = new CheckBoxList() {
      protected void adjustRendering(final JCheckBox checkBox, final boolean selected, final boolean hasFocus) {
        final String locale = checkBox.getText();
        checkBox.setForeground(mySdkLocales.contains(locale) ? UIUtil.getListForeground(selected) : Color.red);
      }
    };
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public List<String> getLocales() {
    final List<String> result = new ArrayList<String>();

    final ListModel model = myCheckBoxList.getModel();
    for (int i = 0; i < model.getSize(); i++) {
      final JCheckBox checkBox = (JCheckBox)model.getElementAt(i);
      if (checkBox.isSelected()) {
        result.add(checkBox.getText());
      }
    }

    return result;
  }
}
