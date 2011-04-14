package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.psi.impl.JSFileReference;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiReference;

import java.util.List;

public final class InjectionUtil {
  public static VirtualFile getReferencedFile(PsiElement element, List<String> problems) {
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
        problems.add("TODO text about error");
        return null;
      }
    }

    PsiFileSystemItem psiFile = fileReference.resolve();
    if (psiFile == null) {
      problems.add(fileReference.getUnresolvedMessagePattern());
    }
    else if (psiFile.isDirectory()) {
      problems.add(FlexUIDesignerBundle.message("error.embed.source.is.directory", fileReference.getText()));
    }
    else {
      return psiFile.getVirtualFile();
    }

    return null;
  }
}
