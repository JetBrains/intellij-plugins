// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.javascript.nodejs.monorepo.JSMonorepoManager
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.js.NAMESPACE_JS
import com.intellij.polySymbols.query.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.isScriptSetupLocalDirectiveName
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.web.scopes.*

val VUE_TOP_LEVEL_ELEMENTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-file-top-elements"]
val VUE_COMPONENTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-components"]
val VUE_COMPONENT_PROPS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "props"]
val VUE_COMPONENT_COMPUTED_PROPERTIES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "computed-properties"]
val VUE_COMPONENT_DATA_PROPERTIES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "data-properties"]
val VUE_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-directives"]
val VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-script-setup-local-directives"]
val VUE_AVAILABLE_SLOTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-available-slots"]
val VUE_MODEL: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-model"]
val VUE_DIRECTIVE_ARGUMENT: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "argument"]
val VUE_DIRECTIVE_MODIFIERS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "modifiers"]
val VUE_COMPONENT_NAMESPACES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "vue-component-namespaces"]
val VUE_PROVIDES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "vue-provides"]
val VUE_SPECIAL_PROPERTIES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-special-properties"]
val VUE_BINDING_SHORTHANDS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "vue-binding-shorthands"]

val PROP_VUE_MODEL_PROP: PolySymbolProperty<String> = PolySymbolProperty["prop"]
val PROP_VUE_MODEL_EVENT: PolySymbolProperty<String> = PolySymbolProperty["event"]

val PROP_VUE_PROXIMITY: PolySymbolProperty<VueModelVisitor.Proximity> = PolySymbolProperty["x-vue-proximity"]
val PROP_VUE_COMPOSITION_COMPONENT: PolySymbolProperty<Boolean> = PolySymbolProperty["x-vue-composition-component"]

class VueSymbolQueryConfigurator : PolySymbolQueryConfigurator {

  override fun getNameConversionRulesProviders(
    project: Project,
    element: PsiElement?,
    context: PolyContext,
  ): List<PolySymbolNameConversionRulesProvider> =
    if (context.framework == VueFramework.ID)
      listOf(VueScriptSetupLocalDirectiveNameConversionRulesProvider)
    else
      super.getNameConversionRulesProviders(project, element, context)

  object VueScriptSetupLocalDirectiveNameConversionRulesProvider : PolySymbolNameConversionRulesProvider, PolySymbolNameConversionRules {
    override fun getNameConversionRules(): PolySymbolNameConversionRules = this

    override fun createPointer(): Pointer<out PolySymbolNameConversionRulesProvider> =
      Pointer.hardPointer(this)

    override fun getModificationCount(): Long = 0

    override val canonicalNames: Map<PolySymbolQualifiedKind, PolySymbolNameConverter> =
      mapOf(VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES to
              PolySymbolNameConverter {
                listOf(
                  if (isScriptSetupLocalDirectiveName(it))
                    fromAsset(it.substring(1))
                  else
                    it
                )
              })

    override val renames: Map<PolySymbolQualifiedKind, PolySymbolNameConverter>
      get() = canonicalNames

    override val matchNames: Map<PolySymbolQualifiedKind, PolySymbolNameConverter>
      get() = canonicalNames

    override val completionVariants: Map<PolySymbolQualifiedKind, PolySymbolNameConverter>
      get() = canonicalNames

  }

}

class VueSymbolQueryScopeContributor : PolySymbolQueryScopeContributor {
  override fun registerProviders(registrar: PolySymbolQueryScopeProviderRegistrar) {
    registrar
      .inContext { it.framework == VueFramework.ID }
      .withResolveRequired()
      .apply {
        forPsiLocation(
          psiElement(JSElement::class.java)
            .andNot(psiElement(XmlElement::class.java))
        ).contributeScopeProvider(VueInjectScopeContributor)

        forPsiLocation(JSObjectLiteralExpression::class.java)
          .contributeScopeProvider(VueWatchSymbolScopeContributor)

        forPsiLocations(XmlAttributeValue::class.java, XmlAttribute::class.java, XmlTag::class.java, XmlText::class.java)
          .contributeScopeProvider(VueTemplateSymbolScopeContributor)
      }


    registrar
      .inContext { it.framework == VueFramework.ID }
      .forPsiLocation(HtmlTag::class.java)
      .contributeScopeProvider(VueTopLevelElementsScopeContributor)
  }

