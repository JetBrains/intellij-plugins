// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.javascript.nodejs.monorepo.JSMonorepoManager
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolNameConversionRules
import com.intellij.webSymbols.query.WebSymbolNameConversionRulesProvider
import com.intellij.webSymbols.query.WebSymbolNameConverter
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.isScriptSetupLocalDirectiveName
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.web.scopes.*

val VUE_TOP_LEVEL_ELEMENTS = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-file-top-elements")
val VUE_COMPONENTS = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-components")
val VUE_COMPONENT_PROPS = WebSymbolQualifiedKind(NAMESPACE_HTML, "props")
val VUE_COMPONENT_COMPUTED_PROPERTIES = WebSymbolQualifiedKind(NAMESPACE_HTML, "computed-properties")
val VUE_COMPONENT_DATA_PROPERTIES = WebSymbolQualifiedKind(NAMESPACE_HTML, "data-properties")
val VUE_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-directives")
val VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-script-setup-local-directives")
val VUE_AVAILABLE_SLOTS = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-available-slots")
val VUE_MODEL = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-model")
val VUE_DIRECTIVE_ARGUMENT = WebSymbolQualifiedKind(NAMESPACE_HTML, "argument")
val VUE_DIRECTIVE_MODIFIERS = WebSymbolQualifiedKind(NAMESPACE_HTML, "modifiers")
val VUE_COMPONENT_NAMESPACES = WebSymbolQualifiedKind(NAMESPACE_JS, "vue-component-namespaces")
val VUE_PROVIDES = WebSymbolQualifiedKind(NAMESPACE_JS, "vue-provides")
val VUE_SPECIAL_PROPERTIES = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-special-properties")
val VUE_BINDING_SHORTHANDS = WebSymbolQualifiedKind(NAMESPACE_HTML, "vue-binding-shorthands")

const val PROP_VUE_MODEL_PROP = "prop"
const val PROP_VUE_MODEL_EVENT = "event"

const val PROP_VUE_PROXIMITY = "x-vue-proximity"
const val PROP_VUE_COMPOSITION_COMPONENT = "x-vue-composition-component"

class VueWebSymbolsQueryConfigurator : WebSymbolsQueryConfigurator {

  override fun getScope(project: Project,
                        location: PsiElement?,
                        context: WebSymbolsContext,
                        allowResolve: Boolean): List<WebSymbolsScope> {
    if (context.framework != VueFramework.ID || location == null) return emptyList()

    if (location is JSElement && location !is XmlElement)
      return getScopeForJSElement(location, allowResolve)

    val result = SmartList<WebSymbolsScope>()
    val attribute = (location as? XmlAttributeValue)?.parent as? XmlAttribute ?: location as? XmlAttribute
    val tag = attribute?.parent ?: location as? XmlTag
    val fileContext = location.containingFile?.originalFile ?: return emptyList()

    if (allowResolve) {
      withTypeEvaluationLocation(fileContext) { addEntityContainers(location, fileContext, result) }
      tag?.let { result.add(VueAvailableSlotsScope(it)) }
      tag?.takeIf { it.name == SLOT_TAG_NAME }?.let { result.add(VueSlotElementScope(it)) }
      attribute?.takeIf { it.valueElement == null }?.let { result.add(VueBindingShorthandScope(it)) }
      findModule(tag, true)?.let {
        result.add(VueScriptSetupNamespacedComponentsScope(it))
      }
    }

    // Top level tags
    if (tag != null && tag.parentTag == null && fileContext.isVueFile) {
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
    (element is JSLiteralExpression || element is JSReferenceExpression && !element.hasQualifier()) &&
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
    element.asSafely<JSExpression>()
      ?.takeIf { it is JSLiteralExpression || it is JSReferenceExpression && !it.hasQualifier() }
      ?.let { JSPsiImplUtils.isArgumentOfCallWithName(it, 0, INJECT_PROP) }
    ?: false

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

      JSMonorepoManager.getInstance(element.project).getRelatedProjects(element).asSequence()
        .mapNotNull { it.findPsiFile(element.project) }
        .mapNotNull { VueModule.get(it) }
        .forEach { containerToProximity[it] = VueModelVisitor.Proximity.OUT_OF_SCOPE }

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
      mapOf(VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES to
              WebSymbolNameConverter {
                listOf(
                  if (isScriptSetupLocalDirectiveName(it))
                    fromAsset(it.substring(1))
                  else
                    it
                )
              })

    override val renames: Map<WebSymbolQualifiedKind, WebSymbolNameConverter>
      get() = canonicalNames

    override val matchNames: Map<WebSymbolQualifiedKind, WebSymbolNameConverter>
      get() = canonicalNames

    override val completionVariants: Map<WebSymbolQualifiedKind, WebSymbolNameConverter>
      get() = canonicalNames

  }

}