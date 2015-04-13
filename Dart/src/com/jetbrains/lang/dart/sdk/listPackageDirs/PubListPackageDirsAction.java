package com.jetbrains.lang.dart.sdk.listPackageDirs;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashSet;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PubListPackageDirsAction extends AnAction {

  public static final String PUB_LIST_PACKAGE_DIRS_LIB_NAME = "Dart pub list-package-dirs";

  private static final Logger LOG = Logger.getInstance(PubListPackageDirsAction.class.getName());

  public PubListPackageDirsAction() {
    super("Configure Dart package roots using 'pub list-package-dirs'", null, DartIcons.Dart_16);
  }

  private static void computeLibraryRoots(@NotNull final Project project,
                                          @NotNull final DartSdk dartSdk,
                                          @NotNull final String[] libraries,
                                          @NotNull final Collection<String> rootsToAddToLib) {
    if(libraries.length == 0) return;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final SortedSet<String> folderPaths = new TreeSet<String>();

    for (final String path : libraries) {
      if (path != null) {
        folderPaths.add(PathUtil.getParentPath(FileUtil.toSystemIndependentName(path)));
      }
    }

    outer:
    for (final String path : folderPaths) {
      final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (!path.startsWith(dartSdk.getHomePath() + "/") && (vFile == null || !fileIndex.isInContent(vFile))) {
        for (String configuredPath : rootsToAddToLib) {
          if (path.startsWith(configuredPath + "/")) {
            continue outer; // folderPaths is sorted so subfolders go after parent folder
          }
        }
        rootsToAddToLib.add(path);
      }
    }
  }

  @NotNull
  private static String getModuleStringKey(@NotNull final Module module) {
    return module.getModuleFilePath();
  }

  private static void computePackageMap(@NotNull final Project project, @NotNull final Map<String, Map<String, List<String>>> packageMapMap,
                                                                        @NotNull final Map<String, Map<String, List<String>>> modulesToPackageNameToDirMap) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (final Map.Entry<String, Map<String, List<String>>> entry : packageMapMap.entrySet()) {
      final String contextSourceRoot = entry.getKey();
      // Find the module that corresponds with this contextSourceRoot, then get the moduleStringKey for the Module
      String moduleStringKey = null;
      for (final Module module : modules) {
        for (final ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
          if(contextSourceRoot.equals(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentEntry.getUrl())))) {
            moduleStringKey = getModuleStringKey(module);
            break;
          }
        }
        if(moduleStringKey != null) {
          break;
        }
      }
      if(moduleStringKey == null) continue;

      final Map<String, List<String>> packageNameToDirMap = new TreeMap<String, List<String>>();

      final Map<String, List<String>> packageMapN = entry.getValue();
      for (final Map.Entry<String, List<String>> entry2 : packageMapN.entrySet()) {
        String packageName = entry2.getKey();
        List<String> listStr = entry2.getValue();
        List<String> packageDirList = new ArrayList<String>(listStr.size());
        for (final String path : listStr) {
          packageDirList.add(FileUtil.toSystemIndependentName(path));
        }
        packageNameToDirMap.put(packageName, packageDirList);
      }
      if(!modulesToPackageNameToDirMap.containsKey(moduleStringKey)) {
        modulesToPackageNameToDirMap.put(moduleStringKey, packageNameToDirMap);
      } else {
        modulesToPackageNameToDirMap.get(moduleStringKey).putAll(packageNameToDirMap);
      }
    }
  }

  public void update(@NotNull final AnActionEvent event) {
    final Project project = event.getProject();
    final DartSdk sdk = project == null ? null : DartSdk.getDartSdk(project);
    event.getPresentation().setEnabled(sdk != null && DartAnalysisServerAnnotator.isDartSDKVersionSufficient(sdk));
  }

  public void actionPerformed(@NotNull final AnActionEvent event) {
    final Project project = event.getProject();
    final DartSdk sdk = project == null ? null : DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerAnnotator.isDartSDKVersionSufficient(sdk)) return;

    FileDocumentManager.getInstance().saveAllDocuments();

    final Set<Module> affectedModules = new THashSet<Module>();
    final Collection<String> rootsToAddToLib = new THashSet<String>();
    final Map<String, Map<String, List<String>>> moduleToPackageNameToDirMap = new TreeMap<String, Map<String, List<String>>>();

    final Runnable runnable = new Runnable() {
      public void run() {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setIndeterminate(true);
          indicator.setText("pub list-package-dirs");
        }

        if (!DartAnalysisServerService.getInstance().serverReadyForRequest(project, sdk)) return;

        DartAnalysisServerService.getInstance().updateFilesContent();

        DartAnalysisServerService.LibraryDependenciesResult libraryDependenciesResult =
          DartAnalysisServerService.getInstance().analysis_getLibraryDependencies();

        if (libraryDependenciesResult == null) {
          libraryDependenciesResult = new DartAnalysisServerService.LibraryDependenciesResult(new String[]{}, Collections
            .<String, Map<String, List<String>>>emptyMap());
        }
        String[] libraries = libraryDependenciesResult.getLibraries();
        if (libraries == null) {
          libraries = new String[]{};
        }
        Map<String, Map<String, List<String>>> packageMapMap = libraryDependenciesResult.getPackageMap();
        if (packageMapMap == null) {
          packageMapMap = Collections.emptyMap();
        }

        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (final Module module : modules) {
          if (DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) {
            for (final VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
              // if there is a pubspec, skip this contentRoot
              if (contentRoot.findChild(PubspecYamlUtil.PUBSPEC_YAML) != null) continue;

              affectedModules.add(module);
            }
          }
        }

        computeLibraryRoots(project, sdk, libraries, rootsToAddToLib);
        computePackageMap(project, packageMapMap, moduleToPackageNameToDirMap);
      }
    };

    if (ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, "pub list-package-dirs", true, project)) {
      @NotNull final DartListPackageDirsDialog dialog = new DartListPackageDirsDialog(project, rootsToAddToLib, moduleToPackageNameToDirMap);
      dialog.show();

      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        configurePubListPackageDirsLibrary(project, affectedModules, rootsToAddToLib, moduleToPackageNameToDirMap);
      }

      if (dialog.getExitCode() == DartListPackageDirsDialog.CONFIGURE_NONE_EXIT_CODE) {
        removePubListPackageDirsLibrary(project);
      }
    }
  }

  @VisibleForTesting
  public static void configurePubListPackageDirsLibrary(@NotNull final Project project,
                                                        @NotNull final Set<Module> modules,
                                                        @NotNull final Collection<String> rootsToAddToLib,
                                                        @NotNull final Map<String, Map<String, List<String>>> moduleToPackageNameToDirMap) {
    if (modules.isEmpty() || moduleToPackageNameToDirMap.isEmpty()) {
      removePubListPackageDirsLibrary(project);
      return;
    }

    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          doConfigurePubListPackageDirsLibrary(project, modules, rootsToAddToLib, moduleToPackageNameToDirMap);
        }
      }
    );
  }

  private static void doConfigurePubListPackageDirsLibrary(@NotNull final Project project,
                                                           @NotNull final Set<Module> modules,
                                                           @NotNull final Collection<String> rootsToAddToLib,
                                                           @NotNull final Map<String, Map<String, List<String>>> moduleToPackageNameToDirMap) {
    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      final Library library = createPubListPackageDirsLibrary(project, module, rootsToAddToLib, moduleToPackageNameToDirMap);
      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      try {
        OrderEntry existingEntry = null;
        for (final OrderEntry entry : modifiableModel.getOrderEntries()) {
          if (entry instanceof LibraryOrderEntry &&
              LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)entry).getLibraryLevel()) &&
              PUB_LIST_PACKAGE_DIRS_LIB_NAME.equals(((LibraryOrderEntry)entry).getLibraryName())) {
            existingEntry = entry;
            break;
          }
        }

        final boolean contains = existingEntry != null;
        final boolean mustContain = modules.contains(module);

        if (contains != mustContain) {
          if (mustContain) {
            modifiableModel.addLibraryEntry(library);
          }
          else {
            modifiableModel.removeOrderEntry(existingEntry);
          }
        }

        if (modifiableModel.isChanged()) {
          modifiableModel.commit();
        }
      }
      finally {
        if (!modifiableModel.isDisposed()) {
          modifiableModel.dispose();
        }
      }
    }
  }


  private static Library createPubListPackageDirsLibrary(@NotNull final Project project,
                                                         @NotNull final Module module,
                                                         @NotNull final Collection<String> rootsToAddToLib,
                                                         @NotNull final Map<String, Map<String, List<String>>> moduleToPackageNameToDirMap) {
    Library library = ProjectLibraryTable.getInstance(project).getLibraryByName(PUB_LIST_PACKAGE_DIRS_LIB_NAME);
    if (library == null) {
      final LibraryTableBase.ModifiableModel libTableModel = ProjectLibraryTable.getInstance(project).getModifiableModel();
      library = libTableModel.createLibrary(PUB_LIST_PACKAGE_DIRS_LIB_NAME, DartListPackageDirsLibraryType.LIBRARY_KIND);
      libTableModel.commit();
    }

    final LibraryEx.ModifiableModelEx libModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
    try {
      for (String url : libModel.getUrls(OrderRootType.CLASSES)) {
        libModel.removeRoot(url, OrderRootType.CLASSES);
      }

      for (String packageDir : rootsToAddToLib) {
        libModel.addRoot(VfsUtilCore.pathToUrl(packageDir), OrderRootType.CLASSES);
      }

      final Map<String, List<String>> packageMap = moduleToPackageNameToDirMap.get(getModuleStringKey(module));
      if(packageMap != null) {
        final DartListPackageDirsLibraryProperties libraryProperties = new DartListPackageDirsLibraryProperties();
        libraryProperties.setPackageNameToFileDirsMap(packageMap);
        libModel.setProperties(libraryProperties);
        libModel.commit();
      }
    }
    finally {
      if (!Disposer.isDisposed(libModel)) {
        Disposer.dispose(libModel);
      }
    }
    return library;
  }

  private static void removePubListPackageDirsLibrary(@NotNull final Project project) {
    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          doRemovePubListPackageDirsLibrary(project);
        }
      }
    );
  }

  private static void doRemovePubListPackageDirsLibrary(@NotNull final Project project) {
    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      try {
        for (final OrderEntry entry : modifiableModel.getOrderEntries()) {
          if (entry instanceof LibraryOrderEntry &&
              LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)entry).getLibraryLevel()) &&
              PUB_LIST_PACKAGE_DIRS_LIB_NAME.equals(((LibraryOrderEntry)entry).getLibraryName())) {
            modifiableModel.removeOrderEntry(entry);
          }
        }

        if (modifiableModel.isChanged()) {
          modifiableModel.commit();
        }
      }
      finally {
        if (!modifiableModel.isDisposed()) {
          modifiableModel.dispose();
        }
      }
    }

    final Library library = ProjectLibraryTable.getInstance(project).getLibraryByName(PUB_LIST_PACKAGE_DIRS_LIB_NAME);
    if (library != null) {
      ProjectLibraryTable.getInstance(project).removeLibrary(library);
    }
  }
}
