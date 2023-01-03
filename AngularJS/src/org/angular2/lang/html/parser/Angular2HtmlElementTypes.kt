// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlElementType
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.impl.*
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import org.jetbrains.annotations.NonNls
import java.util.function.Function

interface Angular2HtmlElementTypes : XmlElementType, Angular2HtmlTokenTypes, Angular2HtmlStubElementTypes {
  class Angular2ElementType(debugName: @NonNls String, private val myClassConstructor: Function<Angular2ElementType, ASTNode>)
    : IElementType(debugName, Angular2HtmlLanguage.INSTANCE), ICompositeElementType {
    override fun createCompositeNode(): ASTNode {
      return myClassConstructor.apply(this)
    }
  }

  companion object {
    @JvmField
    val EXPANSION_FORM: IElementType = Angular2ElementType("NG:EXPANSION_FORM") { node ->
      Angular2HtmlExpansionFormImpl(node)
    }

    @JvmField
    val EXPANSION_FORM_CASE: IElementType = Angular2ElementType("NG:EXPANSION_FORM_CASE") { node ->
      Angular2HtmlExpansionFormCaseImpl(node)
    }

    @JvmField
    val EXPANSION_FORM_CASE_CONTENT: IElementType = Angular2ElementType("NG:EXPANSION_FORM_CASE_CONTENT") { node ->
      Angular2HtmlExpansionFormCaseContentImpl(node)
    }

    @JvmField
    val EVENT: IElementType = Angular2ElementType("NG:EVENT") { node ->
      Angular2HtmlEventImpl(node)
    }

    @JvmField
    val BANANA_BOX_BINDING: IElementType = Angular2ElementType("NG:BANANA_BOX_BINDING") { node ->
      Angular2HtmlBananaBoxBindingImpl(node)
    }

    @JvmField
    val PROPERTY_BINDING: IElementType = Angular2ElementType("NG:PROPERTY_BINDING") { node ->
      Angular2HtmlPropertyBindingImpl(node)
    }

    @JvmField
    val REFERENCE: IElementType = Angular2ElementType("NG:REFERENCE") { node ->
      Angular2HtmlReferenceImpl(node)
    }

    @JvmField
    val LET: IElementType = Angular2ElementType("NG:LET") { node ->
      Angular2HtmlLetImpl(node)
    }

    @JvmField
    val TEMPLATE_BINDINGS: IElementType = Angular2ElementType("NG:TEMPLATE_BINDINGS") { node ->
      Angular2HtmlTemplateBindingsImpl(node)
    }

    @JvmField
    val ALL_ATTRIBUTES = TokenSet.create(
      EVENT,
      BANANA_BOX_BINDING,
      PROPERTY_BINDING,
      TEMPLATE_BINDINGS,
      LET,
      REFERENCE
    )
  }
}