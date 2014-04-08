package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import com.intellij.util.containers.BidirectionalMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.*;

class DartUrlResolverImpl extends DartUrlResolver {

  private final @NotNull Project myProject;
  private final @NotNull VirtualFile myContextFile;

  private boolean myInitialized = false;
  private @NotNull BidirectionalMap<String, VirtualFile> myPackageNameToDirMap;

  public DartUrlResolverImpl(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
    myProject = project;
    myContextFile = contextFile;
  }

  public void processLivePackages(final PairConsumer<String, VirtualFile> packageNameAndDirConsumer) {
    ensureInitialized();
    for (Map.Entry<String, VirtualFile> entry : myPackageNameToDirMap.entrySet()) {
      packageNameAndDirConsumer.consume(entry.getKey(), entry.getValue());
    }
  }

  private void ensureInitialized() {
    if (!myInitialized) {
      myPackageNameToDirMap = new BidirectionalMap<String, VirtualFile>();

      final VirtualFile yamlFile = getPubspecYamlFile(myProject, myContextFile);
      if (yamlFile != null) {
        final VirtualFile baseDir = yamlFile.getParent();
        final Map<String, Object> yamlInfo = getPubspecYamlInfo(yamlFile);
        if (baseDir != null && yamlInfo != null) {
          fillPackageNameToDirMap(myPackageNameToDirMap, baseDir, yamlInfo);
        }
      }

      myInitialized = true;
    }
  }

  private static void fillPackageNameToDirMap(final @NotNull BidirectionalMap<String, VirtualFile> packageNameToDirMap,
                                              final @NotNull VirtualFile baseDir,
                                              final @NotNull Map<String, Object> yamlInfo) {
    final Object name = yamlInfo.get(NAME);
    final VirtualFile libFolder = baseDir.findChild(LIB_DIRECTORY_NAME);
    if (name instanceof String && libFolder != null && libFolder.isDirectory()) {
      packageNameToDirMap.put((String)name, libFolder);
    }

    //noinspection unchecked
    addPathPackagesToMap(packageNameToDirMap, (Map<String, Object>)yamlInfo.get(DEPENDENCIES), baseDir);
    //noinspection unchecked
    addPathPackagesToMap(packageNameToDirMap, (Map<String, Object>)yamlInfo.get(DEV_DEPENDENCIES), baseDir);
  }

  /**
   * Path packages: https://www.dartlang.org/tools/pub/dependencies.html#path-packages
   */
  private static void addPathPackagesToMap(final @NotNull BidirectionalMap<String, VirtualFile> packageNameToDirMap,
                                           final @Nullable Map<String, Object> yamlDep,
                                           final @NotNull VirtualFile baseDir) {
    // see com.google.dart.tools.core.pub.PubspecModel#processDependencies
    if (yamlDep == null) return;

    for (Map.Entry<String, Object> packageEntry : yamlDep.entrySet()) {
      final String packageName = packageEntry.getKey();

      final Object packageEntryValue = packageEntry.getValue();
      if (packageEntryValue instanceof Map) {
        final Object pathObj = ((Map)packageEntryValue).get(PATH);
        if (pathObj instanceof String) {
          final VirtualFile packageFolder = VfsUtilCore.findRelativeFile(pathObj + "/" + LIB_DIRECTORY_NAME, baseDir);
          if (packageFolder != null && packageFolder.isDirectory()) {
            packageNameToDirMap.put(packageName, packageFolder);
          }
        }
      }
    }
  }
}
