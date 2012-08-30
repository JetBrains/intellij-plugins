package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;

/**
 * @author Sergey Simonchik
 */
public class JstdImplicitUsageProvider implements ImplicitUsageProvider {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    JSFile jsFile = ObjectUtils.tryCast(element.getContainingFile(), JSFile.class);
    if (jsFile == null) {
      return false;
    }
    VirtualFile virtualFile = jsFile.getVirtualFile();
    if (virtualFile == null) {
      return false;
    }
    boolean isInScope = JstdLibraryUtil.isFileInJstdLibScope(element.getProject(), virtualFile);
    if (!isInScope) {
      return false;
    }
    JstdTestFileStructureBuilder builder = JstdTestFileStructureBuilder.getInstance();
    JstdTestFileStructure fileStructure = builder.fetchCachedTestFileStructure(jsFile);
    return fileStructure.isPrototypeTestElement(element);
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
