// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolNameConversionRules
import com.intellij.webSymbols.query.WebSymbolNameConversionRulesProvider
import com.intellij.webSymbols.query.WebSymbolNameConverter
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.isScriptSetupLocalDirectiveName
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.web.scopes.*

class VueWebSymbolsQueryConfigurator : WebSymbolsQueryConfigurator {

  companion object {
    const val KIND_VUE_TOP_LEVEL_ELEMENTS = "vue-file-top-elements"
    const val KIND_VUE_COMPONENTS = "vue-components"
    const val KIND_VUE_COMPONENT_PROPS = "props"
    const val KIND_VUE_COMPONENT_COMPUTED_PROPERTIES = "computed-properties"
    const val KIND_VUE_COMPONENT_DATA_PROPERTIES = "data-properties"
    const val KIND_VUE_DIRECTIVES = "vue-directives"
    const val KIND_VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES = "vue-script-setup-local-directives"
    const val KIND_VUE_AVAILABLE_SLOTS = "vue-available-slots"
    const val KIND_VUE_MODEL = "vue-model"
    const val KIND_VUE_DIRECTIVE_ARGUMENT = "argument"
    const val KIND_VUE_DIRECTIVE_MODIFIERS = "modifiers"
    const val KIND_VUE_COMPONENT_NAMESPACES = "vue-component-namespaces"
    const val KIND_VUE_PROVIDES = "vue-provides"

    const val PROP_VUE_MODEL_PROP = "prop"
    const val PROP_VUE_MODEL_EVENT = "event"

    const val PROP_VUE_PROXIMITY = "x-vue-proximity"
    const val PROP_VUE_COMPOSITION_COMPONENT = "x-vue-composition-component"

  }

  override fun getScope(project: Project,
                        element: PsiElement?,
                        context: WebSymbolsContext,
                        allowResolve: Boolean): List<WebSymbolsScope> {
    if (context.framework != VueFramework.ID || element == null) return emptyList()

    if (element is JSElement && element !is XmlElement)
      return getScopeForJSElement(element, allowResolve)

    val result = SmartList<WebSymbolsScope>()
    val tag = (element as? XmlAttribute)?.parent ?: element as? XmlTag
    val fileContext = element.containingFile?.originalFile ?: return emptyList()

    if (allowResolve) {
      addEntityContainers(element, fileContext, result)
      tag?.let { result.add(VueAvailableSlotsScope(it)) }
      findModule(tag, true)?.let {
        result.add(VueScriptSetupNamespacedComponentsScope(it))
      }
    }

    // Top level tags
    if (tag != null && tag.parentTag == null && fileContext.isDotVueFile) {
      result.add(VueTopLevelElementsScope)
    }

    return result
  }

  override fun getNameConversionRulesProviders(project: Project,
                                               element: PsiElement?,
                                               context: WebSymbolsContext): List<WebSymbolNameConversionRulesProvider> =
    if (context.framework == VueFramework.ID)
      listOf(VueScriptSetupLocalDirectiveNameConversionRulesProvider)
    else
      super.getNameConversionRulesProviders(project, element, context)

  private fun getScopeForJSElement(element: JSElement, allowResolve: Boolean): List<WebSymbolsScope> {
    if (element is JSObjectLiteralExpression
        && allowResolve
        && element.context?.asSafely<JSProperty>()?.name == WATCH_PROP) {
      val enclosingComponent = VueModelManager.findEnclosingComponent(element) as? VueSourceComponent
                               ?: return emptyList()
      return listOf(VueWatchSymbolsScope(enclosingComponent))
    }

    if (allowResolve && (
        isInjectedAsArrayLiteral(element) ||
        isInjectedAsAlias(element) ||
        isInjectedAsProperty(element) ||
        isInjectedAsMacroCall(element))) {
      val enclosingComponent = VueModelManager.findEnclosingComponent(element) as? VueSourceComponent
                               ?: return emptyList()
      return listOf(VueInjectSymbolsScope(enclosingComponent))
    }

    return emptyList()
  }

