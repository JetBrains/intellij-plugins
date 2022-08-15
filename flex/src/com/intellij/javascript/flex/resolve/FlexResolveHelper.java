// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.resolve;

import com.intellij.javascript.flex.mxml.MxmlJSClassProvider;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.CssString;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


public class FlexResolveHelper implements JSResolveHelper {
  public static final PsiScopedImportSet ourPsiScopedImportSet = new PsiScopedImportSet();

  @Override
  @Nullable
  public PsiElement findClassByQName(final String link, final Project project, final String className, final GlobalSearchScope scope) {
    final Ref<JSClass> result = new Ref<>();

    final String expectedPackage = link.equals(className) ? "" : link.substring(0, link.length() - className.length() - 1);

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final PsiManager manager = PsiManager.getInstance(project);
    final Processor<VirtualFile> processor = file -> {
      VirtualFile rootForFile = projectFileIndex.getSourceRootForFile(file);
      if (rootForFile == null) return true;

      if (expectedPackage.equals(VfsUtilCore.getRelativePath(file.getParent(), rootForFile, '.'))) {
        PsiFile psiFile = manager.findFile(file);
        final JSClass clazz = psiFile instanceof XmlFile ? XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)psiFile):null;
        if (clazz != null) {
          result.set(clazz);
          return false;
        }
      }
      return true;
    };

    Collection<VirtualFile> files =
      FilenameIndex.getVirtualFilesByName(className + JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT, scope);
    ContainerUtil.process(files, processor);


    if (result.isNull()) {
      files = FilenameIndex.getVirtualFilesByName(className + JavaScriptSupportLoader.FXG_FILE_EXTENSION_DOT, scope);
      ContainerUtil.process(files, processor);
    }
    return result.get();
  }

  @Override
  public boolean importClass(final PsiScopeProcessor processor, final PsiNamedElement file) {
    if (file instanceof JSFunction) return true;    // there is no need to process package stuff at function level

    if (file instanceof XmlBackedJSClassImpl) {
      if (!processInlineComponentsInScope((XmlBackedJSClassImpl)file,
                                          inlineComponent -> processor.execute(inlineComponent, ResolveState.initial()))) {
        return false;
      }
    }

    final String packageQualifierText = JSResolveUtil.findPackageStatementQualifier(file);
    final Project project = file.getProject();
    GlobalSearchScope scope = JSResolveUtil.getResolveScope(file);
    final MxmlAndFxgFilesProcessor filesProcessor = new MxmlAndFxgFilesProcessor() {
      final PsiManager manager = PsiManager.getInstance(project);

      @Override
      public void addDependency(final PsiDirectory directory) {
      }

      @Override
      public boolean processFile(final VirtualFile file, final VirtualFile root) {
        final PsiFile xmlFile = manager.findFile(file);
        if (!(xmlFile instanceof XmlFile)) return true;
        return processor.execute(XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)xmlFile), ResolveState.initial());
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

  @Override
  public boolean processPackage(String packageQualifierText, String resolvedName, Processor<? super VirtualFile> processor, GlobalSearchScope globalSearchScope,
                                Project project) {
    for (VirtualFile vfile: DirectoryIndex.getInstance(project).getDirectoriesByPackageName(packageQualifierText, globalSearchScope)) {
      if (vfile.getFileSystem() instanceof JarFileSystem) {
        VirtualFile fileForJar = JarFileSystem.getInstance().getVirtualFileForJar(vfile);
        if (fileForJar != null &&
            !("swc".equalsIgnoreCase(fileForJar.getExtension()) || "ane".equalsIgnoreCase(fileForJar.getExtension()))) {
          continue;
        }
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
          if (!index.isExcluded(child) && !processor.process(child)) return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean isAdequatePlaceForImport(final PsiElement place) {
    return place instanceof CssString;
  }

  @Override
  public boolean resolveTypeNameUsingImports(final ResolveProcessor resolveProcessor, PsiNamedElement parent) {
    if (parent instanceof XmlBackedJSClassImpl) {
      return processInlineComponentsInScope((XmlBackedJSClassImpl)parent,
                                            inlineComponent -> resolveProcessor.execute(inlineComponent, ResolveState.initial()));
    }
    return true;
  }

  @Override
  public long getResolveResultTimestamp(PsiElement candidate) {
    return SwcCatalogXmlUtil.getTimestampFromCatalogXml(candidate);
  }

  @Override
  public JSReferenceExpression bindReferenceToElement(JSReferenceExpression ref,
                                                      String qName,
                                                      String newName, boolean justMakeQualified, PsiNamedElement element) {
    PsiFile file;
    if (qName != null &&
        (element instanceof XmlBackedJSClass ||
         (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) ||
         (file = element.getContainingFile()) == null ||
         file.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4))) {
      boolean qualify;
      boolean doImport;

      if (justMakeQualified ||
          ref.getParent() instanceof JSImportStatement ||
          element instanceof PsiDirectoryContainer ||
          (ref.getParent() instanceof JSReferenceListMember && ref.getContainingFile().getContext() instanceof XmlAttributeValue)) {
        qualify = true;
        doImport = false;
      }
      else {
        doImport = evaluateImportStatus(newName, ref) == ImportStatus.ABSENT &&
                   evaluateImportStatus(ref.getReferencedName(), ref) == ImportStatus.ABSENT;
        JSQualifiedNamedElement qualifiedElement = null;

        if (element instanceof JSQualifiedNamedElement) {
          qualifiedElement = (JSQualifiedNamedElement)element;
        }
        else if (element instanceof JSFile) {
          qualifiedElement = JSPsiImplUtils.findQualifiedElement((JSFile)element);
        }
        else if (element instanceof XmlFile) {
          qualifiedElement = XmlBackedJSClassFactory.getXmlBackedClass(((XmlFile)element));
        }
        assert qualifiedElement != null:qualifiedElement.getClass();
        // at this moment package declaration is out of date so element has it's original qName
        qualify = JSResolveUtil.shortReferenceIsAmbiguousOrUnequal(newName, ref, qualifiedElement.getQualifiedName(), null);
      }

      if (qualify) {
        ASTNode newChild = JSChangeUtil.createExpressionFromText(ref.getProject(), qName);
        ref.getParent().getNode().replaceChild(ref.getNode(), newChild);
        ref = (JSReferenceExpression)newChild.getPsi();
      }
      if (doImport && qName.indexOf('.') != -1 && !StringUtil.getPackageName(qName).equals(JSResolveUtil.getPackageNameFromPlace(ref))) {
        final SmartPsiElementPointer<JSReferenceExpression> refPointer =
          SmartPointerManager.getInstance(ref.getProject()).createSmartPsiElementPointer(ref);
        ImportUtils.doImport(ref, qName, false);
        ref = refPointer.getElement();
      }
    }
    return ref;
  }

  @Override
  public boolean isStrictTypeContext(PsiElement element) {
    return true;
  }

  public static boolean processAllMxmlAndFxgFiles(final GlobalSearchScope scope,
                                                  Project project,
                                                  final MxmlAndFxgFilesProcessor processor,
                                                  final String nameHint) {
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    for (final VirtualFile root : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
      final boolean b = projectFileIndex.iterateContentUnderDirectory(root, fileOrDir -> {
        if (scope.contains(fileOrDir) &&
            JavaScriptSupportLoader.isMxmlOrFxgFile(fileOrDir) &&
            (nameHint == null || nameHint.equals(fileOrDir.getNameWithoutExtension()))) {
          if (!processor.processFile(fileOrDir, root)) return false;
        }
        return true;
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

  public static ImportStatus evaluateImportStatus(String newName, PsiElement context) {
    EvaluateImportStatusProcessor statusProcessor = new EvaluateImportStatusProcessor(newName);
    ActionScriptResolveUtil.walkOverStructure(context, statusProcessor);
    return statusProcessor.myStatus.get();
  }

  public static boolean isValidClassName(String name, boolean acceptFqn) {
    if (StringUtil.isEmptyOrSpaces(name)) return false;
    if (acceptFqn) {
      return name.trim().matches("[\\p{Alpha}][\\p{Alnum}_]*(\\.[\\p{Alpha}][\\p{Alnum}_]*)*");
    }
    else {
      return name.trim().matches("[\\p{Alpha}][\\p{Alnum}_]*");
    }
  }

  public enum ImportStatus {
    ABSENT, UNIQUE, MULTIPLE
  }

  public interface MxmlAndFxgFilesProcessor {
    void addDependency(PsiDirectory directory);
    boolean processFile(VirtualFile file, final VirtualFile root);
  }

  public static boolean mxmlPackageExists(String packageName, Project project, GlobalSearchScope scope) {
    return !processMxmlAndFxgFilesInPackage(scope, project, packageName, new MxmlAndFxgFilesProcessor() {
      @Override
      public void addDependency(final PsiDirectory directory) {
      }

      @Override
      public boolean processFile(final VirtualFile file, final VirtualFile root) {
        return false;
      }
    });
  }

  private static boolean processInlineComponentsInScope(XmlBackedJSClassImpl context, Processor<? super XmlBackedJSClass> processor) {
    XmlTag rootTag = ((XmlFile)context.getContainingFile()).getDocument().getRootTag();
    boolean recursive =
      context.getParent().getParentTag() != null && XmlBackedJSClassImpl.isComponentTag(context.getParent().getParentTag());
    Collection<XmlBackedJSClass> components = MxmlJSClassProvider.getChildInlineComponents(rootTag, recursive);
    return ContainerUtil.process(components, processor);
  }

  private static class EvaluateImportStatusProcessor implements Processor<PsiNamedElement>, ScopedImportSet.ImportProcessor<Object> {
    private final String myNewName;
    private final Ref<ImportStatus> myStatus = new Ref<>(ImportStatus.ABSENT);

    EvaluateImportStatusProcessor(String newName) {
      myNewName = newName;
    }

    @Override
    public boolean process(PsiNamedElement context) {
      ourPsiScopedImportSet.process(myNewName, null, context, this);
      if (context instanceof JSPackageStatement) return false;

      return myStatus.get() == ImportStatus.ABSENT;
    }

    @Override
    public Object process(@NotNull String referenceName, @NotNull ImportInfo info, @NotNull PsiNamedElement scope) {
      if (info.starImport) {
        final PsiElement clazz = JSDialectSpecificHandlersFactory.forElement(scope).getClassResolver()
          .findClassByQName(info.getQNameToSearch(referenceName), scope);
        if (clazz == null) return null;
      }

      ImportStatus status = myStatus.get();
      if (status == ImportStatus.ABSENT) myStatus.set(ImportStatus.UNIQUE);
      else if (status == ImportStatus.UNIQUE) myStatus.set(ImportStatus.MULTIPLE);

      return null;
    }
  }
}
