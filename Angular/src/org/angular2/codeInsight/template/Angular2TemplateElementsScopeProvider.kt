// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.utils.qualifiedKind
import com.intellij.webSymbols.utils.withNavigationTarget
import org.angular2.Angular2InjectionUtils
import org.angular2.codeInsight.blocks.BLOCK_FOR
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.codeInsight.blocks.PARAMETER_LET
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2RecursiveVisitor
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType.*
import org.angular2.lang.html.psi.*
import org.angular2.web.ELEMENT_NG_TEMPLATE
import org.jetbrains.annotations.NonNls
import java.util.function.Consumer

class Angular2TemplateElementsScopeProvider : Angular2TemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<Angular2TemplateScope> {
    val hostFile = CompletionUtil.getOriginalOrSelf(hostElement ?: element).containingFile

    val isInjected = hostElement != null
    val templateRootScope = CachedValuesManager.getCachedValue(hostFile) {
      val result: Angular2TemplateElementScope
      if (!isInjected) {
        result = Angular2TemplateScopeBuilder(hostFile).topLevelScope
      }
      else {
        result = Angular2ForeignTemplateScopeBuilder(hostFile).topLevelScope
      }
      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
    return listOfNotNull(templateRootScope.findBestMatchingTemplateScope(hostElement ?: element))
  }

  private class Angular2TemplateElementScope(private val root: PsiElement, parent: Angular2TemplateElementScope?)
    : Angular2TemplateScope(parent) {

    private val elements = ArrayList<JSPsiElementBase>()
    private val myRange = root.textRange

    init {
      if (parent != null) {
        assert(parent.myRange.contains(myRange))
      }
    }

    override val source: PsiElement
      get() = root

    override val symbols = ArrayList<WebSymbol>()

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      elements.forEach { el -> consumer.accept(JSResolveResult(el)) }
    }

    fun add(element: JSPsiElementBase) {
      elements.add(element)
    }

    fun add(symbol: WebSymbol) {
      assert(symbol.qualifiedKind == WebSymbol.JS_SYMBOLS)
      symbols.add(symbol)
    }

    fun findBestMatchingTemplateScope(element: PsiElement): Angular2TemplateElementScope? {
      if (!myRange.contains(element.textOffset)) {
        return null
      }
      var curScope: Angular2TemplateElementScope? = null
      var innerScope: Angular2TemplateElementScope? = this
      while (innerScope != null) {
        curScope = innerScope
        innerScope = null
        for (child in curScope.getChildren()) {
          if (child is Angular2TemplateElementScope && child.myRange.contains(element.textOffset)) {
            innerScope = child
            break
          }
        }
      }
      if (PsiTreeUtil.getParentOfType(element, Angular2HtmlTemplateBindings::class.java) != null && curScope != this) {
        curScope = curScope?.parent as Angular2TemplateElementScope?
      }
      else if (element.parentOfType<Angular2BlockParameter>(true)
          ?.let { it.isPrimaryExpression && it.block?.name != BLOCK_LET } == true) {
        curScope = curScope?.parent as Angular2TemplateElementScope?
      }
      return curScope
    }

    override fun equals(other: Any?): Boolean =
      other is Angular2TemplateElementScope
      && other.root == root
      && other.myRange == myRange

