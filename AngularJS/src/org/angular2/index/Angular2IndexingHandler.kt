// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.stubs.JSClassStub
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.TypeScriptClassStub
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.io.FileUtilRt.getNameWithoutExtension
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.util.Consumer
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.ContainerUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.ATTRIBUTE_DEC
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.NAME_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.STYLES_PROP
import org.angular2.Angular2DecoratorUtil.STYLE_URLS_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import org.angular2.Angular2DecoratorUtil.getProperty
import org.angular2.Angular2DecoratorUtil.getPropertyStringValue
import org.angular2.entities.Angular2ComponentLocator.isStylesheet
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.ivy.Angular2IvySymbolDef
import org.angular2.lang.Angular2Bundle
import org.angularjs.index.AngularIndexUtil
import org.angularjs.index.AngularSymbolIndex
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NonNls
import java.util.function.Predicate

class Angular2IndexingHandler : FrameworkIndexingHandler() {

  override fun shouldCreateStubForLiteral(node: ASTNode): Boolean {
    return checkIsInterestingPropertyValue(node.treeParent)
  }

  override fun shouldCreateStubForCallExpression(node: ASTNode): Boolean {
    val parent = node.treeParent
    if (parent != null && parent.elementType === JSStubElementTypes.ES6_DECORATOR) {
      val methodExpression = node.firstChildNode
      if (methodExpression.elementType !== JSElementTypes.REFERENCE_EXPRESSION) return false

      val referencedNameElement = methodExpression.firstChildNode ?: return false
      val decoratorName = referencedNameElement.text
      return STUBBED_DECORATORS_STRING_ARGS.contains(decoratorName)
    }
    return false
  }

  private fun checkIsInterestingPropertyValue(parent: ASTNode?): Boolean {
    if (parent == null) return false
    if (parent.elementType === JSElementTypes.ARGUMENT_LIST) {
      val grandParent = parent.treeParent
      return (grandParent != null
              && grandParent.elementType === JSStubElementTypes.CALL_EXPRESSION
              && shouldCreateStubForCallExpression(grandParent))
    }
    val property = if (parent.elementType === JSElementTypes.ARRAY_LITERAL_EXPRESSION) {
      parent.treeParent
    }
    else parent
    if (property != null && property.elementType === JSStubElementTypes.PROPERTY) {
      val identifier = JSPropertyImpl.findNameIdentifier(property)
      val propName = if (identifier != null) JSPsiImplUtils.getNameFromIdentifier(identifier) else null
      return propName != null && STUBBED_PROPERTIES.contains(propName)
    }
    return false
  }

  override fun hasSignificantValue(expression: JSLiteralExpression): Boolean {
    return shouldCreateStubForLiteral(expression.node)
  }

  override fun processDecorator(decorator: ES6Decorator, data: JSElementIndexingDataImpl?): JSElementIndexingDataImpl? {
    val enclosingClass = getClassForDecoratorElement(decorator)
                         ?: return data
    val decoratorName = decorator.decoratorName
    val isComponent = COMPONENT_DEC == decoratorName
    return when {
      PIPE_DEC == decoratorName -> {
        val notNullData = data ?: JSElementIndexingDataImpl()
        addPipe(enclosingClass, { notNullData.addImplicitElement(it) }, getPropertyStringValue(decorator, NAME_PROP))
        notNullData
      }
      isComponent || DIRECTIVE_DEC == decoratorName -> {
        val notNullData = data ?: JSElementIndexingDataImpl()
        val selector = getPropertyStringValue(decorator, SELECTOR_PROP)
        addDirective(enclosingClass, { notNullData.addImplicitElement(it) }, selector)
        if (isComponent) {
          addComponentExternalFilesRefs(decorator, "", { notNullData.addImplicitElement(it) },
                                        listOfNotNull(getTemplateFileUrl(decorator)))
          addComponentExternalFilesRefs(decorator, STYLESHEET_INDEX_PREFIX, { notNullData.addImplicitElement(it) },
                                        getStylesUrls(decorator))
        }
        notNullData
      }
      MODULE_DEC == decoratorName -> {
        val notNullData = data ?: JSElementIndexingDataImpl()
        addModule(enclosingClass) { notNullData.addImplicitElement(it) }
        notNullData
      }
      else -> data
    }
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    if (sink == null) {
      return false
    }
    val userID = element.userString
    val index = if (userID != null) INDEX_MAP[userID] else null
    if (index == Angular2SourceDirectiveIndex.KEY) {
      var type = element.toImplicitElement(null).userStringData
      if (type != null && type.startsWith(DIRECTIVE_TYPE)) {
        type = type.substring(DIRECTIVE_TYPE.length)
        StringUtil.split(type, "/")
          .forEach { name -> sink.occurrence(index, name) }
      }
      return true
    }
    else if (index != null) {
      sink.occurrence(index, element.name)
      if (index == Angular2SourcePipeIndex.KEY) {
        sink.occurrence(AngularSymbolIndex.KEY, element.name)
      }
      else {
        return true
      }
    }
    return false
  }

