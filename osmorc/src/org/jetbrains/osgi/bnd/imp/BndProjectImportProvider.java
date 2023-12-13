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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

public final class BndProjectImportProvider extends ProjectImportProvider {
  public BndProjectImportProvider() {
    super(new BndProjectImportBuilder());
  }

  @Override
  public boolean canImport(@NotNull VirtualFile fileOrDir, @Nullable Project project) {
    return BndProjectImporter.getWorkspace(project) == null &&
           fileOrDir.isDirectory() && fileOrDir.findChild(BndProjectImporter.CNF_DIR) != null;
  }

  @Override
  public boolean canImportModule() {
    return false;
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
    return OsmorcBundle.message("bnd.import.workspace.sample");
  }
}
