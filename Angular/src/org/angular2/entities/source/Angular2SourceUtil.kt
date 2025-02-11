package org.angular2.entities.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.types.JSBooleanLiteralTypeImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeChildren
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.AstLoadingFilter
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.isHostBindingExpression
import org.angular2.Angular2InjectionUtils
import org.angular2.codeInsight.refs.Angular2TemplateReferencesProvider
import org.angular2.entities.*
import org.angular2.index.resolveComponentsFromIndex
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import java.util.*
import java.util.function.BiPredicate

internal object Angular2SourceUtil {
  @JvmStatic
  fun getNgContentSelectors(template: PsiFile?): List<Angular2DirectiveSelector> =
    if (template is PsiFileImpl) {
      val result = SmartList<Angular2DirectiveSelector>()
      template.withGreenStubOrAst(
        { stub ->
          for (el in stub.childrenStubs) {
            if (el.stubType === Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR) {
              result.add((el.psi as Angular2HtmlNgContentSelector).selector)
            }
          }
        }, {
          template.accept(object : Angular2HtmlRecursiveElementWalkingVisitor() {
            override fun visitNgContentSelector(ngContentSelector: Angular2HtmlNgContentSelector) {
              result.add(ngContentSelector.selector)
            }
          })
        }
      )
      result
    }
    else
      emptyList()

  @JvmStatic
  fun getComponentSelector(propertyOwner: PsiElement, property: JSProperty?): Angular2DirectiveSelector {
    var value: String? = null
    if (property != null) {
      val initializer: JSLiteralExpression?
      val stub = (property as JSPropertyImpl).stub
      if (stub != null) {
        initializer = stub.childrenStubs.firstNotNullOfOrNull { it.psi as? JSLiteralExpression }
        value = initializer?.significantValue
          ?.let { JSStringUtil.unquoteWithoutUnescapingStringLiteralValue(it) }
      }
      else {
        initializer = property.value as? JSLiteralExpression
        value = initializer?.stringValue
      }
      if (value != null && initializer != null) {
        return Angular2DirectiveSelectorImpl(initializer, StringUtil.unquoteString(value), 1)
      }
      value = AstLoadingFilter.forceAllowTreeLoading<String, RuntimeException>(property.containingFile) {
        Angular2DecoratorUtil.getExpressionStringValue(property.value)
      }
    }
    return Angular2DirectiveSelectorImpl(propertyOwner, value, null)
  }

  @JvmStatic
  fun getExportAs(directive: Angular2Directive, property: JSProperty?): Map<String, Angular2DirectiveExportAs> {
    val propertyValue = property?.value
    return if (propertyValue is JSLiteralExpression && propertyValue.isQuotedLiteral) {
      val text = propertyValue.stringValue ?: return emptyMap()
      val split = text.split(',')
      var offset = 1
      val result = mutableMapOf<String, Angular2DirectiveExportAs>()
      split.forEach { name ->
        val startOffset = StringUtil.skipWhitespaceForward(name, 0)
        val endOffset = StringUtil.skipWhitespaceBackward(name, name.length)
        val trimmedName = name.substring(startOffset, endOffset)
        result[trimmedName] = Angular2DirectiveExportAs(
          trimmedName, directive, propertyValue, TextRange(offset + startOffset, offset + endOffset))
        offset += name.length + 1
      }
      result.toMap()
    }
    else {
      val exportAsString = Angular2DecoratorUtil.getExpressionStringValue(propertyValue)
      if (exportAsString == null)
        emptyMap()
      else StringUtil.split(exportAsString, ",").map { it.trim() }.associateWith {
        Angular2DirectiveExportAs(it, directive)
      }
    }
  }

