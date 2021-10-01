package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class BCCombo extends JComboBox {

  private final Project myProject;

  private FlexBuildConfiguration[] myAllConfigs;
  private boolean mySingleModuleProject;
  private Map<FlexBuildConfiguration, Module> myBCToModuleMap = new THashMap<>();

  public BCCombo(final Project project) {
    myProject = project;
    initCombo();
  }

  private void initCombo() {
    setMinimumSize(new Dimension(150, getMinimumSize().height));

    final Collection<FlexBuildConfiguration> allConfigs = new ArrayList<>();

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    mySingleModuleProject = modules.length == 1;
    for (final Module module : modules) {
      if (ModuleType.get(module) instanceof FlexModuleType) {
        for (final FlexBuildConfiguration config : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
          allConfigs.add(config);
          myBCToModuleMap.put(config, module);
        }
      }
    }
    myAllConfigs = allConfigs.toArray(new FlexBuildConfiguration[0]);

    setRenderer(new ColoredListCellRenderer<>() {
      @Override
      protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof Pair) {
          final String moduleName = (String)((Pair<?, ?>)value).first;
          final String configName = (String)((Pair<?, ?>)value).second;
          //setIcon(PlatformIcons.ERROR_INTRODUCTION_ICON);
          if (moduleName.isEmpty() || configName.isEmpty()) {
            append("[none]", SimpleTextAttributes.ERROR_ATTRIBUTES);
          }
          else {
            BCUtils.renderMissingBuildConfiguration(configName, moduleName).appendToComponent(this);
          }
        }
        else {
          assert value instanceof FlexBuildConfiguration : value;
          final FlexBuildConfiguration bc = (FlexBuildConfiguration)value;
          setIcon(bc.getIcon());
          BCUtils.renderBuildConfiguration(bc, mySingleModuleProject ? null : myBCToModuleMap.get(bc).getName()).appendToComponent(this);
        }
      }
    });

    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        // remove invalid entry
        final Object selectedItem = getSelectedItem();
        final Object firstItem = getItemAt(0);
        if (selectedItem instanceof FlexBuildConfiguration && !(firstItem instanceof FlexBuildConfiguration)) {
          setModel(new DefaultComboBoxModel(myAllConfigs));
          setSelectedItem(selectedItem);
        }
      }
    });
  }

  public void resetFrom(final BCBasedRunnerParameters params) {
    final Module module = ModuleManager.getInstance(myProject).findModuleByName(params.getModuleName());
    final FlexBuildConfiguration config = module != null && (ModuleType.get(module) instanceof FlexModuleType)
                                             ? FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(params.getBCName())
                                             : null;

    if (config == null) {
      final Object[] model = new Object[myAllConfigs.length + 1];
      model[0] = Pair.create(params.getModuleName(), params.getBCName());
      System.arraycopy(myAllConfigs, 0, model, 1, myAllConfigs.length);
      setModel(new DefaultComboBoxModel(model));
      setSelectedIndex(0);
    }
    else {
      setModel(new DefaultComboBoxModel(myAllConfigs));
      setSelectedItem(config);
    }
  }

  public void applyTo(final BCBasedRunnerParameters params) {
    final Object selectedItem = getSelectedItem();

    if (selectedItem instanceof Pair) {
      params.setModuleName((String)((Pair<?, ?>)selectedItem).first);
      params.setBCName((String)((Pair<?, ?>)selectedItem).second);
    }
    else {
      assert selectedItem instanceof FlexBuildConfiguration : selectedItem;
      params.setModuleName(myBCToModuleMap.get(((FlexBuildConfiguration)selectedItem)).getName());
      params.setBCName(((FlexBuildConfiguration)selectedItem).getName());
    }
  }

  public void dispose() {
    myAllConfigs = null;
    myBCToModuleMap = null;
  }

  @Nullable
  public FlexBuildConfiguration getBC() {
    final Object selectedItem = getSelectedItem();
    return selectedItem instanceof FlexBuildConfiguration ? (FlexBuildConfiguration)selectedItem : null;
  }

  @Nullable
  public Module getModule() {
    final Object selectedItem = getSelectedItem();
    return selectedItem instanceof FlexBuildConfiguration ? myBCToModuleMap.get((FlexBuildConfiguration)selectedItem) : null;
  }

  private static String getPresentableText(final String moduleName, final String configName, final boolean singleModuleProject) {
    if (moduleName.isEmpty() || configName.isEmpty()) return "[none]";
    return singleModuleProject ? configName : configName + " (" + moduleName + ")";
  }
}
