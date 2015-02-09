package org.osmorc.bnd;

import aQute.bnd.build.Project;
import aQute.bnd.build.Workspace;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.projectImport.SelectImportedProjectsStep;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;

class BndSelectProjectsStep extends SelectImportedProjectsStep<Project> {
  public BndSelectProjectsStep(WizardContext context) {
    super(context);
  }

  @Override
  public void updateStep() {
    initWorkspace();
    super.updateStep();
  }

  private void initWorkspace() {
    final BndProjectImportBuilder builder = (BndProjectImportBuilder)getContext();

    Workspace workspace = builder.getWorkspace();
    if (workspace != null) return;

    ProgressManager.getInstance().run(new Task.Modal(null, OsmorcBundle.message("bnd.import.progress.enumerating"), false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          String directory = getWizardContext().getProjectFileDirectory();
          Workspace workspace = Workspace.getWorkspace(new File(directory), Workspace.CNFDIR);
          builder.setWorkspace(workspace, BndProjectImporter.getWorkspaceProjects(workspace));
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Override
  protected String getElementText(Project project) {
    return project.getName() + " (" + project.getBase() + ")";
  }
}