  private fun isInjectedAsArrayLiteral(element: JSElement) =
    element is JSLiteralExpression &&
    element.context is JSArrayLiteralExpression &&
    element.context?.context?.asSafely<JSProperty>()?.name == INJECT_PROP

  private fun isInjectedAsProperty(element: JSElement) =
    element is JSObjectLiteralExpression &&
    element.context?.asSafely<JSProperty>()?.name == INJECT_PROP

  private fun isInjectedAsAlias(element: JSElement): Boolean {
    if (element !is JSLiteralExpression) return false
    val alias = element.context?.asSafely<JSProperty>()?.takeIf { it.name == INJECT_FROM } ?: return false
    val inject = alias.context?.context?.asSafely<JSProperty>() ?: return false
    return inject.context?.context?.asSafely<JSProperty>()?.name == INJECT_PROP
  }

  private fun isInjectedAsMacroCall(element: JSElement): Boolean =
    element.asSafely<JSLiteralExpression>()?.let { JSPsiImplUtils.isArgumentOfCallWithName(it, 0, INJECT_PROP) } ?: false

  private fun addEntityContainers(element: PsiElement,
                                  fileContext: PsiFile,
                                  result: SmartList<WebSymbolsScope>) {
    VueModelManager.findEnclosingContainer(element).let { enclosingContainer ->
      val containerToProximity = mutableMapOf<VueEntitiesContainer, VueModelVisitor.Proximity>()

      containerToProximity[enclosingContainer] = VueModelVisitor.Proximity.LOCAL

      enclosingContainer.parents.forEach { parent ->
        when (parent) {
          is VueApp -> containerToProximity[parent] = VueModelVisitor.Proximity.APP
          is VuePlugin -> containerToProximity[parent] = VueModelVisitor.Proximity.PLUGIN
        }
      }

      enclosingContainer.global?.let { global ->
        val apps = containerToProximity.keys.filterIsInstance<VueApp>()
        global.plugins.forEach { plugin ->
          containerToProximity.computeIfAbsent(plugin) {
            apps.maxOfOrNull { it.getProximity(plugin) } ?: plugin.defaultProximity
          }
        }
        containerToProximity[global] = VueModelVisitor.Proximity.GLOBAL
      }

      if (enclosingContainer is VueSourceComponent && containerToProximity.keys.none { it is VueApp }) {
        enclosingContainer.global?.apps?.filterIsInstance<VueCompositionApp>()?.forEach {
          containerToProximity[it] = VueModelVisitor.Proximity.OUT_OF_SCOPE
        }
      }

      containerToProximity.forEach { (container, proximity) ->
        VueCodeModelSymbolsScope.create(container, proximity)
          ?.let {
            if (container == enclosingContainer || container is VueGlobal) {
              VueIncorrectlySelfReferredComponentFilteringScope(it, fileContext)
            }
            else it
          }
          ?.let {
            result.add(it)
          }
      }
    }
  }

  object VueScriptSetupLocalDirectiveNameConversionRulesProvider : WebSymbolNameConversionRulesProvider, WebSymbolNameConversionRules {
    override fun getNameConversionRules(): WebSymbolNameConversionRules = this

    override fun createPointer(): Pointer<out WebSymbolNameConversionRulesProvider> =
      Pointer.hardPointer(this)

    override fun getModificationCount(): Long = 0

    override val canonicalNames: Map<WebSymbolQualifiedKind, WebSymbolNameConverter> =
      mapOf(WebSymbolQualifiedKind(NAMESPACE_HTML, KIND_VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES) to
              WebSymbolNameConverter {
                listOf(
                  if (isScriptSetupLocalDirectiveName(it))
                    fromAsset(it.substring(1))
                  else
                    it
                )
              })

    override val matchNames: Map<WebSymbolQualifiedKind, WebSymbolNameConverter>
      get() = canonicalNames

    override val nameVariants: Map<WebSymbolQualifiedKind, WebSymbolNameConverter>
      get() = canonicalNames

  }

}
