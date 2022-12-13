package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.prisma.PrismaIcons
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.types.PrismaIndexAlgorithm
import org.intellij.prisma.ide.schema.types.PrismaNativeType.PostgreSQL
import org.intellij.prisma.ide.schema.types.PrismaOperatorClass
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.*


object PrismaOperatorClassProvider : PrismaCompletionProvider() {
  override val pattern: ElementPattern<out PsiElement> = psiElement().withSuperParent(
    2,
    PrismaPsiPatterns.namedArgument(PrismaConstants.ParameterNames.OPS)
  )

  override fun addCompletions(
    parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
  ) {
    val indexAlgorithm = (parameters.position.parentOfType<PrismaBlockAttribute>()
      ?.getArgumentsList()
      ?.findArgumentByName(PrismaConstants.ParameterNames.TYPE)
      ?.expression as? PrismaPathExpression)
                           ?.let { PrismaIndexAlgorithm.fromString(it.referenceName) }
                         ?: return

    val fieldExpression =
      parameters.position.parentOfType<PrismaNamedArgument>()?.parentOfType<PrismaFunctionCall>() ?: return
    val declaration = fieldExpression.pathExpression.resolve() as? PrismaFieldDeclaration ?: return

    val declaredType = declaration.type
    val isList = declaredType.isList()
    val fieldType = declaredType.unwrapType()
    val nativeType = declaration.nativeType

    val classes = mutableListOf<PrismaOperatorClass>()

    when (indexAlgorithm) {
      PrismaIndexAlgorithm.Gist -> gist(classes, nativeType)
      PrismaIndexAlgorithm.Gin -> gin(classes, fieldType, isList)
      PrismaIndexAlgorithm.SpGist -> spGist(classes, fieldType, nativeType)
      PrismaIndexAlgorithm.Brin -> brin(classes, fieldType, nativeType)
      else -> {}
    }

    classes.map {
      LookupElementBuilder.create(it).withIcon(PrismaIcons.FIELD)
        .withTypeText(PrismaConstants.Types.OPERATOR_CLASS)
    }.forEach { result.addElement(it) }
  }

  private fun gist(
    classes: MutableList<PrismaOperatorClass>,
    nativeType: String?
  ) {
    if (nativeType == PostgreSQL.INET_TYPE_NAME) {
      classes.add(PrismaOperatorClass.InetOps)
    }
  }

  private fun gin(
    classes: MutableList<PrismaOperatorClass>,
    fieldType: PrismaType?,
    isList: Boolean
  ) {
    if (isList) {
      classes.add(PrismaOperatorClass.ArrayOps)
    }
    else if (fieldType is PrismaJsonType) {
      classes.add(PrismaOperatorClass.JsonbOps)
      classes.add(PrismaOperatorClass.JsonbPathOps)
    }
  }

  private fun spGist(
    classes: MutableList<PrismaOperatorClass>,
    fieldType: PrismaType?,
    nativeType: String?
  ) {
    if (nativeType == PostgreSQL.INET_TYPE_NAME) {
      classes.add(PrismaOperatorClass.InetOps)
    }
    else if (
      fieldType is PrismaStringType && (
        nativeType == null ||
        nativeType == PostgreSQL.TEXT_TYPE_NAME ||
        nativeType == PostgreSQL.VARCHAR_TYPE_NAME)
    ) {
      classes.add(PrismaOperatorClass.TextOps)
    }
  }

