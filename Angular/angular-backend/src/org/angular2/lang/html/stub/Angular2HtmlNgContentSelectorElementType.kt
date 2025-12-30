// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.impl.Angular2HtmlNgContentSelectorImpl
import org.jetbrains.annotations.NonNls

class Angular2HtmlNgContentSelectorElementType
  : IElementType("NG_CONTENT_SELECTOR", Angular2HtmlLanguage),
    ICompositeElementType {
  override fun toString(): @NonNls String {
    return Angular2HtmlElementTypes.EXTERNAL_ID_PREFIX + super.getDebugName()
  }

  override fun createCompositeNode(): ASTNode {
    return CompositeElement(Angular2HtmlElementTypes.NG_CONTENT_SELECTOR)
  }

  fun createPsi(node: ASTNode): Angular2HtmlNgContentSelector {
    return Angular2HtmlNgContentSelectorImpl(node)
  }
}