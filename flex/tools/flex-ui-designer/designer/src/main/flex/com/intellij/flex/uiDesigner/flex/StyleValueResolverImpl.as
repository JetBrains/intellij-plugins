package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.StyleValueResolver;

import flash.system.ApplicationDomain;

public class StyleValueResolverImpl implements StyleValueResolver {
  private var applicationDomain:ApplicationDomain;

  public function StyleValueResolverImpl(applicationDomain:ApplicationDomain) {
    this.applicationDomain = applicationDomain;
  }

  public function resolve(propertyDescriptor:CssDeclaration):* {
    if (propertyDescriptor.type == CssPropertyType.CLASS_REFERENCE) {
      return applicationDomain.getDefinition(ClassReference(propertyDescriptor.value).className);
    }
    else if (propertyDescriptor.type == CssPropertyType.EMBED) {
      return null;
    }
    else {
      return propertyDescriptor.value;
    }
  }
}
}