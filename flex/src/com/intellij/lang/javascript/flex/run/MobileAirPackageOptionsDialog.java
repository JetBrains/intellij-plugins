package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;

import javax.swing.*;
import java.util.List;

import static com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase.FilePathAndPathInPackage;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;

public class MobileAirPackageOptionsDialog extends DialogWrapper {
  private JPanel myMainPanel;

  private FilesToPackageForm myFilesToPackageForm;
  private SigningOptionsForm mySigningOptionsForm;

  private final Project myProject;
  private final Module myModule;
  private final AirMobileRunTarget myRunTarget;

  public MobileAirPackageOptionsDialog(final Project project,
                                       final Module module,
                                       final AirMobileRunTarget runTarget,
                                       final List<FilePathAndPathInPackage> filesToPackage) {
    super(project);
    myProject = project;
    myModule = module;
    myRunTarget = runTarget;
    myFilesToPackageForm.resetFrom(filesToPackage);
    myFilesToPackageForm.setPanelTitle("Assets to package");
    setTitle("Packaging Options");

    mySigningOptionsForm.getMainPanel().setVisible(false);  // not supported yet

    init();
  }

  private void createUIComponents() {
    myFilesToPackageForm = new FilesToPackageForm(myProject);

    final Computable<Module> moduleComputable = new Computable<Module>() {
      public Module compute() {
        return myModule;
      }
    };
    final Computable<Sdk> sdkComputable = new Computable<Sdk>() {
      @Override
      public Sdk compute() {
        return FlexUtils.getSdkForActiveBC(myModule);
      }
    };
    final Runnable resizeHandler = new Runnable() {
      public void run() {
        getPeer().setSize(getPeer().getSize().width, getPeer().getPreferredSize().height);
      }
    };

    mySigningOptionsForm = new SigningOptionsForm(myProject, moduleComputable, sdkComputable, resizeHandler);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected void doOKAction() {
    myFilesToPackageForm.stopEditing();
    super.doOKAction();
  }

  public List<FilePathAndPathInPackage> getFilesToPackage() {
    return myFilesToPackageForm.getFilesToPackage();
  }
}
