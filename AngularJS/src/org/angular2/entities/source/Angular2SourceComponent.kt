// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.util.AstLoadingFilter
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.STYLES_PROP
import org.angular2.Angular2DecoratorUtil.STYLE_URLS_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.Angular2DecoratorUtil.getProperty
import org.angular2.Angular2InjectionUtils
import org.angular2.entities.*
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider

class Angular2SourceComponent(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceDirective(decorator, implicitElement), Angular2Component {

  private var moduleResolverField: Angular2ModuleResolver<ES6Decorator>? = null
  private val moduleResolver: Angular2ModuleResolver<ES6Decorator>
    get() = moduleResolverField
            ?: Angular2ModuleResolver({ decorator }, Angular2SourceModule.symbolCollector)
              .also { moduleResolverField = it }

  override val imports: Set<Angular2Entity>
    get() = if (isStandalone)
      moduleResolver.imports
    else
      emptySet()

  override val isScopeFullyResolved: Boolean
    get() = if (isStandalone)
      moduleResolver.isScopeFullyResolved
    else
      true

  override val templateFile: PsiFile?
    get() = getCachedValue {
      create(findAngularComponentTemplate(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, decorator)
    }

  override val cssFiles: List<PsiFile>
    get() = getCachedValue { create(findCssFiles(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, decorator) }

  override val ngContentSelectors: List<Angular2DirectiveSelector>
    get() = getCachedValue {
      val template = templateFile
      if (template is PsiFileImpl) {
        val result = SmartList<Angular2DirectiveSelector>()
        val root = template.greenStub
        if (root != null) {
          for (el in root.childrenStubs) {
            if (el.stubType === NG_CONTENT_SELECTOR) {
              result.add((el.psi as Angular2HtmlNgContentSelector).selector)
            }
          }
        }
        else {
          template.accept(object : Angular2HtmlRecursiveElementWalkingVisitor() {
            override fun visitNgContentSelector(ngContentSelector: Angular2HtmlNgContentSelector) {
              result.add(ngContentSelector.selector)
            }
          })
        }
        return@getCachedValue create<List<Angular2DirectiveSelector>>(result, template, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                                                                      decorator)
      }
      create(emptyList(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, decorator)
    }

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override fun createPointer(): Pointer<out Angular2Component> {
    return createPointer { decorator, implicitElement ->
      Angular2SourceComponent(decorator, implicitElement)
    }
  }

  private fun getDecoratorProperty(name: String): JSProperty? {
    return getProperty(decorator, name)
  }

  private fun findAngularComponentTemplate(): PsiFile? {
    val file = getReferencedFile(getDecoratorProperty(TEMPLATE_URL_PROP), true)
    return file ?: getReferencedFile(getDecoratorProperty(TEMPLATE_PROP), false)
  }

  private fun findCssFiles(): List<PsiFile> {
    return findCssFiles(getDecoratorProperty(STYLE_URLS_PROP), true)
      .plus(findCssFiles(getDecoratorProperty(STYLES_PROP), false))
      .toList()
  }

  private fun findCssFiles(property: JSProperty?, directRefs: Boolean): Sequence<PsiFile> {
    if (property == null) {
      return emptySequence()
    }
    // TODO need stubbed references
    if (directRefs) { // styles property can contain references to CSS files imported through import statements
      val stub = (property as JSPropertyImpl).stub
      if (stub != null) {
        return stub.childrenStubs
          .asSequence()
          .map { it.psi }
          .filterIsInstance<JSExpression>()
          .mapNotNull { getReferencedFileFromStub(it, directRefs) }
      }
    }
    return AstLoadingFilter.forceAllowTreeLoading<Sequence<PsiFile>, RuntimeException>(property.containingFile) {
      property.value
        .asSafely<JSArrayLiteralExpression>()
        ?.expressions
        ?.asSequence()
        ?.mapNotNull { getReferencedFileFromPsi(it, directRefs) }
      ?: emptySequence()
    }
  }

  @StubSafe
  private fun getReferencedFile(property: JSProperty?, directRefs: Boolean): PsiFile? {
    if (property == null) {
      return null
    }
    // TODO need stubbed references
    if (directRefs) { // template property can contain references to HTML files imported through import statements
      val stub = (property as JSPropertyImpl).stub
      if (stub != null) {
        return getReferencedFileFromStub(stub.childrenStubs.firstNotNullOfOrNull { it.psi as? JSExpression }, directRefs)
      }
    }
    return AstLoadingFilter.forceAllowTreeLoading<PsiFile, RuntimeException>(property.containingFile) {
      getReferencedFileFromPsi(property.value, directRefs)
    }
  }

  @StubSafe
  private fun getReferencedFileFromStub(stubbedExpression: JSExpression?, directRefs: Boolean): PsiFile? {
    val literal = if (!directRefs && stubbedExpression is JSCallExpression && stubbedExpression.isRequireCall) {
      JSStubBasedPsiTreeUtil.findRequireCallArgument((stubbedExpression as JSCallExpression?)!!)
    }
                  else {
      stubbedExpression
    } as? JSLiteralExpression ?: return null

    val url = literal.significantValue ?: return null
    val fakeUrlElement = FakeStringLiteral(literal, url)
    for (ref in AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet(fakeUrlElement).allReferences) {
      ref.resolve()
        .asSafely<PsiFile>()
        ?.let { return it }
    }
    return null
  }

  @StubUnsafe
  private fun getReferencedFileFromPsi(expression: JSExpression?, directRefs: Boolean): PsiFile? {
    expression ?: return null
    if (!directRefs) {
      Angular2InjectionUtils.getFirstInjectedFile(expression)
        ?.let { return it }
    }
    val expressionToCheck: JSExpression
    val actualDirectRefs: Boolean
    if (expression is JSCallExpression) {
      val args = expression.arguments
      if (args.size == 1) {
        expressionToCheck = args[0]
        actualDirectRefs = true
      }
      else {
        return null
      }
    }
    else {
      expressionToCheck = expression
      actualDirectRefs = directRefs
    }
    for (ref in expressionToCheck.references) {
      val el = ref.resolve()
      if (actualDirectRefs) {
        if (el is PsiFile) {
          return el
        }
      }
      else if (el is ES6ImportedBinding) {
        for (importedElement in el.findReferencedElements()) {
          if (importedElement is PsiFile) {
            return importedElement
          }
        }
      }
    }
    return null
  }

  private class FakeStringLiteral(private val myParent: PsiElement, value: String) : FakePsiElement() {
    private val myValue: String = StringUtil.unquoteString(value)
    override fun getParent(): PsiElement = myParent
    override fun getText(): String = myValue
    override fun getTextLength(): Int = myValue.length

    override fun getStartOffsetInParent(): Int {
      throw IllegalStateException()
    }
  }
}
