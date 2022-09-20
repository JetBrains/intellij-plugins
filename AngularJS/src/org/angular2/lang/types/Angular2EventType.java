// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.javascript.web.lang.js.JSWebSymbolUtils;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.tryCast;
import static com.intellij.util.containers.ContainerUtil.isEmpty;
import static org.angular2.lang.html.parser.Angular2AttributeType.EVENT;

public class Angular2EventType extends Angular2BaseType<XmlAttribute> {

  public Angular2EventType(@NotNull XmlAttribute attribute) {
    super(attribute, XmlAttribute.class);
  }

  protected Angular2EventType(@NotNull JSTypeSource source) {
    super(source, XmlAttribute.class);
  }

  @Override
  protected @Nullable String getTypeOfText() {
    return "eventof#" + getSourceElement().getName();
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2EventType(source);
  }

  @Override
  protected @Nullable JSType resolveType(@NotNull JSTypeSubstitutionContext context) {
    XmlAttribute attribute = getSourceElement();
    Angular2AttributeDescriptor descriptor = tryCast(attribute.getDescriptor(), Angular2AttributeDescriptor.class);
    if (descriptor != null && isEmpty(descriptor.getSourceDirectives())) {
      return JSWebSymbolUtils.getJSType(descriptor.getSymbol());
    }
    return BindingsTypeResolver.resolve(attribute,
                                        Angular2EventType::isEventAttribute,
                                        BindingsTypeResolver::resolveDirectiveEventType);
  }

  private static boolean isEventAttribute(Angular2AttributeNameParser.AttributeInfo info) {
    return info.type == EVENT
           && ((Angular2AttributeNameParser.EventInfo)info).eventType == Angular2HtmlEvent.EventType.REGULAR;
  }
}
