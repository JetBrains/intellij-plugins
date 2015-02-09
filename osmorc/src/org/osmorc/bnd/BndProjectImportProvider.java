package org.osmorc.bnd;

import aQute.bnd.build.Workspace;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

public class BndProjectImportProvider extends ProjectImportProvider {
  public BndProjectImportProvider() {
    super(new BndProjectImportBuilder());
  }

  @Override
  public boolean canImport(VirtualFile fileOrDirectory, @Nullable Project project) {
    return fileOrDirectory.isDirectory() && fileOrDirectory.findChild(Workspace.CNFDIR) != null;
  }

  @Override
  public ModuleWizardStep[] createSteps(WizardContext context) {
    return new ModuleWizardStep[]{
      new BndSelectProjectsStep(context),
      ProjectWizardStepFactory.getInstance().createProjectJdkStep(context)
    };
  }

  @Nullable
  @Override
  public String getFileSample() {
    return OsmorcBundle.message("bnd.import.provider.sample");
  }
}
