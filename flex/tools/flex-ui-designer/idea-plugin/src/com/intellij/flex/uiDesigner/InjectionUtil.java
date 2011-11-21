package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSFileReference;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.properties.ResourceBundleReference;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class InjectionUtil {
  public static boolean isSwf(VirtualFile source, String mimeType) {
    return mimeType == null ? source.getName().endsWith(".swf") : mimeType.equals("application/x-shockwave-flash");
  }

  public static boolean isImage(VirtualFile source, String mimeType) {
    if (mimeType == null) {
      String extension = source.getExtension();
      if (extension != null) {
        extension = extension.toLowerCase();
        if (extension.equals("png") || extension.equals("gif") || extension.equals("jpg") || extension.equals("jpeg")) {
          return true;
        }
      }
    }
    else if (mimeType.equals("image/png") || mimeType.equals("image/gif") || mimeType.equals("image/jpeg")) {
      return true;
    }

    return false;
  }

  public static int getProjectComponentFactoryId(String qualifiedClassName, PsiElement element, List<XmlFile> unregisteredDocumentFactories)
      throws InvalidPropertyException {
    // MxmlBackedElementDescriptor returns declaration as MxmlFile, but ClassBackedElementDescriptor returns as JSClass
    element = JSResolveUtil.unwrapProxy(element);
    PsiFile psiFile = element.getContainingFile();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    assert virtualFile != null;
    if (checkSupportedProjectComponentFile(virtualFile, psiFile, qualifiedClassName)) {
      return DocumentFactoryManager.getInstance().getId(virtualFile, (XmlFile)psiFile, unregisteredDocumentFactories);
    }
    else {
      return -1;
    }
  }

  public static int getProjectComponentFactoryId(JSClass jsClass, List<XmlFile> unregisteredDocumentFactories)
        throws InvalidPropertyException {
      PsiFile psiFile = jsClass.getContainingFile();
      VirtualFile virtualFile = psiFile.getVirtualFile();
      assert virtualFile != null;
      if (checkSupportedProjectComponentFile(virtualFile, psiFile, jsClass.getQualifiedName())) {
        return DocumentFactoryManager.getInstance().getId(virtualFile, (XmlFile)psiFile, unregisteredDocumentFactories);
      }
      else {
        return -1;
      }
    }

  private static boolean checkSupportedProjectComponentFile(VirtualFile virtualFile, PsiFile psiFile, String qualifiedClassName)
      throws InvalidPropertyException {
    boolean inSourceContent = ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex().isInSourceContent(virtualFile);
    if (psiFile instanceof XmlFile) {
      if (inSourceContent) {
        return true;
      }
    }
    else if (inSourceContent) {
      throw new InvalidPropertyException("error.support.only.mxml.based.component", qualifiedClassName);
    }

    return false;
  }

  public static boolean isProjectComponent(JSClass jsClass) throws InvalidPropertyException {
    PsiFile psiFile = jsClass.getContainingFile();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    assert virtualFile != null;
    return checkSupportedProjectComponentFile(virtualFile, psiFile, jsClass.getQualifiedName());
  }

  @Nullable
  public static JSClass getJsClassFromPackageAndLocalClassNameReferences(PsiElement element) {
   return getJsClassFromPackageAndLocalClassNameReferences(element.getReferences());
  }

  @Nullable
  public static JSClass getJsClassFromPackageAndLocalClassNameReferences(PsiReference[] references) {
    if (references.length > 0) {
      PsiElement element = references[references.length - 1].resolve();
      if (element instanceof JSClass) {
        return (JSClass)element;
      }
    }

    return null;
  }

  @NotNull
  public static VirtualFile getReferencedFile(PsiElement element)
      throws InvalidPropertyException {
    //noinspection ConstantConditions
    return getReferencedPsiFile(element).getVirtualFile();
  }

  @NotNull
  public static PsiFileSystemItem getReferencedPsiFile(PsiElement element) throws InvalidPropertyException {
    return getReferencedPsiFile(element, false);
  }

  @NotNull
  public static PsiFileSystemItem getReferencedPsiFile(PsiElement element, boolean resolveToFirstIfMulti) throws InvalidPropertyException {
    final PsiReference[] references = element.getReferences();
    final PsiPolyVariantReference fileReference;
    int i = references.length - 1;
    // injection in mxml has com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeNameValuePairImpl$NameReference as last reference
    while (true) {
      final PsiReference reference = references[i];
      if (reference instanceof JSFileReference || reference instanceof ResourceBundleReference) {
        fileReference = (PsiPolyVariantReference)reference;
        break;
      }
      else if (--i < 0) {
        throw new InvalidPropertyException(element, "cannot.find.file.reference");
      }
    }

    // IDEA-68144
    ResolveResult[] resolveResults = fileReference.multiResolve(false);
    final PsiFileSystemItem psiFile;
    if (resolveResults.length == 0) {
      psiFile = null;
    }
    else if (resolveResults.length == 1 || resolveToFirstIfMulti) {
      psiFile = (PsiFileSystemItem)resolveResults[0].getElement();
    }
    else {
      psiFile = resolveResult(element, resolveResults);
    }

    if (psiFile == null) {
      if (fileReference instanceof FileReference) {
        throw new InvalidPropertyException(((FileReference)fileReference).getUnresolvedMessagePattern(), element);
      }
      else {
        throw new InvalidPropertyException(element, "unresolved.resource.bundle", fileReference.getCanonicalText());
      }
    }
    else if (psiFile.isDirectory()) {
      throw new InvalidPropertyException(element, "error.embed.source.is.directory", fileReference.getCanonicalText());
    }
    else {
      return psiFile;
    }
  }

  @Nullable
  private static PsiFileSystemItem resolveResult(PsiElement element, ResolveResult[] resolveResults) {
    final PsiFile currentTopLevelFile = InjectedLanguageUtil.getTopLevelFile(element);
    // find equal files
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement resolvedElement = resolveResult.getElement();
      if (InjectedLanguageUtil.getTopLevelFile(resolvedElement).equals(currentTopLevelFile)) {
        return (PsiFileSystemItem)resolvedElement;
      }
    }

    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module != null) {
      final ModuleFileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();
      // return if is local file
      for (ResolveResult resolveResult : resolveResults) {
        PsiFileSystemItem resolvedElement = (PsiFileSystemItem)resolveResult.getElement();
        assert resolvedElement != null;
        VirtualFile virtualFile = resolvedElement.getVirtualFile();
        if (virtualFile != null && fileIndex.isInSourceContent(virtualFile)) {
          return resolvedElement;
        }
      }
    }

    return (PsiFileSystemItem)resolveResults[0].getElement();
  }
}
