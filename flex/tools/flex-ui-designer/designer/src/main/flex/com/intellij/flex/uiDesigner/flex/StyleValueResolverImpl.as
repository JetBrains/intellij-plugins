package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssDeclarationType;
import com.intellij.flex.uiDesigner.css.StyleValueResolver;

import flash.system.ApplicationDomain;

public class StyleValueResolverImpl implements StyleValueResolver {
  private var applicationDomain:ApplicationDomain;

  public function StyleValueResolverImpl(applicationDomain:ApplicationDomain) {
    this.applicationDomain = applicationDomain;
  }

  public function resolve(propertyDescriptor:CssDeclaration):* {
    if (propertyDescriptor.value is ClassReferenceImpl) {
      return applicationDomain.getDefinition(ClassReference(propertyDescriptor.value).className);
    }
    else if (propertyDescriptor.type == CssDeclarationType.EMBED) {
      return null;
    }
    else {
      return propertyDescriptor.value;
    }
  }
}
}