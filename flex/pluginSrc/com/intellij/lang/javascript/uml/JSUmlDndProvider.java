package com.intellij.lang.javascript.uml;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.diagram.extras.providers.DiagramDnDProvider;

public class JSUmlDndProvider implements DiagramDnDProvider<Object> {

  public boolean isAcceptedForDnD(Object o, Project project) {
    if (o instanceof PsiFile || o instanceof PsiDirectory) {
      return com.intellij.lang.javascript.uml.JSElementManager.isAcceptableAsNodeStatic(o);
    }
    return false;
  }

  public Object[] wrapToModelObject(Object o, Project project) {
    Object result = null;
    if (o instanceof PsiDirectory) {
      result = com.intellij.lang.javascript.uml.JSVfsResolver.getQualifiedNameStatic(o);
    }
    else if (o instanceof JSFile) {
      result = JSPsiImplUtils.findQualifiedElement((JSFile)o);
    }
    else if (o instanceof XmlFile) {
      result = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)o);
    }
    return result == null ? null : new Object[]{result};
  }

}
