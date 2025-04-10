// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.impl.*
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import org.jetbrains.annotations.NonNls
import java.util.function.Function

internal interface Angular2HtmlElementTypes : Angular2HtmlTokenTypes, Angular2HtmlStubElementTypes {
  open class Angular2ElementType(debugName: @NonNls String, private val myClassConstructor: Function<Angular2ElementType, out ASTNode>)
    : IElementType(debugName, Angular2HtmlLanguage), ICompositeElementType {
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
    val BLOCK: IElementType = Angular2ElementType("NG:BLOCK") { node ->
      Angular2HtmlBlockImpl(node)
    }

    @JvmField
    val BLOCK_PARAMETERS: IElementType = Angular2ElementType("NG:BLOCK_PARAMETERS") { node ->
      Angular2HtmlBlockParametersImpl(node)
    }

    @JvmField
    val BLOCK_CONTENTS: IElementType = Angular2ElementType("NG:BLOCK_CONTENTS") { node ->
      Angular2HtmlBlockContentsImpl(node)
    }

    @JvmField
    val ALL_ATTRIBUTES = TokenSet.create(
      Angular2HtmlStubElementTypes.EVENT,
      Angular2HtmlStubElementTypes.BANANA_BOX_BINDING,
      Angular2HtmlStubElementTypes.PROPERTY_BINDING,
      Angular2HtmlStubElementTypes.TEMPLATE_BINDINGS,
      Angular2HtmlStubElementTypes.LET,
      Angular2HtmlStubElementTypes.REFERENCE
    )
  }
}