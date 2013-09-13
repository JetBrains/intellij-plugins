package org.osmorc.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.manifest.ManifestHolderRegistry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Re-implementation of the bundle manager.
 */
public class BundleManagerImpl extends BundleManager {
  private static final Logger LOG = Logger.getInstance("#org.osmorc.impl.MyBundleManager");

  private BundleCache myBundleCache;
  private ManifestHolderRegistry myManifestHolderRegistry;
  private Project myProject;

  /**
   * Pattern which finds the jar filename in a path pattern from a Bundle-ClassPath header.
   */
  private static final Pattern JarPathPattern = Pattern.compile("(.*/)?([^/]+.jar)");

  public BundleManagerImpl(ManifestHolderRegistry manifestHolderRegistry, Project project) {
    myManifestHolderRegistry = manifestHolderRegistry;
    myProject = project;
    myBundleCache = new BundleCache();
  }

  @Override
  public void reindex(@NotNull Module module) {
    if (module.isDisposed()) {
      return; // don't work on disposed modules
    }

    if (!module.getProject().equals(myProject)) {
      LOG.warn("Someone tried to index a module that doesn't belong to my project.");
      return; // don't work on modules outside of the current project.
    }

    // if the module has an osmorc facet, treat it as a bundle and add it to the cache
    if (OsmorcFacet.hasOsmorcFacet(module)) {
      ManifestHolder manifestHolder = myManifestHolderRegistry.getManifestHolder(module);
      myBundleCache.updateWith(manifestHolder);

      CommonProcessors.CollectProcessor<Library> collector = new CommonProcessors.CollectProcessor<Library>();
      OrderEnumerator.orderEntries(module).forEachLibrary(collector);
      reindex(collector.getResults());

      myBundleCache.cleanup();
    }
  }

  @Override
  public void reindex(@NotNull Collection<Library> libraries) {
    for (Library library : libraries) {
      Collection<ManifestHolder> manifestHolders = myManifestHolderRegistry.getManifestHolders(library);
      for (ManifestHolder manifestHolder : manifestHolders) {
        myBundleCache.updateWith(manifestHolder);
      }
    }
    myBundleCache.cleanup();
  }

  @Override
  public void reindexAll() {
    // there are no osgi structures on project level, so we can simply get all modules and index these.
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      reindex(module);
    }

