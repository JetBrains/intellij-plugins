package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.collectExistingAttributeNames
import org.intellij.prisma.ide.completion.findAttributeOwner
import org.intellij.prisma.ide.schema.PrismaSchemaElement
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.lang.PrismaConstants.BlockAttributes
import org.intellij.prisma.lang.PrismaConstants.FieldAttributes
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.psi.*

object PrismaFieldAttributesProvider : PrismaSchemaCompletionProvider() {
  override val kind: PrismaSchemaKind = PrismaSchemaKind.FIELD_ATTRIBUTE

  private val allowedBeforeFieldAttribute = psiElement().andOr(
    psiElement(PrismaFieldType::class.java),
    psiElement(PrismaFieldAttribute::class.java),
  )

  override val pattern: ElementPattern<out PsiElement> =
    psiElement().andOr(
      psiElement().withParent(
        psiElement(PsiErrorElement::class.java).andOr(
          psiElement().afterSibling(allowedBeforeFieldAttribute),
          psiElement().withParent(PrismaEnumValueDeclaration::class.java).afterLeaf(psiElement(PrismaElementTypes.IDENTIFIER))
        )
      ),
      psiElement().withParent(
        psiElement(PrismaPathExpression::class.java)
          .with("withoutQualifier") { el -> el.qualifier == null }
          .withParent(
            psiElement(PrismaFieldAttribute::class.java).andOr(
              psiElement().afterSiblingNewLinesAware(allowedBeforeFieldAttribute),
              psiElement().afterSiblingNewLinesAware(psiElement(PrismaElementTypes.IDENTIFIER))
                .withParent(psiElement(PrismaEnumValueDeclaration::class.java))

            )
          )
      )
    ).inside(psiElement(PrismaEntityDeclaration::class.java))


  override fun collectSchemaElements(
    parameters: CompletionParameters,
    context: ProcessingContext
  ): Collection<PrismaSchemaElement> {
    val attributeOwner = findAttributeOwner(parameters.originalPosition) ?: return emptyList()
    val containingDeclaration = attributeOwner.containingDeclaration ?: return emptyList()

    val excluded = mutableSetOf<String>()

    val field = attributeOwner as? PrismaFieldDeclaration
    if (field != null) {
      val typeReference = field.fieldType?.typeReference ?: return emptyList()
      val typeName = typeReference.referenceName
      val typeDeclaration = typeReference.resolve()

      if (typeDeclaration is PrismaTypeDeclaration) {
        excluded.add(FieldAttributes.DEFAULT)
        excluded.add(FieldAttributes.RELATION)
      }

      val isIdAllowed =
        typeName == PrimitiveTypes.INT || typeName == PrimitiveTypes.STRING || typeDeclaration is PrismaEnumDeclaration
      if (!isIdAllowed) {
        excluded.add(FieldAttributes.ID)
      }

      val isUpdatedAtAllowed = typeName == PrimitiveTypes.DATETIME
      if (!isUpdatedAtAllowed) {
        excluded.add(FieldAttributes.UPDATED_AT)
      }
    }

    val existingAttributes = collectExistingAttributeNames(containingDeclaration)
    if (FieldAttributes.ID in existingAttributes || BlockAttributes.ID in existingAttributes) {
      excluded.add(FieldAttributes.ID)
    }

    excluded.addAll(collectExistingAttributeNames(attributeOwner))

    return super.collectSchemaElements(parameters, context).filter { it.label !in excluded }
  }
}