  @JvmStatic
  fun findCssFiles(property: JSProperty?, directRefs: Boolean): Sequence<PsiFile> {
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
  @JvmStatic
  fun getReferencedFile(property: JSProperty?, directRefs: Boolean): PsiFile? {
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

  @JvmStatic
  fun readDirectivePropertyMappings(jsProperty: JSProperty?): MutableMap<String, Angular2PropertyInfo> {
    if (jsProperty == null) return LinkedHashMap()

    val items = (jsProperty as JSPropertyImpl).stub?.childrenStubs?.asSequence()?.mapNotNull { it.psi }
                ?: jsProperty.value.asSafely<JSArrayLiteralExpression>()?.expressions?.asSequence()
                ?: emptySequence()

    return items
      .mapNotNull {
        when (val expr = it) {
          is JSLiteralExpression ->
            Angular2EntityUtils.parsePropertyMapping(expr.stubSafeStringValue ?: return@mapNotNull null, expr)
          is JSObjectLiteralExpression -> {
            val name = expr.findProperty(Angular2DecoratorUtil.NAME_PROP)?.literalExpressionInitializer?.stubSafeStringValue
                       ?: return@mapNotNull null
            Pair(name, parseInputObjectLiteral(expr, name))
          }
          else -> null
        }
      }
      .filter { it.second.name.isNotBlank() }
      .toMap(LinkedHashMap())
  }

  @JvmStatic
  @Deprecated(message = "Use createPropertyInfo overload with function names list",
              replaceWith = ReplaceWith("createPropertyInfo(call, listOfNotNull(functionName), defaultName, getFunctionNameFromIndex)",
                                        "org.angular2.entities.source.Angular2SourceUtil.createPropertyInfo"))
  fun createPropertyInfo(
    call: JSCallExpression, functionName: String?, defaultName: String,
    getFunctionNameFromIndex: (JSCallExpression) -> String?,
  ): Angular2PropertyInfo? =
    createPropertyInfo(call, listOfNotNull(functionName), defaultName, getFunctionNameFromIndex)

  @JvmStatic
  fun createPropertyInfo(
    call: JSCallExpression, functionNames: List<String>, defaultName: String,
    getFunctionNameFromIndex: (JSCallExpression) -> String?,
  ): Angular2PropertyInfo? {
    if (functionNames.isEmpty()) return null
    val referenceNames = getFunctionNameFromIndex(call)
                           ?.split('.')
                           ?.takeIf { qname -> functionNames.contains(qname.getOrNull(0)) }
                         ?: return null
    return when (referenceNames.size) {
      1 -> {
        call.stubSafeCallArguments.lastOrNull().asSafely<JSObjectLiteralExpression>()
          ?.let { parseInputObjectLiteral(it, defaultName) }
          ?.copy(required = false)
        ?: Angular2PropertyInfo(defaultName, false, call, declaringElement = null)
      }
      2 -> {
        if (referenceNames[1] == Angular2DecoratorUtil.REQUIRED_PROP) {
          call.stubSafeCallArguments.lastOrNull().asSafely<JSObjectLiteralExpression>()
            ?.let { parseInputObjectLiteral(it, defaultName) }
            ?.copy(required = true)
          ?: Angular2PropertyInfo(defaultName, true, call, declaringElement = null)
        }
        else null
      }
      else -> null
    }
  }

  @JvmStatic
  fun parseInputObjectLiteral(expr: JSObjectLiteralExpression, name: String): Angular2PropertyInfo {
    val aliasLiteral = expr.findProperty(Angular2DecoratorUtil.ALIAS_PROP)?.literalExpressionInitializer
    val nameLiteral = expr.findProperty(Angular2DecoratorUtil.NAME_PROP)?.literalExpressionInitializer
    val alias = aliasLiteral?.stubSafeStringValue
    return Angular2PropertyInfo(
      alias ?: name,
      expr.findProperty(Angular2DecoratorUtil.REQUIRED_PROP)?.jsType?.asSafely<JSBooleanLiteralTypeImpl>()?.literal == true,
      expr,
      aliasLiteral,
      declarationRange = if (alias != null) TextRange(1, 1 + alias.length) else null,
      nameElement = nameLiteral
    )
  }

  @JvmStatic
  fun findComponentClass(templateContext: PsiElement): TypeScriptClass? {
    if (templateContext.language is Angular2Language
        && isHostBindingExpression(templateContext)) {
      return Angular2DecoratorUtil.getClassForDecoratorElement(
        InjectedLanguageManager.getInstance(templateContext.project).getInjectionHost(templateContext)
      )
    }
    return if (ApplicationManager.getApplication().let { it.isDispatchThread && !it.isUnitTestMode })
      WebJSResolveUtil.disableIndexUpToDateCheckIn(templateContext) {
        findComponentClasses(templateContext).firstOrNull()
      }
    else findComponentClasses(templateContext).firstOrNull()
  }

  @JvmStatic
  fun findComponentClasses(templateContext: PsiElement): List<TypeScriptClass> {
    val file = templateContext.containingFile
    if (file == null || !(file.language.isKindOf(Angular2HtmlLanguage)
                          || file.language.`is`(Angular2Language)
                          || isStylesheet(file))) {
      return Collections.emptyList()
    }
    val hostFile = getHostFile(templateContext) ?: return Collections.emptyList()
    if (file.originalFile != hostFile && DialectDetector.isTypeScript(hostFile)) {
      // inline content
      return listOfNotNull(Angular2DecoratorUtil.getClassForDecoratorElement(
        InjectedLanguageManager.getInstance(templateContext.project).getInjectionHost(file.originalFile))
      )
    }
    // external content
    val isStylesheet = isStylesheet(file)
    return CachedValuesManager.getCachedValue(hostFile) {
      val result = SmartList(Angular2FrameworkHandler.EP_NAME.extensionList.flatMap { h -> h.findAdditionalComponentClasses(hostFile) })
      if (result.isEmpty() || isStylesheet) {
        result.addAll(resolveComponentsFromSimilarFile(hostFile))
      }
      if (result.isEmpty() || isStylesheet) {
        result.addAll(resolveComponentsFromIndex(hostFile) { dec -> hasFileReference(dec, hostFile) })
      }
      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }
  }

  @JvmStatic
  fun findComponentClassesInFile(file: PsiFile, filter: BiPredicate<TypeScriptClass, ES6Decorator>?): List<TypeScriptClass> =
    file.stubSafeChildren.asSequence()
      .flatMap {
        if (it is ES6ExportDefaultAssignment)
          it.stubSafeChildren
        else
          listOf(it)
      }
      .filterIsInstance<TypeScriptClass>()
      .filter {
        val dec = Angular2DecoratorUtil.findDecorator(it, Angular2DecoratorUtil.COMPONENT_DEC)
        dec != null && (filter == null || filter.test(it, dec))
      }
      .toList()

  @JvmStatic
  fun isStylesheet(file: PsiFile): Boolean {
    return file is StylesheetFile
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
    for (ref in Angular2TemplateReferencesProvider.Angular2SoftFileReferenceSet(fakeUrlElement).allReferences) {
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
      val el = JSTypeEvaluationLocationProvider.withTypeEvaluationLocationForced(expressionToCheck) {
        ref.resolve()
      }
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

  private fun resolveComponentsFromSimilarFile(file: PsiFile): List<TypeScriptClass> {
    val name = file.viewProvider.virtualFile.nameWithoutExtension
    val dir = file.parent ?: return Collections.emptyList()
    for (ext in TypeScriptUtil.TYPESCRIPT_EXTENSIONS_WITHOUT_DECLARATIONS) {
      val directiveFile = dir.findFile(name + ext)

      if (directiveFile != null) {
        return findComponentClassesInFile(directiveFile) { _, dec -> hasFileReference(dec, file) }
      }
    }

    return Collections.emptyList()
  }

  private fun hasFileReference(componentDecorator: ES6Decorator?, file: PsiFile): Boolean {
    val component = Angular2EntitiesProvider.getComponent(componentDecorator)
    return if (component != null) {
      if (isStylesheet(file)) component.cssFiles.contains(file) else file == component.templateFile
    }
    else false
  }

  private fun getHostFile(context: PsiElement): PsiFile? {
    val original = CompletionUtil.getOriginalOrSelf(context)
    val hostFile = FileContextUtil.getContextFile(if (original !== context) original else context.containingFile.originalFile)
    return hostFile?.originalFile
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