    // finally index the project level libraries
    Library[] libraries = ProjectLibraryTable.getInstance(myProject).getLibraries();
    reindex(Arrays.asList(libraries));
  }

  @Override
  @NotNull
  public Set<Object> resolveDependenciesOf(@NotNull final Module module) {
    BundleManifest manifest = getManifestByObject(module);
    if (manifest == null) {
      return Collections.emptySet();
    }

    // set of all manifest holders that are dependencies
    Set<ManifestHolder> dependencyHolders = new HashSet<ManifestHolder>();

    // resolve Import-Package
    List<String> imports = manifest.getImports();
    for (String anImport : imports) {
      dependencyHolders.addAll(myBundleCache.whoProvides(anImport));
    }

    // Resolve Require-Bundle
    List<String> requiredBundles = manifest.getRequiredBundles();
    List<ManifestHolder> allRequiredBundles = new ArrayList<ManifestHolder>();
    for (String requiredBundle : requiredBundles) {
      resolveRequiredBundle(requiredBundle, allRequiredBundles);
    }
    dependencyHolders.addAll(allRequiredBundles);

    // Resolve Fragment-Hosts
    ManifestHolder manifestHolder = myBundleCache.getManifestHolder(module);
    if (manifestHolder != null) {
      dependencyHolders.addAll(myBundleCache.getFragmentHosts(manifestHolder));
    }

    // finally extract result objects from holders.
    Set<Object> result = new HashSet<Object>();
    for (ManifestHolder holder : dependencyHolders) {
      try {
        result.add(holder.getBoundObject());
      }
      catch (ManifestHolderDisposedException ignored) { }
    }

    // Resolve Bundle-ClassPath (this might contain non-osgi-bundles so we have to work on the library level here)
    List<String> entries = manifest.getBundleClassPathEntries();
    result.addAll(resolveBundleClassPath(entries));
    return result;
  }

  /**
   * This method fully resolves a Require-Bundle specification including re-exports and possible amendments by fragments. All resolved
   * dependencies will be added to the <code>resolvedDependencies</code> list.
   *
   * @param requireBundleSpec    the spec to resolve
   * @param resolvedDependencies the resolved dependencies.
   */
  private void resolveRequiredBundle(@NotNull String requireBundleSpec, @NotNull List<ManifestHolder> resolvedDependencies) {
    // first get the manifest holder of the required bundle
    ManifestHolder manifestHolder = myBundleCache.whoIsRequiredBundle(requireBundleSpec);

    if (manifestHolder == null) {
      // unresolvable, may happen if the user misses some dependencies.
      return;
    }

    if (resolvedDependencies.contains(manifestHolder)) {
      // we're done here, we already resolved this dependency
      return;
    }

    BundleManifest requireBundleManifest;
    try {
      requireBundleManifest = manifestHolder.getBundleManifest();
    }
    catch (ManifestHolderDisposedException e) {
      // ok it's gone. Should rarely happen but in this case there is nothing we can do anymore.
      return;
    }

    if (requireBundleManifest != null) {
      // its kosher, so add it to the result list.
      resolvedDependencies.add(manifestHolder);

      // now determine additional dependencies
      List<String> toResolve = new ArrayList<String>();

      // -  bundles that are re-exported from the current dependency
      toResolve.addAll(requireBundleManifest.getReExportedBundles());

      // - bundles that are re-exported from any fragments
      Set<ManifestHolder> fragments = myBundleCache.getFragmentsForBundle(manifestHolder);

      // we only want the highest version of each fragment, so filter this out:
      fragments = BundleCache.getCandidatesWithHighestVersions(fragments);

      for (ManifestHolder fragment : fragments) {
        BundleManifest manifest = null;
        try {
          manifest = fragment.getBundleManifest();
        }
        catch (ManifestHolderDisposedException ignored) { }
        if (manifest != null) {
          toResolve.addAll(manifest.getReExportedBundles());
        }
      }

      // now recursively resolve these dependencies
      for (String dependencySpec : toResolve) {
        resolveRequiredBundle(dependencySpec, resolvedDependencies);
      }
    }
  }

  /**
   * Resolves the given bundle classpath entries.
   *
   * @param classPathEntries
   * @return a set of libraries that are dependencies according to the given classpath entries. Returns an empty set if no libraries
   *         could be found.
   */
  private Set<Library> resolveBundleClassPath(@NotNull Collection<String> classPathEntries) {
    Library[] libraries = ProjectLibraryTable.getInstance(myProject).getLibraries();

    Set<Library> result = new HashSet<Library>();
    for (String entry : classPathEntries) {
      Matcher matcher = JarPathPattern.matcher(entry);
      if (matcher.matches()) {
        String jarName = matcher.group(2);
        for (Library library : libraries) {
          String[] urls = library.getUrls(OrderRootType.CLASSES);
          for (String url : urls) {
            if (url.endsWith(jarName)) {
              result.add(library);
              break;
            }
          }
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
  private List<Object> whoIs(@NotNull String bundleSymbolicName) {
    List<ManifestHolder> holders = myBundleCache.whoIs(bundleSymbolicName);
    if (holders.isEmpty()) {
      return Collections.emptyList();
    }
    List<Object> result = new ArrayList<Object>(holders.size());
    for (ManifestHolder holder : holders) {
      try {
        result.add(holder.getBoundObject());
      }
      catch (ManifestHolderDisposedException ignore) { }
    }
    return result;
  }

  @Nullable
  @Override
  public BundleManifest getManifestByObject(@NotNull Object object) {
    ManifestHolder manifestHolder = myBundleCache.getManifestHolder(object);
    if (manifestHolder != null) {
      try {
        return manifestHolder.getBundleManifest();
      }
      catch (ManifestHolderDisposedException ignore) { }
    }
    return null;
  }

  @Nullable
  @Override
  public BundleManifest getManifestByBundleSpec(@NotNull String bundleSpec) {
    ManifestHolder manifestHolder = myBundleCache.whoIsRequiredBundle(bundleSpec);
    if (manifestHolder != null) {
      try {
        return manifestHolder.getBundleManifest();
      }
      catch (ManifestHolderDisposedException ignore) { }
    }
    return null;
  }

  @Override
  public BundleManifest getManifestBySymbolicName(@NotNull String bundleSymbolicName) {
    List<Object> objects = whoIs(bundleSymbolicName);
    if (!objects.isEmpty()) {
      return getManifestByObject(objects.get(0));
    }
    return null;
  }

  @Override
  public boolean isProvided(@NotNull String packageSpec) {
    return !myBundleCache.whoProvides(packageSpec).isEmpty();
  }

  /**
   * Allows to manually add manifest holders to the bundle manager. This is intended for tests only. Do not use in production code.
   */
  @TestOnly
  public void addManifestHolder(@NotNull ManifestHolder manifestHolder) {
    myBundleCache.updateWith(manifestHolder);
  }
}
