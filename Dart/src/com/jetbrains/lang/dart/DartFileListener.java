// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryProperties;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DotPackagesFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

/**
 * {@link DartFileListener} helps to keep "Dart Packages" library (based on Dart-specific pubspec.yaml and .packages files) up-to-date.
 * Also, it makes sure that Dart build output folder and related folders with Pub-specific cache are excluded.
 * Also, it updates the list of files visible for Analysis Server in case of move and rename.
 *
 * @see DartStartupActivity
 * @see DartModuleRootListener
 */
public class DartFileListener implements AsyncFileListener {

  private static final Key<Boolean> DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS =
    Key.create("DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS");

  @Nullable
  @Override
  public ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
    SmartList<VFileEvent> dotPackageEvents = new SmartList<>();
    SmartList<VFileEvent> moveOrRenameAnalyzableFileEvents = new SmartList<>();

    for (VFileEvent event : events) {
      if (event.getFileSystem() != LocalFileSystem.getInstance() && !ApplicationManager.getApplication().isUnitTestMode()) continue;

      if (event instanceof VFilePropertyChangeEvent) {
        if (((VFilePropertyChangeEvent)event).isRename()) {
          if (DotPackagesFileUtil.DOT_PACKAGES.equals(((VFilePropertyChangeEvent)event).getOldValue()) ||
              DotPackagesFileUtil.DOT_PACKAGES.equals(((VFilePropertyChangeEvent)event).getNewValue())) {
            dotPackageEvents.add(event);
          }

          if (DartAnalysisServerService.isFileNameRespectedByAnalysisServer(((VFilePropertyChangeEvent)event).getOldValue().toString()) ||
              DartAnalysisServerService.isFileNameRespectedByAnalysisServer(((VFilePropertyChangeEvent)event).getNewValue().toString())) {
            moveOrRenameAnalyzableFileEvents.add(event);
          }
        }
      }
      else {
        if (DotPackagesFileUtil.DOT_PACKAGES.equals(PathUtil.getFileName(event.getPath()))) {
          dotPackageEvents.add(event);
        }

        if (event instanceof VFileMoveEvent &&
            DartAnalysisServerService.isFileNameRespectedByAnalysisServer(PathUtil.getFileName(event.getPath()))) {
          moveOrRenameAnalyzableFileEvents.add(event);
        }
      }
    }

    if (dotPackageEvents.isEmpty() && moveOrRenameAnalyzableFileEvents.isEmpty()) {
      return null;
    }

