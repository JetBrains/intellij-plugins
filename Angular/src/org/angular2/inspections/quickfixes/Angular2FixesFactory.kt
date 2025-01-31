// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.hint.QuestionAction
import com.intellij.codeInsight.hint.ShowParameterInfoHandler
import com.intellij.codeInsight.navigation.getPsiElementPopup
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfigurationImpl
import com.intellij.lang.ecmascript6.psi.impl.TypeScriptImportPathBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSStringUtil.unquoteWithoutUnescapingStringLiteralValue
import com.intellij.lang.javascript.ecmascript6.ES6QualifiedNamedElementRenderer
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.containers.ContainerUtil.emptyList
import com.intellij.util.containers.ContainerUtil.map2SetNotNull
import com.intellij.util.containers.MultiMap
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.cli.config.AngularProject
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.shouldPopupParameterInfoOnCompletion
import org.angular2.editor.delayCompletionAutoPopupOnImport
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceDirectiveProperty
import org.angular2.inspections.actions.Angular2ActionFactory
import org.angular2.inspections.quickfixes.AddInputTransformFunctionQuickFix.TransformKind.*
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.web.scopes.OneTimeBindingsScope
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.TestOnly

object Angular2FixesFactory {

  @TestOnly
  @NonNls
  @JvmField
  val DECLARATION_TO_CHOOSE: Key<String> = Key.create<String>("declaration.to.choose")

  fun getCreateInputTransformFixes(attribute: XmlAttribute, expressionType: String): List<LocalQuickFixAndIntentionActionOnPsiElement> {
    val descriptor = attribute.descriptor.asSafely<Angular2AttributeDescriptor>() ?: return emptyList()
    val propertyName = descriptor.info
                         .takeIf {
                           it.type == Angular2AttributeType.REGULAR
                           || it is Angular2AttributeNameParser.PropertyBindingInfo && it.bindingType == PropertyBindingType.PROPERTY
                         }?.name
                       ?: return emptyList()
    val input = descriptor.sourceDirectives.flatMap { it.inputs }.filter { it.name == propertyName }
      .takeIf { it.size == 1 }?.get(0)?.asSafely<Angular2SourceDirectiveProperty>()
    if (input == null || input.transformParameterType != null)
      return emptyList()

    val quickFixes = SmartList<LocalQuickFixAndIntentionActionOnPsiElement>()
    when (JSCompositeTypeFactory.optimizeTypeIfComposite(input.type,
                                                         JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_REMOVED_NULL_UNDEFINED)) {
      is JSBooleanType -> quickFixes.add(AddInputTransformFunctionQuickFix(BooleanAttribute, propertyName, expressionType, input.owner))
      is JSNumberType -> quickFixes.add(AddInputTransformFunctionQuickFix(NumberAttribute, propertyName, expressionType, input.owner))
    }
    quickFixes.add(AddInputTransformFunctionQuickFix(Custom, propertyName, expressionType, input.owner))
    return quickFixes
  }

  @JvmStatic
  fun ensureDeclarationResolvedAfterCodeCompletion(element: PsiElement, editor: Editor) {
    val candidates = getCandidatesForResolution(element, true)
    if (!candidates.get(DeclarationProximity.IMPORTABLE).isEmpty()) {
      delayCompletionAutoPopupOnImport(editor)
      Angular2ActionFactory.createNgModuleImportAction(editor, element, true).execute()
    }
    else if (!candidates.get(DeclarationProximity.NOT_DECLARED_IN_ANY_MODULE).isEmpty()) {
      delayCompletionAutoPopupOnImport(editor)
      selectAndRun(editor,
                   Angular2Bundle.message("angular.quickfix.ngmodule.declare.select.declarable",
                                          getCommonNameForDeclarations(candidates.get(DeclarationProximity.NOT_EXPORTED_BY_MODULE))),
                   candidates.get(DeclarationProximity.NOT_DECLARED_IN_ANY_MODULE)) { candidate ->
        Angular2ActionFactory.createAddNgModuleDeclarationAction(editor, element, candidate, true)
      }
    }
    else if (!candidates.get(DeclarationProximity.NOT_EXPORTED_BY_MODULE).isEmpty()) {
      delayCompletionAutoPopupOnImport(editor)
      selectAndRun(editor,
                   Angular2Bundle.message("angular.quickfix.ngmodule.export.select.declarable",
                                          getCommonNameForDeclarations(candidates.get(DeclarationProximity.NOT_EXPORTED_BY_MODULE))),
                   candidates.get(DeclarationProximity.NOT_EXPORTED_BY_MODULE)) { candidate ->
        Angular2ActionFactory.createExportNgModuleDeclarationAction(editor, element, candidate, true)
      }
    } else {
      // Show parameter info popup immediately
      if (shouldPopupParameterInfoOnCompletion(element) && CodeInsightSettings.getInstance().AUTO_POPUP_PARAMETER_INFO) {
        ShowParameterInfoHandler.invoke(element.project, editor, element.containingFile, -1, null, false)
      }
    }
  }

