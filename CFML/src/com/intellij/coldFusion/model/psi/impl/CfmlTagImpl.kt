/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi.impl

import com.intellij.coldFusion.injection.CfqueryEscaper
import com.intellij.coldFusion.model.CfmlUtil
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.coldFusion.model.psi.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStub
import java.util.Locale


open class CfmlTagImpl : CfmlCompositeElement, CfmlTag, PsiLanguageInjectionHost {
  constructor(astNode: ASTNode) : super(astNode)
  constructor(stub: NamedStub<*>, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

  override fun processDeclarations(processor: PsiScopeProcessor,
                                   state: ResolveState,
                                   lastParent: PsiElement?,
                                   place: PsiElement): Boolean {
    return CfmlPsiUtil.processDeclarations(processor, state, lastParent, this)
  }

  override fun getTagName(): String {
    val tagName = findChildByType<PsiElement>(CfmlTokenTypes.CF_TAG_NAME)
    return tagName?.text?.lowercase(Locale.getDefault()) ?: ""
  }

  override fun getName(): String? = tagName

  override fun getDeclarativeElement(): PsiNamedElement? {
    if ("cfset" == name) {
      val assignment = findChildByClass(CfmlAssignmentExpression::class.java)
      return assignment?.assignedVariable
    }
    return findChildByClass(CfmlNamedAttributeImpl::class.java)
  }

  override fun getReferences(): Array<PsiReference> {
    val prefixAndName = CfmlUtil.getPrefixAndName(name)
    val componentName = prefixAndName.getSecond()
    val cfmlImport = CfmlUtil.getImportByPrefix(this, prefixAndName.getFirst())
    val tagName = findChildByType<PsiElement>(CfmlTokenTypes.CF_TAG_NAME)

    if (tagName != null && cfmlImport != null && !StringUtil.isEmpty(componentName)) {
      return arrayOf(CfmlComponentReference(tagName.node))
    }
    return super.getReferences()
  }

  fun getAttributeValueElement(attributeName: String): PsiElement? = CfmlPsiUtil.getAttributeValueElement(this, attributeName)

  //// PsiLanguageInjectionHost implementation

  override fun isValidHost(): Boolean = true

  override fun updateText(text: String): PsiLanguageInjectionHost? = ElementManipulators.handleContentChange<CfmlTagImpl>(this, text)

  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> = CfqueryEscaper(this)

}
