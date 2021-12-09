// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.imp;

import aQute.bnd.build.Project;
import aQute.bnd.build.Workspace;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.util.containers.ContainerUtil;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class BndProjectImportBuilder extends ProjectImportBuilder<Project> {
  private Workspace myWorkspace = null;
  private List<Project> myProjects = null;
  private Set<Project> myChosenProjects = null;
  private boolean myOpenProjectSettings = false;

  @NotNull
  @Override
  public String getName() {
    return OsmorcBundle.message("bnd.importer.name");
  }

  @Override
  public Icon getIcon() {
    return OsmorcIdeaIcons.Bnd;
  }

  public Workspace getWorkspace() {
    return myWorkspace;
  }

  public void setWorkspace(Workspace workspace, Collection<Project> projects) {
    myWorkspace = workspace;
    myProjects = new ArrayList<>(projects);
  }

  @Override
  public List<Project> getList() {
    return myProjects;
  }

  @Override
  public void setList(List<Project> list) {
    myChosenProjects = new HashSet<>(list);
  }

  @Override
  public boolean isMarked(Project project) {
    return myChosenProjects == null || myChosenProjects.contains(project);
  }

  @Override
  public boolean isOpenProjectSettingsAfter() {
    return myOpenProjectSettings;
  }

  @Override
  public void setOpenProjectSettingsAfter(boolean openProjectSettings) {
    myOpenProjectSettings = openProjectSettings;
  }

  @NotNull
  @Override
  public List<Module> commit(com.intellij.openapi.project.Project project,
                             ModifiableModuleModel model,
                             ModulesProvider modulesProvider,
                             ModifiableArtifactModel artifactModel) {
    if (model == null) {
      model = ModuleManager.getInstance(project).getModifiableModel();
      try {
        List<Module> result = commit(project, model, modulesProvider, artifactModel);
        WriteAction.run(model::commit);
        return result;
      }
      catch (RuntimeException | Error e) {
        model.dispose();
        throw e;
      }
    }

    if (myWorkspace != null) {
      List<Project> toImport = ContainerUtil.filter(myProjects, project1 -> isMarked(project1));
      final BndProjectImporter importer = new BndProjectImporter(project, myWorkspace, toImport);
      Module rootModule = importer.createRootModule(model);
      importer.setupProject();
      StartupManager.getInstance(project).registerPostStartupActivity(() -> importer.resolve(false));
      return Collections.singletonList(rootModule);
    }
    else {
      File file = new File(getFileToImport());
      if (BndProjectImporter.BND_FILE.equals(file.getName())) {
        file = file.getParentFile();
      }
      BndProjectImporter.reimportProjects(project, Collections.singleton(file.getPath()));
      return Collections.emptyList();
    }
  }

  @Override
  public void cleanup() {
    myWorkspace = null;
    myProjects = null;
    myChosenProjects = null;
    super.cleanup();
  }
}
