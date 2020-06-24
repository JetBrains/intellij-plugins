// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.lang.html.parser.Angular2AttributeType.BANANA_BOX_BINDING;
import static org.angular2.lang.html.parser.Angular2AttributeType.PROPERTY_BINDING;
import static org.angular2.lang.html.psi.PropertyBindingType.PROPERTY;

public class Angular2PropertyBindingType extends Angular2BaseType<XmlAttribute> {

  public Angular2PropertyBindingType(@NotNull XmlAttribute attribute) {
    super(attribute, XmlAttribute.class);
  }

  protected Angular2PropertyBindingType(@NotNull JSTypeSource source) {
    super(source, XmlAttribute.class);
  }

  @Override
  protected @Nullable String getTypeOfText() {
    return getSourceElement().getName();
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2PropertyBindingType(source);
  }

  @Override
  protected @Nullable JSType resolveType(@NotNull JSTypeSubstitutionContext context) {
    return BindingsTypeResolver.resolve(getSourceElement(),
                                        Angular2PropertyBindingType::isPropertyBindingAttribute,
                                        BindingsTypeResolver::resolveDirectiveInputType);
  }

  static boolean isPropertyBindingAttribute(Angular2AttributeNameParser.AttributeInfo info) {
    return info.type == BANANA_BOX_BINDING
           || (info.type == PROPERTY_BINDING
               && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PROPERTY);
  }
}