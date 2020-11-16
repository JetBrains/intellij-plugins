package com.intellij.tapestry.core.resource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Finds resources.
 */
public interface IResourceFinder {

    /**
     * Looks up resources in the classpath.
     *
     * @param path                the path of the resource to search for.
     * @param includeDependencies if project dependencies should be included in the search.
     * @return the resources in the given path.
     */
    Collection<IResource> findClasspathResource(String path, boolean includeDependencies);

    /**
     * Looks up localized resources in the classpath.
     *
     * @param path                the path of the resource to search for.
     * @param includeDependencies if project dependencies should be included in the search.
     * @return the resources in the given path.
     */
    Collection<IResource> findLocalizedClasspathResource(String path, boolean includeDependencies);

    /**
     * Looks up a resource in the web context.
     *
     * @param path the path of the resource to search for.
     * @return the resource in the given path, {@code null} if none is found.
     */
    @Nullable
    IResource findContextResource(String path);

    /**
     * Looks up a localized resource in the web context.
     *
     * @param path the path of the resource to search for.
     * @return the resource in the given path, {@code null} if none is found.
     */
    @NotNull
    Collection<IResource> findLocalizedContextResource(String path);

    ///**
    // * Finds all MANIFEST.MF files in the project dependencies.
    // *
    // * @return all MANIFEST.MF files in the project dependencies, not including the file in the current project source code.
    // */
    /*Collection<IResource> findManifestResources();*/
}
