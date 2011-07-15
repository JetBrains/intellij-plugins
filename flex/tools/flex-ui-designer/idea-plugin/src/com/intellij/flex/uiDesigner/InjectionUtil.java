package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSFileReference;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;

public final class InjectionUtil {
  public static boolean isSwf(VirtualFile source, String mimeType) {
    return mimeType == null ? source.getName().endsWith(".swf") : mimeType.equals("application/x-shockwave-flash");
  }

  public static JSClass getJsClassFromPackageAndLocalClassNameReferences(PsiReference[] references) {
    if (references.length > 0) {
      PsiElement element = references[references.length - 1].resolve();
      if (element instanceof JSClass) {
        return (JSClass)element;
      }
    }

    return null;
  }

  public static VirtualFile getReferencedFile(PsiElement element, ProblemsHolder problemsHolder, boolean resolveToFirstIfMulti) {
    final PsiReference[] references = element.getReferences();
    final JSFileReference fileReference;
    int i = references.length - 1;
    // injection in mxml has com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeNameValuePairImpl$NameReference as last reference
    while (true) {
      if (references[i] instanceof JSFileReference) {
        fileReference = (JSFileReference)references[i];
        break;
      }
      else if (--i < 0) {
        problemsHolder.add("TODO text about error");
        return null;
      }
    }

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
      problemsHolder.add(fileReference.getUnresolvedMessagePattern());
    }
    else if (psiFile.isDirectory()) {
      problemsHolder.add(FlexUIDesignerBundle.message("error.embed.source.is.directory", fileReference.getText()));
    }
    else {
      return psiFile.getVirtualFile();
    }

    return null;
  }

  private static PsiFileSystemItem resolveResult(PsiElement element, ResolveResult[] resolveResults) {
    final PsiFile currentTopLevelFile = InjectedLanguageUtil.getTopLevelFile(element);
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement resolvedElement = resolveResult.getElement();
      if (InjectedLanguageUtil.getTopLevelFile(resolvedElement).equals(currentTopLevelFile)) {
        return (PsiFileSystemItem)resolvedElement;
      }
    }

    return (PsiFileSystemItem)resolveResults[0].getElement();
  }
}
