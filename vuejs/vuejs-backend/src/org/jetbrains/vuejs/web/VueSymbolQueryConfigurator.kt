// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.javascript.nodejs.monorepo.JSMonorepoManager
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.framework.framework
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.js.JS_KEYWORDS
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.NAMESPACE_JS
import com.intellij.polySymbols.query.PolySymbolLocationQueryScopeProvider
import com.intellij.polySymbols.query.PolySymbolNameConversionRules
import com.intellij.polySymbols.query.PolySymbolNameConversionRulesProvider
import com.intellij.polySymbols.query.PolySymbolNameConverter
import com.intellij.polySymbols.query.PolySymbolQueryConfigurator
import com.intellij.polySymbols.query.PolySymbolQueryExecutor
import com.intellij.polySymbols.query.PolySymbolQueryScopeContributor
import com.intellij.polySymbols.query.PolySymbolQueryScopeProviderRegistrar
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.PolySymbolIsolatedMappingScope
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.isGlobalDirectiveName
import org.jetbrains.vuejs.codeInsight.isScriptSetupLocalDirectiveName
import org.jetbrains.vuejs.codeInsight.template.VueTemplateSymbolScopesProvider
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.expr.isVueExprMetaLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.model.VueApp
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueGlobal
import org.jetbrains.vuejs.model.VueLibrary
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueModule
import org.jetbrains.vuejs.model.source.INJECT_FROM
import org.jetbrains.vuejs.model.source.INJECT_PROP
import org.jetbrains.vuejs.model.source.VueCompositionApp
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.model.source.WATCH_PROP
import org.jetbrains.vuejs.web.scopes.VueAvailableSlotsScope
import org.jetbrains.vuejs.web.scopes.VueBindingShorthandScope
import org.jetbrains.vuejs.web.scopes.VueCodeModelSymbolScope
import org.jetbrains.vuejs.web.scopes.VueIncorrectlySelfReferredComponentFilteringScope
import org.jetbrains.vuejs.web.scopes.VueInjectSymbolScope
import org.jetbrains.vuejs.web.scopes.VueScriptSetupNamespacedComponentsScope
import org.jetbrains.vuejs.web.scopes.VueSlotElementScope
import org.jetbrains.vuejs.web.scopes.VueTopLevelElementsScope
import org.jetbrains.vuejs.web.scopes.VueWatchSymbolScope

val VUE_TOP_LEVEL_ELEMENTS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-file-top-elements"]
val VUE_COMPONENTS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-components"]
val VUE_COMPONENT_PROPS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "props"]
val VUE_COMPONENT_COMPUTED_PROPERTIES: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "computed-properties"]
val VUE_COMPONENT_DATA_PROPERTIES: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "data-properties"]
val VUE_METHODS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-methods"]
val VUE_MODEL_DECL: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-model-decl"]
val VUE_FILTERS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-filters"]
val VUE_INJECTS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-injects"]
val VUE_DIRECTIVES: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-directives"]
val VUE_GLOBAL_DIRECTIVES: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-global-directives"]
val VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-script-setup-local-directives"]
val VUE_AVAILABLE_SLOTS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-available-slots"]
val VUE_MODEL: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-model"]
val VUE_DIRECTIVE_ARGUMENT: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "argument"]
val VUE_DIRECTIVE_MODIFIERS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "modifiers"]
val VUE_COMPONENT_NAMESPACES: PolySymbolKind = PolySymbolKind[NAMESPACE_JS, "vue-component-namespaces"]
val VUE_PROVIDES: PolySymbolKind = PolySymbolKind[NAMESPACE_JS, "vue-provides"]
val VUE_SPECIAL_PROPERTIES: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-special-properties"]
val VUE_BINDING_SHORTHANDS: PolySymbolKind = PolySymbolKind[NAMESPACE_HTML, "vue-binding-shorthands"]

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
      listOf(
        VueGlobalDirectiveNameConversionRulesProvider,
        VueScriptSetupLocalDirectiveNameConversionRulesProvider,
      )
    else
      super.getNameConversionRulesProviders(project, element, context)
}

private object VueGlobalDirectiveNameConversionRulesProvider :
  VueDirectiveNameConversionRulesProviderBase {

  override val canonicalNames: Map<PolySymbolKind, PolySymbolNameConverter> =
    mapOf(
      VUE_GLOBAL_DIRECTIVES to
        PolySymbolNameConverter {
          listOf(
            if (isGlobalDirectiveName(it))
              fromAsset(it.substring(1))
            else
              it
          )
        },
    )
}

