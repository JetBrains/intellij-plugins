package com.intellij.lang.javascript.uml;

import com.intellij.diagram.extras.providers.DiagramDnDProvider;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

public class FlashUmlDndProvider implements DiagramDnDProvider<Object> {

  @Override
  public boolean isAcceptedForDnD(Object o, Project project) {
    if (o instanceof PsiFile || o instanceof PsiDirectory) {
      return FlashUmlElementManager.isAcceptableAsNodeStatic(o);
    }
    return false;
  }

  @Override
  public Object[] wrapToModelObject(Object o, Project project) {
    Object result = null;
    if (o instanceof PsiDirectory) {
      result = FlashUmlVfsResolver.getQualifiedNameStatic(o);
    }
    else if (o instanceof JSFile) {
      result = JSPsiImplUtils.findQualifiedElement((JSFile)o);
    }
    else if (o instanceof XmlFile) {
      result = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)o);
    }
    return result == null ? null : new Object[]{result};
  }

}
