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

import com.intellij.notification.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ProjectSettings;

import javax.swing.event.HyperlinkEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A project level component which enables some listeners to keep data in sync when the project opens or it's settings change.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class OsmorcProjectComponent {
  private final Project myProject;
  private final MergingUpdateQueue myQueue;
  private final AtomicBoolean myReimportNotification = new AtomicBoolean(false);

  public static OsmorcProjectComponent getInstance(Project project) {
    return ServiceManager.getService(project, OsmorcProjectComponent.class);
  }

  public OsmorcProjectComponent(@NotNull Project project) {
    myProject = project;
    myQueue = new MergingUpdateQueue(OsmorcProjectComponent.class.getName(), 500, true, MergingUpdateQueue.ANY_COMPONENT, myProject);
  }

  public void scheduleImportNotification() {
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
}