  private object VueTopLevelElementsScopeContributor : PolySymbolLocationQueryScopeProvider<HtmlTag> {
    override fun getScopes(location: HtmlTag): List<PolySymbolScope> {
      if (location.parentTag == null && location.containingFile?.originalFile?.isVueFile == true) {
        return listOf(VueTopLevelElementsScope)
      }
      return emptyList()
    }

  }

  private object VueWatchSymbolScopeContributor : PolySymbolLocationQueryScopeProvider<JSObjectLiteralExpression> {
    override fun getScopes(location: JSObjectLiteralExpression): List<PolySymbolScope> {
      if (location.context?.asSafely<JSProperty>()?.name == WATCH_PROP) {
        val enclosingComponent = VueModelManager.findEnclosingComponent(location) as? VueSourceComponent
                                 ?: return emptyList()
        return listOf(VueWatchSymbolScope(enclosingComponent))
      }
      else {
        return emptyList()
      }
    }
  }

  private object VueInjectScopeContributor : PolySymbolLocationQueryScopeProvider<JSElement> {
    override fun getScopes(location: JSElement): List<PolySymbolScope> {
      if (isInjectedAsArrayLiteral(location) ||
          isInjectedAsAlias(location) ||
          isInjectedAsProperty(location) ||
          isInjectedAsMacroCall(location)) {
        val enclosingComponent = VueModelManager.findEnclosingComponent(location) as? VueSourceComponent
                                 ?: return emptyList()
        return listOf(VueInjectSymbolScope(enclosingComponent))
      }
      else {
        return emptyList()
      }
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
  }

  private object VueTemplateSymbolScopeContributor : PolySymbolLocationQueryScopeProvider<XmlElement> {
    override fun getScopes(location: XmlElement): List<PolySymbolScope> {
      val result = SmartList<PolySymbolScope>()
      val attribute = (location as? XmlAttributeValue)?.parent as? XmlAttribute ?: location as? XmlAttribute
      val tag = attribute?.parent ?: location as? XmlTag
      val fileContext = location.containingFile?.originalFile ?: return emptyList()

      withTypeEvaluationLocation(fileContext) { addEntityContainers(location, fileContext, result) }
      tag?.let { result.add(VueAvailableSlotsScope(it)) }
      tag?.takeIf { it.name == SLOT_TAG_NAME }?.let { result.add(VueSlotElementScope(it)) }
      attribute?.takeIf { it.valueElement == null }?.let { result.add(VueBindingShorthandScope(it)) }
      findModule(tag, true)?.let {
        result.add(VueScriptSetupNamespacedComponentsScope(it))
      }
      return result
    }

    private fun addEntityContainers(
      element: PsiElement,
      fileContext: PsiFile,
      result: SmartList<PolySymbolScope>,
    ) {
      VueModelManager.findEnclosingContainer(element).let { enclosingContainer ->
        val containerToProximity = mutableMapOf<VueEntitiesContainer, VueModelVisitor.Proximity>()

        containerToProximity[enclosingContainer] = VueModelVisitor.Proximity.LOCAL

        enclosingContainer.parents.forEach { parent ->
          when (parent) {
            is VueApp -> containerToProximity[parent] = VueModelVisitor.Proximity.APP
            is VueLibrary -> containerToProximity[parent] = VueModelVisitor.Proximity.LIBRARY
          }
        }

        enclosingContainer.global?.let { global ->
          val apps = containerToProximity.keys.filterIsInstance<VueApp>()
          global.libraries.forEach { library ->
            containerToProximity.computeIfAbsent(library) {
              apps.maxOfOrNull { it.getProximity(library) } ?: library.defaultProximity
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
          VueCodeModelSymbolScope.create(container, proximity)
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
  }
}