    return new DartFileChangeApplier(dotPackageEvents, moveOrRenameAnalyzableFileEvents);
  }

  /**
   * Make sure to set it to {@code false} in the corresponding {@code finally} block
   */
  public static void setDartPackageRootUpdateScheduledOrInProgress(@NotNull final Project project, final boolean scheduledOrInProgress) {
    if (scheduledOrInProgress) {
      project.putUserData(DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS, true);
    }
    else {
      project.putUserData(DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS, null);
    }
  }

  public static void scheduleDartPackageRootsUpdate(@NotNull final Project project) {
    if (Registry.is("dart.projects.without.pubspec", false)) return;

    if (project.getUserData(DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS) == Boolean.TRUE) {
      return;
    }

    setDartPackageRootUpdateScheduledOrInProgress(project, Boolean.TRUE);

    final Runnable runnable = () -> {
      try {
        final Library library = actualizePackagesLibrary(project);

        if (library == null) {
          removeDartPackagesLibraryAndDependencies(project);
        }
        else {
          final Condition<Module> moduleFilter = DartSdkLibUtil::isDartSdkEnabled;

          updateDependenciesOnDartPackagesLibrary(project, moduleFilter, library);
        }
      }
      finally {
        setDartPackageRootUpdateScheduledOrInProgress(project, false);
      }
    };

    ApplicationManager.getApplication()
      .invokeLater(runnable, ModalityState.NON_MODAL, DartAnalysisServerService.getInstance(project).getDisposedCondition());
  }

  @Nullable
  private static Library actualizePackagesLibrary(@NotNull final Project project) {
    final DartLibInfo libInfo = collectPackagesLibraryRoots(project);

    if (libInfo.getLibRootUrls().isEmpty()) {
      return null;
    }
    else {
      return updatePackagesLibraryRoots(project, libInfo);
    }
  }

  @NotNull
  private static DartLibInfo collectPackagesLibraryRoots(@NotNull final Project project) {
    final DartLibInfo libInfo = new DartLibInfo();

    final Collection<VirtualFile> pubspecYamlFiles =
      FilenameIndex.getVirtualFilesByName(project, PUBSPEC_YAML, GlobalSearchScope.projectScope(project));
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    for (VirtualFile pubspecFile : pubspecYamlFiles) {
      final Module module = fileIndex.getModuleForFile(pubspecFile);
      if (module == null || !DartSdkLibUtil.isDartSdkEnabled(module)) continue;

      final VirtualFile dotPackagesFile = findDotPackagesFile(pubspecFile.getParent());
      if (dotPackagesFile != null) {
        final Map<String, String> packagesMap = DotPackagesFileUtil.getPackagesMap(dotPackagesFile);
        if (packagesMap != null) {
          for (Map.Entry<String, String> entry : packagesMap.entrySet()) {
            final String packageName = entry.getKey();
            final String packagePath = entry.getValue();
            if (isPathOutsideProjectContent(fileIndex, packagePath)) {
              libInfo.addPackage(packageName, packagePath);
            }
          }
        }
      }
    }

    return libInfo;
  }

  @Nullable
  private static VirtualFile findDotPackagesFile(@Nullable VirtualFile dir) {
    while (dir != null) {
      final VirtualFile file = dir.findChild(DotPackagesFileUtil.DOT_PACKAGES);
      if (file != null && !file.isDirectory()) {
        return file;
      }
      dir = dir.getParent();
    }
    return null;
  }

  @NotNull
  public static Library updatePackagesLibraryRoots(@NotNull final Project project, @NotNull final DartLibInfo libInfo) {
    final LibraryTable projectLibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
    final Library existingLibrary = projectLibraryTable.getLibraryByName(DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME);
    final Library library =
      existingLibrary != null ? existingLibrary
                              : WriteAction.compute(() -> {
                                final LibraryTable.ModifiableModel libTableModel =
                                  LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();
                                final Library lib = libTableModel
                                  .createLibrary(DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME, DartPackagesLibraryType.LIBRARY_KIND);
                                libTableModel.commit();
                                return lib;
                              });

    final String[] existingUrls = library.getUrls(OrderRootType.CLASSES);

    final Collection<String> libRootUrls = libInfo.getLibRootUrls();

    if ((isBrokenPackageMap(((LibraryEx)library).getProperties())) ||
        existingUrls.length != libRootUrls.size() ||
        !libRootUrls.containsAll(Arrays.asList(existingUrls))) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        final LibraryEx.ModifiableModelEx model = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
        for (String url : existingUrls) {
          model.removeRoot(url, OrderRootType.CLASSES);
        }

        for (String url : libRootUrls) {
          model.addRoot(url, OrderRootType.CLASSES);
        }

        final DartPackagesLibraryProperties libraryProperties = new DartPackagesLibraryProperties();
        libraryProperties.setPackageNameToDirsMap(libInfo.getPackagesMap());
        model.setProperties(libraryProperties);

        model.commit();
      });
    }

    return library;
  }

  private static boolean isBrokenPackageMap(@Nullable final LibraryProperties properties) {
    if (!(properties instanceof DartPackagesLibraryProperties)) return true;

    for (Map.Entry<String, List<String>> entry : ((DartPackagesLibraryProperties)properties).getPackageNameToDirsMap().entrySet()) {
      if (entry == null || entry.getKey() == null || entry.getValue() == null) {
        return true;
      }
    }

    return false;
  }

  private static void removeDartPackagesLibraryAndDependencies(@NotNull final Project project) {
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      removeDependencyOnDartPackagesLibrary(module);
    }

    final Library library =
      LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraryByName(DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME);
    if (library != null) {
      ApplicationManager.getApplication()
        .runWriteAction(() -> LibraryTablesRegistrar.getInstance().getLibraryTable(project).removeLibrary(library));
    }
  }

  public static void updateDependenciesOnDartPackagesLibrary(@NotNull final Project project,
                                                             @NotNull final Condition<? super Module> moduleFilter,
                                                             @NotNull final Library library) {
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      if (moduleFilter.value(module)) {
        addDependencyOnDartPackagesLibrary(module, library);
      }
      else {
        removeDependencyOnDartPackagesLibrary(module);
      }
    }
  }

  private static void removeDependencyOnDartPackagesLibrary(@NotNull final Module module) {
    if (!hasDartPackageLibrary(module)) return;

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (isDartPackageLibrary(orderEntry)) {
          modifiableModel.removeOrderEntry(orderEntry);
        }
      }

      if (modifiableModel.isChanged()) {
        ApplicationManager.getApplication().runWriteAction(modifiableModel::commit);
      }
    }
    finally {
      if (!modifiableModel.isDisposed()) {
        modifiableModel.dispose();
      }
    }
  }

  private static boolean isDartPackageLibrary(OrderEntry orderEntry) {
    return orderEntry instanceof LibraryOrderEntry &&
           LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
           DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName());
  }

  private static void addDependencyOnDartPackagesLibrary(@NotNull final Module module, @NotNull final Library library) {
    if (hasDartPackageLibrary(module)) return;

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      modifiableModel.addLibraryEntry(library);

      ApplicationManager.getApplication().runWriteAction(modifiableModel::commit);
    }
    finally {
      if (!modifiableModel.isDisposed()) {
        modifiableModel.dispose();
      }
    }
  }

  private static boolean hasDartPackageLibrary(@NotNull Module module) {
    return ContainerUtil.exists(ModuleRootManager.getInstance(module).getOrderEntries(), DartFileListener::isDartPackageLibrary);
  }

  private static boolean isPathOutsideProjectContent(@NotNull final ProjectFileIndex fileIndex, @NotNull String path) {
    if (ApplicationManager.getApplication().isUnitTestMode() && path.contains("/pub/global/cache/")) {
      return true;
    }

    while (!path.isEmpty()) {
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
      if (file == null) {
        path = PathUtil.getParentPath(path);
      }
      else {
        return !fileIndex.isInContent(file);
      }
    }

    return false;
  }

  private static class DartLibInfo {
    private final Set<String> myLibRootUrls = new TreeSet<>();
    private final Map<String, List<String>> myPackagesMap = new TreeMap<>();

    private void addPackage(@NotNull final String packageName, @NotNull final String packagePath) {
      myLibRootUrls.add(VfsUtilCore.pathToUrl(packagePath));

      List<String> paths = myPackagesMap.get((packageName));
      if (paths == null) {
        paths = new SmartList<>();
        myPackagesMap.put(packageName, paths);
      }

      if (!paths.contains(packagePath)) {
        paths.add(packagePath);
      }
    }

    private Set<String> getLibRootUrls() {
      return myLibRootUrls;
    }

    private Map<String, List<String>> getPackagesMap() {
      return myPackagesMap;
    }
  }

  private static final class DartFileChangeApplier implements ChangeApplier {
    private final List<? extends VFileEvent> myDotPackageEvents;
    private final List<? extends VFileEvent> myMoveOrRenameAnalyzableFileEvents;

    private DartFileChangeApplier(List<? extends VFileEvent> dotPackageEvents,
                                  List<? extends VFileEvent> moveOrRenameAnalyzableFileEvents) {
      myDotPackageEvents = dotPackageEvents;
      myMoveOrRenameAnalyzableFileEvents = moveOrRenameAnalyzableFileEvents;
    }

    @Override
    public void afterVfsChange() {
      Set<Project> projectsToUpdate = new HashSet<>();
      Set<Project> projectsToUpdateVisibleFiles = new HashSet<>();

      for (Project project : ProjectManager.getInstance().getOpenProjects()) {
        if (DartSdk.getDartSdk(project) == null) continue;

        for (VFileEvent event : myDotPackageEvents) {
          VirtualFile file = event.getFile();
          if (file == null) continue;

          VirtualFile parent = file.getParent();
          VirtualFile pubspec = parent == null ? null : parent.findChild(PUBSPEC_YAML);
          if (pubspec == null) continue;

          if (ProjectFileIndex.getInstance(project).isInContent(file)) {
            projectsToUpdate.add(project);

            final Module module = ModuleUtilCore.findModuleForFile(pubspec, project);
            if (module != null) {
              DartStartupActivity.excludeBuildAndToolCacheFolders(module, pubspec);
            }
          }
        }

        for (VFileEvent event : myMoveOrRenameAnalyzableFileEvents) {
          VirtualFile file = event.getFile();
          if (file == null) continue;

          if (ProjectFileIndex.getInstance(project).isInContent(file)) {
            projectsToUpdateVisibleFiles.add(project);
            break;
          }
        }
      }

      for (Project project : projectsToUpdate) {
        scheduleDartPackageRootsUpdate(project);
      }

      for (Project project : projectsToUpdateVisibleFiles) {
        // this fixes WEB-39785
        DartAnalysisServerService.getInstance(project).updateVisibleFiles();
      }
    }
  }
}
