// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import com.intellij.util.PairConsumer;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryProperties;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DartUrlResolverImpl extends DartUrlResolver {
  // TODO Fold into superclass.

  @NotNull private final Project myProject;
  @Nullable private final DartSdk myDartSdk;
  @Nullable private final VirtualFile myPubspecYamlFile;
  // myLivePackageNameToDirMap also contains packages map from .packages file if applicable
  @NotNull private final Map<String, VirtualFile> myLivePackageNameToDirMap = new THashMap<>();
  // myPackagesMapFromLib is not empty only if pubspec.yaml file is null
  @NotNull private final Map<String, List<String>> myPackagesMapFromLib = new THashMap<>();

  public DartUrlResolverImpl(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
    myProject = project;
    myDartSdk = DartSdk.getDartSdk(project);
    myPubspecYamlFile = PubspecYamlUtil.findPubspecYamlFile(myProject, contextFile);

    initLivePackageNameToDirMap();

    if (myPubspecYamlFile == null) {
      initPackagesMapFromLib(contextFile);
    }
  }

  @Override
  @Nullable
  public VirtualFile getPubspecYamlFile() {
    return myPubspecYamlFile;
  }

  @Override
  public void processLivePackages(final @NotNull PairConsumer<String, VirtualFile> packageNameAndDirConsumer) {
    for (Map.Entry<String, VirtualFile> entry : myLivePackageNameToDirMap.entrySet()) {
      packageNameAndDirConsumer.consume(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Collection<String> getLivePackageNames() {
    return myLivePackageNameToDirMap.keySet();
  }

  @Override
  @Nullable
  public VirtualFile getPackageDirIfNotInOldStylePackagesFolder(@NotNull final String packageName,
                                                                @Nullable final String pathRelToPackageDir) {
    final VirtualFile dir = myLivePackageNameToDirMap.get(packageName);
    if (dir != null) return dir;

    final List<String> dirPaths = myPackagesMapFromLib.get(packageName);
    if (dirPaths != null) {
      VirtualFile notNullPackageDir = null;

      for (String dirPath : dirPaths) {
        final VirtualFile packageDir = ApplicationManager.getApplication().isUnitTestMode()
                                       ? TempFileSystem.getInstance().findFileByPath(dirPath)
                                       : LocalFileSystem.getInstance().findFileByPath(dirPath);
        if (notNullPackageDir == null && packageDir != null) {
          notNullPackageDir = packageDir;
        }

        if (packageDir != null && (StringUtil.isEmpty(pathRelToPackageDir) ||
                                   packageDir.findFileByRelativePath(pathRelToPackageDir) != null)) {
          return packageDir;
        }
      }

      return notNullPackageDir; // file by pathRelToPackageDir was not found, but not-null packageDir still may be useful
    }

    return null;
  }

  @Override
  @Nullable
  public VirtualFile findFileByDartUrl(final @NotNull String url) {
    if (url.startsWith(DART_PREFIX)) {
      return findFileInDartSdkLibFolder(myProject, myDartSdk, url);
    }

    if (url.startsWith(PACKAGE_PREFIX)) {
      final String packageRelPath = url.substring(PACKAGE_PREFIX.length());

      final int slashIndex = packageRelPath.indexOf('/');
      final String packageName = slashIndex > 0 ? packageRelPath.substring(0, slashIndex) : packageRelPath;
      final String pathRelToPackageDir = slashIndex > 0 ? packageRelPath.substring(slashIndex + 1) : "";

      final VirtualFile packageDir = StringUtil.isEmpty(packageName) ? null : myLivePackageNameToDirMap.get(packageName);
      if (packageDir != null) {
        return packageDir.findFileByRelativePath(pathRelToPackageDir);
      }

      final List<String> packageDirs = myPackagesMapFromLib.get(packageName);
      if (packageDirs != null) {
        for (String packageDirPath : packageDirs) {
          final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(packageDirPath + "/" + pathRelToPackageDir);
          if (file != null) {
            return file;
          }
        }
      }
    }

    if (url.startsWith(FILE_PREFIX)) {
      final String path = StringUtil.trimLeading(url.substring(FILE_PREFIX.length()), '/');
      return LocalFileSystem.getInstance().findFileByPath(SystemInfoRt.isWindows ? path : ("/" + path));
    }

    if (ApplicationManager.getApplication().isUnitTestMode() && url.startsWith(TEMP_PREFIX)) {
      return TempFileSystem.getInstance().findFileByPath(url.substring((TEMP_PREFIX).length()));
    }

    return null;
  }

  @Override
  @NotNull
  public String getDartUrlForFile(final @NotNull VirtualFile file) {
    String result = null;

    if (myDartSdk != null) result = getUrlIfFileFromSdkLib(myProject, file, myDartSdk);
    if (result != null) return result;

    result = getUrlIfFileFromLivePackage(file, myLivePackageNameToDirMap);
    if (result != null) return result;

    result = getUrlIfFileFromDartPackagesLib(file, myPackagesMapFromLib);
    if (result != null) return result;

    // see com.google.dart.tools.debug.core.server.ServerBreakpointManager#getAbsoluteUrlForResource()
    return new File(file.getPath()).toURI().toString();
  }

  @Nullable
  private static String getUrlIfFileFromSdkLib(final @NotNull Project project,
                                               final @NotNull VirtualFile file,
                                               final @NotNull DartSdk sdk) {
    final VirtualFile sdkLibFolder = LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib");
    final String relativeToSdkLibFolder = sdkLibFolder == null ? null : VfsUtilCore.getRelativePath(file, sdkLibFolder, '/');
    final String sdkLibUri = relativeToSdkLibFolder == null
                             ? null
                             : DartLibraryIndex.getSdkLibUriByRelativePath(project, relativeToSdkLibFolder);
    return sdkLibUri != null
           ? sdkLibUri
           : relativeToSdkLibFolder != null
             ? DART_PREFIX + relativeToSdkLibFolder
             : null;
  }

  @Nullable
  private static String getUrlIfFileFromLivePackage(final @NotNull VirtualFile file,
                                                    final @NotNull Map<String, VirtualFile> livePackageNameToDirMap) {
    for (Map.Entry<String, VirtualFile> entry : livePackageNameToDirMap.entrySet()) {
      final String packageName = entry.getKey();
      final VirtualFile packageDir = entry.getValue();
      final String relPath = VfsUtilCore.getRelativePath(file, packageDir, '/');
      if (relPath != null) {
        return PACKAGE_PREFIX + packageName + "/" + relPath;
      }
    }
    return null;
  }

  @Nullable
  private static String getUrlIfFileFromDartPackagesLib(final @NotNull VirtualFile file,
                                                        final @NotNull Map<String, List<String>> pubListPackageDirsMap) {
    for (Map.Entry<String, List<String>> mapEntry : pubListPackageDirsMap.entrySet()) {
      for (String dirPath : mapEntry.getValue()) {
        if (file.getPath().startsWith(dirPath + "/")) {
          final String packageName = mapEntry.getKey();
          return PACKAGE_PREFIX + packageName + file.getPath().substring(dirPath.length());
        }
      }
    }
    return null;
  }

  private void initLivePackageNameToDirMap() {
    final VirtualFile baseDir = myPubspecYamlFile == null ? null : myPubspecYamlFile.getParent();
    if (myPubspecYamlFile == null || baseDir == null) return;
    final VirtualFile dotPackagesFile = baseDir.findChild(DotPackagesFileUtil.DOT_PACKAGES);

    if (dotPackagesFile != null && !dotPackagesFile.isDirectory()) {
      final Map<String, String> packagesMap = DotPackagesFileUtil.getPackagesMap(dotPackagesFile);
      if (packagesMap != null) {
        for (Map.Entry<String, String> entry : packagesMap.entrySet()) {
          final String packageName = entry.getKey();
          final String packagePath = entry.getValue();
          final VirtualFile packageDir = myPubspecYamlFile.getFileSystem().findFileByPath(packagePath);
          if (packageDir != null) {
            myLivePackageNameToDirMap.put(packageName, packageDir);
          }
        }
      }
    }
    else {
      final String name = PubspecYamlUtil.getDartProjectName(myPubspecYamlFile);
      final VirtualFile libFolder = baseDir.findChild(PubspecYamlUtil.LIB_DIR_NAME);

      if (name != null && libFolder != null && libFolder.isDirectory()) {
        myLivePackageNameToDirMap.put(name, libFolder);
      }

      PubspecYamlUtil
        .processInProjectPathPackagesRecursively(myProject, myPubspecYamlFile, myLivePackageNameToDirMap::put);
    }
  }

  private void initPackagesMapFromLib(final @NotNull VirtualFile contextFile) {
    final Module module = ModuleUtilCore.findModuleForFile(contextFile, myProject);

    final List<OrderEntry> orderEntries = module != null
                                          ? Arrays.asList(ModuleRootManager.getInstance(module).getOrderEntries())
                                          : ProjectRootManager.getInstance(myProject).getFileIndex().getOrderEntriesForFile(contextFile);
    for (OrderEntry orderEntry : orderEntries) {
      if (orderEntry instanceof LibraryOrderEntry &&
          LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
          DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName())) {
        final LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
        final LibraryProperties properties = library == null ? null : library.getProperties();

        if (properties instanceof DartPackagesLibraryProperties) {
          for (Map.Entry<String, List<String>> entry : ((DartPackagesLibraryProperties)properties).getPackageNameToDirsMap().entrySet()) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
              myPackagesMapFromLib.put(entry.getKey(), entry.getValue());
            }
          }
          return;
        }
      }
    }
  }
}
