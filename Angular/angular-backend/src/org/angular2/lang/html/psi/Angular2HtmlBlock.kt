// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi

import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTagChild
import org.angular2.codeInsight.blocks.Angular2HtmlBlockSymbol
import org.angular2.lang.expr.psi.Angular2BlockParameter

interface Angular2HtmlBlock : XmlElement, PsiExternalReferenceHost, PsiNamedElement, XmlTagChild {

  override fun getName(): String

  val nameElement: PsiElement

  val parameters: List<Angular2BlockParameter>

  val contents: Angular2HtmlBlockContents?

  val definition: Angular2HtmlBlockSymbol?

  val isPrimary: Boolean

  val primaryBlockDefinition: Angular2HtmlBlockSymbol?

  val primaryBlock: Angular2HtmlBlock?

  fun blockSiblingsForward(): Sequence<Angular2HtmlBlock>

  fun blockSiblingsBackward(): Sequence<Angular2HtmlBlock>

}