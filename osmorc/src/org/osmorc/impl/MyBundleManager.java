package org.osmorc.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.manifest.ManifestHolderRegistry;

import java.util.*;

/**
 * Re-implementation of the bundle manager.
 */
public class MyBundleManager implements BundleManager {


  private BundleCache myBundleCache;
  private ManifestHolderRegistry myManifestHolderRegistry;


  public MyBundleManager(ManifestHolderRegistry manifestHolderRegistry) {
    myManifestHolderRegistry = manifestHolderRegistry;
    myBundleCache = new BundleCache();
  }


  public void reindex(@NotNull Project project) {
    // clear cache and start fresh
    myBundleCache.clear();

    // there are no osgi structures on project level, so we can simply get all modules and index these.
    Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      reindex(module);
    }
  }


  public void reindex(@NotNull final Module module) {
    if ( module.isDisposed()) {
      return; // don't work on disposed modules
    }

    // if the module has an osmorc facet, treat it as a bundle and add it to the cache
    if (OsmorcFacet.hasOsmorcFacet(module)) {
      ManifestHolder manifestHolder = myManifestHolderRegistry.getManifestHolder(module);
      myBundleCache.updateWith(manifestHolder);
    }

    // now get all dependencies of the module (which are useful even if the module itself is not a bundle)
    ModifiableRootModel model = new ReadAction<ModifiableRootModel>() {
      protected void run(Result<ModifiableRootModel> result) throws Throwable {
        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        result.setResult(model);
      }
    }.execute().getResultObject();

    OrderEntry[] entries = model.getOrderEntries();
    try {
      for (int i = 0, entriesLength = entries.length; i < entriesLength; i++) {
        OrderEntry entry = entries[i];
        if (entry instanceof LibraryOrderEntry) {
          final Library library = ((LibraryOrderEntry)entry).getLibrary();
          if (library != null) {
            ManifestHolder manifestHolder = myManifestHolderRegistry.getManifestHolder(library);
            myBundleCache.updateWith(manifestHolder);
          }
        }
      }
    }
    finally {
      model.dispose();
    }
  }


  /**
   * Returns an object (a {@link Module} or a {@link Library}) which provides the package with the given package specification.
   *
   * @param packageSpec a package specification.
   * @return the best matching object or null, if no object within the project provides this package
   */
  @Nullable
  public Object whoProvidesBest(@NotNull String packageSpec) {
    ManifestHolder holder = myBundleCache.whoProvidesBest(packageSpec);
    if (holder != null) {
      try {
        return holder.getBoundObject();
      }
      catch (ManifestHolderDisposedException e) {
        myBundleCache.markUnclean();
        return null;
      }
    }
    return null;
  }



  @Override
  @NotNull
  public Set<Object> resolveDependenciesOf(@NotNull Module module) {
    BundleManifest manifest = getManifestByObject(module);
    if (manifest == null) {
      return Collections.emptySet();
    }

    Set<Object> result = new HashSet<Object>();
    List<String> imports = manifest.getImports();
    for (String anImport : imports) {
      Object o = whoProvidesBest(anImport);
      if (o != null) {
        result.add(o);
      }
    }

    List<String> requiredBundles = manifest.getRequiredBundles();
    for (String requiredBundle : requiredBundles) {
      ManifestHolder manifestHolder = myBundleCache.whoIsRequiredBundle(requiredBundle);
      if ( manifestHolder != null ) {
        try {
          result.add(manifestHolder.getBoundObject());
        }
        catch (ManifestHolderDisposedException e) {
          // ok, ignore it then.
          myBundleCache.markUnclean();
        }
      }
    }

    return result;
  }


  /**
   * Returns a list of objects (a Module or a Library) which represent the bundle with the given bundle symbolic name. Most of the time there
   * will be only one entry in the list, as usually there is only one module or library which represents the given bundle. However
   * there might be the case when multiple versions of a bundle are in the project. That's when this method will return more than one
   * entry in the list.
   *
   * @param bundleSymbolicName the bundle symbolic name to lookup
   * @return the object representing
   */
  @NotNull
  public List<Object> whoIs(@NotNull String bundleSymbolicName) {
    List<ManifestHolder> holders = myBundleCache.whoIs(bundleSymbolicName);
    if (holders.isEmpty()) {
      return Collections.emptyList();
    }
    List<Object> result = new ArrayList<Object>(holders.size());
    for (ManifestHolder holder : holders) {
      try {
        result.add(holder.getBoundObject());
      }
      catch (ManifestHolderDisposedException e) {
        // ok, ignore it then. notify cache of stale entries.
        myBundleCache.markUnclean();
      }
    }
    return result;
  }


  @Override
  public boolean isReExported(@NotNull Object dependency, @NotNull Module module) {
    BundleManifest depManifest = getManifestByObject(dependency);
    BundleManifest moduleManifest = getManifestByObject(module);

    if ( depManifest == null || moduleManifest == null ) {
      return false;
    }

    return moduleManifest.reExportsBundle(depManifest);
  }


  /**
   * Returns a bundle manifest for the given object.
   *
   * @param object the bundle (a Module or a Library)
   * @return the manifest for this object or null, if the given object does not constitute a bundle or is not in cache.
   */
  @Nullable
  public BundleManifest getManifestByObject(@NotNull Object object) {
    ManifestHolder manifestHolder = myBundleCache.getManifestHolder(object);
    if (manifestHolder != null) {
      try {
        return manifestHolder.getBundleManifest();
      }
      catch (ManifestHolderDisposedException e) {
        // in that case the objec was already disposed.
        myBundleCache.markUnclean();
        return null;
      }
    }
    return null;
  }

  public BundleManifest getManifestBySymbolicName(@NotNull String bundleSymbolicName) {
    List<Object> objects = whoIs(bundleSymbolicName);
    if (!objects.isEmpty()) {
      return getManifestByObject(objects.get(0));
    }
    return null;
  }
}
