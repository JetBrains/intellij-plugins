package com.intellij.flex.uiDesigner.css {
public interface StyleValueResolver {
  function resolve(propertyDescriptor:CssDeclaration):*;
}
}