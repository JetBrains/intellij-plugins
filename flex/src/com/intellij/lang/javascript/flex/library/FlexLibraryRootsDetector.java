package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryRootType;
import com.intellij.openapi.roots.libraries.ui.DetectedLibraryRoot;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.libraries.ui.impl.LibraryRootsDetectorImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlexLibraryRootsDetector extends LibraryRootsDetectorImpl {

  public FlexLibraryRootsDetector() {
    super(getRootDetectors());
  }

  @NotNull
  static List<RootDetector> getRootDetectors() {
    return Arrays.asList(new FlexSwcLibrariesRootDetector(),
                         new FlexDocsRootDetector(),
                         new FlexSourcesRootDetector(),
                         new FlexSwcFoldersRootDetector());
  }

  @Override
  public Collection<DetectedLibraryRoot> detectRoots(@NotNull final VirtualFile rootCandidate,
                                                     @NotNull final ProgressIndicator progressIndicator) {
    Collection<DetectedLibraryRoot> roots = super.detectRoots(rootCandidate, progressIndicator);
    boolean swcsFoldersFound = ContainerUtil.find(roots, root -> {
      LibraryRootType libraryRootType = root.getTypes().get(0);
      return libraryRootType.getType() == OrderRootType.CLASSES && libraryRootType.isJarDirectory();
    }) != null;

    final List<LibraryRootType> types = Arrays.asList(new LibraryRootType(OrderRootType.CLASSES, false),
                                                      new LibraryRootType(OrderRootType.SOURCES, false));
    if (swcsFoldersFound) {
      // if both sources and swcs were detected, assume that source files are src attachment, otherwise assume they are raw as libraries
      Collections.reverse(types);
    }
    return ContainerUtil.map(roots, root -> {
      if (root.getTypes().get(0).getType() == OrderRootType.SOURCES) {
        return new DetectedLibraryRoot(root.getFile(), types);
      }
      return root;
    });
  }

  @Override
  public String getRootTypeName(@NotNull final LibraryRootType rootType) {
    if (rootType.getType() == OrderRootType.SOURCES) {
      return FlexBundle.message("sources.root.detector.name");
    }
    else if (rootType.getType() == OrderRootType.CLASSES) {
      if (rootType.isJarDirectory()) {
        return FlexBundle.message("swc.folders.root.detector.name");
      }
      else {
        return FlexBundle.message("as.libraries.root.detector.name");
      }
    }
    else if (rootType.getType() instanceof JavadocOrderRootType) {
      return FlexBundle.message("docs.root.detector.name");
    }
    return null;
  }
}
