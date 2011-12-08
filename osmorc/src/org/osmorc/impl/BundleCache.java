package org.osmorc.impl;

import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.valueobject.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The bundle cache holds information about all bundles within the project.
 */
public class BundleCache {

  private volatile Set<ManifestHolder> myManifestHolders;

  public BundleCache() {
    myManifestHolders = new HashSet<ManifestHolder>();
  }

  /**
   * Clears the bundle cache.
   */
  public synchronized void clear() {
    myManifestHolders = new HashSet<ManifestHolder>();
  }

  /**
   * Updates the cache with the given manifest holder.
   *
   * @param holder the holder
   * @return true, if the holder was added to the cache, false if the holder was already known.
   */
  public synchronized boolean updateWith(@NotNull final ManifestHolder holder) {
    if (!myManifestHolders.contains(holder)) {
      // copy on write
      HashSet<ManifestHolder> copy = new HashSet<ManifestHolder>(myManifestHolders);
      copy.add(holder);
      myManifestHolders = copy;
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Removes all stale holders from the cache.
   *
   * @return true if there were stale entries, false if nothing changed.
   */
  public synchronized boolean cleanup() {
    Set<ManifestHolder> toRemove = new HashSet<ManifestHolder>();
    for (ManifestHolder manifestHolder : myManifestHolders) {
      if (manifestHolder.isDisposed()) {
        toRemove.add(manifestHolder);
      }
    }
    if (toRemove.isEmpty()) {
      return false;
    }

    // copy on write
    HashSet<ManifestHolder> copy = new HashSet<ManifestHolder>(myManifestHolders);
    copy.removeAll(toRemove);
    myManifestHolders = copy;
    return true;
  }

  /**
   * Returns all manifest holders which provide the given package via export-package.
   *
   * @param packageSpec the package specification (may include version ranges)
   * @return set of matching manifest holders.
   */
  @NotNull
  public Set<ManifestHolder> whoProvides(@NotNull final String packageSpec) {
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
    return result;
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
    return result;
  }


  /**
   * Returns the  manifest holder that confirms to the required-bundle specification. If multiple entities match, the one with the 
   * highest version is returned.
   *
   * @param requiredBundleSpec the required bundle specificition
   * @return the first matching manifest holder, or null if there is no match
   */
  @Nullable
  public ManifestHolder whoIsRequiredBundle(@NotNull final String requiredBundleSpec) {
    List<ManifestHolder> candidates = new ArrayList<ManifestHolder>();
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
          candidates.add(manifestHolder);
        }
      }
    }
    
    if ( candidates.isEmpty() ) {
      return null;
    }
    
    if ( candidates.size() == 1 ) {
      return candidates.iterator().next();
    }

    ManifestHolder result = null;
    for (ManifestHolder candidate : candidates) {
        if ( result == null ) {
          result = candidate;
          continue;
        }

      BundleManifest resultManifest;
      
      try {
        resultManifest = result.getBundleManifest();
        if ( resultManifest == null ) {
          // weird but may happen
          result = candidate; // discard result and replace it with current candidate.
          continue;
        }
      }
      catch (ManifestHolderDisposedException e) {
        // ok result is gone, replace it with the candidate
        result = candidate;
        continue;
      }
      
      try {
        BundleManifest bundleManifest = candidate.getBundleManifest();
        if ( bundleManifest == null ) {
          // weird, but may happen, discard current candidate and go on with the next one
          continue;
        }
        Version candidateVersion = bundleManifest.getBundleVersion();
        Version resultVersion = resultManifest.getBundleVersion();
        if ( resultVersion.compareTo(candidateVersion) < 0 ) { // result version is smaller than candidate version
          result = candidate; // candidate becomes next result
        }
      }
      catch (ManifestHolderDisposedException e) {
        // may happen on rare occations in which case we ignore that candidate and keep the old result.
      }
    }
    
    return result;
  }

  /**
   * Returns the manifest holder for the given bundle object.
   *
   * @param bundle the bundle object
   * @return the manifest
   */
  @Nullable
  public ManifestHolder getManifestHolder(@NotNull final Object bundle) {
    for (ManifestHolder manifestHolder : myManifestHolders) {
      Object boundObject;
      try {
        boundObject = manifestHolder.getBoundObject();
      }
      catch (ManifestHolderDisposedException ignore) {
        continue;
      }
      if (boundObject.equals(bundle)) {
        return manifestHolder;
      }
    }
    return null;
  }
}
