package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.ioc.IServiceBindingDiscoverer;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaTypeFinder;

import java.util.ArrayList;
import java.util.Collection;

public class IntellijJavaTypeFinder implements IJavaTypeFinder {

    private Module _module;

    public IntellijJavaTypeFinder(Module module) {

        _module = module;
    }

    /**
     * {@inheritDoc}
     */
    public IJavaClassType findType(String fullyQualifiedName, boolean includeDependencies) {
        PsiClass psiClass = null;

        try {
            if (includeDependencies) {
                psiClass = JavaPsiFacade.getInstance(_module.getProject()).findClass(fullyQualifiedName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false));

            } else {
                psiClass = JavaPsiFacade.getInstance(_module.getProject()).findClass(fullyQualifiedName, GlobalSearchScope.moduleScope(_module));
            }
        } catch (ProcessCanceledException ex) {
            // ignore
        }

        if (psiClass != null) {
            return new IntellijJavaClassType(_module, psiClass.getContainingFile());
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IJavaClassType> findTypesInPackage(String packageName, boolean includeDependencies) {
        Collection<IJavaClassType> types = new ArrayList<IJavaClassType>();

        if (includeDependencies) {
            PsiClass[] classes = JavaPsiFacade.getInstance(_module.getProject()).findPackage(packageName).getClasses(GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false));
            for (PsiClass clazz : classes)
                types.add(new IntellijJavaClassType(_module, clazz.getContainingFile()));
        } else {
            PsiClass[] classes = JavaPsiFacade.getInstance(_module.getProject()).findPackage(packageName).getClasses(GlobalSearchScope.moduleScope(_module));
            for (PsiClass clazz : classes)
                types.add(new IntellijJavaClassType(_module, clazz.getContainingFile()));
        }

        return types;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IJavaClassType> findTypesInPackageRecursively(String basePackageName, boolean includeDependencies) {
        Collection<IJavaClassType> types = new ArrayList<IJavaClassType>();
        GlobalSearchScope searchScope;

        if (includeDependencies) {
            searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false);
        } else {
            searchScope = GlobalSearchScope.moduleScope(_module);
        }

        PsiPackage psiPackage = JavaPsiFacade.getInstance(_module.getProject()).findPackage(basePackageName);
        if (psiPackage != null) {
            PsiClass[] classes = JavaPsiFacade.getInstance(_module.getProject()).findPackage(basePackageName).getClasses(searchScope);
            for (PsiClass clazz : classes)
                types.add(new IntellijJavaClassType(_module, clazz.getContainingFile()));

            for (PsiPackage pakage : JavaPsiFacade.getInstance(_module.getProject())
                    .findPackage(basePackageName)
                    .getSubPackages(searchScope))
                types.addAll(findTypesInPackageRecursively(pakage.getQualifiedName(), includeDependencies));
        }

        return types;
    }

    /**
     * {@inheritDoc}
     */
    public IServiceBindingDiscoverer getServiceBindingDiscoverer() {
        return null;
    }
}
