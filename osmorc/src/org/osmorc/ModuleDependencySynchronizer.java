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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class synchronizes dependencies by using the manifest entries of the bundles to determine the bundle dependencies. It will
 * only synchronize the dependencies of bundles whose manifests are manually edited. Bundles with generated manifests usually work
 * the other way around (that is, the user manually specifies dependencies and the Manifest entries are generated), so that
 * dependency synchronizing is not useful for bundles with generated manifests.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author Jan Thom&auml; (janthomae@janthomae.de)
 */
public class ModuleDependencySynchronizer {

  private final ModuleRootManager myModuleRootManager;
  private final Application myApplication;
  private final BundleManager myBundleManager;
  private final Module myModule;

  /**
   * Provides an instance of the module dependency synchronizer for the given module.
   *
   * @param module the module
   * @return the ModuleDependencySynchronizer for the given module
   */
  @NotNull
  private static ModuleDependencySynchronizer getInstance(@NotNull Module module) {
    return ModuleServiceManager.getService(module, ModuleDependencySynchronizer.class);
  }


  /**
   * Resynchronizes all module dependencies within the given project. Only dependencies of modules with osmorc facets with manually
   * edited manifests are synced, so it is safe to call this method in any project. Must be called from the event dispatch thread.
   *
   * @param project the project the project that should be synchronized.
   */
  public static void resynchronizeAll(@NotNull final Project project) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(project, "Synchronizing OSGi dependencies", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);
            // sync the dependencies of ALL modules
            ModuleManager instance = ModuleManager.getInstance(project);
            Module[] modules = instance.getModules();
            for (Module module : modules) {
              getInstance(module).syncDependenciesFromManifest();
            }
          }
        }.queue();
      }
    },project.getDisposed());
  }

  /**
   * Ctor. This is invoked by IDEA's service manager. Use {@link #getInstance(Module)} to acquire an
   * instance of this class.
   *
   * @param bundleManager     the bundle manager
   * @param moduleRootManager the rootManager for the wrapped module
   * @param application       the application
   */
  public ModuleDependencySynchronizer(BundleManager bundleManager, ModuleRootManager moduleRootManager,
                                      Application application) {
    this.myBundleManager = bundleManager;
    this.myModuleRootManager = moduleRootManager;
    this.myApplication = application;
    myModule = this.myModuleRootManager.getModule();
  }

  /**
   * Synchronizes the wrapped module's dependencies. This method can be safely called for all modules as it will check if the
   * module has an Osmorc facet and it will also check if the manifest of this module is manually edited before doing the synchronization.
   * If the two prerequisites are not met, this method will do nothing.
   */
  public void syncDependenciesFromManifest() {
    OsmorcFacet facet = OsmorcFacet.getInstance(myModule);
    if (facet != null && facet.getConfiguration().isManifestManuallyEdited()) {
      myApplication.invokeAndWait(new Runnable() {
        public void run() {
          final ModifiableRootModel model = myModuleRootManager.getModifiableModel();
          Set<Object> newDependencies = myBundleManager.resolveDependenciesOf(myModule);

          List<OrderEntry> oldOrderEntries = determineOldModuleDependencies(model);
          List<OrderEntry> obsoleteOrderEntries = determineObsoleteModuleDependencies(oldOrderEntries, newDependencies);

          boolean commitNeeded = false;

          //noinspection ConstantConditions
          commitNeeded |= removeDependenciesFrom(model, obsoleteOrderEntries);
          commitNeeded |= addNewModuleDependencies(model, newDependencies);
          commitNeeded |= checkAndSetReexport(model, newDependencies);

          if (commitNeeded) {
            myApplication.runWriteAction(new Runnable() {
              @Override
              public void run() {
                model.commit();
              }
            });
          }
          else {
            model.dispose();
          }
        }
      }, ModalityState.NON_MODAL);
    }
  }

  /**
   * Returns a list of all order entries of the given model that constitute module or library dependencies.
   *
   * @param model the model.
   * @return a list of current module dependencies.
   */
  @NotNull
  private static List<OrderEntry> determineOldModuleDependencies(@NotNull ModifiableRootModel model) {
    List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
    for (OrderEntry oldOrderEntry : model.getOrderEntries()) {
      if (oldOrderEntry instanceof ModuleOrderEntry || oldOrderEntry instanceof LibraryOrderEntry) {
        oldOrderEntries.add(oldOrderEntry);
      }
    }
    return oldOrderEntries;
  }

  /**
   * Finds out which order entries of the given <code>oldOrderEntry</code> list are not in the collection <code>newDependencies</code>.
   * It will also remove all entries from the <code>newDependencies</code> collection that are already linked to in the oldOrderEntries collection.
   *
   * @param oldOrderEntries the old order entries
   * @param newDependencies the new dependencies. This collection should contain {@link Module} and {@link Library} objects, not {@link OrderEntry}s.
   * @return the list of obsolete dependencies that can be removed from the module.
   */
  @NotNull
  private static List<OrderEntry> determineObsoleteModuleDependencies(@NotNull List<OrderEntry> oldOrderEntries,
                                                                      @NotNull Collection<Object> newDependencies) {
    List<OrderEntry> result = new ArrayList<OrderEntry>();

    for (OrderEntry oldOrderEntry : oldOrderEntries) {
      boolean found = false;

      if (oldOrderEntry instanceof ModuleOrderEntry) {
        found = newDependencies.remove(((ModuleOrderEntry)oldOrderEntry).getModule());
      }
      else if (oldOrderEntry instanceof LibraryOrderEntry) {
        found = newDependencies.remove(((LibraryOrderEntry)oldOrderEntry).getLibrary());
      }

      if (!found) {
        result.add(oldOrderEntry);
      }
    }

    return result;
  }

  /**
   * Removes the given order entries from the given model.
   *
   * @param model                the model from which the dependencies should be removed.
   * @param dependenciesToRemove the dependencies to be removed.
   * @return true, if the model was modified and requires a commit, false otherwise.
   */
  private static boolean removeDependenciesFrom(@NotNull ModifiableRootModel model, @NotNull List<OrderEntry> dependenciesToRemove) {
    boolean commitNeeded = false;

    for (OrderEntry orderEntry : dependenciesToRemove) {
      model.removeOrderEntry(orderEntry);
      commitNeeded = true;
    }
    return commitNeeded;
  }

  /**
   * Adds the given dependencies to the given model.
   *
   * @param model           the model to which the dependencies should be added.
   * @param newDependencies the new dependencies. This collection should contain {@link Module} and {@link Library} objects, not {@link OrderEntry}s.
   * @return true, if the model was modified and requires a commit, false otherwise.
   */
  private boolean addNewModuleDependencies(@NotNull ModifiableRootModel model, @NotNull Collection<Object> newDependencies) {
    boolean commitNeeded = false;
    for (Object newBundle : newDependencies) {
      if (newBundle instanceof Module && newBundle != myModule) {
        model.addModuleOrderEntry((Module)newBundle);
        commitNeeded = true;
      }
      else if (newBundle instanceof Library && !(newBundle instanceof LibraryEx && ((LibraryEx)newBundle).isDisposed())) {
        model.addLibraryEntry((Library)newBundle);
        commitNeeded = true;
      }
    }
    return commitNeeded;
  }

  /**
   * Checks if any of the dependencies of the given model should be re-exported and sets the appropriate flags
   *
   * @param model      the model
   * @param newBundles the new dependencies. This collection should contain {@link Module} and {@link Library} objects, not {@link OrderEntry}s.
   * @return true ift he model was modified and requires a commit, false otherwise.
   */
  protected boolean checkAndSetReexport(ModifiableRootModel model, Collection<Object> newBundles) {
    boolean commitNeeded = false;

    for (OrderEntry orderEntry : model.getOrderEntries()) {
      if (orderEntry instanceof ModuleOrderEntry) {
        ModuleOrderEntry moduleOrderEntry = (ModuleOrderEntry)orderEntry;
        Module module = moduleOrderEntry.getModule();
        if (module != null && OsmorcFacet.hasOsmorcFacet(module)) {
          boolean export = myBundleManager.isReExported(moduleOrderEntry.getModule(), myModule);
          if (export != moduleOrderEntry.isExported()) {
            moduleOrderEntry.setExported(export);
            commitNeeded = true;
          }
        }
      }
      else if (orderEntry instanceof LibraryOrderEntry) {
        LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry)orderEntry;
        Library library = libraryOrderEntry.getLibrary();
        if (library == null) {
          for (Object newBundle : newBundles) {
            if (newBundle instanceof Library &&
                ((Library)newBundle).getName().equals(libraryOrderEntry.getLibraryName())) {
              library = (Library)newBundle;
              break;
            }
          }
        }
        if (library != null) {
          boolean export = myBundleManager.isReExported(library, myModule);
          if (export != libraryOrderEntry.isExported()) {
            libraryOrderEntry.setExported(export);
            commitNeeded = true;
          }
        }
      }
    }
    return commitNeeded;
  }
}
