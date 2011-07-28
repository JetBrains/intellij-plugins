package com.intellij.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.JSResolveHelper;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.CssString;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlexResolveHelper implements JSResolveHelper {
  @Nullable
  public PsiElement findClassByQName(final String link, final Project project, final String className, final GlobalSearchScope scope) {
    final Ref<JSClass> result = new Ref<JSClass>();

    final String expectedPackage = link.equals(className) ? "" : link.substring(0, link.length() - className.length() - 1);

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final PsiManager manager = PsiManager.getInstance(project);
    final FileBasedIndex.ValueProcessor<Void> processor = new FileBasedIndex.ValueProcessor<Void>() {
      public boolean process(VirtualFile file, Void value) {
        VirtualFile rootForFile = projectFileIndex.getSourceRootForFile(file);
        if (rootForFile == null) return true;
        
        if (expectedPackage.equals(VfsUtil.getRelativePath(file.getParent(), rootForFile, '.'))) {
          final JSClass clazz =
            XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)manager.findFile(file));
          if (clazz != null) {
            result.set(clazz);
            return false;
          }
        }
        return true;
      }
    };

    FileBasedIndex.getInstance().processValues(FilenameIndex.NAME, className + JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT, null,
                                               processor, scope);
    if (result.isNull()) {
      FileBasedIndex.getInstance().processValues(FilenameIndex.NAME, className + JavaScriptSupportLoader.FXG_FILE_EXTENSION_DOT, null,
                                                 processor, scope);
    }
    return result.get();
  }

  public boolean importClass(final PsiScopeProcessor processor, final PsiNamedElement file, final String packageQualifierText) {
    if (file instanceof JSFunction) return true;    // there is no need to process package stuff at function level
    final Project project = file.getProject();
    GlobalSearchScope scope = JSResolveUtil.getResolveScope(file);
    final MxmlAndFxgFilesProcessor filesProcessor = new MxmlAndFxgFilesProcessor() {
      final PsiManager manager = PsiManager.getInstance(project);

      public void addDependency(final PsiDirectory directory) {
      }

      public boolean processFile(final VirtualFile file, final VirtualFile root) {
        final PsiFile xmlFile = manager.findFile(file);
        if (!(xmlFile instanceof XmlFile)) return true;
        return processor.execute(XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)xmlFile), ResolveState.initial());
      }
    };

    PsiFile containingFile = file.getContainingFile();
    boolean completion = containingFile.getOriginalFile() != containingFile;

    if (completion) {
      return processAllMxmlAndFxgFiles(scope, project, filesProcessor, null);
    } else {
      if (packageQualifierText != null && packageQualifierText.length() > 0) {
        if (!processMxmlAndFxgFilesInPackage(scope, project, packageQualifierText, filesProcessor)) return false;
      }

      return processMxmlAndFxgFilesInPackage(scope, project, "", filesProcessor);
    }
  }

  public boolean processPackage(String packageQualifierText, String resolvedName, Processor<VirtualFile> processor, GlobalSearchScope globalSearchScope,
                                Project project) {
    for(VirtualFile vfile: DirectoryIndex.getInstance(project).getDirectoriesByPackageName(packageQualifierText, globalSearchScope.isSearchInLibraries())) {
      if (!globalSearchScope.contains(vfile)) continue;
      if (vfile.getFileSystem() instanceof JarFileSystem) {
        VirtualFile fileForJar = JarFileSystem.getInstance().getVirtualFileForJar(vfile);
        if (fileForJar != null && !"swc".equalsIgnoreCase(fileForJar.getExtension())) continue;
      }

      if (resolvedName != null) {
        VirtualFile child = vfile.findChild(resolvedName);
        if (child == null) {
          child = vfile.findChild(resolvedName + JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT);
          if (child == null) child = vfile.findChild(resolvedName + JavaScriptSupportLoader.FXG_FILE_EXTENSION_DOT);
        }
        if (child != null) if (!processor.process(child)) return false;

      } else {
        ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
        for(VirtualFile child:vfile.getChildren()) {
          if (!index.isIgnored(child) && !processor.process(child)) return false;
        }
      }
    }
    return true;
  }

  public boolean isAdequatePlaceForImport(final PsiElement place) {
    return place instanceof CssString;
  }

  public static boolean processAllMxmlAndFxgFiles(final GlobalSearchScope scope,
                                                  Project project,
                                                  final MxmlAndFxgFilesProcessor processor,
                                                  final String nameHint) {
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    for (final VirtualFile root : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
      final boolean b = projectFileIndex.iterateContentUnderDirectory(root, new ContentIterator() {
        public boolean processFile(final VirtualFile fileOrDir) {
          if (scope.contains(fileOrDir) &&
              JavaScriptSupportLoader.isMxmlOrFxgFile(fileOrDir) &&
              (nameHint == null || nameHint.equals(fileOrDir.getNameWithoutExtension()))) {
            if (!processor.processFile(fileOrDir, root)) return false;
          }
          return true;
        }
      });
      if (!b) return false;
    }
    return true;
  }

  private static boolean processMxmlAndFxgFilesInPackage(final GlobalSearchScope scope, Project project, final String packageName, MxmlAndFxgFilesProcessor processor) {
    Query<VirtualFile> packageFiles = DirectoryIndex.getInstance(project).getDirectoriesByPackageName(packageName, scope.isSearchInLibraries());

    final PsiManager manager = PsiManager.getInstance(project);
    for (VirtualFile packageFile : packageFiles) {
      if (!scope.contains(packageFile)) continue;

      PsiDirectory dir = manager.findDirectory(packageFile);
      if (dir == null) continue;

      processor.addDependency(dir);

      for (PsiFile file : dir.getFiles()) {
        if (JavaScriptSupportLoader.isMxmlOrFxgFile(file)) {
          if (!processor.processFile(file.getVirtualFile(), null)) return false;
        }
      }
    }
    return true;
  }

  public interface MxmlAndFxgFilesProcessor {
    void addDependency(PsiDirectory directory);
    boolean processFile(VirtualFile file, final VirtualFile root);
  }
}
