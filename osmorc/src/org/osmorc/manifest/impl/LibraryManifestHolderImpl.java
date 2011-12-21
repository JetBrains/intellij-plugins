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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.manifest.lang.psi.ManifestFile;

import java.util.*;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class LibraryManifestHolderImpl extends AbstractManifestHolderImpl {

  private BundleManifest myBundleManifest;
  private final Library myLibrary;
  private final Project myProject;
  private final String myPath;

  private LibraryManifestHolderImpl(Library library, Project project, String rootPath) {
    myLibrary = library;
    myProject = project;
    myPath = rootPath;
  }

  @Nullable
  public BundleManifest getBundleManifest() throws ManifestHolderDisposedException {
    // FIX for EA-23586
    if (isDisposed()) {
      throw new ManifestHolderDisposedException();
    }

    if (myBundleManifest == null) {

      VirtualFile[] classRoots = myLibrary.getFiles(OrderRootType.CLASSES);
      for (VirtualFile classRoot : classRoots) {
        if (classRoot.getUrl().equals(myPath)) {
          VirtualFile classDir;
          if (classRoot.isDirectory()) {
            classDir = classRoot;
          }
          else {
            classDir = JarFileSystem.getInstance().getJarRootForLocalFile(classRoot);
          }

          if (classDir != null) {
            final VirtualFile manifestFile = classDir.findFileByRelativePath("META-INF/MANIFEST.MF");
            if (manifestFile != null) {
              PsiFile psiFile = new ReadAction<PsiFile>() {
                @Override
                protected void run(Result<PsiFile> psiFileResult) throws Throwable {
                  psiFileResult.setResult(PsiManager.getInstance(myProject).findFile(manifestFile));
                }
              }.execute().getResultObject();
              myBundleManifest = new BundleManifestImpl((ManifestFile)psiFile);
            }
          }
          break; // we're done here.
        }
      }
    }
    return myBundleManifest;
  }

  public boolean isDisposed() {
    return isLibraryDisposed(myLibrary)  || myProject.isDisposed();
  }


  private static boolean isLibraryDisposed(Library library) {
    return library instanceof LibraryEx && ((LibraryEx)library).isDisposed();
  }

  /**
   * Creats manifest holders of the given library and project
   *
   * @param library the library
   * @param project the prorject
   * @return the manifest holders
   */
  @NotNull
  protected static synchronized Collection<ManifestHolder> createForLibrary(@NotNull Library library, @NotNull Project project) {
    cleanupHolderCache();
    List<ManifestHolder> result = new ArrayList<ManifestHolder>();
    if ( isLibraryDisposed(library) || project.isDisposed()  ) {
      return result;
    }
    VirtualFile[] classRoots = library.getFiles(OrderRootType.CLASSES);
    for (VirtualFile classRoot : classRoots) {
      String jarFileUrl = classRoot.getUrl();
      ManifestHolder cachedHolder = myHolderCache.get(jarFileUrl);
      if (cachedHolder != null) {
        result.add(cachedHolder);
        continue;
      }
      VirtualFile classDir;
      if (classRoot.isDirectory()) {
        classDir = classRoot;
      }
      else {
        classDir = JarFileSystem.getInstance().getJarRootForLocalFile(classRoot);
      }

      if (classDir != null) {
        final VirtualFile manifestFile = classDir.findFileByRelativePath("META-INF/MANIFEST.MF");
        if (manifestFile != null) {
          // potential bundle
          final LibraryManifestHolderImpl newHolder = new LibraryManifestHolderImpl(library, project, jarFileUrl);
          
          // kill reference to the project when it is being disposed. (project leak fix by Alexey Kudravtsev)
          Disposer.register(project, new Disposable() {
            @Override
            public void dispose() {
              synchronized (LibraryManifestHolderImpl.class) {  // make sure there is no concurrent access to the holder cache.
                for (Iterator<LibraryManifestHolderImpl> iterator = myHolderCache.values().iterator(); iterator.hasNext(); ) {
                  LibraryManifestHolderImpl holder = iterator.next();
                  if ( holder == newHolder ) {
                    iterator.remove();
                    break;
                  }
                }
              }
            }
          });
          
          myHolderCache.put(jarFileUrl, newHolder);
          result.add(newHolder);
        }
      }
    }
    return result;
  }


  /**
   * Removes disposed holders from the holder cache.
   */
  private static void cleanupHolderCache() {
    for (Iterator<Map.Entry<String,LibraryManifestHolderImpl>> iterator = myHolderCache.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry<String, LibraryManifestHolderImpl> entry = iterator.next();
      if (entry.getValue().isDisposed()) {
        iterator.remove();
      }
    }
  }

  /**
   * Cache for generated manifest holders. This is to make sure we don't create two manifest holders for the same jar file.
   */
  private static final Map<String, LibraryManifestHolderImpl> myHolderCache = new HashMap<String, LibraryManifestHolderImpl>();

  @Override
  public Object getBoundObject() throws ManifestHolderDisposedException {
    if (isDisposed()) {
      throw new ManifestHolderDisposedException();
    }
    return myLibrary;
  }
}
