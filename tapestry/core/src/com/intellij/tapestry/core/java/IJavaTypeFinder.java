package com.intellij.tapestry.core.java;

import com.intellij.tapestry.core.ioc.IServiceBindingDiscoverer;

import java.util.Collection;

/**
 * Searches for JAVA types in the project.
 */
public interface IJavaTypeFinder {

    /**
     * Looks up a JAVA type.
     *
     * @param fullyQualifiedName  the fully qualified name of the type to find.
     * @param includeDependencies if project dependencies should be included in the search.
     * @return the type with the given fully qualified name, {@code null} if none is found.
     */
    IJavaClassType findType(String fullyQualifiedName, boolean includeDependencies);

    /**
     * Looks up JAVA types in a package.
     *
     * @param packageName         the package to search in.
     * @param includeDependencies if project dependencies should be included in the search.
     * @return all the JAVA types declared in the given package.
     */
    Collection<IJavaClassType> findTypesInPackage(String packageName, boolean includeDependencies);

    /**
     * Looks up JAVA types in a package and it's sub-packages.
     *
     * @param basePackageName     the package to start the search in.
     * @param includeDependencies if project dependencies should be included in the search.
     * @return all the JAVA types declared in the given package and it's sub-packages.
     */
    Collection<IJavaClassType> findTypesInPackageRecursively(String basePackageName, boolean includeDependencies);

    /**
     * @return an instance of a service binding discoverer.
     */
    IServiceBindingDiscoverer getServiceBindingDiscoverer();
}
