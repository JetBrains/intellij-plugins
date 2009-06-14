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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceModuleManager;
import org.osmorc.frameworkintegration.FrameworkInstanceUpdateNotifier;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.settings.ProjectSettings;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcProjectComponent implements ProjectComponent, FrameworkInstanceUpdateNotifier.Listener {
    public OsmorcProjectComponent(BundleManager bundleManager,
                                  FrameworkInstanceUpdateNotifier updateNotifier,
                                  ProjectSettings projectSettings, Project project, Application application,
                                  FrameworkInstanceModuleManager frameworkInstanceModuleManager) {
        this.bundleManager = bundleManager;
        this.updateNotifier = updateNotifier;
        this.projectSettings = projectSettings;
        this.project = project;
        this.application = application;
        this.frameworkInstanceModuleManager = frameworkInstanceModuleManager;
    }

    public void initComponent() {
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
            public void childrenChanged(PsiTreeChangeEvent event) {
                processChange(event);
            }

            public void childAdded(PsiTreeChangeEvent event) {
                processChange(event);
            }

            public void childRemoved(PsiTreeChangeEvent event) {
                processChange(event);
            }

            public void childReplaced(PsiTreeChangeEvent event) {
                processChange(event);
            }
        });
    }

    private void processChange(final PsiTreeChangeEvent event) {
        final PsiFile file = event.getFile();
        if (!(file instanceof ManifestFile)) return;

        Runnable onChangeRunnable = new Runnable() {
            public void run() {
                // TODO: this should be better handled lazily

                Module moduleOfChangedManifest = ModuleUtil.findModuleForPsiElement(file);
                if (moduleOfChangedManifest != null) {
                    bundleManager.addOrUpdateBundle(moduleOfChangedManifest);

                    syncAllModuleDependencies();
                }
            }
        };

        if (ApplicationManager.getApplication().isCommandLine()) onChangeRunnable.run();
        else {
            application.invokeLater(onChangeRunnable);
        }
    }

    private void syncAllModuleDependencies() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (OsmorcFacet.hasOsmorcFacet(module)) {
                ModuleDependencySynchronizer.getInstance(module).syncDependenciesFromManifest();
            }
        }
    }

    public void updateFrameworkInstance(@NotNull final FrameworkInstanceDefinition frameworkInstanceDefinition, @NotNull
    FrameworkInstanceUpdateNotifier.UpdateKind updateKind) {
        if (frameworkInstanceDefinition.getName().equals(projectSettings.getFrameworkInstanceName())) {
            runFrameworkInstanceUpdate(false);
        }
    }

    public void updateFrameworkInstanceSelection(@NotNull Project project) {
        if (this.project == project) {
            runFrameworkInstanceUpdate(true);
        }
    }

    public void frameworkInstanceModuleHandlingChanged(@NotNull Project project) {
        if (this.project == project) {
            frameworkInstanceModuleManager.updateFrameworkInstanceModule();
        }
    }

    private void runFrameworkInstanceUpdate(final boolean onlyIfFrameworkInstanceSelectionChanged) {
        if (bundleManager.reloadFrameworkInstanceLibraries(onlyIfFrameworkInstanceSelectionChanged)) {
            syncAllModuleDependencies();
            frameworkInstanceModuleManager.updateFrameworkInstanceModule();
        }
    }


    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "OsmorcProjectComponent";
    }

    public void projectOpened() {
        updateNotifier.addListener(this);
        frameworkInstanceModuleManager.updateFrameworkInstanceModule();
    }

    public void projectClosed() {
        updateNotifier.removeListener(this);
    }

    private final BundleManager bundleManager;
    private final FrameworkInstanceUpdateNotifier updateNotifier;
    private final ProjectSettings projectSettings;
    private final Project project;
    private final Application application;
    private FrameworkInstanceModuleManager frameworkInstanceModuleManager;
}
