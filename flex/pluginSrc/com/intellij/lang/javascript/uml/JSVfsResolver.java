package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

public class JSVfsResolver implements DiagramVfsResolver<Object> {

  private static final Logger LOG = Logger.getInstance(JSVfsResolver.class.getName());

  public String getQualifiedName(Object element) {
    return getQualifiedNameStatic(element);
  }

  @Nullable
  public static String getQualifiedNameStatic(Object element) {
    if (element == null) {
      return null;
    }

    if (element instanceof JSQualifiedNamedElement) {
      return ((JSQualifiedNamedElement)element).getQualifiedName();
    }
    else if (element instanceof JSFile) {
      return getQualifiedNameStatic(JSPsiImplUtils.findQualifiedElement((JSFile)element));
    }
    else if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      //noinspection ConstantConditions
      return getQualifiedNameStatic(XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element));
    }
    else if (element instanceof PsiDirectory) {
      PsiDirectory directory = (PsiDirectory)element;
      return JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
    }
    else if (element instanceof String) {
      return (String)element;
    }
    LOG.error("can't get qualified name of " + element);
    return null;
  }

  public Object resolveElementByFQN(String fqn, Project project) {
    return resolveElementByFqnStatic(fqn, project);
  }

  @Nullable
  public static Object resolveElementByFqnStatic(String fqn, Project project) {
    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
    final PsiElement clazz = JSResolveUtil.findClassByQName(fqn, searchScope);
    if (clazz instanceof JSClass) return clazz;
    if (JSUtils.packageExists(fqn, searchScope)) return fqn;
    return null;
  }
}
