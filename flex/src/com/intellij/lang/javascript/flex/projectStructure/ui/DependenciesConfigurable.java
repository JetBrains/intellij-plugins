package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.Dependencies;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.text.MessageFormat;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {

  private JPanel myMainPanel;
  private FlexSdkChooserPanel mySdkPanel;
  private JComboBox myComponentSetCombo;
  private JComboBox myFrameworkLinkageCombo;
  private JLabel myComponentSetLabel;

  private final Dependencies myDependencies;
  private final Project myProject;
  private final ModifiableRootModel myRootModel;

  public DependenciesConfigurable(final FlexIdeBuildConfiguration bc, Project project, ModifiableRootModel rootModel) {
    myDependencies = bc.DEPENDENCIES;
    myProject = project;
    myRootModel = rootModel;

    final boolean mobilePlatform = bc.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile;

    myComponentSetLabel.setVisible(!mobilePlatform && !bc.PURE_ACTION_SCRIPT);
    myComponentSetCombo.setVisible(!mobilePlatform && !bc.PURE_ACTION_SCRIPT);

    myComponentSetCombo.setModel(new DefaultComboBoxModel(FlexIdeBuildConfiguration.ComponentSet.values()));
    myComponentSetCombo.setRenderer(new ListCellRendererWrapper<FlexIdeBuildConfiguration.ComponentSet>(myComponentSetCombo.getRenderer()) {
      public void customize(JList list, FlexIdeBuildConfiguration.ComponentSet value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });

    final FlexIdeBuildConfiguration.FrameworkLinkage defaultLinkage =
      BCUtils.getDefaultFrameworkLinkage(bc.TARGET_PLATFORM, bc.PURE_ACTION_SCRIPT, bc.OUTPUT_TYPE);
    myFrameworkLinkageCombo
      .setRenderer(new ListCellRendererWrapper<FlexIdeBuildConfiguration.FrameworkLinkage>(myFrameworkLinkageCombo.getRenderer()) {
        public void customize(JList list, FlexIdeBuildConfiguration.FrameworkLinkage value, int index, boolean selected, boolean hasFocus) {
          if (value == FlexIdeBuildConfiguration.FrameworkLinkage.Default) {
            setText(MessageFormat.format("SDK Default ({0})", defaultLinkage.PRESENTABLE_TEXT));
          }
          else {
            setText(value.PRESENTABLE_TEXT);
          }
        }
      });

    myFrameworkLinkageCombo.setModel(new DefaultComboBoxModel(BCUtils.getSuitableFrameworkLinkages(bc.TARGET_PLATFORM,
                                                                                                   bc.PURE_ACTION_SCRIPT, bc.OUTPUT_TYPE)));
  }

  @Nls
  public String getDisplayName() {
    return "Dependencies";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "Dependencies";
  }

  public Icon getIcon() {
    return null;
  }

  public Dependencies getEditableObject() {
    return myDependencies;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (mySdkPanel.isModified()) return true;
    if (myDependencies.COMPONENT_SET != myComponentSetCombo.getSelectedItem()) return true;
    if (myDependencies.FRAMEWORK_LINKAGE != myFrameworkLinkageCombo.getSelectedItem()) return true;
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);
    mySdkPanel.apply();
  }

  public void applyTo(final Dependencies dependencies) {
    dependencies.COMPONENT_SET = (FlexIdeBuildConfiguration.ComponentSet)myComponentSetCombo.getSelectedItem();
    dependencies.FRAMEWORK_LINKAGE = (FlexIdeBuildConfiguration.FrameworkLinkage)myFrameworkLinkageCombo.getSelectedItem();
  }

  public void reset() {
    mySdkPanel.reset();
    myComponentSetCombo.setSelectedItem(myDependencies.COMPONENT_SET);
    myFrameworkLinkageCombo.setSelectedItem(myDependencies.FRAMEWORK_LINKAGE);
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    mySdkPanel = new FlexSdkChooserPanel(myProject, myRootModel);
  }

  public FlexSdkChooserPanel getSdkChooserPanel() {
    return mySdkPanel;
  }

  public void addFlexSdkListener(ChangeListener listener) {
    mySdkPanel.addListener(listener);
  }

  public void removeFlexSdkListener(ChangeListener listener) {
    mySdkPanel.removeListener(listener);
  }
}
