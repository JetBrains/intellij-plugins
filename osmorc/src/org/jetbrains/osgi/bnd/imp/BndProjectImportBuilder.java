// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

import javax.swing.Icon;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BndProjectImportBuilder extends ProjectImportBuilder<Project> {
  private Workspace myWorkspace = null;
  private List<Project> myProjects = null;
  private Set<Project> myChosenProjects = null;
  private boolean myOpenProjectSettings = false;

  @Override
  public @NotNull String getName() {
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

  @Override
  public @NotNull List<Module> commit(com.intellij.openapi.project.Project project,
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
      List<Project> toImport = ContainerUtil.filter(myProjects, this::isMarked);
      final BndProjectImporter importer = new BndProjectImporter(project, myWorkspace, toImport);
      Module rootModule = importer.createRootModule(model);
      importer.setupProject();
      StartupManager.getInstance(project).runAfterOpened(() -> importer.resolve(false));
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
