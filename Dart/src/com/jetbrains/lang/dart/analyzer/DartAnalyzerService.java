package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.AnalysisEngine;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.ChangeSet;
import com.google.dart.engine.sdk.DirectoryBasedDartSdk;
import com.google.dart.engine.source.DartUriResolver;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.SourceFactory;
import com.google.dart.engine.source.UriResolver;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.*;
import com.intellij.reference.SoftReference;
import com.intellij.util.Function;
import com.jetbrains.lang.dart.DartFileType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class DartAnalyzerService {

  private final Project myProject;

  private @Nullable String mySdkPath;
  private @Nullable VirtualFile myDartPackagesFolder;
  private @Nullable WeakReference<AnalysisContext> myAnalysisContextRef;

  private final Collection<VirtualFile> myCreatedFiles = Collections.synchronizedSet(new THashSet<VirtualFile>());

  private final Map<VirtualFile, DartFileBasedSource> myFileToSourceMap =
    Collections.synchronizedMap(new THashMap<VirtualFile, DartFileBasedSource>());

  public DartAnalyzerService(final Project project) {
    myProject = project;

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
      }
    });
  }

  @NotNull
  public static DartAnalyzerService getInstance(final @NotNull Project project) {
    return ServiceManager.getService(project, DartAnalyzerService.class);
  }

  @NotNull
  public AnalysisContext getAnalysisContext(final @NotNull VirtualFile annotatedFile,
                                            final @NotNull String sdkPath,
                                            final @Nullable VirtualFile packagesFolder) {
    AnalysisContext analysisContext = SoftReference.dereference(myAnalysisContextRef);

    if (analysisContext != null && Comparing.equal(sdkPath, mySdkPath) && Comparing.equal(packagesFolder, myDartPackagesFolder)) {
      applyChangeSet(analysisContext, annotatedFile);
      myCreatedFiles.clear();
    }
    else {
      final DartUriResolver dartUriResolver = new DartUriResolver(new DirectoryBasedDartSdk(new File(sdkPath)));
      final UriResolver fileResolver = new DartFileUriResolver(myProject);
      final SourceFactory sourceFactory =
        packagesFolder == null
        ? new SourceFactory(dartUriResolver, fileResolver)
        : new SourceFactory(dartUriResolver, fileResolver, new DartPackageUriResolver(myProject, packagesFolder));

      analysisContext = AnalysisEngine.getInstance().createAnalysisContext();
      analysisContext.setSourceFactory(sourceFactory);

      mySdkPath = sdkPath;
      myDartPackagesFolder = packagesFolder;
      myAnalysisContextRef = new WeakReference<AnalysisContext>(analysisContext);
    }

    return analysisContext;
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
