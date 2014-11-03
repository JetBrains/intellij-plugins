package com.jetbrains.lang.dart.sdk.listPackageDirs;

import com.google.dart.engine.AnalysisEngine;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.context.ChangeSet;
import com.google.dart.engine.element.CompilationUnitElement;
import com.google.dart.engine.element.ExportElement;
import com.google.dart.engine.element.ImportElement;
import com.google.dart.engine.element.LibraryElement;
import com.google.dart.engine.internal.context.AnalysisOptionsImpl;
import com.google.dart.engine.internal.context.InternalAnalysisContext;
import com.google.dart.engine.sdk.DirectoryBasedDartSdk;
import com.google.dart.engine.source.DartUriResolver;
import com.google.dart.engine.source.ExplicitPackageUriResolver;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.SourceFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartFileAndPackageUriResolver;
import com.jetbrains.lang.dart.analyzer.DartFileBasedSource;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashSet;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class PubListPackageDirsAction extends AnAction {

  public static final String PUB_LIST_PACKAGE_DIRS_LIB_NAME = "Dart pub list-package-dirs";

  private static class LibraryDependencyCollector {

    private final InternalAnalysisContext myContext;
    private final Set<LibraryElement> myVisitedLibraries = new HashSet<LibraryElement>();
    private final Set<String> myDependencies = new TreeSet<String>();

    LibraryDependencyCollector(@NotNull InternalAnalysisContext context) {
      this.myContext = context;
    }

    Set<String> collectFolderDependencies() {
      for (Source source : myContext.getLibrarySources()) {
        addDependencies(myContext.getLibraryElement(source));
      }
      return myDependencies;
    }

    private String getFolderName(@Nullable String fullPath) {
      if (fullPath == null) {
        return null;
      }
      return fullPath.substring(0, Math.max(0, fullPath.lastIndexOf(File.separator)));
    }

    private void addDependencies(@Nullable LibraryElement libraryElement) {
      if (libraryElement == null) {
        return;
      }
      if (myVisitedLibraries.add(libraryElement)) {
        for (CompilationUnitElement cu : libraryElement.getUnits()) {
          myDependencies.add(getFolderName(cu.getSource().getFullName()));
        }
        for (ImportElement importElement : libraryElement.getImports()) {
          addDependencies(importElement.getImportedLibrary());
        }
        for (ExportElement exportElement : libraryElement.getExports()) {
          addDependencies(exportElement.getExportedLibrary());
        }
      }
    }
  }

  public PubListPackageDirsAction() {
    super("Configure Dart package roots using 'pub list-package-dirs'", null, DartIcons.Dart_16);
  }

  public void update(final AnActionEvent e) {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    e.getPresentation().setEnabled(sdk != null);
  }

  public void actionPerformed(final AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) return;

    final DirectoryBasedDartSdk dirBasedSdk = new DirectoryBasedDartSdk(new File(sdk.getHomePath()));

    final Set<Module> affectedModules = new THashSet<Module>();
    final SortedMap<String, List<File>> packageNameToDirMap = new TreeMap<String, List<File>>();

    final Runnable runnable = new Runnable() {
      public void run() {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (int i = 0; i < modules.length; i++) {
          final Module module = modules[i];

          final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
          if (indicator != null) {
            indicator.setText("pub list-package-dirs");
            indicator.setText2("Module: " + module.getName());
            indicator.setIndeterminate(false);
            indicator.setFraction((i + 1.) / modules.length);
            indicator.checkCanceled();
          }

          if (DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) {
            for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
              if (contentRoot.findChild(PubspecYamlUtil.PUBSPEC_YAML) != null) continue;

              final File rootDir = new File(contentRoot.getPath());
              final Map<String, List<File>> map = new MyExplicitPackageUriResolver(dirBasedSdk, rootDir).calculatePackageMap();

              if (!map.isEmpty()) {
                affectedModules.add(module);
                addResults(packageNameToDirMap, map);
              }
            }
          }
        }
      }
    };

    if (ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, "pub list-package-dirs", true, project)) {
      final DartListPackageDirsDialog dialog = new DartListPackageDirsDialog(project, packageNameToDirMap);
      dialog.show();

      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        configurePubListPackageDirsLibrary(project, affectedModules, packageNameToDirMap);
      }

      if (dialog.getExitCode() == DartListPackageDirsDialog.CONFIGURE_NONE_EXIT_CODE) {
        removePubListPackageDirsLibrary(project);
      }
    }
  }

  private static void addResults(final @NotNull Map<String, List<File>> packageNameToDirMap,
                                 final @NotNull Map<String, List<File>> map) {
    for (Map.Entry<String, List<File>> entry : map.entrySet()) {
      final String packageName = entry.getKey();
      List<File> packageRoots = packageNameToDirMap.get(packageName);

      if (packageRoots == null) {
        packageRoots = new ArrayList<File>();
        packageNameToDirMap.put(packageName, packageRoots);
      }

      for (File file : entry.getValue()) {
        packageRoots.add(file);
      }
    }
  }

  static void configurePubListPackageDirsLibrary(final @NotNull Project project,
                                                 final @NotNull Set<Module> modules,
                                                 final @NotNull Map<String, List<File>> packageMap) {
    if (modules.isEmpty() || packageMap.isEmpty()) {
      removePubListPackageDirsLibrary(project);
      return;
    }

    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          doConfigurePubListPackageDirsLibrary(project, modules, packageMap);
        }
      }
    );
  }

  private static void doConfigurePubListPackageDirsLibrary(final @NotNull Project project,
                                                           final @NotNull Set<Module> modules,
                                                           final @NotNull Map<String, List<File>> packageMap) {
    final Library library = createPubListPackageDirsLibrary(project, packageMap);

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

  private static Library createPubListPackageDirsLibrary(final @NotNull Project project,
                                                         final @NotNull Map<String, List<File>> packageMap) {
    Library library = ProjectLibraryTable.getInstance(project).getLibraryByName(PUB_LIST_PACKAGE_DIRS_LIB_NAME);
    if (library == null) {
      final LibraryTableBase.ModifiableModelEx libTableModel =
        (LibraryTableBase.ModifiableModelEx)ProjectLibraryTable.getInstance(project).getModifiableModel();
      library = libTableModel.createLibrary(PUB_LIST_PACKAGE_DIRS_LIB_NAME, DartListPackageDirsLibraryType.LIBRARY_KIND);
      libTableModel.commit();
    }

    final LibraryEx.ModifiableModelEx libModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
    try {
      for (String url : libModel.getUrls(OrderRootType.CLASSES)) {
        libModel.removeRoot(url, OrderRootType.CLASSES);
      }

      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      final Set<String> folders = gatherReachableFilesInProject(project, packageMap);
      for (String packageDir : folders) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(packageDir);
        if (!fileIndex.isInContent(virtualFile)) {
          libModel.addRoot(VfsUtilCore.pathToUrl(packageDir), OrderRootType.CLASSES);
        }
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

  private static Set<String> gatherReachableFilesInProject(final @NotNull Project project,
                                                           final @NotNull Map<String, List<File>> packageMap) {
    final VirtualFile contentRoot = project.getBaseDir();
    final DartUrlResolver dartUrlResolver = DartUrlResolver.getInstance(project, contentRoot);
    final DirectoryBasedDartSdk dirBasedSdk = new DirectoryBasedDartSdk(new File(DartSdk.getGlobalDartSdk().getHomePath()));
    final DartUriResolver dartUriResolver = new DartUriResolver(dirBasedSdk);
    final DartFileAndPackageUriResolver fileAndPackageUriResolver = new DartFileAndPackageUriResolver(project, dartUrlResolver);

    final SourceFactory sourceFactory = new SourceFactory(dartUriResolver, fileAndPackageUriResolver,
                                                          new MyExplicitPackageUriResolverMapProvided(dirBasedSdk,
                                                                                                      new File(contentRoot.getPath()),
                                                                                                      packageMap));

    final AnalysisContext analysisContext = AnalysisEngine.getInstance().createAnalysisContext();
    analysisContext.setSourceFactory(sourceFactory);

    final AnalysisOptionsImpl contextOptions = new AnalysisOptionsImpl();
    contextOptions.setAnalyzeFunctionBodies(false);
    contextOptions.setGenerateSdkErrors(false);
    contextOptions.setEnableAsync(true);
    contextOptions.setEnableDeferredLoading(true);
    contextOptions.setEnableEnum(true);
    analysisContext.setAnalysisOptions(contextOptions);

    ChangeSet changeSet = new ChangeSet();
    Collection<VirtualFile> dartFiles = FileTypeIndex.getFiles(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    Set<Source> sources = new HashSet<Source>();
    for (VirtualFile virtualFile : dartFiles) {
      Source source = DartFileBasedSource.getSource(project, virtualFile);
      changeSet.addedSource(source);
      sources.add(source);
    }
    analysisContext.applyChanges(changeSet);

    try {
      for (Source source : sources) {
        analysisContext.computeLibraryElement(source);
      }
    }
    catch (AnalysisException e) {
    }

    return new LibraryDependencyCollector((InternalAnalysisContext)analysisContext).collectFolderDependencies();
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

class MyExplicitPackageUriResolver extends ExplicitPackageUriResolver {
  public MyExplicitPackageUriResolver(final DirectoryBasedDartSdk sdk, final File rootDir) {
    super(sdk, rootDir);
  }

  // need public access to this method
  @Override
  public Map<String, List<File>> calculatePackageMap() {
    return super.calculatePackageMap();
  }
}

class MyExplicitPackageUriResolverMapProvided extends ExplicitPackageUriResolver {
  final Map<String, List<File>> packageMap;

  public MyExplicitPackageUriResolverMapProvided(final DirectoryBasedDartSdk sdk,
                                                 final File rootDir,
                                                 final @NotNull Map<String, List<File>> packageMap) {
    super(sdk, rootDir);
    this.packageMap = packageMap;
  }

  // pass back the result that this object was created with
  @Override
  public Map<String, List<File>> calculatePackageMap() {
    return packageMap;
  }
}
