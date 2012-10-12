package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlashUmlVfsResolver implements DiagramVfsResolver<Object> {

  private static final Logger LOG = Logger.getInstance(FlashUmlVfsResolver.class.getName());
  public static final String SEPARATOR = ":";

  public String getQualifiedName(Object element) {
    return getQualifiedNameStatic(element);
  }

  @Nullable
  public static String getQualifiedNameStatic(Object element) {
    if (element == null) {
      return null;
    }

    if (element instanceof PsiElement) {
      if (((PsiElement)element).getProject().isDisposed()) {
        return null;
      }
      if (element instanceof JSQualifiedNamedElement) {
        JSQualifiedNamedElement qualifiedNamedElement = (JSQualifiedNamedElement)element;
        String qName = qualifiedNamedElement.getQualifiedName();
        if (qName == null) return null;
        return combineWithModuleName(qualifiedNamedElement, fixVectorTypeName(qName));
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
    }
    else if (element instanceof String) {
      return (String)element;
    }
    LOG.error("can't get qualified name of " + element);
    return null;
  }

  @Nullable
  private static String combineWithModuleName(@NotNull final PsiElement element, @Nullable final String qName) {
    if (qName == null) return null;
    if (ApplicationManager.getApplication().isUnitTestMode()) return qName;
    Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module != null) {
      return module.getName() + SEPARATOR + qName;
    }
    return qName;
  }

  public Object resolveElementByFQN(String fqn, Project project) {
    return resolveElementByFqnStatic(fqn, project);
  }

  @Nullable
  public static Object resolveElementByFqnStatic(String fqn, Project project) {
    final GlobalSearchScope searchScope;
    int separatorIndex = fqn.indexOf(SEPARATOR);
    if (separatorIndex != -1) {
      String moduleName = fqn.substring(0, separatorIndex);
      Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
      if (module == null) {
        return null;
      }
      fqn = fqn.substring(separatorIndex + 1);
      searchScope = module.getModuleScope(true);
    }
    else {
      searchScope = GlobalSearchScope.allScope(project);
    }
    final PsiElement clazz = JSResolveUtil.findClassByQName(fqn, searchScope);
    if (clazz instanceof JSClass) return clazz;
    if (FlashUmlElementManager.packageExists(project, fqn, searchScope)) return fqn;
    return null;
  }

  static String fixVectorTypeName(String name) {
    if (isVectorType(name)) {
      return JSCommonTypeNames.VECTOR_CLASS_NAME;
    }
    return name;
  }

  static boolean isVectorType(final String name) {
    return name.startsWith("Vector$");
  }
}
