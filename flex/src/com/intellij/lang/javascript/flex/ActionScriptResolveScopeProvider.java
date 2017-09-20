package com.intellij.lang.javascript.flex;

import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider;
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveScopeProvider;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.ResolveScopeManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class ActionScriptResolveScopeProvider extends JSResolveScopeProvider implements JSElementResolveScopeProvider {

  @Nullable
  @Override
  public GlobalSearchScope getResolveScope(@NotNull VirtualFile file, Project project) {
    return getResolveScope(file, project, true);
  }

  @Nullable
  private static GlobalSearchScope getResolveScope(@NotNull VirtualFile file, Project project, boolean checkApplicable) {
    if (file instanceof VirtualFileWindow) {
      file = ((VirtualFileWindow)file).getDelegate();
    }
    if (checkApplicable && !isApplicable(file)) return null;
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final Module module = projectFileIndex.getModuleForFile(file);
    if (module != null) {
      boolean includeTests = projectFileIndex.isInTestSourceContent(file) || !projectFileIndex.isInSourceContent(file);

      final GlobalSearchScope moduleScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, includeTests);
      // TODO [Konstantin.Ulitin] add package and swc file types
      //final GlobalSearchScope fileTypesScope =
      //  GlobalSearchScope.getScopeRestrictedByFileTypes(moduleScope, ActionScriptFileType.INSTANCE,
      //                                                  FlexApplicationComponent.SWF_FILE_TYPE, FlexApplicationComponent.MXML);
      final GlobalSearchScope fileTypesScope =
        moduleScope.intersectWith(GlobalSearchScope.notScope(
          GlobalSearchScope.getScopeRestrictedByFileTypes(
            moduleScope, TypeScriptFileType.INSTANCE, JavaScriptFileType.INSTANCE
          )
        ));
      return fileTypesScope.union(GlobalSearchScope.filesScope(project, JSCorePredefinedLibrariesProvider.getActionScriptPredefinedLibraryFiles()));
    }
    return null;
  }

  @Nullable
  @Override
  public GlobalSearchScope getElementResolveScope(@NotNull PsiElement element) {
    final GlobalSearchScope tempScope = JSInheritanceUtil.getEnforcedScope();
    if (tempScope != null) {
      return tempScope;
    }

    PsiElement explicitContext = JSResolveUtil.getContext(element);
    if (explicitContext != null) element = explicitContext;
    if (element instanceof PsiCodeFragment) {
      final GlobalSearchScope forced = ((PsiCodeFragment)element).getForcedResolveScope();
      if (forced != null) return forced;
    }

    if (!DialectDetector.isActionScript(element)) return null;

    Project project = element.getProject();
    VirtualFile file = getFileForScopeEvaluation(element);
    if (file == null) {
      return getProjectScopeIncludingPredefines(project);
    }

    final GlobalSearchScope scope = isApplicable(file) ? ResolveScopeManager.getInstance(project).getDefaultResolveScope(file) : null;
    if (scope != null) {
      if (ProjectFileIndex.SERVICE.getInstance(project).isInLibraryClasses(file) && !scope.contains(file)) {
        // safe but not 100% correct fix for IDEA-157606
        // The problem happens when ModuleA -> ModuleB -> lib.swc and we calculate resolve scope for file from lib.swc. FlexOrderEnumerationHandler filters our 'ModuleB -> lib.swc' dependency because it is initialized with ModuleA. Oh, LibraryRuntimeClasspathScope computation is so complicated...
        return scope.union(GlobalSearchScope.fileScope(project, file));
      }
      return scope;
    }

    final GlobalSearchScope fileResolveScope = getResolveScope(file, project, false);
    return fileResolveScope != null ? fileResolveScope : getProjectScopeIncludingPredefines(project);
  }

  @Override
  protected List<VirtualFile> getPredefinedLibraryFiles(@NotNull Project project) {
    return JSResolveUtil.JS_INDEXED_ROOT_PROVIDER.getLibraryFiles(project)
      .stream().filter(ActionScriptResolveScopeProvider::isApplicable).collect(Collectors.toList());
  }

  private static boolean isApplicable(@NotNull final VirtualFile file) {
    return file.getFileType() == ActionScriptFileType.INSTANCE || file.getFileType() == FlexApplicationComponent.MXML ||
           file.getFileType() == FlexApplicationComponent.SWF_FILE_TYPE || JavaScriptSupportLoader.isFlexMxmFile(file);
  }
}
