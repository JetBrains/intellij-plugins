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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.util.containers.ContainerUtil;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BndProjectImportBuilder extends ProjectImportBuilder<Project> {
  private Workspace myWorkspace = null;
  private List<Project> myProjects = null;
  private Set<Project> myChosenProjects = null;
  private boolean myOpenProjectSettings = false;

  @NotNull
  @Override
  public String getName() {
    return "Bnd/Bndtools";
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
    myProjects = ContainerUtil.newArrayList(projects);
  }

  @Override
  public List<Project> getList() {
    return myProjects;
  }

  @Override
  public void setList(List<Project> list) throws ConfigurationException {
    myChosenProjects = ContainerUtil.newHashSet(list);
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
        commitModel(model);
        return result;
      }
      catch (RuntimeException e) {
        disposeModel(model);
        throw e;
      }
      catch (Error e) {
        disposeModel(model);
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

  private static void commitModel(final ModifiableModuleModel moduleModel) {
    ApplicationManager.getApplication().runWriteAction(() -> moduleModel.commit());
  }

  private static void disposeModel(final ModifiableModuleModel moduleModel) {
    ApplicationManager.getApplication().runWriteAction(() -> moduleModel.dispose());
  }

  @Override
  public void cleanup() {
    myWorkspace = null;
    myProjects = null;
    myChosenProjects = null;
    super.cleanup();
  }
}