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

import java.io.File;
import java.util.*;

// todo instead of unioning all of the package name maps and configuring all Dart modules to the unioned value, the module roots could be
// todo used as a key to only set the specific package map information for the specific module.
public class PubListPackageDirsAction2 extends AnAction {

  public static final String PUB_LIST_PACKAGE_DIRS_LIB_NAME = "Dart pub list-package-dirs";

  private static final Logger LOG = Logger.getInstance(PubListPackageDirsAction2.class.getName());

  public PubListPackageDirsAction2() {
    super("Configure Dart package roots using 'pub list-package-dirs'", null, DartIcons.Dart_16);
  }

  private static void computeLibraryRoots(@NotNull final Project project,
                                          @NotNull final DartSdk dartSdk,
                                          @NotNull final String[] libraries,
                                          @NotNull final Collection<String> rootsToAddToLib) {
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

  private static void computePackageMap(@NotNull final Map<String, Map<String, List<String>>> packageMapMap,
                                        @NotNull final Map<String, List<File>> packageNameToDirMap) {
    for (final Map.Entry<String, Map<String, List<String>>> entry : packageMapMap.entrySet()) {
      Map<String, List<String>> packageMapN = entry.getValue();
      for (final Map.Entry<String, List<String>> entry2 : packageMapN.entrySet()) {
        String packageName = entry2.getKey();
        List<String> listStr = entry2.getValue();
        List<File> packageDirList = new ArrayList<File>(listStr.size());
        for (final String path : listStr) {
          packageDirList.add(new File(FileUtil.toSystemDependentName(path)));
        }
        packageNameToDirMap.put(packageName, packageDirList);
      }
    }
  }

  public void update(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    final DartSdk sdk = project == null ? null : DartSdk.getDartSdk(project);
    e.getPresentation().setEnabled(sdk != null && DartAnalysisServerAnnotator.isDartSDKVersionSufficient(sdk));
  }

  public void actionPerformed(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    final DartSdk sdk = project == null ? null : DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerAnnotator.isDartSDKVersionSufficient(sdk)) return;

    FileDocumentManager.getInstance().saveAllDocuments();

    final Set<Module> affectedModules = new THashSet<Module>();
    final Collection<String> rootsToAddToLib = new THashSet<String>();
    final Map<String, List<File>> packageNameToDirMap = new TreeMap<String, List<File>>();

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
        computePackageMap(packageMapMap, packageNameToDirMap);
      }
    };

    if (ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, "pub list-package-dirs", true, project)) {
      @NotNull final DartListPackageDirsDialog dialog = new DartListPackageDirsDialog(project, rootsToAddToLib, packageNameToDirMap);
      dialog.show();

      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        configurePubListPackageDirsLibrary(project, affectedModules, rootsToAddToLib, packageNameToDirMap);
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
                                                        @NotNull final Map<String, List<File>> packageMap) {
    if (modules.isEmpty() || packageMap.isEmpty()) {
      removePubListPackageDirsLibrary(project);
      return;
    }

    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          doConfigurePubListPackageDirsLibrary(project, modules, rootsToAddToLib, packageMap);
        }
      }
    );
  }

  private static void doConfigurePubListPackageDirsLibrary(@NotNull final Project project,
                                                           @NotNull final Set<Module> modules,
                                                           @NotNull final Collection<String> rootsToAddToLib,
                                                           @NotNull final Map<String, List<File>> packageMap) {
    final Library library = createPubListPackageDirsLibrary(project, rootsToAddToLib, packageMap);

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
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
                                                         @NotNull final Collection<String> rootsToAddToLib,
                                                         @NotNull final Map<String, List<File>> packageMap) {
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

      final DartListPackageDirsLibraryProperties libraryProperties = new DartListPackageDirsLibraryProperties();
      libraryProperties.setPackageNameToFileDirsMap(packageMap);
      libModel.setProperties(libraryProperties);
      libModel.commit();
    }
    finally {
      if (!Disposer.isDisposed(libModel)) {
        Disposer.dispose(libModel);
      }
    }
    return library;
  }

  static void removePubListPackageDirsLibrary(final @NotNull Project project) {
    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          doRemovePubListPackageDirsLibrary(project);
        }
      }
    );
  }

  private static void doRemovePubListPackageDirsLibrary(final @NotNull Project project) {
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