  override fun indexClassStub(jsClassStub: JSClassStub<*>, sink: IndexSink) {
    if (jsClassStub is TypeScriptClassStub) {
      val entityDef = Angular2IvySymbolDef.get(jsClassStub, false)
      if (entityDef != null) {
        if (entityDef is Angular2IvySymbolDef.Module) {
          sink.occurrence(Angular2IvyModuleIndex.KEY, NG_MODULE_INDEX_NAME)
        }
        else if (entityDef is Angular2IvySymbolDef.Pipe) {
          val name = entityDef.name
          if (name != null) {
            sink.occurrence(Angular2IvyPipeIndex.KEY, name)
            sink.occurrence(AngularSymbolIndex.KEY, name)
          }
        }
        else if (entityDef is Angular2IvySymbolDef.Directive) {
          val selector = entityDef.selector
          if (selector != null) {
            for (indexName in Angular2EntityUtils.getDirectiveIndexNames(selector.trim { it <= ' ' })) {
              sink.occurrence(Angular2IvyDirectiveIndex.KEY, indexName)
            }
          }
        }
      }
    }
  }

  private fun addComponentExternalFilesRefs(decorator: ES6Decorator,
                                            namePrefix: String,
                                            processor: Consumer<in JSImplicitElement>,
                                            fileUrls: List<String>) {
    for (fileUrl in fileUrls) {
      val lastSlash = fileUrl.lastIndexOf('/')
      val name = fileUrl.substring(lastSlash + 1)
      //don't index if file name matches TS file name and is in the same directory
      if ((lastSlash <= 0 || lastSlash == 1 && fileUrl[0] == '.')
          && getNameWithoutExtension(name) == getNameWithoutExtension(decorator.containingFile.originalFile.name)) {
        continue
      }
      val elementBuilder = JSImplicitElementImpl.Builder(namePrefix + name, decorator)
        .setUserString(this, ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING)
      processor.consume(elementBuilder.toImplicitElement())
    }
  }

  private fun addDirective(directiveClass: TypeScriptClass,
                           processor: Consumer<in JSImplicitElement>,
                           @NonNls selector: String?) {
    val indexNames: Set<String>
    if (selector == null) {
      indexNames = emptySet()
    }
    else {
      indexNames = Angular2EntityUtils.getDirectiveIndexNames(selector.trim { it <= ' ' })
    }
    val directive = JSImplicitElementImpl.Builder(directiveClass.name ?: selector ?: "<null>", directiveClass)
      .setType(JSImplicitElement.Type.Class)
      .setUserStringWithData(this, ANGULAR2_DIRECTIVE_INDEX_USER_STRING, DIRECTIVE_TYPE + StringUtil.join(indexNames, "/"))
      .toImplicitElement()
    processor.consume(directive)
  }

