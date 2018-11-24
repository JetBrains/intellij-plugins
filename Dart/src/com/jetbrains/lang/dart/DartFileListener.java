package com.jetbrains.lang.dart;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryProperties;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DotPackagesFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartFileListener extends VirtualFileAdapter {

  private static final Key<Boolean> DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS =
    Key.create("DART_PACKAGE_ROOTS_UPDATE_SCHEDULED_OR_IN_PROGRESS");

  private final Project myProject;

  public DartFileListener(Project project) {
    myProject = project;
  }

  @Override
  public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
    propertyChanged(event);
  }

  @Override
  public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
    if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
      fileChanged(myProject, event.getFile());
    }
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    fileChanged(myProject, event.getFile());
  }

  @Override
  public void fileCreated(@NotNull VirtualFileEvent event) {
    fileChanged(myProject, event.getFile());
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    fileChanged(myProject, event.getFile());
  }

  @Override
  public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    fileChanged(myProject, event.getFile());
  }

  @Override
  public void fileCopied(@NotNull VirtualFileCopyEvent event) {
    fileChanged(myProject, event.getFile());
  }

  private static void fileChanged(@NotNull final Project project, @NotNull final VirtualFile file) {
    if (!DotPackagesFileUtil.DOT_PACKAGES.equals(file.getName())) return;
    if (LocalFileSystem.getInstance() != file.getFileSystem()) return;
    final VirtualFile parent = file.getParent();
    final VirtualFile pubspec = parent == null ? null : parent.findChild(PUBSPEC_YAML);

    if (pubspec != null) {
      scheduleDartPackageRootsUpdate(project);

      final Module module = ModuleUtilCore.findModuleForFile(pubspec, project);
      if (module != null) {
        DartProjectComponent.excludeBuildAndPackagesFolders(module, pubspec);
      }
    }
  }

  /**
   * Make sure to set it to <code>false</code> in the corresponding <code>finally</code> block
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

    DumbService.getInstance(project).smartInvokeLater(() -> {
      try {
        final DartSdk sdk = DartSdk.getDartSdk(project);
        final Library library = actualizePackagesLibrary(project, sdk);

        if (library == null) {
          removeDartPackagesLibraryAndDependencies(project);
        }
        else {
          final Condition<Module> moduleFilter = module -> DartSdkGlobalLibUtil.isDartSdkEnabled(module);

          updateDependenciesOnDartPackagesLibrary(project, moduleFilter, library);
        }
      }
      finally {
        setDartPackageRootUpdateScheduledOrInProgress(project, false);
      }
    }, ModalityState.NON_MODAL);
  }

  @Nullable
  private static Library actualizePackagesLibrary(@NotNull final Project project, @Nullable final DartSdk sdk) {
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), "1.12") < 0) return null;

    final DartLibInfo libInfo = collectPackagesLibraryRoots(project, sdk);

    if (libInfo.getLibRootUrls().isEmpty()) {
      return null;
    }
    else {
      return updatePackagesLibraryRoots(project, libInfo);
    }
  }

  @NotNull
  private static DartLibInfo collectPackagesLibraryRoots(@NotNull final Project project, @NotNull final DartSdk sdk) {
    final DartLibInfo libInfo = new DartLibInfo(false);

    final Collection<VirtualFile> pubspecYamlFiles =
      FilenameIndex.getVirtualFilesByName(project, PUBSPEC_YAML, GlobalSearchScope.projectScope(project));
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    for (VirtualFile pubspecFile : pubspecYamlFiles) {
      final VirtualFile dotPackagesFile = pubspecFile.getParent().findChild(DotPackagesFileUtil.DOT_PACKAGES);
      final Module module = dotPackagesFile == null ? null : fileIndex.getModuleForFile(dotPackagesFile);

      if (dotPackagesFile != null &&
          !dotPackagesFile.isDirectory() &&
          module != null &&
          DartSdkGlobalLibUtil.isDartSdkEnabled(module)) {
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

  @NotNull
  public static Library updatePackagesLibraryRoots(@NotNull final Project project, @NotNull final DartLibInfo libInfo) {
    final LibraryTable projectLibraryTable = ProjectLibraryTable.getInstance(project);
    final Library existingLibrary = projectLibraryTable.getLibraryByName(DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME);
    final Library library = existingLibrary != null
                            ? existingLibrary
                            : ApplicationManager.getApplication().runWriteAction(new Computable<Library>() {
                              @Override
                              public Library compute() {
                                final LibraryTableBase.ModifiableModel libTableModel =
                                  ProjectLibraryTable.getInstance(project).getModifiableModel();
                                final Library lib = libTableModel
                                  .createLibrary(DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME, DartPackagesLibraryType.LIBRARY_KIND);
                                libTableModel.commit();
                                return lib;
                              }
                            });

    final String[] existingUrls = library.getUrls(OrderRootType.CLASSES);

    final Collection<String> libRootUrls = libInfo.getLibRootUrls();

    if ((!libInfo.isProjectWithoutPubspec() && isBrokenPackageMap(((LibraryEx)library).getProperties())) ||
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

    final Library library = ProjectLibraryTable.getInstance(project).getLibraryByName(DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME);
    if (library != null) {
      ApplicationManager.getApplication().runWriteAction(() -> ProjectLibraryTable.getInstance(project).removeLibrary(library));
    }
  }

  public static void updateDependenciesOnDartPackagesLibrary(@NotNull final Project project,
                                                             @NotNull final Condition<Module> moduleFilter,
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
    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry &&
            LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
            DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName())) {
          modifiableModel.removeOrderEntry(orderEntry);
        }
      }

      if (modifiableModel.isChanged()) {
        ApplicationManager.getApplication().runWriteAction(() -> modifiableModel.commit());
      }
    }
    finally {
      if (!modifiableModel.isDisposed()) {
        modifiableModel.dispose();
      }
    }
  }

  private static void addDependencyOnDartPackagesLibrary(@NotNull final Module module, @NotNull final Library library) {
    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry &&
            LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
            DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName())) {
          return; // dependency already exists
        }
      }

      modifiableModel.addLibraryEntry(library);

      ApplicationManager.getApplication().runWriteAction(() -> modifiableModel.commit());
    }
    finally {
      if (!modifiableModel.isDisposed()) {
        modifiableModel.dispose();
      }
    }
  }

  private static boolean isPathOutsideProjectContent(@NotNull final ProjectFileIndex fileIndex, @NotNull String path) {
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

  public static class DartLibInfo {
    private final boolean myProjectWithoutPubspec;
    private final Set<String> myLibRootUrls = new TreeSet<>();
    private final Map<String, List<String>> myPackagesMap = new TreeMap<>();

    public DartLibInfo(final boolean projectWithoutPubspec) {
      myProjectWithoutPubspec = projectWithoutPubspec;
    }

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

    public void addRoots(final Collection<String> dirPaths) {
      for (String path : dirPaths) {
        myLibRootUrls.add(VfsUtilCore.pathToUrl(path));
      }
    }

    public boolean isProjectWithoutPubspec() {
      return myProjectWithoutPubspec;
    }

    public Set<String> getLibRootUrls() {
      return myLibRootUrls;
    }

    public Map<String, List<String>> getPackagesMap() {
      return myPackagesMap;
    }
  }
}
