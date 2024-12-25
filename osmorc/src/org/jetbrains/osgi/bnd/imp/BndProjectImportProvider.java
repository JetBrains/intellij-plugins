// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  @Override
  public @Nullable String getFileSample() {
    return OsmorcBundle.message("bnd.import.workspace.sample");
  }
}
