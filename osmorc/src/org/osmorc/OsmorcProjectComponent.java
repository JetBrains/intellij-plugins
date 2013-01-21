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

import com.intellij.ProjectTopics;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceLibraryManager;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

/**
 * A project level component which enables some listeners to keep data in sync when the project opens or it's settings change.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcProjectComponent implements ProjectComponent, ProjectSettings.ProjectSettingsListener,
                                               ApplicationSettings.ApplicationSettingsListener {

  public static final NotificationGroup IMPORTANT_ERROR_NOTIFICATION =
    new NotificationGroup("OSGi important errors", NotificationDisplayType.STICKY_BALLOON, true);

  private final BundleManager myBundleManager;
  private ApplicationSettings myApplicationSettings;
  private final ProjectSettings myProjectSettings;
  private final Project myProject;
  private final FrameworkInstanceLibraryManager myFrameworkInstanceLibraryManager;
  private Alarm myAlarm;

  public OsmorcProjectComponent(BundleManager bundleManager, ApplicationSettings applicationSettings,
                                ProjectSettings projectSettings,
                                Project project,
                                FrameworkInstanceLibraryManager frameworkInstanceLibraryManager) {
    this.myBundleManager = bundleManager;
    myApplicationSettings = applicationSettings;
    this.myProjectSettings = projectSettings;
    this.myProject = project;
    this.myFrameworkInstanceLibraryManager = frameworkInstanceLibraryManager;
    myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, myProject);
  }

  @NotNull
  public String getComponentName() {
    return "OsmorcProjectComponent";
  }

  public void initComponent() {
    myProjectSettings.addProjectSettingsListener(this);
    myApplicationSettings.addApplicationSettingsListener(this);
    myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new MyModuleRootListener());
  }

  public void disposeComponent() {
    myProjectSettings.removeProjectSettingsListener(this);
    myApplicationSettings.removeApplicationSettingsListener(this);
  }

  public void projectOpened() {
    refreshFrameworkInstanceLibrary();
    rebuildOSGiIndices();
  }

  public void projectClosed() {
  }


  public void projectSettingsChanged() {
    refreshFrameworkInstanceLibrary();
  }


  @Override
  public void frameworkInstancesChanged() {
    refreshFrameworkInstanceLibrary();
  }

  /**
   * Refreshes the framework instance library.
   */
  private void refreshFrameworkInstanceLibrary() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return;
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!myProject.isOpen()) return;
        myFrameworkInstanceLibraryManager.updateFrameworkInstanceLibraries();
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(new Runnable() {
          @Override
          public void run() {
            rebuildOSGiIndices();
          }
        }, 500);
      }
    }, myProject.getDisposed());
  }


  private void rebuildOSGiIndices() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return;
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(myProject, "Updating OSGi indices", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            if (!myProject.isOpen()) return;
            indicator.setIndeterminate(true);
            indicator.setText("Updating OSGi indices");
            myBundleManager.reindexAll();
          }
        }.queue();
      }
    }, myProject.getDisposed());
  }


  private class MyModuleRootListener extends ModuleRootAdapter {
    @Override
    public void rootsChanged(ModuleRootEvent event) {
      if (!event.isCausedByFileTypesChange()) {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(new Runnable() {
          @Override
          public void run() {
            rebuildOSGiIndices();
          }
        }, 500);
      }
    }
  }
}
