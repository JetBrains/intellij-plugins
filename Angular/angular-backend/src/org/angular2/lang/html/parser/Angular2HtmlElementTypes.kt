// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.lang.html.psi.impl.Angular2HtmlBananaBoxBindingImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlBlockContentsImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlBlockImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlBlockParametersImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlEventImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlExpansionFormCaseContentImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlExpansionFormCaseImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlExpansionFormImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlLetImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlPropertyBindingImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlReferenceImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlTemplateBindingsImpl
import org.angular2.lang.html.stub.Angular2HtmlAttributeStubElementType
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorElementType
import org.angular2.lang.html.stub.Angular2HtmlVariableElementType
import org.jetbrains.annotations.NonNls
import java.util.function.Function

object Angular2HtmlElementTypes : Angular2HtmlTokenTypes {
  open class Angular2ElementType(debugName: @NonNls String, private val myClassConstructor: Function<Angular2ElementType, out ASTNode>)
    : IElementType(debugName, Angular2HtmlLanguage), ICompositeElementType {
    override fun createCompositeNode(): ASTNode {
      return myClassConstructor.apply(this)
    }
  }

  const val EXTERNAL_ID_PREFIX: String = "NG-HTML:"

  val REFERENCE_VARIABLE: JSElementType<JSVariable> = Angular2HtmlVariableElementType(Angular2HtmlAttrVariable.Kind.REFERENCE)
  val LET_VARIABLE: JSElementType<JSVariable> = Angular2HtmlVariableElementType(Angular2HtmlAttrVariable.Kind.LET)

  val NG_CONTENT_SELECTOR: Angular2HtmlNgContentSelectorElementType = Angular2HtmlNgContentSelectorElementType()

  val EVENT: Angular2HtmlAttributeStubElementType = Angular2HtmlAttributeStubElementType("EVENT") { node ->
    Angular2HtmlEventImpl(node)
  }
  val BANANA_BOX_BINDING: Angular2HtmlAttributeStubElementType = Angular2HtmlAttributeStubElementType("BANANA_BOX_BINDING") { node ->
    Angular2HtmlBananaBoxBindingImpl(node)
  }

  val PROPERTY_BINDING: Angular2HtmlAttributeStubElementType = Angular2HtmlAttributeStubElementType("PROPERTY_BINDING") { node ->
    Angular2HtmlPropertyBindingImpl(node)
  }

  val REFERENCE: Angular2HtmlAttributeStubElementType = Angular2HtmlAttributeStubElementType("REFERENCE") { node ->
    Angular2HtmlReferenceImpl(node)
  }

  val LET: Angular2HtmlAttributeStubElementType = Angular2HtmlAttributeStubElementType("LET") { node ->
    Angular2HtmlLetImpl(node)
  }

  val TEMPLATE_BINDINGS: Angular2HtmlAttributeStubElementType = Angular2HtmlAttributeStubElementType("TEMPLATE_BINDINGS") { node ->
    Angular2HtmlTemplateBindingsImpl(node)
  }

  val EXPANSION_FORM: IElementType = Angular2ElementType("NG:EXPANSION_FORM") { node ->
    Angular2HtmlExpansionFormImpl(node)
  }

  val EXPANSION_FORM_CASE: IElementType = Angular2ElementType("NG:EXPANSION_FORM_CASE") { node ->
    Angular2HtmlExpansionFormCaseImpl(node)
  }

  val EXPANSION_FORM_CASE_CONTENT: IElementType = Angular2ElementType("NG:EXPANSION_FORM_CASE_CONTENT") { node ->
    Angular2HtmlExpansionFormCaseContentImpl(node)
  }

  val BLOCK: IElementType = Angular2ElementType("NG:BLOCK") { node ->
    Angular2HtmlBlockImpl(node)
  }

  val BLOCK_PARAMETERS: IElementType = Angular2ElementType("NG:BLOCK_PARAMETERS") { node ->
    Angular2HtmlBlockParametersImpl(node)
  }

  val BLOCK_CONTENTS: IElementType = Angular2ElementType("NG:BLOCK_CONTENTS") { node ->
    Angular2HtmlBlockContentsImpl(node)
  }

  val ALL_ATTRIBUTES: TokenSet = TokenSet.create(
    EVENT,
    BANANA_BOX_BINDING,
    PROPERTY_BINDING,
    TEMPLATE_BINDINGS,
    LET,
    REFERENCE
  )
}