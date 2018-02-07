package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.*;
import com.intellij.tapestry.core.resource.IResource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for easy creation of IJavaClassType mocks.
 */
public class JavaClassTypeMock implements IJavaClassType {

  private String _fullyQualifiedName;
  private boolean _interface;
  private boolean _public;
  private boolean _defaultConstructor;
  private final Collection<IJavaMethod> _publicMethods = new ArrayList<>();
  private final Collection<IJavaMethod> _allMethods = new ArrayList<>();
  private final Collection<IJavaAnnotation> _annotations = new ArrayList<>();
  private final Map<String, IJavaField> _fields = new HashMap<>();
  private String _documentation;
  private IResource _file;
  private IJavaClassType _superClassType;

  public JavaClassTypeMock() {
  }

  public JavaClassTypeMock(String fullyQualifiedName) {
    _fullyQualifiedName = fullyQualifiedName;
  }

  @Override
  public String getFullyQualifiedName() {
    return _fullyQualifiedName;
  }

  @Override
  public String getName() {
    if (_fullyQualifiedName == null) {
      return null;
    }

    if (_fullyQualifiedName.indexOf('.') == -1) {
      return _fullyQualifiedName;
    }

    return _fullyQualifiedName.substring(_fullyQualifiedName.lastIndexOf('.') + 1);
  }

  @Override
  public boolean isInterface() {
    return _interface;
  }

  public void setInterface(boolean anInterface) {
    _interface = anInterface;
  }

  @Override
  public boolean isPublic() {
    return _public;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  public JavaClassTypeMock setPublic(boolean aPublic) {
    _public = aPublic;

    return this;
  }

  @Override
  public boolean hasDefaultConstructor() {
    return _defaultConstructor;
  }

  @Override
  public IJavaClassType getSuperClassType() {
    return _superClassType;
  }

  public void setSuperClassType(IJavaClassType superClassType) {
    _superClassType = superClassType;
  }

  public JavaClassTypeMock setDefaultConstructor(boolean defaultConstructor) {
    _defaultConstructor = defaultConstructor;

    return this;
  }

  @Override
  public Collection<IJavaMethod> getPublicMethods(boolean fromSuper) {
    return _publicMethods;
  }

  @Override
  public Collection<IJavaMethod> getAllMethods(boolean fromSuper) {
    return _allMethods;
  }

  public JavaClassTypeMock addPublicMethod(IJavaMethod method) {
    _publicMethods.add(method);

    return this;
  }

  @Override
  public Collection<IJavaMethod> findPublicMethods(String methodNameRegExp) {
    Pattern pattern = Pattern.compile(methodNameRegExp);
    Collection<IJavaMethod> foundMethods = new ArrayList<>();

    Collection<IJavaMethod> allMethods = getPublicMethods(true);
    for (IJavaMethod method : allMethods) {
      if (pattern.matcher(method.getName()).matches()) {
        foundMethods.add(method);
      }
    }

    return foundMethods;
  }

  @Override
  public Collection<IJavaAnnotation> getAnnotations() {
    return _annotations;
  }

  @Override
  public Map<String, IJavaField> getFields(boolean fromSuper) {
    return _fields;
  }

  public JavaClassTypeMock addField(IJavaField field) {
    _fields.put(field.getName(), field);

    return this;
  }

  @Override
  public String getDocumentation() {
    return _documentation;
  }

  public void setDocumentation(String documentation) {
    _documentation = documentation;
  }

  @Override
  public IResource getFile() {
    return _file;
  }

  @Override
  public boolean supportsInformalParameters() {
    return false;
  }

  public JavaClassTypeMock setFile(IResource file) {
    _file = file;

    return this;
  }

  @Override
  public boolean isAssignableFrom(IJavaType type) {
    return false;
  }

  @Override
  @NotNull
  public Object getUnderlyingObject() {
    return _fullyQualifiedName;
  }

}
