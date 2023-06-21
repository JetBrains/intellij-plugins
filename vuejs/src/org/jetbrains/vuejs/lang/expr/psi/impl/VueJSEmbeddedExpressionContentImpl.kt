// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.evaluation.JSExpressionTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl
import com.intellij.lang.javascript.psi.resolve.generic.JSTypeSubstitutorImpl
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.typescript.resolve.TypeScriptGenericTypesEvaluator
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.CachedValuesManagerImpl
import com.intellij.util.asSafely
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.stub.VueJSEmbeddedExpressionContentStub
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol

class VueJSEmbeddedExpressionContentImpl :
  JSStubElementImpl<VueJSEmbeddedExpressionContentStub>, JSSuppressionHolder, VueJSEmbeddedExpressionContent,
  HintedReferenceHost, TypeScriptTypeParameterListOwner {

  constructor(node: ASTNode) : super(node)

  constructor(stub: VueJSEmbeddedExpressionContentStub, type: IStubElementType<*, *>) : super(stub, type)

  override fun getLanguage(): Language {
    return elementType.language
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is JSElementVisitor) {
      visitor.visitJSEmbeddedContent(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun allowTopLevelThis(): Boolean {
    return true
  }

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun getQuoteChar(): Char? {
    return JSEmbeddedContentImpl.getQuoteChar(this)
  }

  override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> = PsiReference.EMPTY_ARRAY

  override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean = false

  override fun toString(): String {
    return super.toString() + "(${language.id})"
  }

  override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
    // In case of expressions on <script setup> tag, we need to process declarations in script setup contents as well
    parentOfType<XmlTag>()
      ?.takeIf { it.isScriptSetupTag() }
      ?.let { PsiTreeUtil.getStubChildOfType(it, JSEmbeddedContent::class.java) }
      ?.processDeclarations(processor, state, lastParent, place)
      ?.let {
        if (!it) return false
      }
    return super.processDeclarations(processor, state, lastParent, place)
  }

  override fun getTypeParameterList(): TypeScriptTypeParameterList? =
    (findModule(this, true) as? VueScriptSetupEmbeddedContentImpl)
      ?.typeParameterList

  override fun getTypeSubstitutorForGenerics(): JSTypeSubstitutor? {
    val tag = (InjectedLanguageManager.getInstance(project).getInjectionHost(this) ?: this)
                .parentOfType<XmlTag>() ?: return null
    return CachedValuesManagerImpl.getCachedValue(tag) {
      CachedValueProvider.Result.create(getTypeSubstitutorForGenerics(tag), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  companion object {
    fun getTypeSubstitutorForGenerics(tag: XmlTag): JSTypeSubstitutor {
      val pairs = tag.attributes.mapNotNull { attr ->
        val value = attr.valueElement
        val expression = if (value != null)
          value.findJSExpression<JSExpression>()
          ?: return@mapNotNull null
        else
          null

        val expectedType =
          attr.descriptor
            ?.asSafely<WebSymbolAttributeDescriptor>()
            ?.symbol
            ?.jsType
        if (expectedType != null)
          Pair(expectedType, expression)
        else
          null
      }

      val substitutor = TypeScriptGenericTypesEvaluator.getInstance().getTypeSubstitutorForCallItem(
        JSFunctionTypeImpl(
          JSTypeSource.EMPTY_TS,
          pairs.map { (paramType, _) -> JSParameterTypeDecoratorImpl(paramType, false, false, true) },
          null
        ),
        object : JSCallItem {
          override fun getArgumentTypes(argumentTypeFactory: JSExpressionTypeFactory): List<JSType?> =
            pairs.map { (_, expression) ->
              if (expression == null) JSBooleanLiteralTypeImpl(true, false, JSTypeSource.EMPTY_TS)
              else argumentTypeFactory.evaluate(expression)
            }

          override fun getArgumentSize(): Int = pairs.size

          override fun getPsiContext(): PsiElement =
            tag
        }, null
      ) as JSTypeSubstitutorImpl
      val component = (tag.descriptor as? WebSymbolElementDescriptor)
        ?.symbol
        ?.unwrapMatchedSymbols()
        ?.firstNotNullOfOrNull { it as? VueComponentSymbol }
      component?.typeParameters?.forEach {
        if (!substitutor.containsId(it.genericId)) {
          substitutor.put(it.genericId, JSUnknownType.TS_INSTANCE)
        }
      }
      return substitutor
    }
  }


}
