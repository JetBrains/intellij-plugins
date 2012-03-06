package com.intellij.lang.javascript.flex.library;

import com.intellij.ide.util.projectWizard.importSources.util.CommonSourceRootDetectionUtil;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.projectStructure.detection.FlexProjectStructureDetector;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.DistinctRootsCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * User: ksafonov
 */
class AsRootDetectorBase extends RootDetector {

  private final boolean myDetectMxml;

  protected AsRootDetectorBase(final OrderRootType orderRootType, final String name, boolean detectMxml) {
    super(orderRootType, false, name);
    myDetectMxml = detectMxml;
  }

  @NotNull
  @Override
  public Collection<VirtualFile> detectRoots(@NotNull final VirtualFile rootCandidate, @NotNull final ProgressIndicator progressIndicator) {
    DistinctRootsCollection<VirtualFile> result = new DistinctRootsCollection<VirtualFile>() {
      @Override
      protected boolean isAncestor(@NotNull final VirtualFile ancestor, @NotNull final VirtualFile virtualFile) {
        return VfsUtilCore.isAncestor(ancestor, virtualFile, false);
      }
    };
    collectRoots(rootCandidate, result, rootCandidate, progressIndicator);
    return result;
  }

  /**
   *
   * @return detected root or <code>null</code>
   */
  @Nullable
  private VirtualFile collectRoots(VirtualFile file,
                                          Collection<VirtualFile> result,
                                          VirtualFile topmostRoot,
                                          ProgressIndicator progressIndicator) {
    progressIndicator.checkCanceled();
    progressIndicator.setText2(file.getPresentableUrl());
    String extension;
    if (!file.isDirectory() &&
        ((extension = file.getExtension()) != null)) {
      if (JavaScriptSupportLoader.ECMA_SCRIPT_L4.equals(JavaScriptSupportLoader.getLanguageDialect(extension))) {

        Pair<VirtualFile, String> root =
          CommonSourceRootDetectionUtil.VIRTUAL_FILE
            .suggestRootForFileWithPackageStatement(file, topmostRoot, FlexProjectStructureDetector.PACKAGE_NAME_FETCHER, false);
        if (root != null) {
          result.add(root.first);
          return root.first;
        }
      }
      else if (myDetectMxml && JavaScriptSupportLoader.isFlexMxmFile(file.getName())) {
        result.add(file.getParent());
        return null; // don't skip parent directory since this MXML class may be located in package
      }
    }

    for (VirtualFile child : file.getChildren()) {
      VirtualFile detectedRoot = collectRoots(child, result, topmostRoot, progressIndicator);
      if (detectedRoot != null) {
        if (VfsUtilCore.isAncestor(detectedRoot, file, false)) {
          return detectedRoot; // skip all the directories under detectedRoot
        }
      }
    }
    return null;
  }
}
