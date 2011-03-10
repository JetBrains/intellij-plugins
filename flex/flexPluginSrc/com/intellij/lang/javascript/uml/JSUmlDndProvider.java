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
      return JSElementManager.isAcceptableAsNodeStatic(o);
    }
    return false;
  }

  public Object wrapToModelObject(Object o, Project project) {
    if (o instanceof PsiDirectory) {
      return JSVfsResolver.getQualifiedNameStatic(o);
    }
    else if (o instanceof JSFile) {
      return JSPsiImplUtils.findQualifiedElement((JSFile)o);
    }
    else if (o instanceof XmlFile) {
      return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)o);
    }
    return null;
  }

}
