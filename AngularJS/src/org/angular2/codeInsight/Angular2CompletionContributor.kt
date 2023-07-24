// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.javascript.web.css.CssInBindingExpressionCompletionProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.completion.*
import com.intellij.lang.javascript.completion.JSImportCompletionUtil.IMPORT_PRIORITY
import com.intellij.lang.javascript.completion.JSLookupPriority.*
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser
import com.intellij.lang.javascript.ecmascript6.types.OverloadStrictness
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.JSTypeUtils.isNullOrUndefinedType
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink
import com.intellij.lang.javascript.psi.types.JSFunctionTypeImpl
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import com.intellij.util.containers.ContainerUtil
import icons.AngularJSIcons
import org.angular2.Angular2DecoratorUtil
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity
import org.angular2.codeInsight.imports.Angular2GlobalImportCandidate
import org.angular2.codeInsight.template.Angular2StandardSymbolsScopesProvider
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2PipeExpression
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.web.codeCompletion.Angular2HostDirectivePropertyMappingCompletionProvider
import org.jetbrains.annotations.NonNls

class Angular2CompletionContributor : CompletionContributor() {
  init {

    extend(CompletionType.BASIC,
           psiElement().with(language(Angular2Language.INSTANCE)),
           CssInBindingExpressionCompletionProvider())

    extend(CompletionType.BASIC,
           psiElement().with(language(Angular2Language.INSTANCE)),
           TemplateExpressionCompletionProvider())

    extend(CompletionType.BASIC,
           psiElement(JSTokenTypes.STRING_LITERAL).with(language(JavaScriptSupportLoader.TYPESCRIPT)),
           Angular2HostDirectivePropertyMappingCompletionProvider())
  }

