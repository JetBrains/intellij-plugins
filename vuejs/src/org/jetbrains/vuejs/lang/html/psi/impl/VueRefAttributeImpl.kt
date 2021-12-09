// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.impl

import com.intellij.javascript.web.types.WebJSTypesUtil
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.TypeScriptMergedTypeImplicitElement
import com.intellij.lang.javascript.psi.types.JSArrayTypeImpl
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.openapi.util.TextRange
import com.intellij.pom.PomRenameableTarget
import com.intellij.pom.PomTarget
import com.intellij.pom.PomTargetPsiElement
import com.intellij.pom.PsiDeclaredTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlStubBasedAttributeBase
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.vuejs.codeInsight.ATTR_DIRECTIVE_PREFIX
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind
import org.jetbrains.vuejs.codeInsight.resolveLocalComponent
import org.jetbrains.vuejs.index.processScriptSetupTopLevelDeclarations
import org.jetbrains.vuejs.lang.html.psi.VueRefAttribute
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.model.source.VueCompositionInfoHelper

class VueRefAttributeImpl : XmlStubBasedAttributeBase<VueRefAttributeStubImpl>, VueRefAttribute {

  constructor(stub: VueRefAttributeStubImpl,
              nodeType: IStubElementType<out VueRefAttributeStubImpl, out VueRefAttributeImpl>) : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override fun getValue(): String? =
    stub?.value ?: super.getValue()

  override val isList: Boolean
    get() = stub?.isList ?: (parent.getAttribute(V_FOR_NAME) != null)

  override val containingTagName: String
    get() = stub?.containingTagName ?: parent.name

  override val implicitElement: JSImplicitElement?
    get() = CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result(
        value
          ?.trim()
          ?.takeIf { it.isNotEmpty() }
          ?.let { name ->
            findScriptSetupVar(name)?.let {
              VueCompositionInfoHelper.getUnwrappedRefElement(
                it, VueCompositionInfoHelper.getUnwrapRefType(this))
            }
            //?: findSetupVar(name) // TODO implement support for references from setup() method
            ?: VueRefDeclarationImpl(name, resolveTagType(), this, JSImplicitElement.Type.Property)
          },
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  private fun resolveTagType(): JSType? {
    val component = VueModelManager.findEnclosingContainer(this) as? VueRegularComponent ?: return null
    val source = JSTypeSourceFactory.createTypeSource(this, true)
    return (resolveLocalComponent(component, containingTagName, containingFile.originalFile)
              .takeIf { it.isNotEmpty() }
              ?.map { it.thisType }
              ?.let { JSCompositeTypeFactory.createUnionType(source, it) }
            ?: WebJSTypesUtil.getHtmlElementClassType(source, containingTagName))
      ?.let { if (isList) JSArrayTypeImpl(it, it.source) else it }
  }

  private fun findScriptSetupVar(refName: String): JSVariable? {
    var result: JSVariable? = null
    processScriptSetupTopLevelDeclarations(this) { resolved ->
      if (resolved is JSVariable && resolved.name == refName) {
        result = resolved
        false
      }
      else true
    }
    return result
  }

  private class VueRefDeclarationImpl(name: String, jsType: JSType?, provider: PsiElement, kind: JSImplicitElement.Type)
    : JSLocalImplicitElementImpl(name, jsType, provider, kind), VueRefAttribute.VueRefDeclaration,
      PomRenameableTarget<PsiElement>, PomTargetPsiElement, PsiDeclaredTarget {

    override fun setName(name: String): PsiElement {
      (myProvider as VueRefAttribute).setValue(name)
      return myProvider.implicitElement!!
    }

    override fun getNavigationElement(): PsiElement {
      return (myProvider as VueRefAttribute).valueElement ?: super.getNavigationElement()
    }

    override fun getNameIdentifierRange(): TextRange? =
      (myProvider as VueRefAttribute).valueElement?.let { it.valueTextRange?.shiftLeft(it.startOffset) }

    override fun getTextRange(): TextRange =
      (myProvider as VueRefAttribute).valueElement?.valueTextRange ?: TextRange.EMPTY_RANGE

    override fun isEquivalentTo(another: PsiElement?): Boolean =
      when (another) {
        is VueRefDeclarationImpl -> equals(another)
        is TypeScriptMergedTypeImplicitElement -> equals(another.explicitElement)
        else -> false
      }

    override fun getTarget(): PomTarget = this
  }

  companion object {
    private val V_FOR_NAME = ATTR_DIRECTIVE_PREFIX + VueDirectiveKind.FOR.directiveName
  }

}