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
package org.osmorc.make;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import java.util.jar.JarFile;

/**
 * Action which allows view the manifest of a given jar file. The action will open the manifest in a new editor tab.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thomä</a>
 */
public class ViewManifestAction extends AnAction implements DumbAware {
  private final String myJarFilePath;

  public ViewManifestAction(@NlsSafe String title, String jarFilePath) {
    myJarFilePath = jarFilePath;
    getTemplatePresentation().setText(title);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) return;

    VirtualFile jarFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(myJarFilePath);
    if (jarFile == null) {
      String title = OsmorcBundle.message("view.manifest.title"), message = OsmorcBundle.message("view.manifest.no.jar");
      OsmorcBundle.notification(title, message, NotificationType.WARNING).notify(project);
      return;
    }

    String manifestPath = jarFile.getPath() + JarFileSystem.JAR_SEPARATOR + JarFile.MANIFEST_NAME;
    VirtualFile manifestFile = JarFileSystem.getInstance().refreshAndFindFileByPath(manifestPath);
    if (manifestFile == null) {
      String title = OsmorcBundle.message("view.manifest.title"), message = OsmorcBundle.message("view.manifest.missing");
      OsmorcBundle.notification(title, message, NotificationType.WARNING).notify(project);
      return;
    }

    PsiNavigationSupport.getInstance().createNavigatable(project, manifestFile, -1).navigate(true);
  }
}
