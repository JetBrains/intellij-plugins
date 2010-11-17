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
package org.osmorc.manifest.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.osmorc.manifest.BundleManifest;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class LibraryManifestHolderImpl extends AbstractManifestHolderImpl
{
  public LibraryManifestHolderImpl(Library library, Project project)
  {
    _library = library;
    _project = project;
  }

  public BundleManifest getBundleManifest()
  {
    if (_bundleManifest == null)
    {
      // FIX for EA-23586
      if ( _library instanceof LibraryEx && ((LibraryEx)_library).isDisposed() ) {
        return _bundleManifest;
      }
      VirtualFile[] classRoots = _library.getFiles(OrderRootType.CLASSES);
      for (VirtualFile classRoot : classRoots)
      {
        VirtualFile classDir;
        if (classRoot.isDirectory())
        {
          classDir = classRoot;
        }
        else
        {
          classDir = JarFileSystem.getInstance().getJarRootForLocalFile(classRoot);
        }

        if (classDir != null)
        {
          final VirtualFile manifestFile = classDir.findFileByRelativePath("META-INF/MANIFEST.MF");
          if (manifestFile != null)
          {
            PsiFile psiFile = new ReadAction<PsiFile>() {
              @Override
              protected void run(Result<PsiFile> psiFileResult) throws Throwable {
                psiFileResult.setResult(PsiManager.getInstance(_project).findFile(manifestFile));
              }

            }.execute().getResultObject();
            _bundleManifest = new BundleManifestImpl(psiFile);
          }
        }
      }
    }
    return _bundleManifest;
  }

  private BundleManifest _bundleManifest;
  private final Library _library;
  private final Project _project;
}
