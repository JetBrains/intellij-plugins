package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.AnalysisEngine;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.ChangeSet;
import com.google.dart.engine.internal.context.AnalysisOptionsImpl;
import com.google.dart.engine.sdk.DirectoryBasedDartSdk;
import com.google.dart.engine.source.DartUriResolver;
import com.google.dart.engine.source.ExplicitPackageUriResolver;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.SourceFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.LowMemoryWatcher;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.*;
import com.intellij.reference.SoftReference;
import com.intellij.util.Function;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DartAnalyzerService {

  private final Project myProject;

  private @Nullable String mySdkPath;
  private long myPubspecYamlTimestamp;
  private @Nullable VirtualFile[] myDartPackageRoots;
  private @Nullable VirtualFile myContentRoot; // checked only in case of ExplicitPackageUriResolver

  private @Nullable SoftReference<AnalysisContext> myAnalysisContextRef;

  private final Collection<VirtualFile> myCreatedFiles = Collections.synchronizedSet(new THashSet<VirtualFile>());

  private final Map<VirtualFile, DartFileBasedSource> myFileToSourceMap =
    Collections.synchronizedMap(new THashMap<VirtualFile, DartFileBasedSource>());

  private static final AtomicInteger ourOpenDartProjectsCount = new AtomicInteger(0);

  private @Nullable static DirectoryBasedDartSdk ourDirectoryBasedDartSdk;

  static {
    // no need in stopping this LowMemoryWatcher ever
    //noinspection ResultOfMethodCallIgnored
    LowMemoryWatcher.register(new Runnable() {
      @Override
      public void run() {
        AnalysisEngine.getInstance().clearCaches();
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        ourDirectoryBasedDartSdk = null;
      }
    });
  }

  public DartAnalyzerService(final Project project) {
    myProject = project;
    ourOpenDartProjectsCount.incrementAndGet();

    final VirtualFileAdapter listener = new VirtualFileAdapter() {
      public void beforePropertyChange(@NotNull final VirtualFilePropertyEvent event) {
        if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
          fileDeleted(event);
        }
      }

      public void beforeFileMovement(@NotNull final VirtualFileMoveEvent event) {
        fileDeleted(event);
      }

      public void fileDeleted(@NotNull final VirtualFileEvent event) {
        if (FileUtilRt.extensionEquals(event.getFileName(), DartFileType.DEFAULT_EXTENSION)) {
          myFileToSourceMap.remove(event.getFile());
        }
      }

      public void propertyChanged(@NotNull final VirtualFilePropertyEvent event) {
        if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
          fileCreated(event);
        }
      }

      public void fileMoved(@NotNull final VirtualFileMoveEvent event) {
        fileCreated(event);
      }

      public void fileCopied(@NotNull final VirtualFileCopyEvent event) {
        fileCreated(event);
      }

      public void fileCreated(@NotNull final VirtualFileEvent event) {
        if (FileUtilRt.extensionEquals(event.getFileName(), DartFileType.DEFAULT_EXTENSION)) {
          myCreatedFiles.add(event.getFile());
        }
      }
    };

    LocalFileSystem.getInstance().addVirtualFileListener(listener);

    Disposer.register(project, new Disposable() {
      public void dispose() {
        LocalFileSystem.getInstance().removeVirtualFileListener(listener);

        if (myAnalysisContextRef != null) {
          myAnalysisContextRef.clear();
        }

        if (ourOpenDartProjectsCount.decrementAndGet() == 0) {
          AnalysisEngine.getInstance().clearCaches();
          //noinspection AssignmentToStaticFieldFromInstanceMethod
          ourDirectoryBasedDartSdk = null;
        }
      }
    });
  }

  @NotNull
  public static DartAnalyzerService getInstance(final @NotNull Project project) {
    return ServiceManager.getService(project, DartAnalyzerService.class);
  }

  @NotNull
  public AnalysisContext getAnalysisContext(final @NotNull VirtualFile annotatedFile,
                                            final @NotNull String sdkPath) {
    AnalysisContext analysisContext = SoftReference.dereference(myAnalysisContextRef);

    final DartUrlResolver dartUrlResolver = DartUrlResolver.getInstance(myProject, annotatedFile);
    final VirtualFile yamlFile = dartUrlResolver.getPubspecYamlFile();
    final Document cachedDocument = yamlFile == null ? null : FileDocumentManager.getInstance().getCachedDocument(yamlFile);
    final long pubspecYamlTimestamp = yamlFile == null ? -1
                                                       : cachedDocument == null ? yamlFile.getModificationCount()
                                                                                : cachedDocument.getModificationStamp();

    final VirtualFile[] packageRoots = dartUrlResolver.getPackageRoots();

    final VirtualFile contentRoot = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(annotatedFile);
    final Module module = ModuleUtilCore.findModuleForFile(annotatedFile, myProject);

    final boolean useExplicitPackageUriResolver = !ApplicationManager.getApplication().isUnitTestMode() &&
                                                  contentRoot != null &&
                                                  module != null &&
                                                  !DartConfigurable.isCustomPackageRootSet(module) &&
                                                  yamlFile == null;

    final boolean sameContext = analysisContext != null &&
                                Comparing.equal(sdkPath, mySdkPath) &&
                                pubspecYamlTimestamp == myPubspecYamlTimestamp &&
                                Comparing.haveEqualElements(packageRoots, myDartPackageRoots) &&
                                (!useExplicitPackageUriResolver || Comparing.equal(contentRoot, myContentRoot));

    if (sameContext) {
      applyChangeSet(analysisContext, annotatedFile);
      myCreatedFiles.clear();
    }
    else {
      final DirectoryBasedDartSdk dirBasedSdk = getDirectoryBasedDartSdkSdk(sdkPath);
      final DartUriResolver dartUriResolver = new DartUriResolver(dirBasedSdk);
      final DartFileAndPackageUriResolver fileAndPackageUriResolver = new DartFileAndPackageUriResolver(myProject, dartUrlResolver);

      final SourceFactory sourceFactory = useExplicitPackageUriResolver
                                          ? new SourceFactory(dartUriResolver, fileAndPackageUriResolver,
                                                              new ExplicitPackageUriResolver(dirBasedSdk, new File(contentRoot.getPath())))
                                          : new SourceFactory(dartUriResolver, fileAndPackageUriResolver);

      analysisContext = AnalysisEngine.getInstance().createAnalysisContext();
      analysisContext.setSourceFactory(sourceFactory);
      AnalysisOptionsImpl contextOptions = new AnalysisOptionsImpl();
      contextOptions.setEnableAsync(true);
      contextOptions.setEnableEnum(true);
      analysisContext.setAnalysisOptions(contextOptions);

      mySdkPath = sdkPath;
      myPubspecYamlTimestamp = pubspecYamlTimestamp;
      myDartPackageRoots = packageRoots;
      myContentRoot = contentRoot;
      myAnalysisContextRef = new SoftReference<AnalysisContext>(analysisContext);
    }

    return analysisContext;
  }

  private static synchronized DirectoryBasedDartSdk getDirectoryBasedDartSdkSdk(@NotNull final String sdkPath) {
    final File sdkDir = new File(sdkPath);
    if (ourDirectoryBasedDartSdk == null || !FileUtil.filesEqual(sdkDir, ourDirectoryBasedDartSdk.getDirectory())) {
      AnalysisEngine.getInstance().clearCaches();
      ourDirectoryBasedDartSdk = new DirectoryBasedDartSdk(sdkDir);
    }
    return ourDirectoryBasedDartSdk;
  }

  private void applyChangeSet(final AnalysisContext context, final VirtualFile annotatedFile) {
    final ChangeSet changeSet = new ChangeSet();

    final DartFileBasedSource source = myFileToSourceMap.get(annotatedFile);
    if (source != null) {
      handleDeletedAndOutOfDateSources(changeSet, source);
    }

    handleDeletedAndOutOfDateSources(changeSet, context.getLibrarySources());
    handleDeletedAndOutOfDateSources(changeSet, context.getHtmlSources());

    synchronized (myCreatedFiles) {
      for (VirtualFile file : myCreatedFiles) {
        changeSet.addedSource(DartFileBasedSource.getSource(myProject, file));
      }
    }

    context.applyChanges(changeSet);
  }

  private void handleDeletedAndOutOfDateSources(final ChangeSet changeSet, final Source... sources) {
    for (final Source source : sources) {
      if (source instanceof DartFileBasedSource) {
        if (!source.exists() || !myFileToSourceMap.containsKey(((DartFileBasedSource)source).getFile())) {
          changeSet.removedSource(source);
          continue;
        }

        if (((DartFileBasedSource)source).isOutOfDate()) {
          changeSet.changedSource(source);
        }
      }
    }
  }

  /**
   * Do not use this method directly, use {@link com.jetbrains.lang.dart.analyzer.DartFileBasedSource#getSource(com.intellij.openapi.project.Project, com.intellij.openapi.vfs.VirtualFile)}
   */
  @NotNull
  DartFileBasedSource getOrCreateSource(final @NotNull VirtualFile file,
                                        final @NotNull Function<VirtualFile, DartFileBasedSource> creator) {
    DartFileBasedSource source = myFileToSourceMap.get(file);
    if (source == null) {
      source = creator.fun(file);
      myFileToSourceMap.put(file, source);
    }
    return source;
  }
}
