/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.GemModificationUtil;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.motion.paramdefs.RubyMotionParamdefsProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.tasks.rake.RakeUtilBase;
import org.jetbrains.plugins.ruby.utils.VirtualFileUtil;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionFacet extends Facet<RubyMotionFacetConfiguration> {
  public RubyMotionFacet(@NotNull final FacetType facetType,
                  @NotNull final Module module,
                  @NotNull final String name,
                  @NotNull final RubyMotionFacetConfiguration configuration,
                  final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
  }

  @Override
  public void initFacet() {
    PsiManager.getInstance(getModule().getProject()).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      @Override
      public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        resetCachesIfNeeded(event);
      }

      @Override
      public void childAdded(@NotNull PsiTreeChangeEvent event) {
        resetCachesIfNeeded(event);
      }

      @Override
      public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        resetCachesIfNeeded(event);
      }

      @Override
      public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        resetCachesIfNeeded(event);
      }

      @Override
      public void childMoved(@NotNull PsiTreeChangeEvent event) {
        resetCachesIfNeeded(event);
      }
    });
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      RubyMotionParamdefsProvider.ensureParamdefsLoaded();
    }
  }

  private void resetCachesIfNeeded(final PsiTreeChangeEvent event) {
    final PsiFile file = event.getFile();
    if (file instanceof RFile && RakeUtilBase.RAKE_FILE.equals(file.getName())) {
      RubyMotionUtil.getInstance().resetSdkAndFrameworks(getModule());
    }
  }

  @Nullable
  public static RubyMotionFacet getInstance(@NotNull final Module module) {
    if (module.isDisposed()) {
      return null;
    }
    return FacetManager.getInstance(module).getFacetByType(RubyMotionFacetType.ID);
  }

  public static void updateMotionLibrary(final ModifiableRootModel model) {
    WriteAction.run(() -> {
      boolean librarySeen = false;
      for (OrderEntry entry : model.getOrderEntries()) {
        if (entry instanceof LibraryOrderEntry) {
          final String libraryName = ((LibraryOrderEntry)entry).getLibraryName();
          if (RubyMotionUtil.RUBY_MOTION_LIBRARY.equals(libraryName)) {
            librarySeen = true;
            break;
          }
        }
      }
      if (!librarySeen) {
        Library library = LibraryTablesRegistrar.getInstance().getLibraryTable().getLibraryByName(RubyMotionUtil.RUBY_MOTION_LIBRARY);
        if (library == null) {
          // we just create new project library
          library = createLibrary();
        }
        if (library != null) {
          final LibraryOrderEntry libraryOrderEntry = model.addLibraryEntry(library);
          libraryOrderEntry.setScope(DependencyScope.PROVIDED);
        }
      }
    });
  }

  @Nullable
  private static Library createLibrary() {
    final VirtualFile motion = VirtualFileUtil.findFileBy(RubyMotionUtil.getInstance().getRubyMotionPath());
    if (motion == null) return null;

    final LibraryTable.ModifiableModel model = GemModificationUtil.getLibraryTableModifiableModel();
    final Library library = model.createLibrary(RubyMotionUtil.RUBY_MOTION_LIBRARY);
    final Library.ModifiableModel libModel = library.getModifiableModel();
    for (VirtualFile child : motion.getChildren()) {
      if (child != null) {
        libModel.addRoot(child, OrderRootType.CLASSES);
        libModel.addRoot(child, OrderRootType.SOURCES);
      }
    }
    libModel.commit();
    model.commit();
    return library;
  }
}
