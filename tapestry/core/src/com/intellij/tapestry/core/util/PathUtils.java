package com.intellij.tapestry.core.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Utility methods for path manipulation.
 */
public final class PathUtils {

    /**
     * The Tapestry path separator character.
     */
    public static final String TAPESTRY_PATH_SEPARATOR = "/";

    /**
     * The file path separator character.
     */
    public static final String SYSTEM_PATH_SEPARATOR = File.separator;

    /**
     * The unix file path separator character.
     */
    public static final String UNIX_PATH_SEPARATOR = "/";

    /**
     * The windows file path separator character.
     */
    public static final String WINDOWS_PATH_SEPARATOR = "\\";

    /**
     * The package separator character.
     */
    public static final String PACKAGE_SEPARATOR = ".";

    /**
     * Transforms a package name into a valid Tapestry path.<br/>
     * Examples:<br/>
     * {@code admin.login} -> admin/login
     *
     * @param packageName           the package name to transform.
     * @param includeFinalSeparator if a path separator should be included in the end.
     * @return the given package name as a valid Tapestry path.
     */
    public static String packageIntoPath(String packageName, boolean includeFinalSeparator) {
        if (isEmpty(packageName)) {
            return "";
        }

        return packageName.replace(PACKAGE_SEPARATOR, TAPESTRY_PATH_SEPARATOR) + (includeFinalSeparator ? TAPESTRY_PATH_SEPARATOR : "");
    }

    /**
     * Transforms a path into a package.<br/>
     * Examples:<br/>
     * {@code admin/login} -> admin.login
     *
     * @param path              the path to transform.
     * @param removeLastElement if the last element of the path should be removed from the resulting package.
     * @return the given path as a valid package.
     */
    public static String pathIntoPackage(String path, boolean removeLastElement) {
        if (isEmpty(path)) {
            return "";
        }

        if (path.endsWith(TAPESTRY_PATH_SEPARATOR)) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.startsWith(TAPESTRY_PATH_SEPARATOR)) {
            path = path.substring(1);
        }

        if (removeLastElement && path.lastIndexOf(TAPESTRY_PATH_SEPARATOR) != -1) {
            path = path.substring(0, path.lastIndexOf(TAPESTRY_PATH_SEPARATOR));
        }

        return path.replace(TAPESTRY_PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    /**
     * Constructs the full package name of a component.<br/>
     * Examples:<br/>
     * {@code com.myapp.pages} | {@code admin/Login} -> com.myapp.pages.admin
     *
     * @param basePackage   the base package for the given type of component.
     * @param componentName the component name.
     * @return the full package name of the given component.
     */
    public static String getFullComponentPackage(String basePackage, String componentName) {
        if (isEmpty(componentName) || !componentName.contains(TAPESTRY_PATH_SEPARATOR)) {
            return basePackage != null ? basePackage : "";
        }

        String pathPath = componentName.substring(0, componentName.lastIndexOf(TAPESTRY_PATH_SEPARATOR));
        return basePackage + PACKAGE_SEPARATOR + pathPath.replace(TAPESTRY_PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    /**
     * Computes the last element of a path.<br/>
     * Examples:<br/>
     * {@code admin/Login} -> Login
     *
     * @param path the path.
     * @return the last element of the given path.
     */
    @NotNull
    public static String getLastPathElement(String path) {
        if (isEmpty(path)) {
            return "";
        }

        if (!path.contains(TAPESTRY_PATH_SEPARATOR)) {
            return path;
        }

        return path.substring(path.lastIndexOf(TAPESTRY_PATH_SEPARATOR) + 1);
    }

    /**
     * Computes the first element of a path.<br/>
     * Examples:<br/>
     * {@code admin/Login} -> admin
     *
     * @param path the path.
     * @return the last element of the given path.
     */
    public static String getFirstPathElement(String path) {
        if (isEmpty(path)) {
            return "";
        }

        if (!path.contains(TAPESTRY_PATH_SEPARATOR)) {
            return path;
        }

        if (path.startsWith(TAPESTRY_PATH_SEPARATOR)) {
            path = path.substring(1);
        }

        return path.substring(0, path.indexOf(TAPESTRY_PATH_SEPARATOR));
    }

    /**
     * Removes the last path element.<br/>
     * Examples:<br/>
     * {@code admin/Login} -> admin
     *
     * @param path                   the path.
     * @param removeIfOnlyOneElement remove the last element even if it's the only element in the path.
     * @return the given path without the last element.
     */
    public static String removeLastFilePathElement(String path, boolean removeIfOnlyOneElement) {
        if (isEmpty(path)) {
            return "";
        }

        String separator = findSeparator(path);

        if (separator == null) {
            if (removeIfOnlyOneElement) {
                return "";
            } else {
                return path;
            }
        }

        return path.substring(0, path.lastIndexOf(separator));
    }

    /**
     * Computes the component file name from the component name.<br/>
     * Examples:<br/>
     * {@code admin/Login} -> Login
     *
     * @param componentName the component name.
     * @return the component file name.
     */
    public static String getComponentFileName(String componentName) {
        if (isEmpty(componentName)) {
            return "";
        }

        if (!componentName.contains(TAPESTRY_PATH_SEPARATOR)) {
            return componentName;
        }

        return componentName.substring(componentName.lastIndexOf(TAPESTRY_PATH_SEPARATOR) + 1);
    }

    /**
     * Returns a given path in UNIX format.
     *
     * @param path the path to change.
     * @return the given path in UNIX format.
     */
    public static String toUnixPath(String path) {
        if (path == null) {
            return null;
        }

        return path.replace(File.separatorChar, '/');
    }

    private static boolean isEmpty(final String s) {
        return s == null || s.length() == 0;
    }

    private static String findSeparator(String path) {
        if (path.contains(WINDOWS_PATH_SEPARATOR)) {
            return WINDOWS_PATH_SEPARATOR;
        }

        if (path.contains(UNIX_PATH_SEPARATOR)) {
            return UNIX_PATH_SEPARATOR;
        }

        return null;
    }
}
