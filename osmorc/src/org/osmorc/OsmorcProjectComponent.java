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
import com.intellij.compiler.impl.FileProcessingCompilerAdapterTask;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Alarm;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.OsgiConfigurationType;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;
import org.osmorc.settings.FrameworkDefinitionListener;
import org.osmorc.settings.ProjectSettings;

import java.util.List;

/**
 * A project level component which enables some listeners to keep data in sync when the project opens or it's settings change.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcProjectComponent implements ProjectComponent, ProjectSettings.ProjectSettingsListener {
  public static final NotificationGroup IMPORTANT_ERROR_NOTIFICATION =
    new NotificationGroup("OSGi Important Messages", NotificationDisplayType.STICKY_BALLOON, true);

  private final Application myApplication;
  private final OsgiConfigurationType myConfigurationType;
  private final Project myProject;
  private final CompilerManager myCompilerManager;
  private final ProjectSettings myProjectSettings;
  private final BundleManager myBundleManager;
  private final Alarm myAlarm;

  public OsmorcProjectComponent(@NotNull Application application,
                                @NotNull OsgiConfigurationType configurationType,
                                @NotNull Project project,
                                @NotNull CompilerManager compilerManager,
                                @NotNull ProjectSettings projectSettings,
                                @NotNull BundleManager bundleManager) {
    myApplication = application;
    myConfigurationType = configurationType;
    myProject = project;
    myCompilerManager = compilerManager;
    myBundleManager = bundleManager;
    myProjectSettings = projectSettings;

    myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, myProject);
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "OsmorcProjectComponent";
  }

  @Override
  public void initComponent() {
    MessageBusConnection appBus = myApplication.getMessageBus().connect(myProject);
    appBus.subscribe(FrameworkDefinitionListener.TOPIC, new MyFrameworkDefinitionListener());

    myProjectSettings.addProjectSettingsListener(this);

    MessageBusConnection projectBus = myProject.getMessageBus().connect(myProject);
    projectBus.subscribe(ProjectTopics.PROJECT_ROOTS, new MyModuleRootListener());
    projectBus.subscribe(ProjectTopics.MODULES, new MyModuleRenameHandler());
  }

  @Override
  public void disposeComponent() {
    myProjectSettings.removeProjectSettingsListener(this);
  }

  @Override
  public void projectOpened() {
    scheduleIndexRebuild();
  }

  @Override
  public void projectClosed() { }

  @Override
  public void projectSettingsChanged() {
    scheduleIndexRebuild();
  }

  private void scheduleIndexRebuild() {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      myAlarm.cancelAllRequests();
      myAlarm.addRequest(new Runnable() {
        @Override
        public void run() {
          rebuildOSGiIndices();
        }
      }, 500);
    }
    else {
      rebuildOSGiIndices();
    }
  }

  private void rebuildOSGiIndices() {
    myApplication.invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(myProject, "Updating OSGi indices", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            if (!OsmorcProjectComponent.this.myProject.isOpen()) return;
            indicator.setIndeterminate(true);
            indicator.setText("Updating OSGi indices");
            myBundleManager.reindexAll();
          }
        }.queue();
      }
    }, myProject.getDisposed());
  }


  private class MyFrameworkDefinitionListener implements FrameworkDefinitionListener {
    @Override
    public void definitionsChanged(@NotNull List<Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition>> changes) {
      scheduleIndexRebuild();

      for (Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition> pair : changes) {
        if (pair.first == null) continue;
        for (RunConfiguration runConfiguration : RunManager.getInstance(myProject).getConfigurationsList(myConfigurationType)) {
          OsgiRunConfiguration osgiRunConfiguration = (OsgiRunConfiguration)runConfiguration;
          if (pair.first.equals(osgiRunConfiguration.getInstanceToUse())) {
            osgiRunConfiguration.setInstanceToUse(pair.second);
          }
        }
      }
    }
  }

  private class MyModuleRootListener extends ModuleRootAdapter {
    @Override
    public void rootsChanged(ModuleRootEvent event) {
      if (!event.isCausedByFileTypesChange()) {
        scheduleIndexRebuild();
      }
    }
  }

  private class MyModuleRenameHandler extends ModuleAdapter {
    @Override
    public void modulesRenamed(Project project, List<Module> modules, Function<Module, String> oldNameProvider) {
      final List<Pair<SelectedBundle, String>> pairs = ContainerUtil.newSmartList();

      for (Module module : modules) {
        String oldName = oldNameProvider.fun(module);
        for (RunConfiguration runConfiguration : RunManager.getInstance(myProject).getConfigurationsList(myConfigurationType)) {
          for (SelectedBundle bundle : ((OsgiRunConfiguration)runConfiguration).getBundlesToDeploy()) {
            if (bundle.isModule() && bundle.getName().equals(oldName)) {
              pairs.add(Pair.create(bundle, module.getName()));
              break;
            }
          }
        }
      }

      if (!pairs.isEmpty()) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            for (Pair<SelectedBundle, String> pair : pairs) {
              pair.first.setName(pair.second);
            }
          }
        });
      }
    }
  }
}
