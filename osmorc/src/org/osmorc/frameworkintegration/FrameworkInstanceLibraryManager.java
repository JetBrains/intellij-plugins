/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.frameworkintegration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.util.FileUtil;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

import java.util.Collection;
import java.util.List;

/**
 * Manager for keeping the Framework instance library up to date.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FrameworkInstanceLibraryManager {

  private ApplicationSettings myApplicationSettings;
  private final ProjectSettings myProjectSettings;
  private final Application myApplication;
  private final Project myProject;
  private final ModuleManager myModuleManager;
  protected static final String OsmorcControlledLibrariesPrefix = "Osmorc:";


  public FrameworkInstanceLibraryManager(ApplicationSettings applicationSettings,
                                         ProjectSettings projectSettings,
                                         Application application,
                                         Project project,
                                         ModuleManager moduleManager) {
    myApplicationSettings = applicationSettings;
    this.myProjectSettings = projectSettings;
    this.myApplication = application;
    this.myProject = project;
    this.myModuleManager = moduleManager;
  }


  /**
   * Updates the framework instance library, so it resembles the project settings. If it's enabled and set in the project settings,
   * it will be created/updated if its unset, it will be deleted.
   */
  public void updateFrameworkInstanceLibraries() {
    if (myProjectSettings.isCreateFrameworkInstanceModule()) {
      if (existsAtLeastOneOsmorcFacetInProject()) {
        rebuildLibraryEntries(myProjectSettings.getFrameworkInstanceName());
      }
    }
    else {
      deleteFrameworkInstanceLibraries();
    }
  }

  /**
   * Deletes the framework instance libraries, that is all libraries that start with the {@link #OsmorcControlledLibrariesPrefix}.
   */
  private void deleteFrameworkInstanceLibraries() {
    myApplication.runWriteAction(new Runnable() {
      public void run() {
        LibraryTable.ModifiableModel projectModel = doDeleteFrameworkInstanceLibraries();
        projectModel.commit();
      }
    });
  }

  /**
   * Deletes the framework instance libraries and returns the modifiable model. This method must be called from within a write action.
   *
   * @return the modifiable model of the project library table. The caller is responsible for either committing or disposing this model.
   */
  private LibraryTable.ModifiableModel doDeleteFrameworkInstanceLibraries() {
    LibraryTable.ModifiableModel projectModel = ProjectLibraryTable.getInstance(myProject).getModifiableModel();
    Library[] libraries = projectModel.getLibraries();
    for (Library library : libraries) {
      if (library.getName().startsWith(OsmorcControlledLibrariesPrefix)) {
        projectModel.removeLibrary(library);
      }
    }
    return projectModel;
  }

  /**
   * Checks if the project contains at least one osmorc facet.
   *
   * @return true if there is at least one osmorc facet.
   */
  private boolean existsAtLeastOneOsmorcFacetInProject() {
    Module[] modules = myModuleManager.getModules();
    for (Module module : modules) {
      if (OsmorcFacet.hasOsmorcFacet(module)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Deletes all old Osmorc controlled project library entries and creates new ones for the given framework instance.
   * @param instanceName the instance
   */
  private void rebuildLibraryEntries(@Nullable final String instanceName) {
    FrameworkIntegratorRegistry registry = ServiceManager.getService(myProject, FrameworkIntegratorRegistry.class);
    FrameworkInstanceDefinition frameworkInstance = myApplicationSettings.getFrameworkInstance(instanceName);
    if (frameworkInstance != null) {
      FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(frameworkInstance);
      if (integrator != null) {
        integrator.getFrameworkInstanceManager().collectLibraries(frameworkInstance, new JarFileLibraryCollector() {

          @Override
          protected void collectFrameworkJars(@NotNull final Collection<VirtualFile> jarFiles,
                                              @NotNull final FrameworkInstanceLibrarySourceFinder sourceFinder) {
            myApplication.runWriteAction(new Runnable() {
              @Override
              public void run() {
                // first clean up
                LibraryTable.ModifiableModel projectModel = doDeleteFrameworkInstanceLibraries();
                int idx = 1; // the idx is just to make the library names unique, in case we encounter a duplicate jar name..
                for (VirtualFile jarFile : jarFiles) {
                  // create a new library per jar file (like maven does)
                  Library library =
                    projectModel.createLibrary(OsmorcControlledLibrariesPrefix + idx + ":" + jarFile.getNameWithoutExtension());
                  Library.ModifiableModel libraryModel = library.getModifiableModel();
                  VirtualFile folder = FileUtil.getFolder(jarFile);
                  if (folder != null) {
                    libraryModel.addRoot(folder, OrderRootType.CLASSES);
                  }
                  // find sources and add them to library
                  List<VirtualFile> sources = sourceFinder.getSourceForLibraryClasses(jarFile);
                  for (VirtualFile source : sources) {
                    VirtualFile sourceFolder = FileUtil.getFolder(source);
                    libraryModel.addRoot(sourceFolder, OrderRootType.SOURCES);
                  }
                  // finish the library
                  libraryModel.commit();
                  idx++;
                }
                // and commit the project changes
                projectModel.commit();
              }
            });
          }
        });
      }
    }
  }
}