    override fun hashCode(): Int =
      root.hashCode()
  }

  private open class Angular2BaseScopeBuilder(private val myTemplateFile: PsiFile) : Angular2HtmlRecursiveElementVisitor() {
    private val scopes = Stack<Angular2TemplateElementScope>()

    val topLevelScope: Angular2TemplateElementScope
      get() {
        myTemplateFile.accept(this)
        assert(scopes.size == 1)
        return scopes.peek()
      }

    init {
      scopes.add(Angular2TemplateElementScope(myTemplateFile, null))
    }

    fun currentScope(): Angular2TemplateElementScope {
      return scopes.peek()
    }

    fun popScope() {
      scopes.pop()
    }

    fun pushScope(tagOrBlock: PsiElement) {
      scopes.push(Angular2TemplateElementScope(tagOrBlock, currentScope()))
    }

    fun addElement(element: JSPsiElementBase) {
      currentScope().add(element)
    }

    fun addSymbol(symbol: WebSymbol) {
      currentScope().add(symbol)
    }

    fun prevScope(): Angular2TemplateElementScope {
      return scopes[scopes.size - 2]
    }
  }

  private class Angular2TemplateScopeBuilder(templateFile: PsiFile) : Angular2BaseScopeBuilder(templateFile) {

    override fun visitXmlTag(tag: XmlTag) {
      val isTemplateTag = tag.children.any { it is Angular2HtmlTemplateBindings } || isTemplateTag(tag)
      if (isTemplateTag) {
        pushScope(tag)
      }
      super.visitXmlTag(tag)
      if (isTemplateTag) {
        popScope()
      }
    }

    override fun visitBoundAttribute(boundAttribute: Angular2HtmlBoundAttribute) {
      //do not visit expressions
    }

    override fun visitReference(reference: Angular2HtmlReference) {
      val `var` = reference.variable
                  ?: return
      if (isTemplateTag(reference.parent)) {
        // References on ng-template are visible within parent scope
        prevScope().add(`var`)
      }
      else {
        currentScope().add(`var`)
      }
    }

    override fun visitLet(variable: Angular2HtmlLet) {
      variable.variable?.let { addElement(it) }
    }

    override fun visitTemplateBindings(bindings: Angular2HtmlTemplateBindings) {
      for (b in bindings.bindings.bindings) {
        if (b.keyIsVar() && b.variableDefinition != null) {
          addElement(b.variableDefinition!!)
        }
      }
    }

    override fun visitBlock(block: Angular2HtmlBlock) {
      val blockName = block.getName()
      if (blockName == BLOCK_LET) {
        // Do not create scope or visit children for @let
        block.parameters.getOrNull(0)?.variables?.firstOrNull()
          ?.let { addElement(it) }
        return
      }
      pushScope(block)
      super.visitBlock(block)
      if (blockName == BLOCK_FOR) {
        val usedVariables = block.parameters
          .filter { it.name == PARAMETER_LET }
          .flatMap { it.variables }
          .mapNotNull { variable -> (variable.initializer as? JSReferenceExpression)?.takeIf { it.qualifier == null }?.referenceName }
        block.definition
          ?.implicitVariables
          ?.filter { it.name !in usedVariables }
          ?.forEach { addSymbol(it.withNavigationTarget(block)) }
      }
      popScope()
    }

    override fun visitBlockParameters(blockParameters: Angular2HtmlBlockParameters) {
      blockParameters.accept(expressionVisitor)
    }

    private val expressionVisitor = object : Angular2RecursiveVisitor() {

      override fun visitAngular2BlockParameter(parameter: Angular2BlockParameter) {
        parameter.variables.forEach(::addElement)
      }

    }
  }

  private class Angular2ForeignTemplateScopeBuilder(templateFile: PsiFile) : Angular2BaseScopeBuilder(templateFile) {

    override fun visitXmlTag(tag: XmlTag) {
      val isTemplateTag = tag.children.any { it is XmlAttribute && it.name.startsWith("*") }
                          || isTemplateTag(tag)
      if (isTemplateTag) {
        pushScope(tag)
      }
      super.visitXmlTag(tag)
      if (isTemplateTag) {
        popScope()
      }
    }

    override fun visitXmlAttribute(attribute: XmlAttribute) {
      val parent = attribute.parent
                   ?: return
      val info = Angular2AttributeNameParser.parse(attribute.name, parent)
      when (info.type) {
        REFERENCE -> addReference(attribute, info, isTemplateTag(parent))
        LET -> addVariable(attribute, info)
        TEMPLATE_BINDINGS -> addTemplateBindings(attribute)
        else -> {}
      }
    }

    fun addReference(
      attribute: XmlAttribute,
      info: Angular2AttributeNameParser.AttributeInfo,
      isTemplateTag: Boolean,
    ) {
      val `var` = createVariable(info.name, attribute)
      if (isTemplateTag) {
        // References on ng-template are visible within parent scope
        prevScope().add(`var`)
      }
      else {
        currentScope().add(`var`)
      }
    }

    fun addVariable(attribute: XmlAttribute, info: Angular2AttributeNameParser.AttributeInfo) {
      addElement(createVariable(info.name, attribute))
    }

    fun addTemplateBindings(attribute: XmlAttribute) {
      val bindings = Angular2InjectionUtils.findInjectedAngularExpression(attribute, Angular2TemplateBindings::class.java)
                     ?: return
      for (b in bindings.bindings) {
        if (b.keyIsVar()) {
          b.variableDefinition?.let { addElement(it) }
        }
      }
    }

    private fun createVariable(
      name: String,
      contributor: PsiElement,
    ): JSImplicitElement {
      return JSImplicitElementImpl.Builder(name, contributor)
        .setType(JSImplicitElement.Type.Variable)
        .setProperties(JSImplicitElement.Property.Constant)
        .toImplicitElement()
    }
  }
}

@NonNls
private const val LEGACY_TEMPLATE_TAG = "template"

fun isTemplateTag(tag: XmlTag?): Boolean {
  return tag != null && isTemplateTag(tag.localName)
}

fun isTemplateTag(tagName: String?): Boolean {
  return ELEMENT_NG_TEMPLATE.equals(tagName!!, ignoreCase = true) || LEGACY_TEMPLATE_TAG.equals(tagName, ignoreCase = true)
}

fun getTemplateElementsScopeFor(element: PsiElement): Angular2TemplateScope? =
  Angular2TemplateScopesResolver
    .getScopes(element, listOf(Angular2TemplateElementsScopeProvider()))
    .firstOrNull()