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

import aQute.bnd.build.Workspace;
import com.intellij.ProjectTopics;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.OsgiConfigurationType;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;
import org.osmorc.settings.FrameworkDefinitionListener;
import org.osmorc.settings.ProjectSettings;

import javax.swing.event.HyperlinkEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A project level component which enables some listeners to keep data in sync when the project opens or it's settings change.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsmorcProjectComponent implements BaseComponent {
  public static final NotificationGroup IMPORTANT_NOTIFICATIONS =
    new NotificationGroup("OSGi Important Messages", NotificationDisplayType.STICKY_BALLOON, true);

  private final Application myApplication;
  private final OsgiConfigurationType myConfigurationType;
  private final Project myProject;
  private final ProjectSettings myProjectSettings;
  private final MergingUpdateQueue myQueue;
  private final AtomicBoolean myReimportNotification = new AtomicBoolean(false);

  public OsmorcProjectComponent(@NotNull Application application,
                                @NotNull OsgiConfigurationType configurationType,
                                @NotNull Project project,
                                @NotNull ProjectSettings projectSettings) {
    myApplication = application;
    myConfigurationType = configurationType;
    myProject = project;
    myProjectSettings = projectSettings;

    myQueue = new MergingUpdateQueue(getComponentName(), 500, true, MergingUpdateQueue.ANY_COMPONENT, myProject);
  }

  @Override
  public void initComponent() {
    MessageBusConnection appBus = myApplication.getMessageBus().connect(myProject);
    appBus.subscribe(FrameworkDefinitionListener.TOPIC, new MyFrameworkDefinitionListener());

    MessageBusConnection projectBus = myProject.getMessageBus().connect(myProject);
    projectBus.subscribe(ProjectTopics.MODULES, new MyModuleRenameHandler());

    Workspace workspace = BndProjectImporter.findWorkspace(myProject);
    if (workspace != null) {
      appBus.subscribe(VirtualFileManager.VFS_CHANGES, new MyVfsListener());
    }
  }
  
  private void scheduleImportNotification() {
    myQueue.queue(new Update("reimport") {
      @Override
      public void run() {
        if (myProjectSettings.isBndAutoImport()) {
          BndProjectImporter.reimportWorkspace(myProject);
          return;
        }

        if (myReimportNotification.getAndSet(true)) {
          return;
        }

        String title = OsmorcBundle.message("bnd.reimport.title");
        String text = OsmorcBundle.message("bnd.reimport.text");
        BndProjectImporter.NOTIFICATIONS
          .createNotification(title, text, NotificationType.INFORMATION, new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
              notification.expire();
              if (e.getDescription().equals("auto")) {
                myProjectSettings.setBndAutoImport(true);
              }
              BndProjectImporter.reimportWorkspace(myProject);
            }
          })
          .whenExpired(() -> myReimportNotification.set(false))
          .notify(myProject);
      }
    });
  }


  private class MyFrameworkDefinitionListener implements FrameworkDefinitionListener {
    @Override
    public void definitionsChanged(@NotNull List<Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition>> changes) {
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

  private class MyModuleRenameHandler implements ModuleListener {
    @Override
    public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
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
        ApplicationManager.getApplication().runWriteAction(() -> {
          for (Pair<SelectedBundle, String> pair : pairs) {
            pair.first.setName(pair.second);
          }
        });
      }
    }
  }

  private class MyVfsListener implements BulkFileListener {
    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
      for (VFileEvent event : events) {
        if (event instanceof VFileContentChangeEvent) {
          VirtualFile file = event.getFile();
          if (file != null) {
            String name = file.getName();
            if (BndProjectImporter.BND_FILE.equals(name) || BndProjectImporter.BUILD_FILE.equals(name)) {
              scheduleImportNotification();
              break;
            }
          }
        }
      }
    }
  }
}
