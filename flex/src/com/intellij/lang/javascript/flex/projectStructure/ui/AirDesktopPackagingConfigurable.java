package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.projectStructure.options.AirDesktopPackagingOptions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.NullableComputable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class AirDesktopPackagingConfigurable extends NamedConfigurable<AirDesktopPackagingOptions> {
  private JPanel myMainPanel;
  private LabeledComponent myInstallerFileNameComponent;
  private LabeledComponent myInstallerLocationComponent;
  private FilesToPackageForm myFilesToPackageForm;
  private SigningOptionsForm mySigningOptionsForm;
  private JCheckBox myDoNotSignCheckBox;

  private final Project myProject;
  private final AirDesktopPackagingOptions myAirDesktopPackagingOptions;

  public AirDesktopPackagingConfigurable(final Project project, final AirDesktopPackagingOptions airDesktopPackagingOptions) {
    myProject = project;
    myAirDesktopPackagingOptions = airDesktopPackagingOptions;
  }

  @Nls
  public String getDisplayName() {
    return "AIR Package";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "AIR Package";
  }

  public Icon getIcon() {
    return null;
  }

  public AirDesktopPackagingOptions getEditableObject() {
    return myAirDesktopPackagingOptions;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myAirDesktopPackagingOptions);
  }

  public void applyTo(final AirDesktopPackagingOptions airDesktopPackagingOptions) {

  }

  public void reset() {
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    myFilesToPackageForm = new FilesToPackageForm(myProject);

    mySigningOptionsForm = new SigningOptionsForm(myProject, new NullableComputable<Module>() {
      public Module compute() {
        return null;
      }
    }, new NullableComputable<Sdk>() {
      public Sdk compute() {
        return null;
      }
    }, new Runnable() {
      public void run() {
      }
    }
    );
  }
}
