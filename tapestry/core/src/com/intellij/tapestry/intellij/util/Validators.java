package com.intellij.tapestry.intellij.util;

import com.intellij.refactoring.rename.RegExpValidator;

/**
 * Utility validators.
 */
public final class Validators {

    private static final RegExpValidator PACKAGE_NAME_VALIDATOR = new RegExpValidator("[a-zA-Z_\\$][\\w\\$]*(?:\\.[a-zA-Z_\\$][\\w\\$]*)*");
    private static final RegExpValidator COMPONENT_NAME_VALIDATOR = new RegExpValidator("[a-zA-Z_\\$][\\w\\$]*(?:\\/[a-zA-Z_\\$][\\w\\$]*)*");

    /**
     * Checks if a string is a valid package name.
     *
     * @param packageName the string to check.
     * @return {@code true} if the given string is a valid package name, {@code false} otherwise.
     */
    public static boolean isValidPackageName(String packageName) {
        return packageName != null && PACKAGE_NAME_VALIDATOR.value(packageName);
    }

    /**
     * Checks if a string is a valid component name.
     *
     * @param componentName the string to check.
     * @return {@code true} if the given string is a valid component name, {@code false} otherwise.
     */
    public static boolean isValidComponentName(String componentName) {
        return componentName != null && COMPONENT_NAME_VALIDATOR.value(componentName);
    }
}
