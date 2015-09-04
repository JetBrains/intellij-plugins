package org.jetbrains.plugins.ruby.motion.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.SpeedSearchBase;
import com.intellij.util.NullableConsumer;
import com.intellij.util.PlatformUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.TabbedSettingsEditorTab;
import org.jetbrains.plugins.ruby.remote.RubyRemoteInterpreterManager;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.jruby.JRubySdkType;
import org.jetbrains.plugins.ruby.ruby.ui.RubySdkRenderer;
import org.jetbrains.plugins.ruby.utils.RubyUIUtil;
import org.jetbrains.plugins.ruby.version.management.rbenv.gemsets.RbenvGemsetManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionGeneratorTab extends TabbedSettingsEditorTab {
  private static final String ADD_RUBY_SDK = "Add Ruby SDK...";
  private static final int COMBO_WIDTH = PlatformUtils.isRubyMine() ? 320 : 150;

  public static final Condition<Sdk> IS_PURE_RBENV_SDK = new Condition<Sdk>() {
    @Override
    public boolean value(Sdk sdk) {
      return RbenvGemsetManager.getInstance().getRbenvGemset(sdk) == null;
    }
  };

  private final RubyMotionSettingsHolder mySettingsHolder;
  private ComboBox myRubyInterpreterComboBox;
  private JPanel myContentPane;
  private ComboBox myProjectType;
  private JCheckBox myUseCalabash;
  private JLabel myRubyLabel;
  private JLabel myProjectTypeLabel;

  public RubyMotionGeneratorTab(final RubyMotionSettingsHolder settingsHolder) {
    mySettingsHolder = settingsHolder;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return RBundle.message("ruby.motion.wizard.tab.project.generator.title");
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    myRubyLabel.setDisplayedMnemonic('S');
    myRubyInterpreterComboBox.setRenderer(new RubySdkRenderer(myRubyInterpreterComboBox.getRenderer(), true, true));
    refreshSdkList();
    if (mySettingsHolder.getSdk() != null) {
      myRubyInterpreterComboBox.setSelectedItem(mySettingsHolder.getSdk());
    }
    myRubyInterpreterComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!SpeedSearchBase.hasActiveSpeedSearch(myRubyInterpreterComboBox)) {
          final Object selectedItem = myRubyInterpreterComboBox.getSelectedItem();
          if (ADD_RUBY_SDK.equals(selectedItem)) {
            addSdk();
            return;
          }
          final Sdk sdk = (Sdk)selectedItem;
          if (sdk != null) {
            mySettingsHolder.setSdk(sdk);
          }
        }
      }
    });
    myUseCalabash.setSelected(mySettingsHolder.isUseCalabash());
    myUseCalabash.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        mySettingsHolder.setUseCalabash(myUseCalabash.isSelected());
      }
    });

    myProjectTypeLabel.setDisplayedMnemonic('T');
    myProjectType.setModel(new EnumComboBoxModel<RubyMotionUtil.ProjectType>(RubyMotionUtil.ProjectType.class));
    myProjectType.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        final RubyMotionUtil.ProjectType projectType = (RubyMotionUtil.ProjectType)myProjectType.getSelectedItem();
        mySettingsHolder.setProjectType(projectType);
        myUseCalabash.setSelected(projectType == RubyMotionUtil.ProjectType.IOS);
        myUseCalabash.setEnabled(projectType == RubyMotionUtil.ProjectType.IOS);
      }
    });
    myProjectType.setSelectedIndex(0);
    return myContentPane;
  }

  private void addSdk() {
    Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(myContentPane));
    final NullableConsumer<Sdk> onSdkCreatedCallBack = new NullableConsumer<Sdk>() {
      @Override
      public void consume(@Nullable Sdk sdk) {
        if (sdk != null) {
          SdkConfigurationUtil.addSdk(sdk);
          refreshSdkList();
          myRubyInterpreterComboBox.setSelectedItem(sdk);
        }
        else {
          myRubyInterpreterComboBox.setSelectedItem(mySettingsHolder.getSdk());
          myRubyInterpreterComboBox.repaint();
        }
      }
    };
    SdkConfigurationUtil.createSdk(project,
                                   ProjectJdkTable.getInstance().getAllJdks(),
                                   onSdkCreatedCallBack,
                                   RubySdkType.getInstance(),
                                   JRubySdkType.getInstance());
  }

  private void refreshSdkList() {
    List<Sdk> sdkList = new ArrayList<Sdk>();
    for (Sdk sdk : ProjectJdkTable.getInstance().getSdksOfType(RubySdkType.getInstance())) {
      final RubyRemoteInterpreterManager manager = RubyRemoteInterpreterManager.getInstance();
      if (!RubySdkUtil.isRuby18(sdk) && !manager.isRemoteSdk(sdk)) {
        sdkList.add(sdk);
      }
    }

    sdkList = ContainerUtil.filter(sdkList, IS_PURE_RBENV_SDK);

    myRubyInterpreterComboBox.setModel(RubyUIUtil.createSdkComboboxModel(sdkList, null, ADD_RUBY_SDK));
  }

  @Override
  public boolean isModified() {
    return true;
  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {
    myContentPane = null;
  }

  public void hideInterpreterPanel() {
    myContentPane.getComponent(0).setVisible(false);
  }

  private void createUIComponents() {
    myContentPane = new JPanel(new BorderLayout());
    myRubyInterpreterComboBox = new ComboBox(COMBO_WIDTH);
    myProjectType = new ComboBox(COMBO_WIDTH);
    RubySdkRenderer.installSdkNameSpeedSearch(myRubyInterpreterComboBox);
  }
}
