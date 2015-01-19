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
import com.google.dart.engine.sdk.DirectoryBasedDartSdk;
import com.google.dart.engine.source.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProcessCanceledException;
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
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalyzerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class PubListPackageDirsAction extends AnAction {

  public static final String PUB_LIST_PACKAGE_DIRS_LIB_NAME = "Dart pub list-package-dirs";

  private static final Logger LOG = Logger.getInstance(PubListPackageDirsAction.class.getName());

  private static final com.google.dart.engine.utilities.logging.Logger.NullLogger PROCESS_CANCELLING_LOGGER =
    new com.google.dart.engine.utilities.logging.Logger.NullLogger() {
      @Override
      public void logError(final String message, final Throwable exception) {
        if (exception instanceof ProcessCanceledException) {
          throw (ProcessCanceledException)exception;
        }
      }
    };

  public PubListPackageDirsAction() {
    super("Configure Dart package roots using 'pub list-package-dirs'", null, DartIcons.Dart_16);
  }

  public void update(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    final DartSdk sdk = project == null ? null : DartSdk.getDartSdk(project);
    e.getPresentation().setEnabled(sdk != null);
  }

  public void actionPerformed(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    final DartSdk sdk = project == null ? null : DartSdk.getDartSdk(project);
    if (sdk == null) return;

    FileDocumentManager.getInstance().saveAllDocuments();

    final DirectoryBasedDartSdk dirBasedSdk = DartAnalyzerService.getInstance(project).getDirectoryBasedDartSdkSdk(sdk.getHomePath());

    final Set<Module> affectedModules = new THashSet<Module>();
    final Collection<String> rootsToAddToLib = new TreeSet<String>();
    final Map<String, List<File>> packageNameToDirMap = new TreeMap<String, List<File>>();

    final Runnable runnable = new Runnable() {
      public void run() {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setIndeterminate(true);
        }

        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (final Module module : modules) {
          if (indicator != null) {
            indicator.checkCanceled();
            indicator.setText("pub list-package-dirs");
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

        if (!packageNameToDirMap.isEmpty()) {
          if (indicator != null) {
            indicator.checkCanceled();
            indicator.setText("Analyzing project dependencies");
          }
          collectRootsToAddToLib(project, dirBasedSdk, rootsToAddToLib, packageNameToDirMap);
        }
      }
    };

    if (ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, "pub list-package-dirs", true, project)) {
      final DartListPackageDirsDialog dialog = new DartListPackageDirsDialog(project, rootsToAddToLib, packageNameToDirMap);
      dialog.show();

      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        configurePubListPackageDirsLibrary(project, affectedModules, rootsToAddToLib, packageNameToDirMap);
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

  private static void configurePubListPackageDirsLibrary(@NotNull final Project project,
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

  private static void collectRootsToAddToLib(@NotNull final Project project,
                                             @NotNull final DirectoryBasedDartSdk dirBasedSdk,
                                             @NotNull final Collection<String> rootsToAddToLib,
                                             @NotNull final Map<String, List<File>> packageMap) {
    AnalysisEngine.getInstance().setLogger(PROCESS_CANCELLING_LOGGER);
    try {
      doCollectRootsToAddToLib(project, dirBasedSdk, rootsToAddToLib, packageMap);
    }
    finally {
      AnalysisEngine.getInstance().setLogger(null);
    }
  }

  private static void doCollectRootsToAddToLib(@NotNull final Project project,
                                               @NotNull final DirectoryBasedDartSdk dirBasedSdk,
                                               @NotNull final Collection<String> rootsToAddToLib,
                                               @NotNull final Map<String, List<File>> packageMap) {
    final AnalysisContext analysisContext = setupAnalysisContext(dirBasedSdk, packageMap);
    final Collection<Source> sources = initSources(project, analysisContext, packageMap);

    for (Source source : sources) {
      try {
        analysisContext.computeLibraryElement(source);
      }
      catch (AnalysisException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ProcessCanceledException) {
          throw (ProcessCanceledException)cause;
        }

        LOG.warn("source=" + source.getUri(), e);
      }
    }

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final SortedSet<String> folderPaths = new LibraryDependencyCollector(analysisContext).collectFolderDependencies();
    final String sdkRoot = FileUtil.toSystemIndependentName(dirBasedSdk.getDirectory().getPath()) + "/";

    outer:
    for (String path : folderPaths) {
      final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (!path.startsWith(sdkRoot) && (vFile == null || !fileIndex.isInContent(vFile))) {
        for (String configuredPath : rootsToAddToLib) {
          if (path.startsWith(configuredPath + "/")) {
            continue outer; // folderPaths is sorted so subfolders go after parent folder
          }
        }

        rootsToAddToLib.add(path);
      }
    }
  }

  private static AnalysisContext setupAnalysisContext(@NotNull final DirectoryBasedDartSdk dirBasedSdk,
                                                      @NotNull final Map<String, List<File>> packageMap) {
    final UriResolver checkCancelledUriResolver = new UriResolver() {
      @Override
      public Source resolveAbsolute(final URI uri) {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.checkCanceled();
        }
        return null;
      }
    };

    final SourceFactory sourceFactory = new SourceFactory(checkCancelledUriResolver,
                                                          new DartUriResolver(dirBasedSdk),
                                                          new FileUriResolver(),
                                                          new MyExplicitPackageUriResolverMapProvided(dirBasedSdk, packageMap));

    final AnalysisOptionsImpl contextOptions = new AnalysisOptionsImpl();
    contextOptions.setAnalyzeFunctionBodies(false);
    contextOptions.setGenerateSdkErrors(false);
    contextOptions.setEnableAsync(true);
    contextOptions.setEnableDeferredLoading(true);
    contextOptions.setEnableEnum(true);

    final AnalysisContext analysisContext = AnalysisEngine.getInstance().createAnalysisContext();
    analysisContext.setSourceFactory(sourceFactory);
    analysisContext.setAnalysisOptions(contextOptions);
    return analysisContext;
  }

  @NotNull
  private static Collection<Source> initSources(@NotNull final Project project,
                                                @NotNull final AnalysisContext analysisContext,
                                                @NotNull final Map<String, List<File>> packageMap) {
    final ChangeSet changeSet = new ChangeSet();
    final Collection<VirtualFile> dartFiles = FileTypeIndex.getFiles(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    final Collection<Source> sources = new ArrayList<Source>(dartFiles.size());
    final Map<String, String> packageRootToPackageNameMap = invertPackageMap(packageMap);

    for (VirtualFile virtualFile : dartFiles) {
      final URI uri = getUriForFile(virtualFile.getPath(), packageRootToPackageNameMap);
      Source source = new FileBasedSource(uri, new File(virtualFile.getPath()));
      changeSet.addedSource(source);
      sources.add(source);
    }

    analysisContext.applyChanges(changeSet);

    return sources;
  }

  @NotNull
  private static Map<String, String> invertPackageMap(@NotNull final Map<String, List<File>> packageNameToPackageRootsMap) {
    final Map<String, String> result = new THashMap<String, String>(packageNameToPackageRootsMap.size());

    for (Map.Entry<String, List<File>> entry : packageNameToPackageRootsMap.entrySet()) {
      final String packageName = entry.getKey();
      final List<File> packageRoots = entry.getValue();
      for (File packageRoot : packageRoots) {
        result.put(FileUtil.toSystemIndependentName(packageRoot.getPath()), packageName);
      }
    }

    return result;
  }

  @NotNull
  private static URI getUriForFile(@NotNull final String filePath, @NotNull final Map<String, String> packageRootToPackageNameMap) {
    String parentPath = PathUtil.getParentPath(filePath);
    while (parentPath.length() > 0) {
      final String packageName = packageRootToPackageNameMap.get(parentPath);
      if (packageName != null) {
        final String uri = DartUrlResolver.PACKAGE_PREFIX + packageName + filePath.substring(parentPath.length());
        try {
          return new URI(uri);
        }
        catch (URISyntaxException e) {
          LOG.warn(uri, e);
        }
      }
      parentPath = PathUtil.getParentPath(parentPath);
    }

    return new File(filePath).toURI();
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
                                                 final @NotNull Map<String, List<File>> packageMap) {
    super(sdk, new File("Nonexistent project root"));
    this.packageMap = packageMap;
  }

  // pass back the result that this object was created with
  @Override
  public Map<String, List<File>> calculatePackageMap() {
    return packageMap;
  }
}

class LibraryDependencyCollector {
  private final AnalysisContext myContext;
  private final Set<LibraryElement> myVisitedLibraries = new HashSet<LibraryElement>();
  private final SortedSet<String> myDependencies = new TreeSet<String>();

  LibraryDependencyCollector(@NotNull AnalysisContext context) {
    this.myContext = context;
  }

  SortedSet<String> collectFolderDependencies() {
    for (Source source : myContext.getLibrarySources()) {
      addDependencies(myContext.getLibraryElement(source));
    }
    return myDependencies;
  }

  private void addDependencies(@Nullable LibraryElement libraryElement) {
    if (libraryElement == null) {
      return;
    }
    if (myVisitedLibraries.add(libraryElement)) {
      for (CompilationUnitElement cu : libraryElement.getUnits()) {
        final String path = cu.getSource().getFullName();
        if (path != null) {
          myDependencies.add(FileUtil.toSystemIndependentName(PathUtil.getParentPath(path)));
        }
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
