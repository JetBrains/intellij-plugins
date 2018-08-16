package com.intellij.tapestry.core.java;

import com.intellij.tapestry.core.resource.IResource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a JAVA class type.
 */
public interface IJavaClassType extends IJavaType {

    /**
     * @return the fully qualified name of the type.
     */
    String getFullyQualifiedName();

    /**
     * @return the name of the type.
     */
    @Override
    String getName();

    /**
     * @return {@code true} if the type is an interface, {@code false} otherwise.
     */
    boolean isInterface();

    /**
     * @return {@code true} if the type is public, {@code false} otherwise.
     */
    boolean isPublic();

    /**
     * @return {@code true} if the type has the default constructor, {@code false} otherwise.
     */
    boolean hasDefaultConstructor();

    @Nullable
    IJavaClassType getSuperClassType();

    /**
     * @return @return {@code true} if the type is an Enum, {@code false} otherwise.
     */
    boolean isEnum();

    /**
     * @param fromSuper indicates if methods from super classes should also be returned.
     * @return the public methods declared in the type.
     */
    Collection<IJavaMethod> getPublicMethods(boolean fromSuper);

    /**
     * @param fromSuper indicates if methods from super classes should also be returned.
     * @return all the methods declared in the type.
     */
    Collection<IJavaMethod> getAllMethods(boolean fromSuper);

    /**
     * @param methodNameRegExp the regexp.
     * @return all public methods whose name matches the given regexp.
     */
    Collection<IJavaMethod> findPublicMethods(String methodNameRegExp);

    /**
     * @return the annotations of the type.
     */
    Collection<IJavaAnnotation> getAnnotations();

    /**
     * @param fromSuper indicates if fields from super classes should also be returned.
     * @return the fields declared in the type.
     */
    Map<String, IJavaField> getFields(boolean fromSuper);

    /**
     * @return the javadoc description of the type.
     */
    String getDocumentation();

    /**
     * @return the file that contains this class.
     */
    IResource getFile();

    boolean supportsInformalParameters();
}