  private fun addPipe(pipeClass: TypeScriptClass,
                      processor: Consumer<in JSImplicitElement>,
                      @NonNls pipe: String?) {
    val pipeName = pipe ?: Angular2Bundle.message("angular.description.unnamed")
    val pipeElement = JSImplicitElementImpl.Builder(pipeName, pipeClass)
      .setUserStringWithData(this, ANGULAR2_PIPE_INDEX_USER_STRING, PIPE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement()
    processor.consume(pipeElement)
  }

  private fun addModule(moduleClass: TypeScriptClass, processor: Consumer<JSImplicitElement>) {
    val pipeElement = JSImplicitElementImpl.Builder(NG_MODULE_INDEX_NAME, moduleClass)
      .setUserStringWithData(this, ANGULAR2_MODULE_INDEX_USER_STRING, MODULE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement()
    processor.consume(pipeElement)
  }

  override fun computeJSImplicitElementUserStringKeys(): Set<String> {
    return INDEX_MAP.keys
  }

  companion object {

    const val REQUIRE = "require"

    @NonNls
    private val ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING = "a2tui"

    @NonNls
    private val ANGULAR2_PIPE_INDEX_USER_STRING = "a2pi"

    @NonNls
    private val ANGULAR2_DIRECTIVE_INDEX_USER_STRING = "a2di"

    @NonNls
    private val ANGULAR2_MODULE_INDEX_USER_STRING = "a2mi"

    @NonNls
    private val PIPE_TYPE = "P;;;"

    @NonNls
    private val DIRECTIVE_TYPE = "D;;;"

    @NonNls
    private val MODULE_TYPE = "M;;;"

    @NonNls
    const val NG_MODULE_INDEX_NAME = "ngModule"

    @NonNls
    private val STYLESHEET_INDEX_PREFIX = "ss/"

    @JvmField
    val TS_CLASS_TOKENS = TokenSet.create(JSStubElementTypes.TYPESCRIPT_CLASS,
                                          JSStubElementTypes.TYPESCRIPT_CLASS_EXPRESSION)

    private val STUBBED_PROPERTIES = ContainerUtil.newHashSet(
      TEMPLATE_URL_PROP, STYLE_URLS_PROP, SELECTOR_PROP, INPUTS_PROP, OUTPUTS_PROP)
    private val STUBBED_DECORATORS_STRING_ARGS = ContainerUtil.newHashSet(
      INPUT_DEC, OUTPUT_DEC, ATTRIBUTE_DEC)

    private val INDEX_MAP = HashMap<String, StubIndexKey<String, JSImplicitElementProvider>>()

    init {
      INDEX_MAP[ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING] = Angular2TemplateUrlIndex.KEY
      INDEX_MAP[ANGULAR2_DIRECTIVE_INDEX_USER_STRING] = Angular2SourceDirectiveIndex.KEY
      INDEX_MAP[ANGULAR2_PIPE_INDEX_USER_STRING] = Angular2SourcePipeIndex.KEY
      INDEX_MAP[ANGULAR2_MODULE_INDEX_USER_STRING] = Angular2SourceModuleIndex.KEY
    }

    @JvmStatic
    fun isPipe(element: JSImplicitElement): Boolean {
      return ANGULAR2_PIPE_INDEX_USER_STRING == element.userString
    }

    @JvmStatic
    fun isDirective(element: JSImplicitElement): Boolean {
      return ANGULAR2_DIRECTIVE_INDEX_USER_STRING == element.userString
    }

    @JvmStatic
    fun isModule(element: JSImplicitElement): Boolean {
      return ANGULAR2_MODULE_INDEX_USER_STRING == element.userString
    }

    @JvmStatic
    fun isDecoratorStringArgStubbed(decorator: ES6Decorator): Boolean {
      return STUBBED_DECORATORS_STRING_ARGS.contains(decorator.decoratorName)
    }

    @ApiStatus.Internal
    @JvmStatic
    fun resolveComponentsFromIndex(file: PsiFile, filter: Predicate<ES6Decorator>): List<TypeScriptClass> {
      val name = (if (isStylesheet(file)) STYLESHEET_INDEX_PREFIX else "") + file.viewProvider.virtualFile.name
      val result = SmartList<TypeScriptClass>()
      AngularIndexUtil.multiResolve(file.project, Angular2TemplateUrlIndex.KEY, name) { el ->
        if (el != null) {
          val componentDecorator = el.parent
          if (componentDecorator is ES6Decorator && filter.test(componentDecorator)) {
            ContainerUtil.addIfNotNull(result, getClassForDecoratorElement(componentDecorator))
          }
        }
        true
      }
      return result
    }

    private fun getTemplateFileUrl(decorator: ES6Decorator): String? {
      val templateUrl = getPropertyStringValue(decorator, TEMPLATE_URL_PROP)
      if (templateUrl != null) {
        return templateUrl
      }
      val property = getProperty(decorator, TEMPLATE_PROP)
      return if (property != null) {
        getExprReferencedFileUrl(property.value)
      }
      else null
    }

    private fun getStylesUrls(decorator: ES6Decorator): List<String> {
      val result = SmartList<String>()

      val urlsGetter = { name: String, func: (JSExpression) -> String? ->
        getProperty(decorator, name)
          ?.value
          ?.asSafely<JSArrayLiteralExpression>()
          ?.expressions
          ?.mapNotNullTo(result, func)
      }

      urlsGetter(STYLE_URLS_PROP) { Angular2DecoratorUtil.getExpressionStringValue(it) }
      urlsGetter(STYLES_PROP) { getExprReferencedFileUrl(it) }

      return result
    }

    @JvmStatic
    fun getExprReferencedFileUrl(expression: JSExpression?): String? {
      if (expression is JSReferenceExpression) {
        for (resolvedElement in AngularIndexUtil.resolveLocally(expression)) {
          if (resolvedElement is ES6ImportedBinding) {
            val from = resolvedElement.declaration?.fromClause
            if (from != null) {
              return from.referenceText?.let { StringUtil.unquoteString(it) }
            }
          }
        }
      }
      else if (expression is JSCallExpression) {
        val referenceExpression = expression.methodExpression as? JSReferenceExpression
        val arguments = expression.arguments
        if (arguments.size == 1
            && arguments[0] is JSLiteralExpression
            && (arguments[0] as JSLiteralExpression).isQuotedLiteral
            && referenceExpression != null
            && referenceExpression.qualifier == null
            && REQUIRE == referenceExpression.referenceName) {
          return (arguments[0] as JSLiteralExpression).stringValue
        }
      }
      return null
    }
  }
}
