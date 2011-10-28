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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The bundle cache holds information about all bundles within the project.
 */
public class BundleCache {

  private Set<ManifestHolder> myManifestHolders;
  private boolean myIsUnclean;

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
   * Updates the cache with the given manifest holder. Also performs a cleanup of stale entries
   *
   * @param holder the holder
   */
  public void updateWith(@NotNull final ManifestHolder holder) {
    new WriteAction() {
      @Override
      protected void run(Result result) throws Throwable {
        if (!myManifestHolders.contains(holder)) {
          myManifestHolders.add(holder);
        }
        cleanup();
      }
    }.execute();
  }

  /**
   * Marks the cache as being unclean (containing stale entries).
   */
  public void markUnclean() {
    myIsUnclean = true;
  }

  /**
   * Removes all stale holders from the cache. This will do nothing if the cache has not been marked as unclean.
   */
  public void cleanup() {
    if (myIsUnclean) {
      new WriteAction() {
        @Override
        protected void run(Result result) throws Throwable {
          Set<ManifestHolder> toDispose = new HashSet<ManifestHolder>();
          for (ManifestHolder manifestHolder : myManifestHolders) {
            try {
              manifestHolder.getBundleManifest();
            }
            catch (ManifestHolderDisposedException e) {
              toDispose.add(manifestHolder);
            }
          }
          myManifestHolders.removeAll(toDispose);
          myIsUnclean = false;
        }
      }.execute();
    }
  }

  /**
   * Returns the manifest holder which is the best match for  the given package specification
   *
   * @param packageSpec the package specification (may include version ranges)
   * @return
   */
  @Nullable
  public ManifestHolder whoProvidesBest(@NotNull final String packageSpec) {
    return new ReadAction<ManifestHolder>() {
      @Override
      protected void run(Result<ManifestHolder> manifestHolderResult) throws Throwable {
        for (ManifestHolder manifestHolder : myManifestHolders) {
          BundleManifest bundleManifest;
          try {
            bundleManifest = manifestHolder.getBundleManifest();
          }
          catch (ManifestHolderDisposedException e) {
            // ok this thing is gone
            markUnclean();
            continue;
          }
          if (bundleManifest != null) {
            if (bundleManifest.exportsPackage(packageSpec)) {
              manifestHolderResult.setResult(manifestHolder);
              return;
            }
          }
        }
      }
    }.execute().getResultObject();
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
          BundleManifest bundleManifest = null;
          try {
            bundleManifest = manifestHolder.getBundleManifest();
          }
          catch (ManifestHolderDisposedException e) {
            // this thing is gone
            markUnclean();
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
          BundleManifest bundleManifest = null;
          try {
            bundleManifest = manifestHolder.getBundleManifest();
          }
          catch (ManifestHolderDisposedException e) {
            // this thing is gone
            markUnclean();
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
          if (manifestHolder.getBoundObject().equals(bundle)) {
            manifestHolderResult.setResult(manifestHolder);
            return;
          }
        }
      }
    }.execute().getResultObject();
  }
}
