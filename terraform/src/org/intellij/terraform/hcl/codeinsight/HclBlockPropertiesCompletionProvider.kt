// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.components.service
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.codeinsight.QuoteInsertHandler
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createPropertyOrBlockType
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getOriginalObject
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.documentation.psi.HclFakeElementPsiFactory
import org.intellij.terraform.config.model.BaseModelType
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.HclType
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.SimpleValueHint
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.getType
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns.Block
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteral
import org.intellij.terraform.hcl.patterns.HCLPatterns.Object
import org.intellij.terraform.hcl.patterns.HCLPatterns.Property
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.HCLValue
import org.intellij.terraform.hil.HILFileType
import org.intellij.terraform.hil.psi.ILExpression

internal object HclBlockPropertiesCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position

    val original = parameters.originalPosition ?: return
    if (isInvalidCurly(original)) return

    val defaults = checkPropertyDefaults(position)
    // NOTE: do NOT check `defaults.isNotEmpty()` here.
    // We distinguish between:
    //  - `null` => this provider is NOT applicable to the current context, continue with `resolvePositionContext`
    //  - empty list => this provider IS applicable but has no suggestions (we must stop completion here
    //                  to avoid falling back to unrelated suggestions).
    // Returning an empty list signals "handled, but nothing to suggest" and prevents undesired fallthrough
    // Regression example: without this, `resource "aws_instance" "test" { ami = "<caret>"}` other resource's properties were suggested.
    if (defaults != null) {
      result.addAllElements(defaults)
      return
    }

    val positionContext = resolvePositionContext(position)
    suggestCompletionsForContext(parameters, result, positionContext)
  }

  private fun isInvalidCurly(original: PsiElement): Boolean {
    val parent = original.parent
    return original.node.elementType == HCLElementTypes.L_CURLY
           && parent is HCLObject
           && parent.parent is HCLBlock
  }

  private fun checkPropertyDefaults(position: PsiElement): List<LookupElement>? {
    val parent = position.parent
    if (parent !is HCLIdentifier && parent !is HCLStringLiteral) return null

    val container = parent.parent as? HCLProperty ?: return null
    val value = container.value as? HCLValue ?: return null
    if (value !== parent) return null

    val hclBlock = container.parentOfType<HCLBlock>() ?: return null
    val property = TfModelHelper.getBlockProperties(hclBlock)[container.name] as? PropertyType ?: return null
    val suggestedValues = property.type.suggestedValues.map { create(it) }

    val hintOfProperty = property.hint as? SimpleValueHint
    val hintValues = hintOfProperty?.hint?.map { create(it).withInsertHandler(QuoteInsertHandler) }.orEmpty()

    return suggestedValues + hintValues
  }

  private fun resolvePositionContext(position: PsiElement): HclPositionContext {
    val initialParent: PsiElement? = position.parent

    return if (initialParent is HCLIdentifier || initialParent is HCLStringLiteral) {
      when (val pob = initialParent.parent) {
        is HCLProperty -> {
          val type = getRightTypeOfProperty(pob)
          HclPositionContext(pob.parent, isProperty = true, rightType = type)
        }
        is HCLBlock -> {
          val isNameElement = pob.nameElements.firstOrNull() == initialParent
          val followedByNewline = initialParent.nextSibling is PsiWhiteSpace && initialParent.nextSibling.text.contains("\n")
          val computedIsBlock = !(isNameElement && followedByNewline)

          HclPositionContext(pob.parent, isBlock = computedIsBlock)
        }
        else -> HclPositionContext(initialParent)
      }
    }
    else {
      HclPositionContext(initialParent)
    }
  }

  private fun getRightTypeOfProperty(pob: HCLProperty): HclType? {
    val value = pob.value as? HCLValue ?: return null
    var right: HclType? = value.getType()

    if (right == Types.String && value is PsiLanguageInjectionHost) {
      InjectedLanguageManager.getInstance(pob.project).enumerate(value) { injectedPsi, _ ->
        if (injectedPsi.fileType == HILFileType) {
          right = Types.StringWithInjection
          val root = injectedPsi.firstChild
          if (root == injectedPsi.lastChild && root is ILExpression) {
            val type = root.getType()
            if (type != null && type != Types.Any) {
              right = type
            }
          }
        }
      }
    }
    return right
  }

  private fun suggestCompletionsForContext(parameters: CompletionParameters, result: CompletionResultSet, context: HclPositionContext) {
    val hclObject: HCLObject = context.parent as? HCLObject ?: return
    val parent = getOriginalObject(parameters, hclObject)
    val block = parent.parent as? HCLBlock ?: return
    val properties = TfModelHelper.getBlockProperties(block)

    if (properties.isEmpty()) return

    val fakeFactory = parent.project.service<HclFakeElementPsiFactory>()
    val candidates = properties.values
      .asSequence()
      .filter { it.name != Constants.HAS_DYNAMIC_ATTRIBUTES && it.configurable }
      .filter { isApplicableInContext(context, it) }
      .filter { shouldSuggestCandidate(parent, it) }
      .map { toLookupElement(it, parent, fakeFactory) }
      .asIterable()

    addResultsWithCustomSorter(result, candidates)
  }

  private fun isApplicableInContext(context: HclPositionContext, candidate: PropertyOrBlockType): Boolean = when {
    context.isProperty -> isCompatiblePropertyType(candidate, context.rightType)
    context.isBlock -> candidate is BlockType
    else -> true
  }

  private fun isCompatiblePropertyType(element: PropertyOrBlockType, rightType: HclType?): Boolean {
    return element is PropertyType &&
           (rightType == Types.StringWithInjection || element.type == rightType)
  }

  private fun shouldSuggestCandidate(parent: HCLObject, candidate: PropertyOrBlockType): Boolean = when (candidate) {
    is PropertyType -> parent.findProperty(candidate.name) == null
    is BlockType -> true
    else -> false
  }

  private fun toLookupElement(property: PropertyOrBlockType, parent: HCLObject, fakeFactory: HclFakeElementPsiFactory): LookupElement {
    return if (property is BaseModelType && property.descriptionKind != null) {
      val block = parent.parentOfType<HCLBlock>()
      val fakeProperty = block?.let { fakeFactory.createFakeHclProperty(it, property) }
      createPropertyOrBlockType(property, property.name, fakeProperty)
    }
    else {
      createPropertyOrBlockType(property, property.name, fakeFactory.emptyHclBlock)
    }
  }

  // Psi Patterns
  private fun getBlockPropertyKeyPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> = psiElement()
    .withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
    .inFile(filePattern)
    .withParent(Object)
    .withSuperParent(2, Block)

  private fun getPropertyInBlockPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> = psiElement()
    .withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
    .inFile(filePattern)
    .withParent(IdentifierOrStringLiteral)
    .withSuperParent(2, Property)
    .withSuperParent(3, Object)
    .withSuperParent(4, Block)

  private fun getNestedBlockPropertyPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> = psiElement()
    .withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
    .inFile(filePattern)
    .and(psiElement().insideStarting(Block))
    .withParent(IdentifierOrStringLiteral)
    .withSuperParent(2, Block)
    .withSuperParent(3, Object)
    .withSuperParent(4, Block)

  fun registerTo(contributor: CompletionContributor, filePattern: PsiFilePattern.Capture<HCLFile>) {
    contributor.extend(CompletionType.BASIC, getBlockPropertyKeyPattern(filePattern), this)
    contributor.extend(CompletionType.BASIC, getPropertyInBlockPattern(filePattern), this)
    contributor.extend(CompletionType.BASIC, getNestedBlockPropertyPattern(filePattern), this)
  }
}

private data class HclPositionContext(
  val parent: PsiElement? = null,
  val isBlock: Boolean = false,
  val isProperty: Boolean = false,
  val rightType: HclType? = null,
)

private val HclElementsSorter = CompletionSorter.emptySorter().weigh(
  object : LookupElementWeigher("hcl.required.property") {
    override fun weigh(element: LookupElement): Comparable<Nothing> {
      val obj = element.`object`
      if (obj !is PropertyOrBlockType) return 100

      val requiredWeight = if (obj.required) 0 else 1
      val typeWeight = when (obj) {
        is BlockType -> 0
        is PropertyType -> 1
        else -> 2
      }

      return requiredWeight * 10 + typeWeight
    }
  }
)

internal fun addResultsWithCustomSorter(result: CompletionResultSet, toAdd: Iterable<LookupElement>) {
  result.withRelevanceSorter(HclElementsSorter).addAllElements(toAdd)
}