package com.jetbrains.lang.dart.ide.watcher;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugins.watcher.config.FileDependencyFinder;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class DartFileDependencyFinder extends FileDependencyFinder {
  @Override
  public boolean accept(@Nullable String fileExtension) {
    return DartFileType.DEFAULT_EXTENSION.equals(fileExtension);
  }

  @NotNull
  @Override
  public Set<VirtualFile> findDependentFiles(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull GlobalSearchScope scope) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (psiFile == null || DartResolveUtil.isLibraryRoot(psiFile)) {
      return Collections.emptySet();
    }
    return new THashSet<VirtualFile>(DartResolveUtil.findLibrary(psiFile, scope));
  }
}
