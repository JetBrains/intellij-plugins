/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.ui;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtilImpl;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.RubyFacetEditorTab;
import org.jetbrains.plugins.ruby.remote.RubyRemoteInterpreterManager;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.wizard.RubySdkPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionGeneratorTabBase extends RubyFacetEditorTab {
  private static final int COMBO_WIDTH = PlatformUtils.isRubyMine() ? 320 : 150;
  private final RubyMotionSettingsHolder mySettingsHolder;
  private JPanel mySettingsPane;
  private ComboBox myProjectType;
  private JLabel myProjectTypeLabel;
  private final RubySdkPanel mySdkPanel;

  public RubyMotionGeneratorTabBase(final RubyMotionSettingsHolder settingsHolder) {
    super(settingsHolder);
    mySettingsHolder = settingsHolder;
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
    mySdkPanel = new SdkPanel();
    init();
  }

  @Nls
  @Override
  public String getDisplayName() {
    return RBundle.message("ruby.motion.wizard.tab.project.generator.title");
  }

  @Override
  protected JComponent getSettingsComp() {
    return mySettingsPane;
  }

  @Override
  protected RubySdkPanel getSdkComboPanel() {
    return mySdkPanel;
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return getMainPanel();
  }

  @Override
  public boolean isModified() {
    return true;
  }

  @Override
  public void reset() {}

  private void createUIComponents() {
    myProjectType = new ComboBox(COMBO_WIDTH);
  }

  static class SdkPanel extends RubySdkPanel {
    @Override
    protected List<Sdk> getSdks() {
      List<Sdk> sdkList = new ArrayList<>();
      for (Sdk sdk : ProjectJdkTable.getInstance().getSdksOfType(RubySdkType.getInstance())) {
        final RubyRemoteInterpreterManager manager = RubyRemoteInterpreterManager.getInstance();
        if (!RubySdkUtil.isRuby18(sdk) && !manager.isRemoteSdk(sdk)) {
          sdkList.add(sdk);
        }
      }
      return sdkList;
    }
  }
}
