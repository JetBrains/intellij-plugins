package com.intellij.lang.javascript.refactoring;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSInheritedLanguagesHelper;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class ActionScriptQualifiedElementRenameProcessor extends JSDefaultRenameProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    VirtualFile vFile;
    return (element instanceof JSQualifiedNamedElement && DialectDetector.isActionScript(element) &&
            (element.getParent() instanceof JSPackageStatement || element instanceof JSClass)) ||
           ((element instanceof JSFile && DialectDetector.isActionScript(element)) ||
            (element instanceof XmlFile && FlexSupportLoader.isFlexMxmFile((PsiFile)element))) &&
           (vFile = ((PsiFile)element).getVirtualFile()) != null &&
           ProjectRootManager.getInstance(element.getProject()).getFileIndex().getSourceRootForFile(vFile) != null;
  }

  @Override
  public PsiElement substituteElementToRename(@NotNull PsiElement element, Editor editor) {
    if (element instanceof JSFileImpl) {
      JSNamedElement mainDeclaredElement = ActionScriptResolveUtil.findMainDeclaredElement((JSFileImpl)element);
      if (mainDeclaredElement != null) return mainDeclaredElement;
    } else if (element instanceof XmlFile) {
      JSClass jsClass = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
      if (jsClass != null) return jsClass;
    }
    return super.substituteElementToRename(element, editor);
  }

  @Override
  public void prepareRenaming(@NotNull PsiElement element, @NotNull String newName, @NotNull Map<PsiElement, String> allRenames) {
    if (element instanceof JSClass) {
      JSFunction constructor = ((JSClass)element).getConstructor();
      if (constructor != null) {
        allRenames.put(constructor, newName);
      }
    }
    
    if (element instanceof JSQualifiedNamedElement) {
      PsiFile containingFile = element.getContainingFile();
      if ((!(containingFile instanceof JSFileImpl) ||
           ActionScriptResolveUtil.findMainDeclaredElement((JSFileImpl)containingFile) == element)
        && JSInheritedLanguagesHelper.shouldRenameFileWithClass(element)) {
        allRenames.put(containingFile, newName + "." + containingFile.getVirtualFile().getExtension());
      }
    }
  }
}
