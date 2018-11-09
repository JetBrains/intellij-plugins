package com.intellij.tapestry.core.java;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Creates JAVA element instances.
 */
public interface IJavaTypeCreator {

    /**
     * Creates a new field.
     *
     * @param name                           the field name.
     * @param type                           the field type.
     * @param isPrivate                      {@code true} if the type is public, {@code false} otherwise.
     * @param changeNameToReflectIdeSettings {@code true} if the IDE coding style should be taken into account and be used to change the field name accordingly.
     * @return the field.
     */
    IJavaField createField(@NotNull String name, IJavaClassType type, boolean isPrivate, boolean changeNameToReflectIdeSettings);

    /**
     * Creates a new field annotation and adds it to the field.
     *
     * @param field              the field to create the annotation to.
     * @param fullyQualifiedName the fully qualified name of the annotation class.
     * @param parameters         the parameters of the annotation.
     * @return the annotation.
     */
    IJavaAnnotation createFieldAnnotation(IJavaField field, String fullyQualifiedName, Map<String, String> parameters);

    /**
     * Ensures that a type is in the import list of a class.
     *
     * @param baseClass the class that should have the import statement.
     * @param type      the class type that has to be imported.
     * @return {@code true} if the import was insured, {@code false} otherwise.
     */
    boolean ensureClassImport(IJavaClassType baseClass, IJavaClassType type);
}
