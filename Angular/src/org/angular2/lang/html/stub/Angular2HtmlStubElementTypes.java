// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.stub;

import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.JSVariable;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable.Kind;
import org.angular2.lang.html.psi.impl.*;

public interface Angular2HtmlStubElementTypes {

  int STUB_VERSION = 2;

  String EXTERNAL_ID_PREFIX = "NG-HTML:";

  JSElementType<JSVariable> REFERENCE_VARIABLE = new Angular2HtmlVariableElementType(Kind.REFERENCE);
  JSElementType<JSVariable> LET_VARIABLE = new Angular2HtmlVariableElementType(Kind.LET);

  Angular2HtmlNgContentSelectorElementType NG_CONTENT_SELECTOR = new Angular2HtmlNgContentSelectorElementType();

  Angular2HtmlAttributeStubElementType EVENT =
    new Angular2HtmlAttributeStubElementType(
      "EVENT",
      (stub, nodeType) -> new Angular2HtmlEventImpl(stub, nodeType),
      node -> new Angular2HtmlEventImpl(node)
    );

  Angular2HtmlAttributeStubElementType BANANA_BOX_BINDING =
    new Angular2HtmlAttributeStubElementType(
      "BANANA_BOX_BINDING",
      (stub, nodeType) -> new Angular2HtmlBananaBoxBindingImpl(stub, nodeType),
      node -> new Angular2HtmlBananaBoxBindingImpl(node)
    );

  Angular2HtmlAttributeStubElementType PROPERTY_BINDING =
    new Angular2HtmlAttributeStubElementType(
      "PROPERTY_BINDING",
      (stub, nodeType) -> new Angular2HtmlPropertyBindingImpl(stub, nodeType),
      node -> new Angular2HtmlPropertyBindingImpl(node)
    );

  Angular2HtmlAttributeStubElementType REFERENCE =
    new Angular2HtmlAttributeStubElementType(
      "REFERENCE",
      (stub, nodeType) -> new Angular2HtmlReferenceImpl(stub, nodeType),
      node -> new Angular2HtmlReferenceImpl(node)
    );

  Angular2HtmlAttributeStubElementType LET =
    new Angular2HtmlAttributeStubElementType(
      "LET",
      (stub, nodeType) -> new Angular2HtmlLetImpl(stub, nodeType),
      node -> new Angular2HtmlLetImpl(node)
    );

  Angular2HtmlAttributeStubElementType TEMPLATE_BINDINGS =
    new Angular2HtmlAttributeStubElementType(
      "TEMPLATE_BINDINGS",
      (stub, nodeType) -> new Angular2HtmlTemplateBindingsImpl(stub, nodeType),
      node -> new Angular2HtmlTemplateBindingsImpl(node)
    );
}

