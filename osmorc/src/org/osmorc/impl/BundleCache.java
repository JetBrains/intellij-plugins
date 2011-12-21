package org.osmorc.impl;

import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.valueobject.Version;

import java.util.*;

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
   * Returns a set of  manifest holders that represent fragments of the given manifest holder. Note, that this is a potentially costly operation
   * when there are many manifests.
   *
   * @param bundle the manifest holder to find the fragments for.
   * @return a set of matching manifest holders. If there are no fragments known, returns an empty set.
   */
  @NotNull
  public Set<ManifestHolder> getFragmentsForBundle(@NotNull ManifestHolder bundle) {
    try {
      BundleManifest bundleManifest = bundle.getBundleManifest();
      // if it has no manifest, we can short cut here
      if (bundleManifest == null) {
        return Collections.emptySet();
      }

      Set<ManifestHolder> result = new HashSet<ManifestHolder>();
      for (ManifestHolder manifestHolder : myManifestHolders) {
        try {
          BundleManifest potentialFragmentManifest = manifestHolder.getBundleManifest();
          if (potentialFragmentManifest == null) {
            continue;
          }
          if (bundleManifest.isFragmentHostFor(potentialFragmentManifest)) {
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
   * @param requiredBundleSpec the required bundle specification
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

    return getCandidateWithHighestVersion(candidates);
  }

 

  /**
   * Helper function which takes a collection of manifest holders, creates sets of manifest holders that have the same bundle symbolic name
   * and returns the manifest holder with the highest version for each set.
   *
   * @param manifestHolders the manifest holders
   * @return a set with an entry for each set of manifest holders that have the same bundle symbolic name. The entry is the manifest holder
   *         with the highest version in the respective set. If no highest version can be determined for any given set, then this result
   *         set will not contain an entry for the respective set.
   */
  public static Set<ManifestHolder> getCandidatesWithHighestVersions(@NotNull Collection<ManifestHolder> manifestHolders) {
    Map<String, Set<ManifestHolder>> sets = new HashMap<String,Set<ManifestHolder>>();
    
    // first build sets of entries with the same bundle symbolic name. This will also eliminate duplicate holders.
    for (ManifestHolder holder : manifestHolders) {
      try {
        BundleManifest manifest = holder.getBundleManifest();
        if ( manifest != null ) {
          String bundleSymbolicName = manifest.getBundleSymbolicName();
          Set<ManifestHolder> set = null;
          if ( sets.containsKey(bundleSymbolicName)) {
            set = sets.get(bundleSymbolicName);
          }
          else {
            set = new HashSet<ManifestHolder>();
            sets.put(bundleSymbolicName, set);
          }
          set.add(holder);
        }
      }
      catch (ManifestHolderDisposedException e) {
        // its gone, ignore it
      }
    }
    Set<ManifestHolder> result = new HashSet<ManifestHolder>();
    for (Set<ManifestHolder> holders : sets.values()) {
      ManifestHolder candidateWithHighestVersion = getCandidateWithHighestVersion(holders);
      if ( candidateWithHighestVersion != null ) {
        result.add(candidateWithHighestVersion);
      }
    }

    return result;
  }

  /**
   * Helper method which allows filtering a list of manifest holders and return the candidate with the highest version. It is assumed
   * that the list only contains manifest holders with the same bundle-symbolic name. This method will not check this assumption and will
   * also not depend on it, so the GIGO principle applies.
   *
   * @param candidates the list of candidates
   * @return the candidate with the highest version or null if no such candidate could be determined.
   */
  @Nullable
  public static ManifestHolder getCandidateWithHighestVersion(@NotNull Collection<ManifestHolder> candidates) {
    if (candidates.isEmpty()) {
      return null;
    }

    if (candidates.size() == 1) {
      return candidates.iterator().next();
    }

    ManifestHolder result = null;
    for (ManifestHolder candidate : candidates) {
      if (result == null) {
        result = candidate;
        continue;
      }

      BundleManifest resultManifest;

      try {
        resultManifest = result.getBundleManifest();
        if (resultManifest == null) {
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
        if (bundleManifest == null) {
          // weird, but may happen, discard current candidate and go on with the next one
          continue;
        }
        Version candidateVersion = bundleManifest.getBundleVersion();
        Version resultVersion = resultManifest.getBundleVersion();
        if (resultVersion.compareTo(candidateVersion) < 0) { // result version is smaller than candidate version
          result = candidate; // candidate becomes next result
        }
      }
      catch (ManifestHolderDisposedException e) {
        // may happen on rare occasions in which case we ignore that candidate and keep the old result.
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
