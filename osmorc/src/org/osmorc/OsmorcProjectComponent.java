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
package org.osmorc;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.FrameworkInstanceModuleManager;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.settings.ProjectSettings;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcProjectComponent implements ProjectComponent, ProjectSettings.ProjectSettingsListener {
  private final BundleManager myBundleManager;
  private final ProjectSettings myProjectSettings;
  private final Project myProject;
  private final FrameworkInstanceModuleManager myFrameworkInstanceModuleManager;

  public OsmorcProjectComponent(BundleManager bundleManager,
                                ProjectSettings projectSettings,
                                Project project,
                                FrameworkInstanceModuleManager frameworkInstanceModuleManager) {
    this.myBundleManager = bundleManager;
    this.myProjectSettings = projectSettings;
    this.myProject = project;
    this.myFrameworkInstanceModuleManager = frameworkInstanceModuleManager;
  }

  @NotNull
  public String getComponentName() {
    return "OsmorcProjectComponent";
  }

  public void initComponent() {
    //PsiManager.getInstance(myProject).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
    //  public void childrenChanged(PsiTreeChangeEvent event) {
    //    processChange(event);
    //  }
    //
    //  public void childAdded(PsiTreeChangeEvent event) {
    //    processChange(event);
    //  }
    //
    //  public void childRemoved(PsiTreeChangeEvent event) {
    //    processChange(event);
    //  }
    //
    //  public void childReplaced(PsiTreeChangeEvent event) {
    //    processChange(event);
    //  }
    //});
    myProjectSettings.addProjectSettingsListener(this);
  }

  public void disposeComponent() {
    myProjectSettings.removeProjectSettingsListener(this);
  }

  public void projectOpened() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(myProject, "Updating OSGi indices", false) {
          @Override
          public void run
            (@NotNull ProgressIndicator
               indicator) {
            indicator.setIndeterminate(true);
            // TODO Remove FrameworkInstance stuff
            indicator.setText("Refreshing framework instance");
            myFrameworkInstanceModuleManager.updateFrameworkInstanceModule();

            indicator.setText("Updating OSGi indices");
            myBundleManager.reindex(myProject);

            syncAllModuleDependencies();
          }
        }.queue();
      }
    });
  }

  public void projectClosed() {
  }


  /**
   * Processes changes in the project tree.
   *
   * @param event the change event.
   */
  private void processChange(final PsiTreeChangeEvent event) {
    final PsiFile file = event.getFile();
    if (!(file instanceof ManifestFile)) return;
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(myProject, "Processing manifest change", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setText("Updating OSGi indices");
            indicator.setIndeterminate(true);
            Module moduleOfChangedManifest = ModuleUtil.findModuleForPsiElement(file);
            if (moduleOfChangedManifest != null) {
              myBundleManager.reindex(moduleOfChangedManifest);
              // sync the dependencies of ALL modules
              syncAllModuleDependencies();
            }
          }
        }.queue();
      }
    });
  }

  /**
   * Synchronizes the OSGi dependencies of all modules.
   */
  private void syncAllModuleDependencies() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(myProject, "Synchronizing OSGi module dependencies", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            Module[] modules = ModuleManager.getInstance(myProject).getModules();
            for (Module module : modules) {
              if (OsmorcFacet.hasOsmorcFacet(module)) {
                ModuleDependencySynchronizer.getInstance(module).syncDependenciesFromManifest();
              }
            }
          }
        }.queue();
      }
    });
  }

  public void projectSettingsChanged() {
  }
}
