// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.imp;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ProjectSettings;

import javax.swing.event.HyperlinkEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReimportPostStartupActivity implements StartupActivity.DumbAware {
  @Override
  public void runActivity(@NotNull Project project) {
    if (BndProjectImporter.findWorkspace(project) != null) {
      FileListener listener = new FileListener(project);
      Disposer.register(project, listener);
      VirtualFileManager.getInstance().addAsyncFileListener(listener, listener);
    }
  }

  private static class FileListener implements AsyncFileListener, Disposable {
    private final Project myProject;
    private final MergingUpdateQueue myQueue;
    private final AtomicBoolean myReimportNotification = new AtomicBoolean(false);

    FileListener(Project project) {
      myProject = project;
      myQueue = new MergingUpdateQueue(ReimportPostStartupActivity.class.getName(), 500, true, MergingUpdateQueue.ANY_COMPONENT, this);
    }

    @Nullable
    @Override
    public ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
      for (VFileEvent event : events) {
        if (event instanceof VFileContentChangeEvent) {
          String name = ((VFileContentChangeEvent)event).getFile().getName();
          if (BndProjectImporter.BND_FILE.equals(name) || BndProjectImporter.BUILD_FILE.equals(name)) {
            scheduleReimportNotification();
            break;
          }
        }
      }

      return null;
    }

    private void scheduleReimportNotification() {
      myQueue.queue(new Update("reimport") {
        @Override
        public void run() {
          ProjectSettings projectSettings = ProjectSettings.getInstance(myProject);
          if (projectSettings.isBndAutoImport()) {
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
                  projectSettings.setBndAutoImport(true);
                }
                BndProjectImporter.reimportWorkspace(myProject);
              }
            })
            .whenExpired(() -> myReimportNotification.set(false))
            .notify(myProject);
        }
      });
    }

    @Override
    public void dispose() { }
  }
}