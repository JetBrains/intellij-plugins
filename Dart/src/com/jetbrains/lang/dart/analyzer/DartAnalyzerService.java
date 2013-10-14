package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.AnalysisEngine;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.ChangeSet;
import com.google.dart.engine.sdk.DirectoryBasedDartSdk;
import com.google.dart.engine.source.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.*;
import com.intellij.util.Function;
import com.jetbrains.lang.dart.DartFileType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DartAnalyzerService {

  private final Project myProject;

  private final Map<Pair<String, VirtualFile>, AnalysisContext> mySdkPathAndPackagesDirToAnalysisContext =
    new THashMap<Pair<String, VirtualFile>, AnalysisContext>();

  private final Collection<VirtualFile> myCreatedFiles = Collections.synchronizedSet(new THashSet<VirtualFile>());

  private final Map<VirtualFile, DartFileBasedSource> myFileToSourceMap =
    Collections.synchronizedMap(new HashMap<VirtualFile, DartFileBasedSource>());


  public DartAnalyzerService(final Project project) {
    myProject = project;

    LocalFileSystem.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
      public void beforePropertyChange(final VirtualFilePropertyEvent event) {
        if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
          fileDeleted(event);
        }
      }

      public void beforeFileMovement(final VirtualFileMoveEvent event) {
        fileDeleted(event);
      }

      public void fileDeleted(final VirtualFileEvent event) {
        if (FileUtilRt.extensionEquals(event.getFileName(), DartFileType.DEFAULT_EXTENSION)) {
          myFileToSourceMap.remove(event.getFile());
        }
      }

      public void propertyChanged(final VirtualFilePropertyEvent event) {
        if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
          fileCreated(event);
        }
      }

      public void fileMoved(final VirtualFileMoveEvent event) {
        fileCreated(event);
      }

      public void fileCopied(final VirtualFileCopyEvent event) {
        fileCreated(event);
      }

      public void fileCreated(final VirtualFileEvent event) {
        if (FileUtilRt.extensionEquals(event.getFileName(), DartFileType.DEFAULT_EXTENSION)) {
          myCreatedFiles.add(event.getFile());
        }
      }
    });
  }

  @NotNull
  public static DartAnalyzerService getInstance(final @NotNull Project project) {
    return ServiceManager.getService(project, DartAnalyzerService.class);
  }

  @NotNull
  public AnalysisContext getAnalysisContext(final @NotNull String sdkPath, @Nullable VirtualFile packagesFolder) {
    if (packagesFolder != null) packagesFolder = packagesFolder.getCanonicalFile();

    final Pair<String, VirtualFile> key = Pair.create(sdkPath, packagesFolder);
    AnalysisContext context = mySdkPathAndPackagesDirToAnalysisContext.get(key);

    if (context == null) {
      final DartUriResolver dartUriResolver = new DartUriResolver(new DirectoryBasedDartSdk(new File(sdkPath)));
      final UriResolver fileResolver = new DartFileResolver(myProject);
      final SourceFactory sourceFactory = packagesFolder == null
                                          ? new SourceFactory(dartUriResolver, fileResolver)
                                          : new SourceFactory(dartUriResolver, fileResolver,
                                                              new PackageUriResolver(new File(packagesFolder.getPath())));

      context = AnalysisEngine.getInstance().createAnalysisContext();
      context.setSourceFactory(sourceFactory);
      mySdkPathAndPackagesDirToAnalysisContext.put(key, context);
    }
    else {
      applyChangeSet(context);
      myCreatedFiles.clear();
    }

    return context;
  }

  private void applyChangeSet(final AnalysisContext context) {
    final ChangeSet changeSet = new ChangeSet();

    for (final Source source : context.getLibrarySources()) {
      if (source instanceof DartFileBasedSource) {
        if (!source.exists() || !myFileToSourceMap.containsKey(((DartFileBasedSource)source).getFile())) {
          changeSet.removed(source);
          continue;
        }

        if (((DartFileBasedSource)source).isOutOfDate()) {
          changeSet.changed(source);
        }
      }
    }

    synchronized (myCreatedFiles) {
      for (VirtualFile file : myCreatedFiles) {
        changeSet.added(DartFileBasedSource.getSource(myProject, file));
      }
    }

    context.applyChanges(changeSet);
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
