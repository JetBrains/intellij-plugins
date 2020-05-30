// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElementType;
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Angular2AttributeType {

  REFERENCE(Angular2HtmlElementTypes.REFERENCE, "#", "", "ref-"),
  REGULAR(XmlElementType.XML_ATTRIBUTE, "", "", null),
  LET(Angular2HtmlElementTypes.LET, "let-", "", null),
  BANANA_BOX_BINDING(Angular2HtmlElementTypes.BANANA_BOX_BINDING, "[(", ")]", "bindon-"),
  PROPERTY_BINDING(Angular2HtmlElementTypes.PROPERTY_BINDING, "[", "]", "bind-"),
  EVENT(Angular2HtmlElementTypes.EVENT, "(", ")", "on-"),
  TEMPLATE_BINDINGS(Angular2HtmlElementTypes.TEMPLATE_BINDINGS, "*", "", null),
  NG_CONTENT_SELECTOR(Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR, "", "", null),
  I18N(XmlElementType.XML_ATTRIBUTE, "i18n-", "", null);

  private final IElementType myElementType;
  private final String myPrefix;
  private final String mySuffix;
  private final String myCanonicalPrefix;

  Angular2AttributeType(@NotNull IElementType elementType,
                        @NotNull String prefix,
                        @NotNull String suffix,
                        @Nullable String canonicalPrefix) {
    myElementType = elementType;
    myPrefix = prefix;
    mySuffix = suffix;
    myCanonicalPrefix = canonicalPrefix;
  }


  public @NotNull String buildName(@NotNull String name) {
    return buildName(name, false);
  }

  @Contract("_,false -> !null")
  public String buildName(@NotNull String name, boolean canonical) {
    if (canonical) {
      if (myCanonicalPrefix == null) {
        return null;
      }
      return myCanonicalPrefix + name;
    }
    return myPrefix + name + mySuffix;
  }

  public @NotNull IElementType getElementType() {
    return myElementType;
  }

  public String getCanonicalPrefix() {
    return myCanonicalPrefix;
  }
}
