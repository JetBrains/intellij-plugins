package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import com.intellij.util.PairConsumer;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.listPackageDirs.DartListPackageDirsLibraryProperties;
import com.jetbrains.lang.dart.sdk.listPackageDirs.PubListPackageDirsAction;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

class DartUrlResolverImpl extends DartUrlResolver {

  private final @NotNull Project myProject;
  private final @Nullable DartSdk myDartSdk;
  private final @Nullable VirtualFile myPubspecYamlFile;
  private final @NotNull List<VirtualFile> myPackageRoots = new ArrayList<VirtualFile>();
  private final @NotNull Map<String, VirtualFile> myLivePackageNameToDirMap = new THashMap<String, VirtualFile>();
  private final @NotNull Map<String, Set<String>> myPubListPackageDirsMap = new THashMap<String, Set<String>>();

  DartUrlResolverImpl(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
    myProject = project;
    myDartSdk = DartSdk.getDartSdk(project);
    myPubspecYamlFile = initPackageRootsAndReturnPubspecYamlFile(contextFile);
    initLivePackageNameToDirMap();
    initPubListPackageDirsMap(contextFile);
  }

  @Nullable
  public VirtualFile getPubspecYamlFile() {
    return myPubspecYamlFile;
  }

  @NotNull
  public VirtualFile[] getPackageRoots() {
    return myPackageRoots.toArray(new VirtualFile[myPackageRoots.size()]);
  }

  public void processLivePackages(final @NotNull PairConsumer<String, VirtualFile> packageNameAndDirConsumer) {
    for (Map.Entry<String, VirtualFile> entry : myLivePackageNameToDirMap.entrySet()) {
      packageNameAndDirConsumer.consume(entry.getKey(), entry.getValue());
    }
  }

  public Collection<String> getLivePackageNames() {
    return myLivePackageNameToDirMap.keySet();
  }

  @Nullable
  public VirtualFile getPackageDirIfLivePackageOrFromPubListPackageDirs(final @NotNull String packageName) {
    return getPackageDirIfLivePackageOrFromPubListPackageDirs(packageName, null);
  }

  @Nullable
  public VirtualFile getPackageDirIfLivePackageOrFromPubListPackageDirs(final @NotNull String packageName,
                                                                        @Nullable String pathAfterPackageName) {
    final VirtualFile dir = myLivePackageNameToDirMap.get(packageName);
    if (dir != null) return dir;

    final Set<String> dirPaths = myPubListPackageDirsMap.get(packageName);
    if (dirPaths != null) {
      if (pathAfterPackageName == null) {
        pathAfterPackageName = "";
      }
      else {
        pathAfterPackageName = '/' + pathAfterPackageName;
      }
      for (String dirPath : dirPaths) {

        final VirtualFile packageDir =
          LocalFileSystem.getInstance().findFileByPath(dirPath + pathAfterPackageName);
        if (packageDir != null) {
          return LocalFileSystem.getInstance().findFileByPath(dirPath);
        }
      }
    }

    return null;
  }

  @Nullable
  public VirtualFile findFileByDartUrl(final @NotNull String url) {
    if (url.startsWith(DART_PREFIX)) {
      return findFileInDartSdkLibFolder(myProject, myDartSdk, url);
    }

    if (url.startsWith(PACKAGE_PREFIX)) {
      final String packageRelPath = url.substring(PACKAGE_PREFIX.length());

      final int slashIndex = packageRelPath.indexOf('/');
      final String packageName = slashIndex > 0 ? packageRelPath.substring(0, slashIndex) : packageRelPath;
      final String relPathToPackageDir = slashIndex > 0 ? packageRelPath.substring(slashIndex + 1) : "";

      final VirtualFile packageDir = StringUtil.isEmpty(packageName) ? null : myLivePackageNameToDirMap.get(packageName);
      if (packageDir != null) {
        return packageDir.findFileByRelativePath(relPathToPackageDir);
      }

      for (final VirtualFile packageRoot : myPackageRoots) {
        final VirtualFile file = packageRoot.findFileByRelativePath(packageRelPath);
        if (file != null) {
          return file;
        }
      }

      final Set<String> packageDirs = myPubListPackageDirsMap.get(packageName);
      if (packageDirs != null) {
        for (String packageDirPath : packageDirs) {
          final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(packageDirPath + "/" + relPathToPackageDir);
          if (file != null) {
            return file;
          }
        }
      }
    }

    if (url.startsWith(FILE_PREFIX)) {
      final String path = StringUtil.trimLeading(url.substring(FILE_PREFIX.length()), '/');
      return LocalFileSystem.getInstance().findFileByPath(SystemInfo.isWindows ? path : ("/" + path));
    }

    if (ApplicationManager.getApplication().isUnitTestMode() && url.startsWith(TEMP_PREFIX)) {
      return TempFileSystem.getInstance().findFileByPath(url.substring((TEMP_PREFIX).length()));
    }

    return null;
  }

