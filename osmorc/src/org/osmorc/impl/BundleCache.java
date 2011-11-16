package org.osmorc.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;

import java.util.*;

/**
 * The bundle cache holds information about all bundles within the project.
 */
public class BundleCache {

  private Set<ManifestHolder> myManifestHolders;

  public BundleCache() {
    myManifestHolders = new HashSet<ManifestHolder>();
  }

  /**
   * Clears the bundle cache.
   */
  public void clear() {
    new WriteAction() {
      @Override
      protected void run(Result result) throws Throwable {
        myManifestHolders.clear();
      }
    }.execute();
  }

  /**
   * Updates the cache with the given manifest holder.
   *
   * @param holder the holder
   * @return true, if the holder was added to the cache, false if the holder was already known.
   */
  public boolean updateWith(@NotNull final ManifestHolder holder) {
    return new WriteAction<Boolean>() {
      @Override
      protected void run(Result<Boolean> result) throws Throwable {
        if (!myManifestHolders.contains(holder)) {
          myManifestHolders.add(holder);
          result.setResult(true);
        }
        else {
          result.setResult(false);
        }
      }
    }.execute().getResultObject();
  }

  /**
   * Removes all stale holders from the cache.
   *
   * @return true if there were stale entries, false if nothing changed.
   */
  public boolean cleanup() {
    return new WriteAction<Boolean>() {
      @Override
      protected void run(Result<Boolean> result) throws Throwable {
        result.setResult(false);
        for (Iterator<ManifestHolder> iterator = myManifestHolders.iterator(); iterator.hasNext(); ) {
          ManifestHolder manifestHolder = iterator.next();
          if (manifestHolder.isDisposed()) {
            iterator.remove();
            result.setResult(true);
          }
        }
      }
    }.execute().getResultObject();
  }

  /**
   * Returns all manifest holders which provide the given package via export-package.
   *
   * @param packageSpec the package specification (may include version ranges)
   * @return set of matching manifest holders.
   */
  @NotNull
  public Set<ManifestHolder> whoProvides(@NotNull final String packageSpec) {
    return new ReadAction<Set<ManifestHolder>>() {
      @Override
      protected void run(Result<Set<ManifestHolder>> manifestHolderResult) throws Throwable {
        Set<ManifestHolder> result = new HashSet<ManifestHolder>();
        for (ManifestHolder manifestHolder : myManifestHolders) {
          BundleManifest bundleManifest;
          try {
            bundleManifest = manifestHolder.getBundleManifest();
          }
          catch (ManifestHolderDisposedException ignore) {
            // ok this thing is gone
            continue;
          }
          if (bundleManifest != null) {
            if (bundleManifest.exportsPackage(packageSpec)) {
              result.add(manifestHolder);
            }
          }
        }
        manifestHolderResult.setResult(result);
      }
    }.execute().getResultObject();
  }

  /**
   * Gets all known fragment hosts for the given fragment.
   *
   * @param fragment the fragment
   * @return a set of fragment hosts. returns an empty set if no hosts could be found or if the given manifest holder does not represent a fragment bundle.
   */
  @NotNull
  public Set<ManifestHolder> getFragmentHosts(@NotNull ManifestHolder fragment) {
    try {
      BundleManifest fragmentManifest = fragment.getBundleManifest();
      // if its not a fragment or has no manifest, we can short cut here
      if (fragmentManifest == null || !fragmentManifest.isFragmentBundle()) {
        return Collections.emptySet();
      }

      Set<ManifestHolder> result = new HashSet<ManifestHolder>();
      for (ManifestHolder manifestHolder : myManifestHolders) {
        try {
          BundleManifest potentialHostManifest = manifestHolder.getBundleManifest();
          if (potentialHostManifest == null) {
            continue;
          }
          if (potentialHostManifest.isFragmentHostFor(fragmentManifest)) {
            result.add(manifestHolder);
          }
        }
        catch (ManifestHolderDisposedException ignore) {
          // ignore.
        }
      }
      return result;
    }
    catch (ManifestHolderDisposedException ignore) {
      return Collections.emptySet();
    }
  }

  /**
   * Returns  the manifest holders that have the given symbolic name.
   *
   * @param bundleSymbolicName the symbolic name
   * @return the matching manifest holders. If no holder matches, returns an empty list.
   */
  @NotNull
  public List<ManifestHolder> whoIs(@NotNull final String bundleSymbolicName) {
    return new ReadAction<List<ManifestHolder>>() {
      @Override
      protected void run(Result<List<ManifestHolder>> listResult) throws Throwable {
        List<ManifestHolder> result = new ArrayList<ManifestHolder>();
        for (ManifestHolder manifestHolder : myManifestHolders) {
          BundleManifest bundleManifest;
          try {
            bundleManifest = manifestHolder.getBundleManifest();
          }
          catch (ManifestHolderDisposedException ignore) {
            continue;
          }
          if (bundleManifest != null) {
            if (bundleSymbolicName.equals(bundleManifest.getBundleSymbolicName())) {
              result.add(manifestHolder);
            }
          }
        }
        listResult.setResult(result);
      }
    }.execute().getResultObject();
  }


  /**
   * Returns the first manifest holder that confirms to the required-bundle specification
   *
   * @param requiredBundleSpec the required bundle specificition
   * @return the first matching manifest holder, or null if there is no match
   */
  @Nullable
  public ManifestHolder whoIsRequiredBundle(@NotNull final String requiredBundleSpec) {
    return new ReadAction<ManifestHolder>() {
      @Override
      protected void run(Result<ManifestHolder> result) throws Throwable {
        for (ManifestHolder manifestHolder : myManifestHolders) {
          BundleManifest bundleManifest;
          try {
            bundleManifest = manifestHolder.getBundleManifest();
          }
          catch (ManifestHolderDisposedException ignore) {
            // this thing is gone
            continue;
          }
          if (bundleManifest != null) {
            if (bundleManifest.isRequiredBundle(requiredBundleSpec)) {
              result.setResult(manifestHolder);
              return;
            }
          }
        }
      }
    }.execute().getResultObject();
  }

  /**
   * Returns the manifest holder for the given bundle object.
   *
   * @param bundle the bundle object
   * @return the manifest
   */
  @Nullable
  public ManifestHolder getManifestHolder(@NotNull final Object bundle) {
    return new ReadAction<ManifestHolder>() {
      @Override
      protected void run(Result<ManifestHolder> manifestHolderResult) throws Throwable {
        for (ManifestHolder manifestHolder : myManifestHolders) {
          Object boundObject;
          try {
            boundObject = manifestHolder.getBoundObject();
          }
          catch (ManifestHolderDisposedException ignore) {
            continue;
          }
          if (boundObject.equals(bundle)) {
            manifestHolderResult.setResult(manifestHolder);
            return;
          }
        }
      }
    }.execute().getResultObject();
  }
}
