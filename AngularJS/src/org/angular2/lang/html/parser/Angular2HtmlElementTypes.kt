// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes;
import org.angular2.lang.html.psi.impl.*;
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.intellij.psi.tree.TokenSet.create;

public interface Angular2HtmlElementTypes extends XmlElementType, Angular2HtmlTokenTypes, Angular2HtmlStubElementTypes {

  IElementType EXPANSION_FORM = new Angular2ElementType("NG:EXPANSION_FORM", node -> new Angular2HtmlExpansionFormImpl(node));
  IElementType EXPANSION_FORM_CASE = new Angular2ElementType("NG:EXPANSION_FORM_CASE", node -> new Angular2HtmlExpansionFormCaseImpl(node));
  IElementType EXPANSION_FORM_CASE_CONTENT = new Angular2ElementType("NG:EXPANSION_FORM_CASE_CONTENT",
                                                                     node -> new Angular2HtmlExpansionFormCaseContentImpl(node));

  IElementType EVENT = new Angular2ElementType("NG:EVENT", node -> new Angular2HtmlEventImpl(node));
  IElementType BANANA_BOX_BINDING = new Angular2ElementType("NG:BANANA_BOX_BINDING", node -> new Angular2HtmlBananaBoxBindingImpl(node));
  IElementType PROPERTY_BINDING = new Angular2ElementType("NG:PROPERTY_BINDING", node -> new Angular2HtmlPropertyBindingImpl(node));
  IElementType REFERENCE = new Angular2ElementType("NG:REFERENCE", node -> new Angular2HtmlReferenceImpl(node));
  IElementType LET = new Angular2ElementType("NG:LET", node -> new Angular2HtmlLetImpl(node));
  IElementType TEMPLATE_BINDINGS = new Angular2ElementType("NG:TEMPLATE_BINDINGS", node -> new Angular2HtmlTemplateBindingsImpl(node));

  TokenSet ALL_ATTRIBUTES = create(
    EVENT,
    BANANA_BOX_BINDING,
    PROPERTY_BINDING,
    TEMPLATE_BINDINGS,
    LET,
    REFERENCE
  );

  class Angular2ElementType extends IElementType implements ICompositeElementType {

    private final Function<Angular2ElementType, ASTNode> myClassConstructor;

    public Angular2ElementType(@NotNull @NonNls String debugName, Function<Angular2ElementType, ASTNode> classConstructor) {
      super(debugName, Angular2HtmlLanguage.INSTANCE);
      myClassConstructor = classConstructor;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
      return myClassConstructor.apply(this);
    }
  }
}