  @JvmStatic
  fun addUnresolvedDeclarationFixes(element: PsiElement, fixes: MutableList<LocalQuickFix>) {
    val candidates = getCandidatesForResolution(element, false)
    if (candidates.containsKey(DeclarationProximity.IN_SCOPE)) {
      return
    }
    if (!candidates.get(DeclarationProximity.IMPORTABLE).isEmpty()) {
      fixes.add(AddNgModuleImportQuickFix(element, candidates.get(DeclarationProximity.IMPORTABLE)))
    }
    for (declaration in candidates.get(DeclarationProximity.NOT_DECLARED_IN_ANY_MODULE)) {
      AddNgModuleDeclarationQuickFix.add(element, declaration, fixes)
    }
    for (declaration in candidates.get(DeclarationProximity.NOT_EXPORTED_BY_MODULE)) {
      ExportNgModuleDeclarationQuickFix.add(element, declaration, fixes)
    }
  }

  @JvmStatic
  fun getCandidatesForResolution(
    element: PsiElement,
    codeCompletion: Boolean,
  ): MultiMap<DeclarationProximity, Angular2Declaration> {
    val scope = Angular2DeclarationsScope(element)
    val importsOwner = scope.importsOwner
    if (importsOwner == null || !scope.isInSource(importsOwner)) {
      return MultiMap.empty()
    }
    val filter: (Angular2Declaration) -> Boolean
    val provider: () -> List<Angular2Declaration>
    val secondaryProvider: (() -> List<Angular2Declaration>)?
    when (element) {
      is XmlAttribute -> {
        val attributeDescriptor = element.descriptor as? Angular2AttributeDescriptor
                                  ?: return MultiMap.empty()
        val info = attributeDescriptor.info
        provider = { Angular2ApplicableDirectivesProvider(element.parent).matched }
        secondaryProvider = if (info.type == Angular2AttributeType.REFERENCE)
          null
        else
          ({ attributeDescriptor.sourceDirectives })

        when (info.type) {
          Angular2AttributeType.PROPERTY_BINDING -> {
            if ((info as Angular2AttributeNameParser.PropertyBindingInfo).bindingType != PropertyBindingType.PROPERTY) {
              return MultiMap.empty()
            }
            filter = { declaration ->
              declaration is Angular2Directive && declaration.inputs.any { input -> info.name == input.name }
            }
          }
          Angular2AttributeType.EVENT -> {
            if ((info as Angular2AttributeNameParser.EventInfo).eventType != Angular2HtmlEvent.EventType.REGULAR) {
              return MultiMap.empty()
            }
            filter = { declaration ->
              declaration is Angular2Directive && declaration.outputs.any { output -> info.name == output.name }
            }
          }
          Angular2AttributeType.BANANA_BOX_BINDING -> {
            filter = { declaration ->
              declaration is Angular2Directive && declaration.inOuts.any { inout -> info.name == inout.name }
            }
          }
          Angular2AttributeType.REGULAR -> {
            filter = { declaration ->
              declaration is Angular2Directive
              && (declaration.inputs.any { input -> info.name == input.name && OneTimeBindingsScope.isOneTimeBindingProperty(input) }
                  || declaration.selector.simpleSelectors.any { selector -> selector.attrNames.any { info.name == it } })
            }
          }
          Angular2AttributeType.REFERENCE -> {
            val exportName = element.value
            if (exportName.isNullOrEmpty()) {
              return MultiMap.empty()
            }
            filter = { declaration -> declaration is Angular2Directive && declaration.exportAs.contains(exportName) }
          }
          else -> return MultiMap.empty()
        }
      }
      is XmlTag -> {
        provider = { Angular2ApplicableDirectivesProvider(element, true).matched }
        secondaryProvider = null
        filter = { _: Angular2Declaration -> true }
      }
      is Angular2TemplateBinding -> {
        provider = { Angular2ApplicableDirectivesProvider(element.getParent() as Angular2TemplateBindings).matched }
        secondaryProvider = createSecondaryProvider(element.getParent() as Angular2TemplateBindings)
        if (element.keyIsVar()) {
          return MultiMap.empty()
        }
        val key = element.key
        filter = { declaration ->
          declaration is Angular2Directive && declaration.inputs.any { input -> key == input.name }
        }
      }
      is Angular2TemplateBindings -> {
        provider = { Angular2ApplicableDirectivesProvider(element).matched }
        secondaryProvider = createSecondaryProvider(element)
        filter = { _: Angular2Declaration -> true }
      }
      is Angular2PipeReferenceExpression -> {
        val referencedName = element.referenceName
        if (referencedName.isNullOrEmpty()) {
          return MultiMap.empty()
        }
        provider = { Angular2EntitiesProvider.findPipes(element.getProject(), referencedName) }
        secondaryProvider = null
        filter = { _: Angular2Declaration -> true }
      }
      else -> {
        throw IllegalArgumentException(element.javaClass.name)
      }
    }
    val declarations = SmartList<Angular2Declaration>()
    val declarationProcessor = { p: () -> List<Angular2Declaration> ->
      for (it in p()) {
        if (filter(it)) {
          declarations.add(it)
        }
      }
    }

    declarationProcessor(provider)
    if (declarations.isEmpty() && codeCompletion && secondaryProvider != null) {
      declarationProcessor(secondaryProvider)
    }

    val result = MultiMap<DeclarationProximity, Angular2Declaration>()
    removeLocalLibraryElements(importsOwner.sourceElement, declarations)
      .forEach { declaration -> result.putValue(scope.getDeclarationProximity(declaration), declaration) }

    return result
  }

