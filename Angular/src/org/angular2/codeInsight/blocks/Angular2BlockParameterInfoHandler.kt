package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.javascript.findArgumentList
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.parameterInfo.ParameterInfoHandlerWithTabActionSupport
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.containers.MultiMap
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.ERROR
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import com.intellij.javascript.ParameterInfoHandlerWithColoredSyntax
import com.intellij.javascript.ParameterInfoHandlerWithColoredSyntax.ParameterInfoHandlerWithColoredSyntaxData
import com.intellij.javascript.ParameterInfoHandlerWithColoredSyntax.SignaturePresentation
import com.intellij.xml.util.XmlUtil
import org.angular2.codeInsight.blocks.Angular2BlockParameterSymbol.Companion.PRIMARY_EXPRESSION
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlBlockParameters

class Angular2BlockParameterInfoHandler : ParameterInfoHandlerWithColoredSyntax<Angular2HtmlBlockParameters, SignaturePresentation>(),
                                          ParameterInfoHandlerWithTabActionSupport<Angular2HtmlBlockParameters, ParameterInfoHandlerWithColoredSyntaxData, Angular2BlockParameter> {

  override val parameterListSeparator: String
    get() = ";"

  override fun findParameterOwner(file: PsiFile, offset: Int): Angular2HtmlBlockParameters? {
    if (findArgumentList(file, offset) != null) return null
    var element = file.findElementAt(offset)
    if (element is PsiWhiteSpace) element = PsiTreeUtil.prevLeaf(element)
    return element?.parentOfType<Angular2HtmlBlockParameters>()
      ?.takeIf { (it.parent as? Angular2HtmlBlock)?.name != BLOCK_LET }
  }

  override fun buildSignaturePresentations(parameterOwner: Angular2HtmlBlockParameters): List<SignaturePresentation> {
    val blockDefinition = (parameterOwner.parent as? Angular2HtmlBlock)?.definition ?: return emptyList()
    val definitionToPrefixMap = buildDefinitionToPrefixMap(blockDefinition)

    val varPlaceholder = "&lt;var&gt;"
    val exprPlaceholder = "&lt;expr&gt;"
    val triggerPlaceholder = "&lt;trigger&gt;"
    val timePlaceholder = "&lt;duration&gt;"

    val parameterDefs = mutableMapOf<String, String>()
    if (blockDefinition.parameters.firstOrNull()?.isPrimaryExpression == true) {
      parameterDefs[PRIMARY_EXPRESSION] = when (blockDefinition.name) {
        BLOCK_FOR -> "$varPlaceholder " + "of".withColor(TS_KEYWORD, parameterOwner, false) + " $exprPlaceholder"
        else -> exprPlaceholder
      }
    }

    definitionToPrefixMap.entrySet().forEach { entry ->
      var definition = entry.key.withColor(TS_KEYWORD, parameterOwner, false) +
                       when (entry.key) {
                         PARAMETER_AS -> " $varPlaceholder"
                         PARAMETER_LET -> " $varPlaceholder = $exprPlaceholder"
                         PARAMETER_ON -> " $triggerPlaceholder"
                         PARAMETER_NEVER -> ""
                         else -> when (blockDefinition.name) {
                           BLOCK_PLACEHOLDER -> " $timePlaceholder"
                           BLOCK_LOADING -> " $timePlaceholder"
                           else -> " $exprPlaceholder"
                         }
                       }
      parameterDefs[entry.key] = definition
    }

    val result = mutableListOf<ParameterPresentation>()
    parameterOwner.parameters.forEach { parameter ->
      if (parameter.isPrimaryExpression) {
        result.add(ParameterPresentation(parameterDefs[PRIMARY_EXPRESSION]
                                         ?: exprPlaceholder.withColor(ERROR, parameterOwner, false), parameter.textRange))
      }
      else {
        val prefix = parameter.prefix
                       ?.withColor(if (parameter.prefixDefinition != null) TS_KEYWORD else ERROR, parameterOwner, false)
                       ?.let { "$it " } ?: ""
        result.add(ParameterPresentation(parameterDefs[parameter.name]?.let { "$prefix$it" }
                                         ?: (parameter.name ?: return@forEach).withColor(ERROR, parameterOwner, false),
                                         parameter.textRange))
      }
    }
    val provided = parameterOwner.parameters
      .filter { it.definition?.isUnique == true || it.isPrimaryExpression }
      .map { if (it.isPrimaryExpression) PRIMARY_EXPRESSION else it.name }
      .toSet()

    val prefixesToFilterOut = if (parameterOwner.parameters.any { it.prefix == PARAMETER_PREFIX_HYDRATE && it.name == PARAMETER_NEVER })
      setOf(PARAMETER_PREFIX_HYDRATE)
    else
      emptySet<String>()

    parameterDefs.forEach {
      val prefixes = definitionToPrefixMap[it.key]
      if (!provided.contains(it.key) && (prefixes.size != 1 || !prefixesToFilterOut.contains(prefixes.first()))) {
        if (it.key == PRIMARY_EXPRESSION)
          result.add(ParameterPresentation(it.value, TextRange(0, 0)))
        else
          result.add(ParameterPresentation("[" + renderPrefixesList(prefixes.filter { !prefixesToFilterOut.contains(it) }, parameterOwner) + it.value + "]", null))
      }
    }
    if (result.isEmpty())
      result.add(ParameterPresentation(XmlUtil.escape(CodeInsightBundle.message("parameter.info.no.parameters"))))
    return listOf(SignaturePresentation(result))
  }

  private fun renderPrefixesList(prefixes: Collection<String>, parameterOwner: Angular2HtmlBlockParameters): String {
    val nonEmptyPrefixes = prefixes.filter { it.isNotEmpty() }
    return when (nonEmptyPrefixes.size) {
      0 -> return ""
      1 -> nonEmptyPrefixes.first().withColor(TS_KEYWORD, parameterOwner, false)
      else -> "(" + nonEmptyPrefixes.joinToString(separator = " | ") { it.withColor(TS_KEYWORD, parameterOwner, false) } + ")"
    }.let {
      if (nonEmptyPrefixes.size != prefixes.size) "$it? " else "$it "
    }
  }

  private fun buildDefinitionToPrefixMap(blockDefinition: Angular2HtmlBlockSymbol): MultiMap<String, String> {
    val definitionToPrefixMap = MultiMap.createSet<String, String>()
    blockDefinition.parameters.forEach { parameter ->
      if (!parameter.isPrimaryExpression) {
        definitionToPrefixMap.putValue(parameter.name, "")
        blockDefinition.parameterPrefixes.forEach { prefix ->
          definitionToPrefixMap.putValue(parameter.name, prefix.name)
        }
      }
    }

    blockDefinition.parameterPrefixes.forEach { prefix ->
      prefix.parameters.forEach { parameter ->
        definitionToPrefixMap.putValue(parameter.name, prefix.name)
      }
    }
    return definitionToPrefixMap
  }

  override fun getActualParameters(o: Angular2HtmlBlockParameters): Array<out Angular2BlockParameter> =
    o.parameters.toTypedArray()

  override fun getActualParameterDelimiterType(): IElementType =
    Angular2HtmlTokenTypes.BLOCK_SEMICOLON

  override fun getActualParametersRBraceType(): IElementType =
    JSTokenTypes.RPAR

  override fun getArgumentListAllowedParentClasses(): Set<Class<*>?> =
    setOf(PsiElement::class.java)

  override fun getArgListStopSearchClasses(): Set<Class<*>?> =
    setOf(Angular2HtmlBlock::class.java)

  override fun getArgumentListClass(): Class<Angular2HtmlBlockParameters> =
    Angular2HtmlBlockParameters::class.java

}