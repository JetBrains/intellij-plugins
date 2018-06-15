package org.angularjs.cli;

import com.google.gson.JsonObject;
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo;
import com.intellij.javascript.nodejs.packageJson.NpmRegistryService;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class AngularCliSchematicsRegistryServiceImpl extends AngularCliSchematicsRegistryService {

  private static final Logger LOG = Logger.getInstance(AngularCliSchematicsRegistryServiceImpl.class);
  private static final int CACHE_EXPIRY = 25 * 60 * 1000; //25 mins

  private final CachedValue<List<NodePackageBasicInfo>> ngAddPackages =
    new CachedValue<List<NodePackageBasicInfo>>(AngularCliSchematicsRegistryServiceImpl::fetchPackagesSupportingNgAdd) {
      @Override
      protected synchronized boolean isCacheExpired() {
        return false;
      }
    };
  private final Map<String, CachedValue<Boolean>> schematicsSupportedCache = ContainerUtil.newConcurrentMap();

  private static final Set<String> NG_ADD_PACKAGES = ContainerUtil.newHashSet(
    "@angular/material", "@ng-toolkit/universal", "@ngrx/store", "@ngrx/effects", "bootstrap-schematics",
    "@jarmee/schematics", "@nativescript/schematics", "ngx-cbp-theme", "ngx-weui", "@schuchard/prettier",
    "yang-schematics", "@ng-toolkit/serverless", "@nrwl/schematics", "angular-popper"
  );

  @NotNull
  @Override
  public List<NodePackageBasicInfo> getPackagesSupportingNgAdd(long timeout) {
    List<NodePackageBasicInfo> result = ngAddPackages.getValue(timeout);
    return result != null ? result : Collections.emptyList();
  }

  @Override
  public boolean supportsNgAdd(@NotNull String packageName,
                               @NotNull String versionOrRange,
                               long timeout) {
    return NG_ADD_PACKAGES.contains(packageName)
           && supportsSchematics(packageName, versionOrRange, timeout);
  }

  @Override
  public boolean supportsSchematics(@NotNull String packageName,
                                    @NotNull String versionOrRange,
                                    long timeout) {
    return Boolean.TRUE.equals(schematicsSupportedCache.computeIfAbsent(
      getKey(packageName, versionOrRange), k -> new CachedValue<>(() -> {
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        JsonObject pkg = NpmRegistryService.getInstance().fetchPackageJson(packageName, versionOrRange, indicator);
        return pkg != null && pkg.has("schematics");
      })).getValue(timeout));
  }

  @NotNull
  private static List<NodePackageBasicInfo> fetchPackagesSupportingNgAdd() {
    ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    Map<String, NodePackageBasicInfo> result = new HashMap<>();
    try {
      NpmRegistryService.getInstance().findPackages(
        indicator, NpmRegistryService.fullTextSearch("angular schematics"), 1000,
        info -> true, info -> result.put(info.getName(), info));
    }
    catch (IOException e) {
      LOG.error("Failed to retrieve list of Angular packages.", e);
    }
    return NG_ADD_PACKAGES
      .stream()
      .map(pkgName -> result.getOrDefault(pkgName, new NodePackageBasicInfo(pkgName, null)))
      .collect(Collectors.toList());
  }

  private static String getKey(@NotNull String packageName,
                               @NotNull String version) {
    return packageName + "@" + version;
  }

  private static class CachedValue<T> {

    private long myUpdateTime;
    private Future<T> myCacheComputation;
    private T myCachedValue;
    private final Callable<T> myValueSupplier;

    @SuppressWarnings("BoundedWildcard")
    public CachedValue(Callable<T> valueSupplier) {
      this.myValueSupplier = valueSupplier;
    }

    protected synchronized boolean isCacheExpired() {
      return myUpdateTime + CACHE_EXPIRY <= System.currentTimeMillis();
    }

    @SuppressWarnings("SynchronizeOnThis")
    @Nullable
    public T getValue(long timeout) {
      Future<T> cacheComputation;
      synchronized (this) {
        if (myCachedValue != null && !isCacheExpired()) {
          return myCachedValue;
        }
        if (myCacheComputation == null) {
          myCachedValue = null;
          myCacheComputation = ApplicationManager.getApplication().executeOnPooledThread(myValueSupplier);
        }
        cacheComputation = myCacheComputation;
      }
      T result = JSLanguageServiceUtil.awaitFuture(cacheComputation, timeout, 10,
                                                   null, false, null);
      synchronized (this) {
        if (myCacheComputation != null && myCacheComputation.isDone()) {
          try {
            result = myCachedValue = myCacheComputation.get();
          }
          catch (InterruptedException | CancellationException ex) {
            //ignore
          }
          catch (ExecutionException e) {
            LOG.error(e);
          }
          myCacheComputation = null;
          myUpdateTime = System.currentTimeMillis();
        }
      }
      return result;
    }
  }
}
