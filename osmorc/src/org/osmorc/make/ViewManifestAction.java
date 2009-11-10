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

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

/**
 * Action which allows view the manifest of a given jar file. The action will open the manifest in a new editor tab.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class ViewManifestAction extends AnAction implements DumbAware {
    private String pathToJar;

    public ViewManifestAction(String title, String pathToJar) {
        this.pathToJar = pathToJar;
        getTemplatePresentation().setText(title);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        @Nullable Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
        if (project == null) {
            return;
        }

        VirtualFile jarFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(pathToJar);
        if (jarFile == null) {
            Messages.showErrorDialog(project, "It seems that the bundle JAR does not exist. Please rebuild the project and try again.", "Cannot open manifest");
            return;
        }

        // that seems a rather cumbersome way of opening a file... maybe there is a more efficient way to do this....
        VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(jarFile);
        if (jarRoot != null) {
            final VirtualFile manifestFile = jarRoot.findFileByRelativePath("META-INF/MANIFEST.MF");

            if (manifestFile == null) {
                Messages.showErrorDialog(project, "There is no manifest in the bundle jar. Please check the facet settings, rebuild and try again.", "Cannot open manifest");
                return;
            }

            FileEditorProviderManager editorProviderManager = FileEditorProviderManager.getInstance();
            if (editorProviderManager.getProviders(project, manifestFile).length == 0) {
                Messages.showMessageDialog(project, IdeBundle.message("error.files.of.this.type.cannot.be.opened",
                                ApplicationNamesInfo.getInstance().getProductName()),
                        IdeBundle.message("title.cannot.open.file"), Messages.getErrorIcon());
                return;
            }

            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, manifestFile);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        }
    }
}