  private class TemplateExpressionCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters,
                                context: ProcessingContext,
                                result: CompletionResultSet) {
      var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
      if (ref is PsiMultiReference) {
        ref = ref.references.find { it is Angular2PipeReferenceExpression || it is JSReferenceExpressionImpl }
      }
      if (ref is Angular2PipeReferenceExpression) {
        val scope = Angular2DeclarationsScope(parameters.position)
        val actualType = calcActualType(ref)
        for ((key, value) in Angular2EntitiesProvider.getAllPipes(ref.project)) {
          val bestMatch = scope.getClosestDeclaration(value)
          if (bestMatch == null || bestMatch.second == DeclarationProximity.NOT_REACHABLE) {
            continue
          }
          val match = bestMatch.first
          var builder = (match.typeScriptClass?.let { LookupElementBuilder.create(it, key) }
                         ?: LookupElementBuilder.create(key))
            .withIcon(AngularJSIcons.Angular2)
            .withTypeText(Angular2Bundle.message("angular.description.pipe"), null, true)
            .withInsertHandler(JSLookupElementInsertHandler(false, null))
          if (bestMatch.second != DeclarationProximity.IN_SCOPE) {
            builder = Angular2CodeInsightUtils.wrapWithImportDeclarationModuleHandler(
              Angular2CodeInsightUtils.decorateLookupElementWithModuleSource(builder, listOf(bestMatch.first), bestMatch.second, scope),
              Angular2PipeReferenceExpression::class.java
            )
          }
          val addResult = { el: LookupElementBuilder ->
            result.consume(PrioritizedLookupElement.withPriority(
              el,
              (if (bestMatch.second == DeclarationProximity.IN_SCOPE || bestMatch.second == DeclarationProximity.IMPORTABLE)
                NG_VARIABLE_PRIORITY
              else
                NG_PRIVATE_VARIABLE_PRIORITY).priorityValue.toDouble()))
          }
          val transformMembers = ArrayList(match.transformMembers)
          if (!transformMembers.isEmpty() && actualType != null) {
            transformMembers.sortWith(
              Comparator
                .comparingInt<JSElement> { if (it is TypeScriptFunction && isNullOrUndefinedType(it.returnType)) 1 else 0 }
                .thenComparingInt { if (it is TypeScriptFunction && it.isOverloadDeclaration) 0 else 1 })
            val transformTypes = transformMembers
              .asSequence()
              .mapNotNull { transform ->
                (transform as? JSTypeOwner)?.jsType?.substitute()
                  ?.asSafely<JSFunctionType>()?.let { Pair(transform, it) }
              }
              .toMap()

            val converted2Original = transformTypes.entries.associateBy({ toTypeWithOneParam(it.value) }, { it.key })
            val resolveResults = JSTypeSignatureChooser(
              parameters.position, listOf(actualType), null, JSTypeDeclaration.EMPTY_ARRAY
            ).chooseOverload(converted2Original.keys, OverloadStrictness.FULL)
            for (resolved in resolveResults) {
              if (resolved.overloadType.isAssignable()) {
                val f = resolved.jsFunction
                addResult(builder.withTypeText(renderPipeTypeText(transformTypes[converted2Original[f]] ?: continue, key), true))
                break
              }
            }
          }
          else {
            addResult(builder)
          }
        }
        result.stopHere()
      }
      else if (ref is JSReferenceExpressionImpl && (ref.qualifier == null || ref.qualifier is JSThisExpression)) {
        val contributedElements = HashSet<String>()
        val localNames = HashSet<String>()

        // Angular template scope
        Angular2TemplateScopesResolver.resolve(parameters.position) { resolveResult ->
          val element = resolveResult.element as? JSPsiElementBase
                        ?: return@resolve true
          val name = element.name
          if (name != null && !NG_LIFECYCLE_HOOKS.contains(name)
              && contributedElements.add(name + "#" + JSLookupUtilImpl.getTypeAndTailTexts(element, null).tailAndType)) {
            localNames.add(name)
            result.consume(JSCompletionUtil.withJSLookupPriority(
              JSLookupUtilImpl.createLookupElement(element, name)
                .withInsertHandler(JSLookupElementInsertHandler(false, null)),
              calcPriority(element)
            ))
          }
          true
        }

        if (ref.qualifier != null) {
          result.stopHere()
          return
        }

        // Declarations local to the component class
        val componentClass = Angular2ComponentLocator.findComponentClass(ref)
        val componentContext = componentClass?.context
        if (componentContext != null) {
          val sink = CompletionResultSink(ref, result.prefixMatcher, localNames, false, false)
          JSStubBasedPsiTreeUtil.processDeclarationsInScope(componentContext, { element, state ->
            if (element != componentClass)
              sink.addResult(element, state, null)
            else true
          }, true)
          sink.resultsAsObjects.forEach { lookupElement ->
            localNames.add(lookupElement.lookupString)
            result.addElement(
              JSCompletionUtil.withJSLookupPriority(wrapWithImportInsertHandler(lookupElement, ref), RELEVANT_NO_SMARTNESS_PRIORITY))
          }
        }

        // Exports, global symbols and keywords, plus any smart code completions
        result.runRemainingContributors(parameters) { completionResult ->
          var lookupElement = completionResult.lookupElement
          val name = lookupElement.lookupString
          if (localNames.contains(name)) {
            return@runRemainingContributors
          }
          if (lookupElement is PrioritizedLookupElement<*> && lookupElement.getUserData<CompletionContributor>(
              BaseCompletionService.LOOKUP_ELEMENT_CONTRIBUTOR) is JSCompletionContributor) {
            val priority = lookupElement.priority.toInt()
            // Filter out unsupported keywords
            if (priority == NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue || priority == KEYWORDS_PRIORITY.priorityValue) {
              if (!SUPPORTED_KEYWORDS.contains(name)) {
                return@runRemainingContributors
              }
            }
            else if (priority == TOP_LEVEL_SYMBOLS_FROM_OTHER_FILES.priorityValue) {
              // Wrap global symbols with insert handler
              lookupElement = wrapWithImportInsertHandler(lookupElement, ref)
            }
            else if (priority != 0) {
              // If we don't know what it is, we better ignore it
              return@runRemainingContributors
            }
          }
          result.withRelevanceSorter(completionResult.sorter)
            .withPrefixMatcher(completionResult.prefixMatcher)
            .addElement(lookupElement)
        }
      }
    }

    private fun toTypeWithOneParam(type: JSFunctionType): JSFunctionType {
      return if (type.parameters.size <= 1)
        type
      else
        JSFunctionTypeImpl(type.source, listOf(type.parameters[0]), type.returnType)
    }

    private fun calcActualType(ref: Angular2PipeReferenceExpression): JSType? {
      val pipeCall = ref.parent as Angular2PipeExpression
      return pipeCall.arguments.firstOrNull()?.let { JSPsiBasedTypeOfType(it, true) }
    }

    private fun renderPipeTypeText(f: JSFunctionType, pipeName: String): String {
      val result = StringBuilder()
      result.append('[')
      var first = true
      for (param in f.parameters) {
        val type = param.simpleType
        result.append("<")
          .append(type?.typeText?.replace("\\|(null|undefined)".toRegex(), "")?.replace("String\\((.*?)\\)".toRegex(), "$1") ?: "*")
          .append(if (param.isOptional) "?" else "")
          .append(">")
        if (first) {
          result.append(" | ")
            .append(pipeName)
          first = false
        }
        result.append(":")
      }
      result.setLength(result.length - 1)
      val type = f.returnType
      return StringUtil.shortenTextWithEllipsis(
        result
          .append("] : <")
          .append(type?.typeText?.replace("\\|(null|undefined)".toRegex(), "") ?: "?")
          .append(">")
          .toString(),
        50, 0, true)
    }

    private fun calcPriority(element: JSPsiElementBase): JSLookupPriority {
      if (Angular2StandardSymbolsScopesProvider.`$ANY` == element.name) {
        return `NG_$ANY_PRIORITY`
      }
      return if (Angular2DecoratorUtil.isPrivateMember(element))
        NG_PRIVATE_VARIABLE_PRIORITY
      else
        NG_VARIABLE_PRIORITY
    }

    private fun wrapWithImportInsertHandler(lookupElement: LookupElement, place: PsiElement): LookupElement {
      return lookupElement.asSafely<PrioritizedLookupElement<*>>()
               ?.delegate
               ?.asSafely<LookupElementBuilder>()
               ?.let {
                 it.withInsertHandler(JSImportCompletionUtil.createInsertHandler(
                   Angular2GlobalImportCandidate(it.lookupString, place)
                 ))
               }
               ?.let { JSCompletionUtil.withJSLookupPriority(it, IMPORT_PRIORITY) }
             ?: lookupElement
    }
  }

  companion object {

    private val NG_VARIABLE_PRIORITY = LOCAL_SCOPE_MAX_PRIORITY
    private val NG_PRIVATE_VARIABLE_PRIORITY = LOCAL_SCOPE_MAX_PRIORITY_EXOTIC
    private val `NG_$ANY_PRIORITY` = TOP_LEVEL_SYMBOLS_FROM_OTHER_FILES

    @NonNls
    private val NG_LIFECYCLE_HOOKS = ContainerUtil.newHashSet(
      "ngOnChanges", "ngOnInit", "ngDoCheck", "ngOnDestroy", "ngAfterContentInit",
      "ngAfterContentChecked", "ngAfterViewInit", "ngAfterViewChecked")

    @NonNls
    private val SUPPORTED_KEYWORDS = ContainerUtil.newHashSet(
      "var", "let", "as", "null", "undefined", "true", "false", "if", "else", "this"
    )

    private fun <T : PsiElement> language(language: Language): PatternCondition<T> {
      return object : PatternCondition<T>("language(" + language.id + ")") {
        override fun accepts(t: T, context: ProcessingContext): Boolean {
          return language.`is`(PsiUtilCore.findLanguageFromElement(t))
        }
      }
    }
  }
}
