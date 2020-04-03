// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ClassExpression
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSClassResolver
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.getHostFile
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.model.source.VueComponents.Companion.getComponentDescriptor

class VueModelManager {

  companion object {

    fun getGlobal(context: PsiElement): VueGlobal? {
      return VueGlobalImpl.get(context)
    }

    fun findEnclosingContainer(templateElement: PsiElement): VueEntitiesContainer? {
      return findComponent(templateElement) as? VueEntitiesContainer
             ?: findVueApp(templateElement)
             ?: getGlobal(templateElement)
    }

    fun findEnclosingComponent(jsElement: JSElement): VueComponent? {
      var context: PsiElement = PsiTreeUtil.getContextOfType(jsElement, false,
                                                             JSObjectLiteralExpression::class.java, JSClass::class.java)
                                ?: return null
      // Find the outermost JSObjectLiteralExpression or first enclosing class
      while (context is JSObjectLiteralExpression) {
        val superContext = PsiTreeUtil.getContextOfType(context, true,
                                                        JSObjectLiteralExpression::class.java, JSClass::class.java)
        if (superContext == null) {
          break
        }
        context = superContext
      }
      return getComponentDescriptor(context)?.let { it.obj ?: it.clazz }?.let { getComponent(it) }
    }

    private fun findComponent(templateElement: PsiElement): VueComponent? {
      val baseElement: PsiElement? =
        if (templateElement is JSElement && templateElement.containingFile is XmlFile) {
          PsiTreeUtil.getParentOfType(templateElement, XmlElement::class.java)
        }
        else {
          templateElement
        }

      return when (baseElement) {
        is XmlElement -> InjectedLanguageManager.getInstance(baseElement.project)
                           .getInjectionHost(baseElement)
                           ?.let { it as? JSLiteralExpression }
                           ?.let { CompletionUtil.getOriginalOrSelf(it) }
                           ?.let { it.parent as? JSProperty }
                         ?: baseElement
        is JSElement -> InjectedLanguageManager.getInstance(templateElement.project).getInjectionHost(templateElement) as? XmlElement
        else -> null
      }
        ?.let { getEnclosingComponentDescriptor(CompletionUtil.getOriginalOrSelf(it)) }
        ?.let { getComponent(it.obj ?: it.clazz!!) }
    }

    private fun getEnclosingComponentDescriptor(element: PsiElement): VueComponentDescriptor? {
      var context: PsiElement? = element
      if (context is JSCallExpression) {
        val stub = (context as? StubBasedPsiElement<*>)?.stub
        if (stub != null) {
          context = stub.getChildrenByType(JSStubElementTypes.OBJECT_LITERAL_EXPRESSION,
                                           JSObjectLiteralExpression.ARRAY_FACTORY)
            .firstOrNull()
        }
        else {
          context = context.argumentList
            ?.arguments
            ?.find { it is JSObjectLiteralExpression }
        }
      }
      else if (context is JSProperty) {
        context = context.context
      }

      if (context is JSObjectLiteralExpression) {
        val parentContext = PsiTreeUtil.getContextOfType(context, JSObjectLiteralExpression::class.java,
                                                         ES6Decorator::class.java)
        if (parentContext is ES6Decorator) {
          context = parentContext
        }
      }

      return when (context) {
        is JSObjectLiteralExpression -> VueComponentDescriptor(obj = context)
        is ES6Decorator -> {
          val clazz = when (val parentContext = context.context?.context) {
            is JSExportAssignment -> parentContext.stubSafeElement as? JSClass
            is JSClass -> parentContext
            else -> null
          }
          val obj = VueComponents.getDescriptorFromDecorator(context)
          if (clazz != null || obj != null)
            VueComponentDescriptor(obj = obj, clazz = clazz)
          else
            null
        }
        is JSClass -> {
          val decorator = VueComponents.getComponentDecorator(context)
          VueComponentDescriptor(
            obj = decorator?.let { VueComponents.getDescriptorFromDecorator(it) },
            clazz = context)
        }
        null -> null
        else -> getDescriptorFromVueModule(context)
                ?: getDescriptorFromReferencedScript(context)
                ?: findReferencingComponentDescriptor(context)
      }
    }

    private fun findReferencingComponentDescriptor(context: PsiElement): VueComponentDescriptor? {
      if (context !is XmlElement) return null

      PsiTreeUtil.findFirstParent(context) {
        (it as? XmlTag)?.let { tag ->
          tag.name == SCRIPT_TAG_NAME
          && tag.getAttribute("type")?.value == "text/x-template"
        } ?: false
      }?.let { scriptTag ->
        val id = (scriptTag as XmlTag).getAttribute("id")?.value ?: return null

        var result: VueComponentDescriptor? = null
        StubIndex.getInstance().processElements(VueIdIndex.KEY, id, context.project,
                                                GlobalSearchScope.projectScope(context.project),
                                                PsiElement::class.java) { element ->
          if ((element as? JSProperty)?.indexingData
              ?.implicitElements
              ?.any {
                it.userString == VueIdIndex.JS_KEY
                && it?.qualifiedName == id
                && it.isValid
              } == true) {
            result = getEnclosingComponentDescriptor(element)
          }
          true
        }
        return result
      }

      val file = getHostFile(context) ?: return null
      val name = file.viewProvider.virtualFile.name
      var result: VueComponentDescriptor? = null

      StubIndex.getInstance().processElements(VueUrlIndex.KEY, name, context.project,
                                              GlobalSearchScope.projectScope(context.project),
                                              PsiElement::class.java) { element ->
        if (element is JSImplicitElementProvider) {
          if (element.indexingData?.implicitElements
              ?.any {
                it.userString == VueUrlIndex.JS_KEY
                && it?.qualifiedName == name
                && it.isValid
              } == true) {
            val candidate = getEnclosingComponentDescriptor(element)
            if (candidate != null
                && VueSourceContainer.getTemplate(candidate)
                  ?.source
                  ?.containingFile == file) {
              result = candidate
            }
          }
        }
        else if (element is XmlAttribute
                 && element.parent?.name == HtmlUtil.TEMPLATE_TAG_NAME
                 && element.valueElement?.references
                   ?.any { it.resolve()?.containingFile == context.containingFile } == true) {
          result = getDescriptorFromVueModule(element)
                   ?: getDescriptorFromReferencedScript(element)
        }
        true
      }
      return result
    }

    private fun getDescriptorFromVueModule(element: PsiElement): VueComponentDescriptor? {
      return getDefaultExportedComponent(findModule(element))
    }

    private fun getDescriptorFromReferencedScript(element: PsiElement): VueComponentDescriptor? {
      val file = element as? XmlFile ?: element.containingFile as? XmlFile
      if (file != null && file.fileType == VueFileType.INSTANCE) {
        // TODO stub safe resolution
        return findTopLevelVueTag(file, SCRIPT_TAG_NAME)
          ?.let { getDefaultExportedComponent(resolveTagSrcReference(it) as? PsiFile) }
      }
      return null
    }

    private fun getDefaultExportedComponent(content: PsiElement?): VueComponentDescriptor? {
      return content
        ?.let {
          (ES6PsiUtil.findDefaultExport(it) as? JSExportAssignment)?.stubSafeElement
          ?: findDefaultCommonJSExport(it)
        }
        ?.let { defaultExport -> getComponentDescriptor(defaultExport) }
    }

    private fun findDefaultCommonJSExport(element: PsiElement): PsiElement? {
      return JSClassResolver.getInstance().findElementsByQNameIncludingImplicit(JSSymbolUtil.MODULE_EXPORTS, element.containingFile)
        .asSequence()
        .filterIsInstance<JSDefinitionExpression>()
        .mapNotNull { it.initializerOrStub }
        .firstOrNull()
    }

    private fun findVueApp(templateElement: PsiElement): VueApp? {
      val global = getGlobal(templateElement) ?: return null
      val xmlElement =
        if (templateElement is XmlElement) {
          templateElement
        }
        else {
          when (val context = PsiTreeUtil.getContextOfType(templateElement, PsiFile::class.java, XmlElement::class.java)) {
            is XmlElement -> context
            is PsiFile -> InjectedLanguageManager.getInstance(templateElement.project).getInjectionHost(context)
            else -> return null
          }
        } ?: return null

      var result: VueApp? = null
      PsiTreeUtil.findFirstParent(xmlElement, Condition {
        if (it is PsiFile) return@Condition true
        val idValue = (it as? XmlTag)?.getAttribute("id")?.valueElement?.value
                      ?: return@Condition false
        if (!StringUtil.isEmptyOrSpaces(idValue)) {
          val idReference = "#$idValue"
          global.apps.find { app -> idReference == app.element }?.let { app ->
            result = app
            return@Condition true
          }
        }
        false
      })
      return result
    }

    fun getComponent(element: PsiElement): VueComponent? {
      val context: PsiElement = getComponentImplicitElement(element)?.context ?: element
      if (!context.isValid)
        return null
      return CachedValuesManager.getCachedValue(context) {
        val implicitElement = getComponentImplicitElement(context)
        val data = implicitElement?.let { getVueIndexData(it) }
        val descriptor = getEnclosingComponentDescriptor(context)
        var declaration: PsiElement = descriptor?.obj ?: context
        if (declaration is JSImplicitElement) {
          declaration = declaration.context ?: declaration
        }
        if (declaration is JSProperty) {
          declaration = declaration.parent ?: declaration
        }
        else if (declaration is JSCallExpression) {
          data?.descriptorRef
            ?.let { VueComponents.resolveReferenceToVueComponent(context, it) }
            ?.obj
            ?.let { declaration = it }
        }
        CachedValueProvider.Result.create(VueSourceComponent(implicitElement ?: buildImplicitElement(context),
                                                             descriptor?.clazz,
                                                             declaration as? JSObjectLiteralExpression,
                                                             data),
                                          context, declaration)
      }
    }

    fun getMixin(mixin: JSObjectLiteralExpression): VueMixin {
      return getMixin(mixin as PsiElement)!!
    }

    fun getMixin(mixin: PsiElement): VueMixin? {
      val context = (getComponentDescriptor(mixin) ?: getEnclosingComponentDescriptor(mixin))
        ?.let { it.clazz ?: it.obj!! }
      return CachedValuesManager.getCachedValue(context ?: return null) {
        val descriptor = getEnclosingComponentDescriptor(context)
        val declaration: PsiElement = descriptor?.obj ?: context

        CachedValueProvider.Result.create(VueSourceMixin(getComponentImplicitElement(context)
                                                         ?: buildImplicitElement(context),
                                                         descriptor?.clazz,
                                                         descriptor?.obj),
                                          context, declaration)
      }
    }

    fun getFilter(it: JSImplicitElement): VueFilter? {
      if (it.userString == VueGlobalFiltersIndex.JS_KEY) {
        val call = it.context ?: return null
        var filterMethod: PsiElement = it
        val data = getVueIndexData(it)
        if (!data.nameRef.isNullOrBlank()) {
          resolveFilterReference(call, data.nameRef)
            ?.let { filterMethod = it }
        }
        else if (call is JSCallExpression) {
          val stub = (call as? StubBasedPsiElement<*>)?.stub
          if (stub != null) {
            stub.childrenStubs.asSequence()
              .filter { it.stubType !== JSStubElementTypes.LITERAL_EXPRESSION }
              .firstOrNull()
              ?.let { filterMethod = it.psi }
          }
          else {
            call.arguments.getOrNull(1)
              ?.let { filterMethod = it }
          }
        }
        return VueSourceFilter(toAsset(it.name), filterMethod)
      }
      return null
    }

    private fun resolveFilterReference(context: PsiElement, refName: String): PsiElement? {
      val localRes = JSStubBasedPsiTreeUtil.resolveLocally(refName, context)
                     ?: return null
      return JSStubBasedPsiTreeUtil.calculateMeaningfulElements(localRes)
               .firstOrNull()
             ?: localRes
    }

    fun getApp(appDeclaration: JSObjectLiteralExpression): VueApp {
      return CachedValuesManager.getCachedValue(appDeclaration) {
        CachedValueProvider.Result.create(VueSourceApp(getComponentImplicitElement(appDeclaration)
                                                       ?: buildImplicitElement(appDeclaration),
                                                       appDeclaration),
                                          appDeclaration)
      }
    }

    private fun getComponentImplicitElement(declaration: PsiElement): JSImplicitElement? {
      return if (declaration is JSImplicitElement && declaration.userString == VueComponentsIndex.JS_KEY)
        declaration
      else if (declaration is JSObjectLiteralExpression) {
        val implicitElement = declaration.findProperty("name")
                                ?.let { getComponentImplicitElement(it) as? JSImplicitElementImpl }
                              ?: declaration.firstProperty
                                ?.let { buildImplicitElement(it) }
        var parent = PsiTreeUtil.getContextOfType(declaration, ES6ClassExpression::class.java, JSExportAssignment::class.java)
        if (parent is JSExportAssignment) {
          parent = parent.stubSafeElement as? ES6ClassExpression
        }
        if (parent is ES6ClassExpression)
          (implicitElement?.toBuilder() ?: JSImplicitElementImpl.Builder(
            JavaScriptBundle.message("element.name.anonymous"), parent))
            .setProvider(parent)
            .forbidAstAccess()
            .toImplicitElement()
        else
          implicitElement
      }
      else if (declaration is ES6ClassExpression)
        buildImplicitElement(declaration, declaration.name ?: JavaScriptBundle.message("element.name.anonymous"))
      else
        (declaration as? JSImplicitElementProvider)?.indexingData?.implicitElements?.find {
          it.userString == VueComponentsIndex.JS_KEY
        }
    }

    private fun buildImplicitElement(parent: PsiElement, name: String = JavaScriptBundle.message("element.name.anonymous")) =
      JSImplicitElementImpl(JSImplicitElementImpl.Builder(name, parent).forbidAstAccess())

  }
}
