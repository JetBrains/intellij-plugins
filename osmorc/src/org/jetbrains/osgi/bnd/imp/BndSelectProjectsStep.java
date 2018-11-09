/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.osgi.bnd.imp;

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
  BndSelectProjectsStep(WizardContext context) {
    super(context);
  }

  @Override
  public String getHelpId() {
    return "Import from Bnd_Bndtools Page 1";
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
          Workspace workspace = Workspace.getWorkspace(new File(directory), BndProjectImporter.CNF_DIR);
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