  @NotNull
  public String getDartUrlForFile(final @NotNull VirtualFile file) {
    String result = null;

    if (myDartSdk != null) result = getUrlIfFileFromSdkLib(myProject, file, myDartSdk);
    if (result != null) return result;

    result = getUrlIfFileFromLivePackage(file, myLivePackageNameToDirMap);
    if (result != null) return result;

    result = getUrlIfFileFromPackageRoot(file, myPackageRoots);
    if (result != null) return result;

    result = getUrlIfFileFromPubListPackageDirs(file, myPubListPackageDirsMap);
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
  private static String getUrlIfFileFromPackageRoot(final @NotNull VirtualFile file, final @NotNull List<VirtualFile> packageRoots) {
    for (VirtualFile packageRoot : packageRoots) {
      final String relPath = VfsUtilCore.getRelativePath(file, packageRoot, '/');
      if (relPath != null) {
        return PACKAGE_PREFIX + relPath;
      }
    }
    return null;
  }

  @Nullable
  private static String getUrlIfFileFromPubListPackageDirs(final @NotNull VirtualFile file,
                                                           final @NotNull Map<String, Set<String>> pubListPackageDirsMap) {
    for (Map.Entry<String, Set<String>> mapEntry : pubListPackageDirsMap.entrySet()) {
      for (String dirPath : mapEntry.getValue()) {
        if (file.getPath().startsWith(dirPath + "/")) {
          final String packageName = mapEntry.getKey();
          return PACKAGE_PREFIX + packageName + file.getPath().substring(dirPath.length());
        }
      }
    }
    return null;
  }

  @Nullable
  private VirtualFile initPackageRootsAndReturnPubspecYamlFile(final @NotNull VirtualFile contextFile) {
    final Module module = ModuleUtilCore.findModuleForFile(contextFile, myProject);
    if (module == null) return null;

    final VirtualFile[] customPackageRoots = DartConfigurable.getCustomPackageRoots(module);
    if (customPackageRoots.length > 0) {
      Collections.addAll(myPackageRoots, customPackageRoots);
      return null;
    }

    final VirtualFile pubspecYamlFile = PubspecYamlUtil.findPubspecYamlFile(myProject, contextFile);
    final VirtualFile parentFolder = pubspecYamlFile == null ? null : pubspecYamlFile.getParent();
    final VirtualFile packagesFolder = parentFolder == null ? null : parentFolder.findChild(PACKAGES_FOLDER_NAME);
    if (packagesFolder != null && packagesFolder.isDirectory()) {
      myPackageRoots.add(packagesFolder);
    }

    return pubspecYamlFile;
  }

  private void initLivePackageNameToDirMap() {
    final VirtualFile baseDir = myPubspecYamlFile == null ? null : myPubspecYamlFile.getParent();
    if (myPubspecYamlFile == null || baseDir == null) return;

    final String name = PubspecYamlUtil.getDartProjectName(myPubspecYamlFile);
    final VirtualFile libFolder = baseDir.findChild(PubspecYamlUtil.LIB_DIR_NAME);

    if (name != null && libFolder != null && libFolder.isDirectory()) {
      myLivePackageNameToDirMap.put(name, libFolder);
    }

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();

    PubspecYamlUtil.processPathPackages(myPubspecYamlFile, new PairConsumer<String, VirtualFile>() {
      @Override
      public void consume(@NotNull final String packageName, @NotNull final VirtualFile packageDir) {
        if (fileIndex.isInContent(packageDir)) {
          myLivePackageNameToDirMap.put(packageName, packageDir);
        }
      }
    });
  }

  private void initPubListPackageDirsMap(final @NotNull VirtualFile contextFile) {
    final Module module = ModuleUtilCore.findModuleForFile(contextFile, myProject);

    final List<OrderEntry> orderEntries = module != null
                                          ? Arrays.asList(ModuleRootManager.getInstance(module).getOrderEntries())
                                          : ProjectRootManager.getInstance(myProject).getFileIndex().getOrderEntriesForFile(contextFile);
    for (OrderEntry orderEntry : orderEntries) {
      if (orderEntry instanceof LibraryOrderEntry &&
          LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
          PubListPackageDirsAction.PUB_LIST_PACKAGE_DIRS_LIB_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName())) {
        final LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
        final LibraryProperties properties = library == null ? null : library.getProperties();

        if (properties instanceof DartListPackageDirsLibraryProperties) {
          myPubListPackageDirsMap.putAll(((DartListPackageDirsLibraryProperties)properties).getPackageNameToDirsMap());
          return;
        }
      }
    }
  }
}
