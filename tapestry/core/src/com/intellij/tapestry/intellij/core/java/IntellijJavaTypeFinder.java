package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.ioc.IServiceBindingDiscoverer;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaTypeFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class IntellijJavaTypeFinder implements IJavaTypeFinder {

  private final Module _module;

  public IntellijJavaTypeFinder(@NotNull Module module) {

    _module = module;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public IJavaClassType findType(@NotNull String fullyQualifiedName, boolean includeDependencies) {

    PsiClass psiClass = JavaPsiFacade.getInstance(_module.getProject()).findClass(fullyQualifiedName, getScope(includeDependencies));
    return psiClass != null ? new IntellijJavaClassType(_module, psiClass.getContainingFile()) : null;
  }

  private GlobalSearchScope getScope(boolean includeDependencies) {
    return includeDependencies
           ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false)
           : GlobalSearchScope.moduleScope(_module);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IJavaClassType> findTypesInPackage(String packageName, boolean includeDependencies) {
    Collection<IJavaClassType> types = new ArrayList<>();

    PsiClass[] classes = findPackage(packageName).getClasses(getScope(includeDependencies));
    for (PsiClass clazz : classes) {
      types.add(new IntellijJavaClassType(_module, clazz.getContainingFile()));
    }
    return types;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IJavaClassType> findTypesInPackageRecursively(String basePackageName, boolean includeDependencies) {
    Collection<IJavaClassType> types = new ArrayList<>();

    PsiPackage psiPackage = findPackage(basePackageName);
    if (psiPackage != null) {
      for (PsiClass clazz : findPackage(basePackageName).getClasses(getScope(includeDependencies))) {
        types.add(new IntellijJavaClassType(_module, clazz.getContainingFile()));
      }

      for (PsiPackage pakage : findPackage(basePackageName).getSubPackages(getScope(includeDependencies))) {
        types.addAll(findTypesInPackageRecursively(pakage.getQualifiedName(), includeDependencies));
      }
    }

    return types;
  }

  private PsiPackage findPackage(String basePackageName) {
    return JavaPsiFacade.getInstance(_module.getProject()).findPackage(basePackageName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IServiceBindingDiscoverer getServiceBindingDiscoverer() {
    return null;
  }
}
