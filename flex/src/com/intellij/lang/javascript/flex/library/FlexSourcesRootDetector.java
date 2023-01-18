package com.intellij.lang.javascript.flex.library;

import com.intellij.ide.util.projectWizard.importSources.util.CommonSourceRootDetectionUtil;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.detection.FlexProjectStructureDetector;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.containers.DistinctRootsCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class FlexSourcesRootDetector extends RootDetector {

  private final boolean myDetectMxml;

  FlexSourcesRootDetector() {
    super(OrderRootType.SOURCES, false, FlexBundle.message("sources.root.detector.name"));
    myDetectMxml = true;
  }

  @NotNull
  @Override
  public Collection<VirtualFile> detectRoots(@NotNull final VirtualFile rootCandidate, @NotNull final ProgressIndicator progressIndicator) {
    DistinctRootsCollection<VirtualFile> result = new DistinctRootsCollection<>() {
      @Override
      protected boolean isAncestor(@NotNull final VirtualFile ancestor, @NotNull final VirtualFile virtualFile) {
        return VfsUtilCore.isAncestor(ancestor, virtualFile, false);
      }
    };
    collectRoots(rootCandidate, result, rootCandidate, progressIndicator);
    return result;
  }

  private void collectRoots(final VirtualFile startFile,
                            final Collection<VirtualFile> result,
                            final VirtualFile topmostRoot,
                            final ProgressIndicator progressIndicator) {
    VfsUtilCore.visitChildrenRecursively(startFile, new VirtualFileVisitor<Void>() {
      @NotNull
      @Override
      public Result visitFileEx(@NotNull VirtualFile file) {
        progressIndicator.checkCanceled();
        progressIndicator.setText2(file.getPresentableUrl());

        if (!file.isDirectory()) {
          if (FileTypeRegistry.getInstance().isFileOfType(file, ActionScriptFileType.INSTANCE)) {

            Pair<VirtualFile, String> root =
              CommonSourceRootDetectionUtil.VIRTUAL_FILE
                .suggestRootForFileWithPackageStatement(file, topmostRoot, FlexProjectStructureDetector.PACKAGE_NAME_FETCHER, false);
            if (root != null) {
              final VirtualFile detectedRoot = root.first;
              result.add(detectedRoot);
              if (VfsUtilCore.isAncestor(detectedRoot, startFile, false)) {
                return skipTo(detectedRoot);  // skip all the directories under detectedRoot
              }
            }
          }
          else if (myDetectMxml && JavaScriptSupportLoader.isFlexMxmFile(file.getName())) {
            result.add(file.getParent());
            // don't skip parent directory since this MXML class may be located in package
          }
        }

        return CONTINUE;
      }
    });
  }
}
