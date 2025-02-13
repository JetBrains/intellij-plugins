package org.intellij.prisma.lang.psi

import com.intellij.patterns.ObjectPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.prevLeaf
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.PRISMA_ENTITY_DECLARATION
import org.intellij.prisma.lang.types.PrismaType
import org.intellij.prisma.lang.types.unwrapOptionalType

object PrismaPsiPatterns {
  val topKeyword: PsiElementPattern.Capture<PsiElement> =
    psiElement(PrismaElementTypes.IDENTIFIER).withParent(
      psiElement(PsiErrorElement::class.java).withParent(PrismaFile::class.java).afterNewLine()
    )

  val typeReference: PsiElementPattern.Capture<PsiElement> =
    psiElement(PrismaElementTypes.IDENTIFIER).withParent(psiElement(PrismaTypeReference::class.java))

  val datasourceField: PsiElementPattern.Capture<PsiElement> =
    psiElement()
      .withParent(PrismaKeyValue::class.java)
      .withSuperParent(3, PrismaDatasourceDeclaration::class.java)

  val generatorField: PsiElementPattern.Capture<PsiElement> =
    psiElement()
      .withParent(PrismaKeyValue::class.java)
      .withSuperParent(3, PrismaGeneratorDeclaration::class.java)

  val newLine: PsiElementPattern.Capture<PsiElement> = psiElement().with("newLine") { element ->
    element.elementType == TokenType.WHITE_SPACE && element.textContains('\n')
  }

  fun withFieldType(unwrapOptional: Boolean = false, predicate: (PrismaType, PsiElement) -> Boolean): PsiElementPattern.Capture<PsiElement> {
    return psiElement().with("withFieldType") { element ->
      element
        .parentOfType<PrismaFieldDeclaration>(true)
        ?.type
        ?.let { if (unwrapOptional) it.unwrapOptionalType() else it }
        ?.let { predicate(it, element) }
      ?: false
    }
  }

  fun namedArgument(name: String): PsiElementPattern.Capture<out PsiElement> {
    return psiElement(PrismaNamedArgument::class.java).with("namedArgument") { element ->
      element.identifier.textMatches(name)
    }
  }

  fun insideEntityDeclaration(declarationPattern: PsiElementPattern.Capture<out PsiElement>): PsiElementPattern.Capture<out PsiElement> {
    return psiElement().with("insideEntityDeclaration") { element, context ->
      val declaration =
        context?.get(PRISMA_ENTITY_DECLARATION)
        ?: element.parentOfType()
        ?: return@with false
      declarationPattern.accepts(declaration, context)
    }
  }
}

fun <T : PsiElement, Self : ObjectPattern<T, Self>> ObjectPattern<T, Self>.afterNewLine(): Self =
  with("afterNewLine") { element, context ->
    val prev = element.skipWhitespacesBackwardWithoutNewLines() ?: return@with true
    PrismaPsiPatterns.newLine.accepts(prev, context)
  }

fun <T : PsiElement, Self : ObjectPattern<T, Self>> ObjectPattern<T, Self>.afterLeafNewLine(): Self =
  with("afterLeafNewLine") { element, context ->
    val prev = element.prevLeaf() ?: return@with true
    PrismaPsiPatterns.newLine.accepts(prev, context)
  }

fun <T : PsiElement, Self : ObjectPattern<T, Self>, P : PsiElement> ObjectPattern<T, Self>.afterSiblingNewLinesAware(
  pattern: PsiElementPattern.Capture<P>,
): Self =
  with("afterSiblingWithoutNewLines") { element, context ->
    val prev = element.skipWhitespacesBackwardWithoutNewLines() ?: return@with true
    pattern.accepts(prev, context)
  }

fun <T : Any, Self : ObjectPattern<T, Self>> ObjectPattern<T, Self>.with(name: String, cond: (T) -> Boolean): Self =
  with(object : PatternCondition<T>(name) {
    override fun accepts(t: T, context: ProcessingContext?): Boolean = cond(t)
  })

fun <T : Any, Self : ObjectPattern<T, Self>> ObjectPattern<T, Self>.with(
  name: String,
  cond: (T, ProcessingContext?) -> Boolean,
): Self = with(object : PatternCondition<T>(name) {
  override fun accepts(t: T, context: ProcessingContext?): Boolean = cond(t, context)
})