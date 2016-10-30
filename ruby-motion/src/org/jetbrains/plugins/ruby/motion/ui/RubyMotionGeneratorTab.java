package org.jetbrains.plugins.ruby.motion.ui;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.RubySdkPanel;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtilImpl;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.RubyFacetEditorTab;
import org.jetbrains.plugins.ruby.remote.RubyRemoteInterpreterManager;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionGeneratorTab extends RubyFacetEditorTab {
  private static final int COMBO_WIDTH = PlatformUtils.isRubyMine() ? 320 : 150;
  private final RubyMotionSettingsHolder mySettingsHolder;
  private JPanel mySettingsPane;
  private JPanel myContentPane;
  private ComboBox myProjectType;
  private JLabel myProjectTypeLabel;

  public RubyMotionGeneratorTab(final RubyMotionSettingsHolder settingsHolder) {
    mySettingsHolder = settingsHolder;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return RBundle.message("ruby.motion.wizard.tab.project.generator.title");
  }

  static class SdkPanel extends RubySdkPanel{
    @Override
    protected Sdk[] getSdks() {
      List<Sdk> sdkList = new ArrayList<>();
      for (Sdk sdk : ProjectJdkTable.getInstance().getSdksOfType(RubySdkType.getInstance())) {
        final RubyRemoteInterpreterManager manager = RubyRemoteInterpreterManager.getInstance();
        if (!RubySdkUtil.isRuby18(sdk) && !manager.isRemoteSdk(sdk)) {
          sdkList.add(sdk);
        }
      }
      return sdkList.toArray(new Sdk[0]);
    }
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    final JPanel sdkPanel = new SdkPanel().getSettingsPanel(mySettingsHolder);
    myContentPane.add(sdkPanel);
    myContentPane.add(mySettingsPane);

    myProjectTypeLabel.setDisplayedMnemonic('T');
    myProjectType.setModel(new EnumComboBoxModel<>(RubyMotionUtilImpl.ProjectType.class));
    myProjectType.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        final RubyMotionUtilImpl.ProjectType projectType = (RubyMotionUtilImpl.ProjectType)myProjectType.getSelectedItem();
        mySettingsHolder.setProjectType(projectType);
      }
    });
    myProjectType.setSelectedIndex(0);
    return myContentPane;
  }

  @Override
  public boolean isModified() {
    return true;
  }

  @Override
  public void reset() {}

  public void hideInterpreterPanel() {
    mySettingsPane.getComponent(0).setVisible(false);
  }

  private void createUIComponents() {
    myContentPane = new JPanel(new VerticalFlowLayout());
    myProjectType = new ComboBox(COMBO_WIDTH);
  }
}