private object VueScriptSetupLocalDirectiveNameConversionRulesProvider :
  VueDirectiveNameConversionRulesProviderBase {

  override val canonicalNames: Map<PolySymbolKind, PolySymbolNameConverter> =
    mapOf(
      VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES to
        PolySymbolNameConverter {
          listOf(
            if (isScriptSetupLocalDirectiveName(it))
              fromAsset(it.substring(1))
            else
              it
          )
        }
    )
}

private interface VueDirectiveNameConversionRulesProviderBase :
  PolySymbolNameConversionRulesProvider,
  PolySymbolNameConversionRules {

  override fun getNameConversionRules(): PolySymbolNameConversionRules = this

  override fun createPointer(): Pointer<out PolySymbolNameConversionRulesProvider> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override val renames: Map<PolySymbolKind, PolySymbolNameConverter>
    get() = canonicalNames

  override val matchNames: Map<PolySymbolKind, PolySymbolNameConverter>
    get() = canonicalNames

  override val completionVariants: Map<PolySymbolKind, PolySymbolNameConverter>
    get() = canonicalNames
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

        forPsiLocations(JSReferenceExpression::class.java)
          .contributeScopeProvider(VueTemplateJSReferenceSymbolScopeContributor)

        forPsiLocations(JSThisExpression::class.java)
          .contributeScopeProvider(VueTemplateThisExpressionScopeContributor)
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
        val enclosingComponent = VueModelManager.findEnclosingComponent(location) as? VueSourceComponent<*>
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
        val enclosingComponent = VueModelManager.findEnclosingComponent(location) as? VueSourceComponent<*>
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

        if (enclosingContainer is VueSourceComponent<*> && containerToProximity.keys.none { it is VueApp }) {
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

  private object VueTemplateJSReferenceSymbolScopeContributor : PolySymbolLocationQueryScopeProvider<JSReferenceExpression> {

    override fun getScopes(location: JSReferenceExpression): List<PolySymbolScope> =
      if (location is VueJSFilterReferenceExpression)
        listOf(VueJSFilterScope(location))
      else if (location.qualifier == null && location.contextOfType<XmlTag>()?.isScriptSetupTag() != true) {
        val original = CompletionUtil.getOriginalOrSelf(location)
        if (!checkLanguage(original)) {
          return emptyList()
        }
        val expressionIsInjected = isVueExprMetaLanguage(original.containingFile.language)
        val hostElement: PsiElement?
        if (expressionIsInjected) {
          //we are working within injection
          hostElement = InjectedLanguageManager.getInstance(location.project).getInjectionHost(location)
          if (hostElement == null) {
            return emptyList()
          }
        }
        else {
          hostElement = null
        }
        VueTemplateSymbolScopesProvider.EP_NAME.extensionList.flatMap {
          it.getScopes(original, hostElement)
        }
      }
      else
        emptyList()

    private fun checkLanguage(element: PsiElement): Boolean {
      return element.language.let {
        isVueExprMetaLanguage(it) || it == VueLanguage || it.isKindOf(CSSLanguage.INSTANCE)
      } || element.parent?.language.let {
        isVueExprMetaLanguage(it) || it == VueLanguage
      }
    }

    private class VueJSFilterScope(
      location: JSReferenceExpression,
    ) : PolySymbolIsolatedMappingScope<JSReferenceExpression>(
      mapOf(JS_SYMBOLS to VUE_FILTERS),
      location,
    ) {

      override fun acceptSymbol(symbol: PolySymbol): Boolean =
        symbol[PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE

      override val subScopeBuilder: (PolySymbolQueryExecutor, JSReferenceExpression) -> List<PolySymbolScope>
        get() = { _, location ->
          listOf(VueModelManager.findEnclosingContainer(location))
        }

      override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
        kind == JS_SYMBOLS || kind == JS_KEYWORDS

      override fun createPointer(): Pointer<out PolySymbolScope> {
        val locationPtr = location.createSmartPointer()
        return Pointer {
          locationPtr.dereference()?.let { VueJSFilterScope(it) }
        }
      }

    }
  }

  private object VueTemplateThisExpressionScopeContributor : PolySymbolLocationQueryScopeProvider<JSThisExpression> {
    override fun getScopes(location: JSThisExpression): List<PolySymbolScope> =
      if (isVueExprMetaLanguage(location.language)) {
        VueModelManager.findEnclosingContainer(location)
      }
      else {
        VueModelManager.findComponentForThisResolve(location)
      }
        ?.instanceScope
        ?.let { return listOf(it) }
      ?: emptyList()
  }
}