  private fun brin(
    classes: MutableList<PrismaOperatorClass>,
    fieldType: PrismaType?,
    nativeType: String?
  ) {
    if (nativeType == PostgreSQL.BIT_TYPE_NAME) {
      classes.add(PrismaOperatorClass.BitMinMaxOps)
    }
    else if (nativeType == PostgreSQL.VAR_BIT_TYPE_NAME) {
      classes.add(PrismaOperatorClass.VarBitMinMaxOps)
    }
    else if (nativeType == PostgreSQL.CHAR_TYPE_NAME) {
      classes.add(PrismaOperatorClass.BpcharBloomOps)
      classes.add(PrismaOperatorClass.BpcharMinMaxOps)
    }
    else if (nativeType == PostgreSQL.DATE_TYPE_NAME) {
      classes.add(PrismaOperatorClass.DateBloomOps)
      classes.add(PrismaOperatorClass.DateMinMaxOps)
      classes.add(PrismaOperatorClass.DateMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.REAL_TYPE_NAME) {
      classes.add(PrismaOperatorClass.Float4BloomOps)
      classes.add(PrismaOperatorClass.Float4MinMaxOps)
      classes.add(PrismaOperatorClass.Float4MinMaxMultiOps)
    }
    else if (fieldType is PrismaFloatType) {
      classes.add(PrismaOperatorClass.Float8BloomOps)
      classes.add(PrismaOperatorClass.Float8MinMaxOps)
      classes.add(PrismaOperatorClass.Float8MinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.INET_TYPE_NAME) {
      classes.add(PrismaOperatorClass.InetBloomOps)
      classes.add(PrismaOperatorClass.InetInclusionOps)
      classes.add(PrismaOperatorClass.InetMinMaxOps)
      classes.add(PrismaOperatorClass.InetMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.SMALL_INT_TYPE_NAME) {
      classes.add(PrismaOperatorClass.Int2BloomOps)
      classes.add(PrismaOperatorClass.Int2MinMaxOps)
      classes.add(PrismaOperatorClass.Int2MinMaxMultiOps)
    }
    else if (fieldType is PrismaIntType && (nativeType == null || nativeType == PostgreSQL.INTEGER_TYPE_NAME)) {
      classes.add(PrismaOperatorClass.Int4BloomOps)
      classes.add(PrismaOperatorClass.Int4MinMaxOps)
      classes.add(PrismaOperatorClass.Int4MinMaxMultiOps)
    }
    else if (fieldType is PrismaBigIntType) {
      classes.add(PrismaOperatorClass.Int8BloomOps)
      classes.add(PrismaOperatorClass.Int8MinMaxOps)
      classes.add(PrismaOperatorClass.Int8MinMaxMultiOps)
    }
    else if (fieldType is PrismaDecimalType) {
      classes.add(PrismaOperatorClass.NumericBloomOps)
      classes.add(PrismaOperatorClass.NumericMinMaxOps)
      classes.add(PrismaOperatorClass.NumericMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.OID_TYPE_NAME) {
      classes.add(PrismaOperatorClass.OidBloomOps)
      classes.add(PrismaOperatorClass.OidMinMaxOps)
      classes.add(PrismaOperatorClass.OidMinMaxMultiOps)
    }
    else if (fieldType is PrismaBytesType && (nativeType == null || nativeType == PostgreSQL.BYTE_A_TYPE_NAME)) {
      classes.add(PrismaOperatorClass.ByteaBloomOps)
      classes.add(PrismaOperatorClass.ByteaMinMaxOps)
    }
    else if (fieldType is PrismaStringType && (
        nativeType == null ||
        nativeType == PostgreSQL.TEXT_TYPE_NAME ||
        nativeType == PostgreSQL.VARCHAR_TYPE_NAME)
    ) {
      classes.add(PrismaOperatorClass.TextBloomOps)
      classes.add(PrismaOperatorClass.TextMinMaxOps)
    }
    else if (fieldType is PrismaDateTimeType && (
        nativeType == null ||
        nativeType == PostgreSQL.TIMESTAMP_TYPE_NAME)
    ) {
      classes.add(PrismaOperatorClass.TimestampBloomOps)
      classes.add(PrismaOperatorClass.TimestampMinMaxOps)
      classes.add(PrismaOperatorClass.TimestampMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.TIMESTAMP_TZ_TYPE_NAME) {
      classes.add(PrismaOperatorClass.TimestampTzBloomOps)
      classes.add(PrismaOperatorClass.TimestampTzMinMaxOps)
      classes.add(PrismaOperatorClass.TimestampTzMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.TIME_TYPE_NAME) {
      classes.add(PrismaOperatorClass.TimeBloomOps)
      classes.add(PrismaOperatorClass.TimeMinMaxOps)
      classes.add(PrismaOperatorClass.TimeMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.TIME_TZ_TYPE_NAME) {
      classes.add(PrismaOperatorClass.TimeTzBloomOps)
      classes.add(PrismaOperatorClass.TimeTzMinMaxOps)
      classes.add(PrismaOperatorClass.TimeTzMinMaxMultiOps)
    }
    else if (nativeType == PostgreSQL.UUID_TYPE_NAME) {
      classes.add(PrismaOperatorClass.UuidBloomOps)
      classes.add(PrismaOperatorClass.UuidMinMaxOps)
      classes.add(PrismaOperatorClass.UuidMinMaxMultiOps)
    }
  }
}