  private fun removeLocalLibraryElements(
    context: PsiElement,
    declarations: List<Angular2Declaration>,
  ): Collection<Angular2Declaration> {
    val contextFile = context.containingFile.originalFile.virtualFile
    val config = AngularConfigProvider.findAngularConfig(context.project, contextFile) ?: return declarations
    val contextProject = config.getProject(contextFile) ?: return declarations
    val localRoots = map2SetNotNull(config.projects) { project ->
      if (project.type === AngularProject.AngularProjectType.LIBRARY && project != contextProject) {
        return@map2SetNotNull project.sourceDir
      }
      null
    }

    // TODO do not provide proposals from dist dir for local lib context - requires parsing ng-package.json
    // localRoots.add(contextProject.getOutputDirectory())

    val projectRoot = config.file.parent
    return declarations.filter { declaration ->
      val declarationFile = PsiUtilCore.getVirtualFile(declaration.sourceElement)
      var file = declarationFile
      while (file != null && file != projectRoot) {
        if (localRoots.contains(file)) {
          return@filter hasNonRelativeModuleName(context, declaration.entitySource, declarationFile!!)
        }
        file = file.parent
      }
      true
    }
  }

  private fun hasNonRelativeModuleName(
    context: PsiElement,
    declaration: PsiElement?,
    declarationFile: VirtualFile,
  ): Boolean {
    if (declaration == null) return false
    val builder = TypeScriptImportPathBuilder(JSImportPathConfigurationImpl(
      unwrapImplicitElement(context), unwrapImplicitElement(declaration), declarationFile, false, "Foo"))
    val isAbsolute = Ref.create(false)
    builder.processDescriptorsWithModuleName { info ->
      if (!unquoteWithoutUnescapingStringLiteralValue(info.moduleName).startsWith(".")) {
        isAbsolute.set(true)
      }
      !isAbsolute.get()
    }
    return isAbsolute.get()
  }

  private fun unwrapImplicitElement(element: PsiElement): PsiElement {
    return (element as? JSImplicitElement)?.context ?: element
  }

  private fun createSecondaryProvider(bindings: Angular2TemplateBindings): () -> List<Angular2Declaration> {
    return {
      (InjectedLanguageManager.getInstance(bindings.project).getInjectionHost(bindings) ?: bindings)
        .let { element -> PsiTreeUtil.getParentOfType(element, XmlAttribute::class.java) }
        ?.descriptor
        ?.asSafely<Angular2AttributeDescriptor>()
        ?.sourceDirectives
      ?: emptyList<Angular2Directive>()
    }
  }

  private fun getCommonNameForDeclarations(declarations: Collection<Angular2Declaration>): String {
    if (declarations.firstOrNull() is Angular2Pipe) {
      return Angular2Bundle.message("angular.entity.pipe")
    }
    var hasDirective = false
    var hasComponent = false
    for (declaration in declarations) {
      if (declaration is Angular2Component) {
        hasComponent = true
      }
      else {
        hasDirective = true
      }
    }
    return if (hasComponent == hasDirective)
      Angular2Bundle.message("angular.entity.component.or.directive")
    else if (hasComponent)
      Angular2Bundle.message("angular.entity.component")
    else
      Angular2Bundle.message("angular.entity.directive")
  }

  private fun selectAndRun(
    editor: Editor,
    @Nls title: String,
    declarations: Collection<Angular2Declaration>,
    actionFactory: (Angular2Declaration) -> QuestionAction?,
  ) {
    if (declarations.isEmpty()) {
      return
    }

    if (declarations.size == 1) {
      actionFactory(declarations.first())?.execute()
      return
    }

    ThreadingAssertions.assertEventDispatchThread()
    val elementMap = declarations
      .mapNotNull { it.entitySource?.let { src -> Pair(src, it) } }
      .toMap()

    val processor = PsiElementProcessor<PsiElement> { element ->
      elementMap[element]?.let(actionFactory)?.execute()
      false
    }

    if (ApplicationManager.getApplication().isUnitTestMode) {
      @Suppress("TestOnlyProblems")
      processor.execute(
        editor.getUserData(DECLARATION_TO_CHOOSE)
          ?.let { name -> declarations.find { declaration -> declaration.getName() == name } }
          ?.entitySource
        ?: throw AssertionError(
          "Declaration name must be specified in test mode. Available names: " +
          declarations.filter { it.entitySource != null }.joinToString { it.getName() }
        )
      )
      return
    }
    if (editor.isDisposed) return

    getPsiElementPopup(elements = elementMap.keys.toTypedArray<PsiElement>(),
                       renderer = ES6QualifiedNamedElementRenderer(),
                       title = title, processor = processor)
      .showInBestPositionFor(editor)
  }
}
