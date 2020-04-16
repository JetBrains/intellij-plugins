// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager;
import com.intellij.javascript.nodejs.npm.registry.NpmRegistryService;
import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion;
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.io.RequestBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class AngularCliSchematicsRegistryServiceImpl extends AngularCliSchematicsRegistryService {

  @NonNls private static final String USER_AGENT = "JetBrains IDE";
  @NonNls private static final String NG_PACKAGES_URL =
    "https://raw.githubusercontent.com/JetBrains/intellij-plugins/master/AngularJS/resources/org/angularjs/cli/ng-packages.json";

  @NonNls private static final Logger LOG = Logger.getInstance(AngularCliSchematicsRegistryServiceImpl.class);
  private static final int CACHE_EXPIRY = 25 * 60 * 1000; //25 mins
  @NonNls private static final ExecutorService ourExecutorService =
    AppExecutorUtil.createBoundedApplicationPoolExecutor("Angular CLI Schematics Registry Pool", 5);
  @NonNls private static final Key<CachedSchematics> SCHEMATICS_PUBLIC =
    new Key<>("angular.cli.schematics.public");
  @NonNls private static final Key<CachedSchematics> SCHEMATICS_ALL =
    new Key<>("angular.cli.schematics.all");
  private static final SimpleModificationTracker SCHEMATICS_CACHE_TRACKER = new SimpleModificationTracker();
  @NonNls private static final String NG_PACKAGES_JSON_PATH = "../../angularjs/cli/ng-packages.json";
  @NonNls private static final String SCHEMATICS_PROP = "schematics";
  @NonNls private static final String NG_ADD_SCHEMATIC = "ng-add";

  private final CachedValue<List<NodePackageBasicInfo>> myNgAddPackages = new CachedValue<>(
    AngularCliSchematicsRegistryServiceImpl::fetchPackagesSupportingNgAdd);
  private final Map<String, Pair<Boolean, Long>> myLocalNgAddPackages = new ConcurrentHashMap<>();
  private final Map<String, CachedValue<Boolean>> myNgAddSupportedCache = new ConcurrentHashMap<>();


  @Override
  public @NotNull List<NodePackageBasicInfo> getPackagesSupportingNgAdd(long timeout) {
    return ContainerUtil.notNullize(myNgAddPackages.getValue(timeout));
  }

  @Override
  public boolean supportsNgAdd(@NotNull String packageName, long timeout) {
    return getPackagesSupportingNgAdd(timeout).stream().anyMatch(pkg -> packageName.equals(pkg.getName()));
  }

  @Override
  public boolean supportsNgAdd(@NotNull String packageName,
                               @NotNull String versionOrRange,
                               long timeout) {
    return supportsNgAdd(packageName, timeout)
           && Boolean.TRUE.equals(myNgAddSupportedCache.computeIfAbsent(
      getKey(packageName, versionOrRange),
      k -> new CachedValue<>(() -> checkForNgAddSupport(packageName, versionOrRange))).getValue(timeout));
  }

  @Override
  public boolean supportsNgAdd(@NotNull InstalledPackageVersion version) {
    try {
      if (version.getPackageJson() != null) {
        return myLocalNgAddPackages.compute(version.getPackageJson().getPath(), (key, curValue) -> {
          if (curValue != null && version.getPackageJson().getModificationStamp() == curValue.getSecond()) {
            return curValue;
          }
          try {
            File schematicsCollection = getSchematicsCollection(new File(version.getPackageJson().getPath()));
            return Pair
              .create(schematicsCollection != null && hasNgAddSchematic(schematicsCollection),
                      version.getPackageJson().getModificationStamp());
          }
          catch (IOException e) {
            return Pair.create(false, version.getPackageJson().getModificationStamp());
          }
        }).getFirst();
      }
    }
    catch (Exception e) {
      LOG.info("Failed to retrieve schematics info for " + version.getPackageDir().getName(), e);
    }
    return false;
  }

  @Override
  public @NotNull List<Schematic> getSchematics(@NotNull Project project,
                                                @NotNull VirtualFile cliFolder,
                                                boolean includeHidden,
                                                boolean logErrors) {
    return Optional.ofNullable(AngularCliUtil.findCliJson(cliFolder))
      .map(angularJson -> ReadAction.compute(() -> PsiManager.getInstance(project).findFile(angularJson)))
      .map(angularJson -> getCachedSchematics(angularJson, includeHidden ? SCHEMATICS_ALL : SCHEMATICS_PUBLIC).getUpToDateOrCompute(
        () -> CachedValueProvider.Result.create(
          SchematicsLoaderKt.doLoad(angularJson.getProject(),
                                    angularJson.getVirtualFile().getParent(), includeHidden, logErrors),
          NodeModulesDirectoryManager.getInstance(angularJson.getProject()).getNodeModulesDirChangeTracker(),
          SCHEMATICS_CACHE_TRACKER,
          angularJson)))
      .orElseGet(Collections::emptyList);
  }

  @Override
  public void clearProjectSchematicsCache() {
    SCHEMATICS_CACHE_TRACKER.incModificationCount();
  }

  private static @NotNull List<NodePackageBasicInfo> fetchPackagesSupportingNgAdd() {
    try {
      RequestBuilder builder = HttpRequests.request(NG_PACKAGES_URL);
      builder.userAgent(USER_AGENT);
      builder.gzip(true);
      return readNgAddPackages(builder.readString(null));
    }
    catch (IOException e) {
      LOG.info("Failed to load current list of ng-add compatible packages.", e);
      try (InputStream is = AngularCliSchematicsRegistryServiceImpl.class.getResourceAsStream(NG_PACKAGES_JSON_PATH)) {
        return readNgAddPackages(FileUtil.loadTextAndClose(new InputStreamReader(is, StandardCharsets.UTF_8)));
      }
      catch (Exception e1) {
        LOG.error("Failed to load list of ng-add compatible packages from static file.", e1);
      }
    }
    return Collections.emptyList();
  }

  private static @NotNull List<NodePackageBasicInfo> readNgAddPackages(@NotNull String content) {
    JsonObject contents = (JsonObject)new JsonParser().parse(content);
    return Collections.unmodifiableList(
      ContainerUtil.map(
        contents.get(NG_ADD_SCHEMATIC).getAsJsonObject().entrySet(),
        e -> new NodePackageBasicInfo(e.getKey(), e.getValue().getAsString())));
  }

  private static @Nullable File getSchematicsCollection(@NotNull File packageJson) throws IOException {
    try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(packageJson),
                                                                  StandardCharsets.UTF_8))) {
      reader.beginObject();
      while (reader.hasNext()) {
        String key = reader.nextName();
        if (key.equals(SCHEMATICS_PROP)) {
          String path = reader.nextString();
          return Paths.get(packageJson.getParent(), path).normalize().toAbsolutePath().toFile();
        }
        else {
          reader.skipValue();
        }
      }
      return null;
    }
  }

  private static boolean hasNgAddSchematic(@NotNull File schematicsCollection) throws IOException {
    try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(schematicsCollection),
                                                                  StandardCharsets.UTF_8))) {
      return hasNgAddSchematic(reader);
    }
  }

  public static boolean hasNgAddSchematic(@NotNull JsonReader reader) throws IOException {
    reader.setLenient(true);
    reader.beginObject();
    while (reader.hasNext()) {
      String key = reader.nextName();
      if (SCHEMATICS_PROP.equals(key)) {
        reader.beginObject();
        while (reader.hasNext()) {
          String schematicName = reader.nextName();
          if (schematicName.equals(NG_ADD_SCHEMATIC)) {
            return true;
          }
          reader.skipValue();
        }
        reader.endObject();
      }
      else {
        reader.skipValue();
      }
    }
    reader.endObject();
    return false;
  }

  private static boolean checkForNgAddSupport(@NotNull String packageName, @NotNull String versionOrRange) {
    try {
      ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      JsonObject pkgJson = NpmRegistryService.getInstance().fetchPackageJson(packageName, versionOrRange, indicator);
      return pkgJson != null && pkgJson.get(SCHEMATICS_PROP) != null;
    }
    catch (Exception e) {
      LOG.info(e);
    }
    return false;
  }

  private static String getKey(@NotNull String packageName,
                               @NotNull String version) {
    return packageName + "@" + version;
  }

  private static @NotNull CachedSchematics getCachedSchematics(@NotNull UserDataHolder dataHolder, @NotNull Key<CachedSchematics> key) {
    CachedSchematics result = dataHolder.getUserData(key);
    if (result != null) {
      return result;
    }

    if (dataHolder instanceof UserDataHolderEx) {
      return ((UserDataHolderEx)dataHolder).putUserDataIfAbsent(key, new CachedSchematics());
    }
    result = new CachedSchematics();
    dataHolder.putUserData(key, result);
    return result;
  }

  private static class CachedSchematics {
    private List<Schematic> mySchematics;
    private List<Pair<Object, Long>> myTrackers;

    public synchronized List<Schematic> getUpToDateOrCompute(Supplier<CachedValueProvider.Result<List<Schematic>>> provider) {
      if (mySchematics != null
          && myTrackers != null
          && ContainerUtil.all(myTrackers, pair -> pair.second >= 0 && getTimestamp(pair.first) == pair.second)) {
        return mySchematics;
      }
      CachedValueProvider.Result<List<Schematic>> schematics = provider.get();
      mySchematics = Collections.unmodifiableList(schematics.getValue());
      myTrackers = ContainerUtil.map(schematics.getDependencyItems(), obj -> Pair.pair(obj, getTimestamp(obj)));
      return mySchematics;
    }

    private static long getTimestamp(Object dependency) {
      if (dependency instanceof ModificationTracker) {
        return ((ModificationTracker)dependency).getModificationCount();
      }
      if (dependency instanceof PsiElement) {
        PsiElement element = (PsiElement)dependency;
        if (!element.isValid()) return -1;
        PsiFile containingFile = element.getContainingFile();
        if (containingFile != null) {
          return containingFile.getVirtualFile().getModificationStamp();
        }
        return -1;
      }
      throw new UnsupportedOperationException(dependency.getClass().toString());
    }
  }

  private static class CachedValue<T> {

    private long myUpdateTime;
    private Future<T> myCacheComputation;
    private T myCachedValue;
    private final Callable<T> myValueSupplier;

    CachedValue(Callable<T> valueSupplier) {
      this.myValueSupplier = valueSupplier;
    }

    protected synchronized boolean isCacheExpired() {
      return myUpdateTime + CACHE_EXPIRY <= System.currentTimeMillis();
    }

    @SuppressWarnings("SynchronizeOnThis")
    public @Nullable T getValue(long timeout) {
      Future<T> cacheComputation;
      synchronized (this) {
        if (myCachedValue != null && !isCacheExpired()) {
          return myCachedValue;
        }
        if (myCacheComputation == null) {
          myCachedValue = null;
          myCacheComputation = ourExecutorService.submit(myValueSupplier);
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
