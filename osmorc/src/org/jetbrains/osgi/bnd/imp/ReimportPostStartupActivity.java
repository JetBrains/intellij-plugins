// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.imp;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.AsyncFileListener;
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

final class ReimportPostStartupActivity implements StartupActivity.DumbAware {
  @Override
  public void runActivity(@NotNull Project project) {
    if (BndProjectImporter.findWorkspace(project) != null) {
      // yes, if project is closed, flag is not reset - it is ok as prepareChange is not expensive (just compare name)
      HelperService.getInstance().isActive = true;
    }
  }

  @Service
  private static final class HelperService implements Disposable {
    private final AtomicBoolean reimportNotification = new AtomicBoolean(false);
    private final MergingUpdateQueue queue = new MergingUpdateQueue(HelperService.class.getName(), 500, true, MergingUpdateQueue.ANY_COMPONENT, this);

    private volatile boolean isActive;

    private static @NotNull HelperService getInstance() {
      return ApplicationManager.getApplication().getService(HelperService.class);
    }

    @Override
    public void dispose() {
    }

    private void scheduleReimportNotification() {
      queue.queue(new Update("reimport") {
        @Override
        public void run() {
          for (Project project : ProjectUtil.getOpenProjects()) {
            if (BndProjectImporter.getWorkspace(project) == null) {
              continue;
            }

            ProjectSettings projectSettings = ProjectSettings.getInstance(project);
            if (projectSettings.isBndAutoImport()) {
              BndProjectImporter.reimportWorkspace(project);
              return;
            }

            if (reimportNotification.getAndSet(true)) {
              return;
            }

            String title = OsmorcBundle.message("bnd.reimport.title");
            String text = OsmorcBundle.message("bnd.reimport.text");
            OsmorcBundle.bnd(title, text, NotificationType.INFORMATION)
              .setListener(new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                  notification.expire();
                  if (e.getDescription().equals("auto")) {
                    projectSettings.setBndAutoImport(true);
                  }
                  BndProjectImporter.reimportWorkspace(project);
                }
              })
              .whenExpired(() -> reimportNotification.set(false))
              .notify(project);
          }
        }
      });
    }
  }

  static final class FileListener implements AsyncFileListener {
    private final HelperService manager = HelperService.getInstance();

    @Override
    public @Nullable ChangeApplier prepareChange(@NotNull List<? extends @NotNull VFileEvent> events) {
      if (!manager.isActive) {
        return null;
      }

      for (VFileEvent event : events) {
        if (event instanceof VFileContentChangeEvent) {
          String name = ((VFileContentChangeEvent)event).getFile().getName();
          if (BndProjectImporter.BND_FILE.equals(name) || BndProjectImporter.BUILD_FILE.equals(name)) {
            manager.scheduleReimportNotification();
            break;
          }
        }
      }

      return null;
    }
  }
}
