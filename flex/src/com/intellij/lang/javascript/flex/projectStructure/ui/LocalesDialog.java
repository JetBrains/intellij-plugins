package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.util.Function;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.*;

public final class LocalesDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JLabel myLabel;
  private CheckBoxList<String> myCheckBoxList;

  private final Collection<String> mySdkLocales;

  public LocalesDialog(final Project project, final @NotNull Sdk sdk, final Collection<String> selectedLocales) {
    super(project);

    mySdkLocales = getAvailableLocales(sdk);

    setTitle(CompilerOptionInfo.getOptionInfo("compiler.locale").DISPLAY_NAME);
    myLabel.setText(FlexBundle.message("locales.dialog.label", sdk.getName()));

    final Map<String, Boolean> map = new TreeMap<>();
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
    final Collection<String> result = new HashSet<>();
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
    return dir.listFiles(FileFilters.withExtension("swc")).length > 0;
  }

  @Override
  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return myCheckBoxList;
  }

  private void createUIComponents() {
    myCheckBoxList = new CheckBoxList<>() {
      @Override
      protected JComponent adjustRendering(JComponent rootComponent,
                                           final JCheckBox checkBox,
                                           int index,
                                           final boolean selected,
                                           final boolean hasFocus) {
        final String locale = checkBox.getText();
        checkBox.setForeground(mySdkLocales.contains(locale) ? UIUtil.getListForeground(selected) : JBColor.RED);
        return rootComponent;
      }
    };

    myCheckBoxList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    ListSpeedSearch.installOn(myCheckBoxList, (Function<Object, String>)o -> ((JCheckBox)o).getText());
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public List<String> getLocales() {
    final List<String> result = new ArrayList<>();

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
